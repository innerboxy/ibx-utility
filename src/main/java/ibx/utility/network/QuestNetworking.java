package ibx.utility.network;

import ibx.utility.block.QuestBoardBlock;
import ibx.utility.client.ClientQuestBoard;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.QuestService;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.quest.state.QuestDepartures;
import ibx.utility.quest.state.QuestTeams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 悬赏板的网络:右键打开、发界面内容、处理界面里的操作。
 * 开着悬赏板的人记在 VIEWERS 里;队伍、准备、出门名单、已接任务有变化时 markDirty,这一刻结束时给他们每人重发一次内容,
 * 界面自己刷新(队友加入、点了准备、出发了都能马上看到)。出发走成了的人,开着的界面由服务端叫他关掉。
 */
public final class QuestNetworking {
    /** 离悬赏板多远以内还能操作(方块中心到玩家,8 格) */
    private static final double REACH_SQR = 8.0 * 8.0;

    /** 每个玩家最近一次打开的悬赏板;下线时清掉 */
    private static final Map<UUID, GlobalPos> OPEN_BOARDS = new HashMap<>();
    /** 开着悬赏板界面的人:右键打开时加,关界面(客户端发 QuestBoardClosedPayload)和下线时去掉 */
    private static final Set<UUID> VIEWERS = new HashSet<>();
    /** 有变化,这一刻结束时给开着界面的人重发 */
    private static boolean dirty;

    private QuestNetworking() {}

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(QuestBoardPayload.TYPE, QuestBoardPayload.STREAM_CODEC, QuestNetworking::handleBoard);
        registrar.playToServer(QuestActionPayload.TYPE, QuestActionPayload.STREAM_CODEC, QuestNetworking::handleAction);
        registrar.playToServer(QuestBoardClosedPayload.TYPE, QuestBoardClosedPayload.STREAM_CODEC, QuestNetworking::handleClosed);
    }

    /** 服务端:玩家右键了悬赏板 */
    public static void openBoard(ServerPlayer player, BlockPos pos) {
        OPEN_BOARDS.put(player.getUUID(), GlobalPos.of(player.level().dimension(), pos));
        VIEWERS.add(player.getUUID());
        PacketDistributor.sendToPlayer(player, content(player, QuestBoardPayload.Mode.OPEN));
    }

    /** 这个人出发了(QuestService 调):他开着悬赏板的话叫客户端关掉,之后不用再给他发 */
    public static void closeBoard(ServerPlayer player) {
        if (VIEWERS.remove(player.getUUID())) {
            PacketDistributor.sendToPlayer(player, QuestBoardPayload.close());
        }
    }

    /** 任务状态变了(QuestService 调):这一刻结束时给开着悬赏板的人重发内容 */
    public static void markDirty() {
        dirty = true;
    }

    /** 每刻结束:有变化就给开着悬赏板的人各发一次新内容 */
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!dirty) {
            return;
        }
        dirty = false;
        for (UUID viewer : List.copyOf(VIEWERS)) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(viewer);
            if (player == null) {
                VIEWERS.remove(viewer);
            } else {
                PacketDistributor.sendToPlayer(player, content(player, QuestBoardPayload.Mode.REFRESH));
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        OPEN_BOARDS.clear();
        VIEWERS.clear();
        dirty = false;
    }

    /** 人还在他打开的那块悬赏板旁边(同一维度、8 格内、那块板还在) */
    public static boolean nearBoard(ServerPlayer player) {
        GlobalPos board = OPEN_BOARDS.get(player.getUUID());
        return board != null && board.dimension().equals(player.level().dimension())
                && player.distanceToSqr(board.pos().getCenter()) <= REACH_SQR
                && player.level().getBlockState(board.pos()).getBlock() instanceof QuestBoardBlock;
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        OPEN_BOARDS.remove(event.getEntity().getUUID());
        VIEWERS.remove(event.getEntity().getUUID());
    }

    /**
     * 这个玩家在悬赏板上能看到的东西。做过的一次性任务本来看不到,但有能加入的队伍时照样列出来(只能加入)。
     * 队伍里每个人带上准备 / 出门的状态;他自己在外面的个人任务单列
     */
    public static QuestBoardPayload content(ServerPlayer player, QuestBoardPayload.Mode mode) {
        MinecraftServer server = player.level().getServer();
        List<Holder.Reference<QuestDefinition>> visible = QuestService.all(player.level().registryAccess())
                .filter(quest -> QuestService.isVisible(player, quest) || !QuestService.joinableTeams(player, quest.key()).isEmpty())
                .toList();
        QuestTeams teams = QuestTeams.get(server);
        PlayerQuests data = PlayerQuests.get(player);
        List<QuestBoardPayload.TeamInfo> infos = new ArrayList<>();
        for (Holder.Reference<QuestDefinition> quest : visible) {
            if (quest.value().team().isEmpty()) {
                continue;
            }
            int max = quest.value().team().get().max();
            ActiveQuest active = data.active(quest.key());
            QuestTeams.Team own = active == null || active.team() == null ? null : teams.get(active.team());
            if (own != null) {
                infos.add(info(server, own, max, player.getUUID(), true));
            }
            for (QuestTeams.Team team : QuestService.joinableTeams(player, quest.key())) {
                infos.add(info(server, team, max, player.getUUID(), false));
            }
        }
        List<ResourceKey<QuestDefinition>> away = data.activeInOrder().stream()
                .filter(entry -> entry.getValue().team() == null
                        && QuestDepartures.isAway(new QuestDepartures.Group(entry.getKey(), player.getUUID()), player.getUUID()))
                .map(Map.Entry::getKey)
                .toList();
        return new QuestBoardPayload(mode, visible.stream().map(Holder.Reference::key).toList(), infos, away);
    }

    private static QuestBoardPayload.TeamInfo info(MinecraftServer server, QuestTeams.Team team, int max, UUID viewer, boolean mine) {
        QuestDepartures.Group group = new QuestDepartures.Group(team.quest(), team.id());
        List<QuestBoardPayload.Member> members = team.members().stream()
                .map(member -> new QuestBoardPayload.Member(member, name(server, member), member.equals(team.leader()),
                        QuestDepartures.isReady(team.id(), member), QuestDepartures.isAway(group, member)))
                .toList();
        return new QuestBoardPayload.TeamInfo(team.id(), team.quest(), name(server, team.leader()), members, max,
                team.leader().equals(viewer), mine);
    }

    private static String name(MinecraftServer server, UUID player) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(player).map(NameAndId::name).orElse(player.toString().substring(0, 8));
    }

    /** 服务端:界面里点了一个操作。不在悬赏板旁边就不做;做完这一刻结束时给开着悬赏板的人(包括他)重发内容 */
    private static void handleAction(QuestActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            VIEWERS.add(player.getUUID());
            perform(player, payload);
            markDirty();
        }
    }

    /** 服务端:他关掉了悬赏板界面 */
    private static void handleClosed(QuestBoardClosedPayload payload, IPayloadContext context) {
        VIEWERS.remove(context.player().getUUID());
    }

    /**
     * 做这个操作,顺带只给这个玩家放一声:接下任务(自己开或加入队伍)、点准备是落笔声,
     * 没做成(太远、到上限、目标没达成、出发动作说没走成……)是一声低音;交了一部分、完成、放弃、取消准备、出发、移出队伍不出声
     * (出发后的传送之类由挂 depart 动作的模组自己放声音)
     */
    public static void perform(ServerPlayer player, QuestActionPayload payload) {
        if (!nearBoard(player)) {
            cue(player, Cue.FAILED);
            return;
        }
        ResourceKey<QuestDefinition> key = payload.quest();
        cue(player, switch (payload.action()) {
            case ACCEPT -> QuestService.accept(player, key) == QuestService.AcceptResult.ACCEPTED ? Cue.ACCEPTED : Cue.FAILED;
            case JOIN -> payload.target()
                    .map(team -> QuestService.join(player, team) == QuestService.JoinResult.JOINED ? Cue.ACCEPTED : Cue.FAILED)
                    .orElse(Cue.FAILED);
            case SUBMIT -> switch (QuestService.submit(player, key)) {
                case COMPLETED, PROGRESSED -> Cue.NONE;
                case NOT_COMPLETE, NOT_ACTIVE, CANCELLED -> Cue.FAILED;
            };
            case ABANDON -> QuestService.abandon(player, key) ? Cue.NONE : Cue.FAILED;
            case READY -> switch (QuestService.toggleReady(player, key)) {
                case READY -> Cue.ACCEPTED;
                case UNREADY -> Cue.NONE;
                default -> Cue.FAILED;
            };
            case DEPART -> QuestService.depart(player, key) == QuestService.DepartResult.DEPARTED ? Cue.NONE : Cue.FAILED;
            case KICK -> payload.target()
                    .map(target -> QuestService.kick(player, key, target) == QuestService.KickResult.KICKED ? Cue.NONE : Cue.FAILED)
                    .orElse(Cue.FAILED);
        });
    }

    /** 操作完给这个玩家的那一声 */
    private enum Cue {
        /** 接下了任务:在契约上落笔(原版制图台取图那声) */
        ACCEPTED,
        /** 没做成:一声低沉的贝斯音 */
        FAILED,
        NONE
    }

    /** 只给这个玩家放 cue 那一声 */
    private static void cue(ServerPlayer player, Cue cue) {
        Holder<SoundEvent> sound = switch (cue) {
            case ACCEPTED -> BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT);
            case FAILED -> SoundEvents.NOTE_BLOCK_BASS;
            case NONE -> null;
        };
        if (sound != null) {
            float pitch = cue == Cue.FAILED ? 0.6F : 1.0F;
            player.connection.send(new ClientboundSoundPacket(sound, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(),
                    1.0F, pitch, player.getRandom().nextLong()));
        }
    }

    /** 客户端:收到界面内容 */
    private static void handleBoard(QuestBoardPayload payload, IPayloadContext context) {
        ClientQuestBoard.accept(payload);
    }
}
