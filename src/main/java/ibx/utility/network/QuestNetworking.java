package ibx.utility.network;

import ibx.utility.block.QuestBoardBlock;
import ibx.utility.client.ClientQuestBoard;
import ibx.utility.config.Config;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.QuestService;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.quest.state.QuestTeams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** 悬赏板的网络:右键打开、发界面内容、处理界面里的操作。 */
public final class QuestNetworking {
    /** 离悬赏板多远以内还能操作(方块中心到玩家,8 格) */
    private static final double REACH_SQR = 8.0 * 8.0;

    /** 每个玩家最近一次打开的悬赏板;下线时清掉 */
    private static final Map<UUID, GlobalPos> OPEN_BOARDS = new HashMap<>();

    private QuestNetworking() {}

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(QuestBoardPayload.TYPE, QuestBoardPayload.STREAM_CODEC, QuestNetworking::handleBoard);
        registrar.playToServer(QuestActionPayload.TYPE, QuestActionPayload.STREAM_CODEC, QuestNetworking::handleAction);
    }

    /** 服务端:玩家右键了悬赏板 */
    public static void openBoard(ServerPlayer player, BlockPos pos) {
        OPEN_BOARDS.put(player.getUUID(), GlobalPos.of(player.level().dimension(), pos));
        PacketDistributor.sendToPlayer(player, content(player, true, Optional.empty()));
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
    }

    /** 这个玩家在悬赏板上能看到的东西。做过的一次性任务本来看不到,但有能加入的队伍时照样列出来(只能加入) */
    public static QuestBoardPayload content(ServerPlayer player, boolean open, Optional<Component> message) {
        MinecraftServer server = player.level().getServer();
        List<Holder.Reference<QuestDefinition>> visible = QuestService.all(player.level().registryAccess())
                .filter(quest -> QuestService.isVisible(player, quest) || !QuestService.joinableTeams(player, quest.key()).isEmpty())
                .toList();
        QuestTeams teams = QuestTeams.get(server);
        List<QuestBoardPayload.TeamInfo> infos = new ArrayList<>();
        for (Holder.Reference<QuestDefinition> quest : visible) {
            if (quest.value().team().isEmpty()) {
                continue;
            }
            int max = quest.value().team().get().max();
            ActiveQuest active = PlayerQuests.get(player).active(quest.key());
            QuestTeams.Team own = active == null || active.team() == null ? null : teams.get(active.team());
            if (own != null) {
                infos.add(info(server, own, max, true));
            }
            for (QuestTeams.Team team : QuestService.joinableTeams(player, quest.key())) {
                infos.add(info(server, team, max, false));
            }
        }
        return new QuestBoardPayload(open, visible.stream().map(Holder.Reference::key).toList(), infos, message);
    }

    private static QuestBoardPayload.TeamInfo info(MinecraftServer server, QuestTeams.Team team, int max, boolean mine) {
        return new QuestBoardPayload.TeamInfo(team.id(), team.quest(), name(server, team.leader()),
                team.members().stream().map(member -> name(server, member)).toList(), max, mine);
    }

    private static String name(MinecraftServer server, UUID player) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(player).map(NameAndId::name).orElse(player.toString().substring(0, 8));
    }

    /** 服务端:界面里点了一个操作。不在悬赏板旁边就不做;做完把新的界面内容发回去 */
    private static void handleAction(QuestActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Component message = perform(player, payload);
            PacketDistributor.sendToPlayer(player, content(player, false, Optional.of(message)));
        }
    }

    /** 做这个操作,返回给玩家看的结果 */
    public static Component perform(ServerPlayer player, QuestActionPayload payload) {
        if (!nearBoard(player)) {
            return Component.translatable("quest.ibx_utility.result.too_far");
        }
        ResourceKey<QuestDefinition> key = payload.quest();
        Component name = QuestService.lookup(player.level(), key).map(quest -> quest.value().name())
                .orElse(Component.literal(key.identifier().toString()));
        return switch (payload.action()) {
            case ACCEPT -> switch (QuestService.accept(player, key)) {
                case ACCEPTED -> Component.translatable("quest.ibx_utility.result.accepted", name);
                case UNKNOWN -> Component.translatable("quest.ibx_utility.result.unknown");
                case ALREADY_ACTIVE -> Component.translatable("quest.ibx_utility.result.already_active");
                case NOT_OFFERED -> Component.translatable("quest.ibx_utility.result.not_offered");
                case LOCKED -> Component.translatable("quest.ibx_utility.result.locked");
                case ALREADY_COMPLETED -> Component.translatable("quest.ibx_utility.result.already_completed");
                case TOO_MANY -> Component.translatable("quest.ibx_utility.result.too_many", Config.MAX_ACTIVE_QUESTS.getAsInt());
                case CANCELLED -> Component.translatable("quest.ibx_utility.result.cancelled");
            };
            case JOIN -> payload.team().map(team -> switch (QuestService.join(player, team)) {
                case JOINED -> Component.translatable("quest.ibx_utility.result.joined", name);
                case UNKNOWN_TEAM -> Component.translatable("quest.ibx_utility.result.unknown_team");
                case ALREADY_ACTIVE -> Component.translatable("quest.ibx_utility.result.already_active");
                case FULL -> Component.translatable("quest.ibx_utility.result.full");
                case LOCKED -> Component.translatable("quest.ibx_utility.result.locked");
                case TOO_MANY -> Component.translatable("quest.ibx_utility.result.too_many", Config.MAX_ACTIVE_QUESTS.getAsInt());
                case CANCELLED -> Component.translatable("quest.ibx_utility.result.cancelled");
            }).orElse(Component.translatable("quest.ibx_utility.result.unknown_team"));
            case SUBMIT -> switch (QuestService.submit(player, key)) {
                case COMPLETED -> Component.translatable("quest.ibx_utility.result.completed", name);
                case PROGRESSED -> Component.translatable("quest.ibx_utility.result.progressed");
                case NOT_COMPLETE -> Component.translatable("quest.ibx_utility.result.not_complete");
                case NOT_ACTIVE -> Component.translatable("quest.ibx_utility.result.not_active");
                case CANCELLED -> Component.translatable("quest.ibx_utility.result.cancelled");
            };
            case ABANDON -> QuestService.abandon(player, key)
                    ? Component.translatable("quest.ibx_utility.result.abandoned", name)
                    : Component.translatable("quest.ibx_utility.result.not_active");
        };
    }

    /** 客户端:收到界面内容 */
    private static void handleBoard(QuestBoardPayload payload, IPayloadContext context) {
        ClientQuestBoard.accept(payload);
    }
}
