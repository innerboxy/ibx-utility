package ibx.utility.quest;

import ibx.utility.api.event.QuestEvent;
import ibx.utility.config.Config;
import ibx.utility.network.QuestNetworking;
import ibx.utility.quest.action.QuestAction;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.requirement.QuestRequirement;
import ibx.utility.quest.reward.QuestReward;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.DailyQuests;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.quest.state.QuestDepartures;
import ibx.utility.quest.state.QuestProgress;
import ibx.utility.quest.state.QuestTeams;
import ibx.utility.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * 任务的服务端逻辑:接、加入队伍、准备、出发、放弃、移出队伍、提交、加进度(击杀计数也走这里)、每日随机,沿路发 QuestEvent。
 * 悬赏板界面和对外接口 QuestApi 都走这里。
 * 组队任务的进度以队伍里的为准(QuestTeams),每次变化抄给在线成员,追踪栏统一只读玩家自己的数据。
 * 队伍、准备、出门名单变了就叫开着悬赏板的人刷新(QuestNetworking.markDirty)。
 */
public final class QuestService {
    public enum AcceptResult {
        ACCEPTED,
        /** 没有这个任务 */
        UNKNOWN,
        ALREADY_ACTIVE,
        /** 没挂在悬赏板上(随机池的不在今天的名单里) */
        NOT_OFFERED,
        /** 前置没满足 */
        LOCKED,
        /** 一次性任务已经做过 */
        ALREADY_COMPLETED,
        /** 已接的任务到上限了 */
        TOO_MANY,
        /** 别的模组在接任务事件里取消了 */
        CANCELLED
    }

    public enum JoinResult {
        JOINED,
        /** 队伍不在了(或任务定义不在了) */
        UNKNOWN_TEAM,
        ALREADY_ACTIVE,
        FULL,
        /** 前置没满足 */
        LOCKED,
        TOO_MANY,
        /** 别的模组在加入事件里取消了 */
        CANCELLED
    }

    public enum ReadyResult {
        /** 点了准备 */
        READY,
        /** 取消了准备 */
        UNREADY,
        NOT_ACTIVE,
        /** 不是要出发的任务 */
        NO_DEPARTURE,
        /** 个人任务不用准备 */
        NOT_TEAM,
        /** 发起人不用准备,直接出发 */
        LEADER,
        /** 已经有人出发了,不用准备,自己点出发进去 */
        IN_PROGRESS
    }

    public enum DepartResult {
        DEPARTED,
        NOT_ACTIVE,
        /** 不是要出发的任务 */
        NO_DEPARTURE,
        /** 开局只有发起人能点 */
        NOT_LEADER,
        /** 自己已经在外面了 */
        ALREADY_AWAY,
        /** 别的模组在出发事件里取消了 */
        CANCELLED,
        /** 点的这个人的 depart 动作说没走成(同一批里别人可能走成了) */
        FAILED
    }

    public enum KickResult {
        KICKED,
        NOT_ACTIVE,
        /** 个人任务没有队伍 */
        NOT_TEAM,
        /** 只有发起人能移人 */
        NOT_LEADER,
        /** 不是这支队伍里的别人(不在队里,或者是他自己) */
        NOT_MEMBER
    }

    public enum SubmitResult {
        /** 目标都达成,任务完成、发了奖励 */
        COMPLETED,
        /** 交了一部分物品,还没达成 */
        PROGRESSED,
        NOT_COMPLETE,
        NOT_ACTIVE,
        /** 目标都达成了,但别的模组在提交事件里取消了;任务留着 */
        CANCELLED
    }

    private QuestService() {}

    /** 找任务;分页不存在的任务(分页写错了、或配置里关掉了)当作没有 */
    public static Optional<Holder.Reference<QuestDefinition>> lookup(Level level, ResourceKey<QuestDefinition> quest) {
        return level.registryAccess().lookupOrThrow(ModRegistries.QUEST).get(quest).filter(q -> hasCategory(level.registryAccess(), q.value()));
    }

    /** 全部算数的任务(分页存在的),按 id 排 */
    public static Stream<Holder.Reference<QuestDefinition>> all(RegistryAccess access) {
        return access.lookupOrThrow(ModRegistries.QUEST).listElements()
                .filter(quest -> hasCategory(access, quest.value()))
                .sorted(Comparator.comparing(quest -> quest.key().identifier()));
    }

    public static boolean hasCategory(RegistryAccess access, QuestDefinition quest) {
        return access.lookupOrThrow(ModRegistries.QUEST_CATEGORY)
                .get(ResourceKey.create(ModRegistries.QUEST_CATEGORY, quest.category())).isPresent();
    }

    /** 今天的随机任务;到了新的一天(主世界游戏总时长每 24000 tick)先重抽 */
    public static DailyQuests daily(MinecraftServer server) {
        DailyQuests daily = DailyQuests.get(server);
        daily.refreshIfNewDay(server);
        return daily;
    }

    /** 任务挂没挂在悬赏板上:固定任务一直挂着;随机池的任务要在今天的名单里 */
    public static boolean isOffered(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
        return quest.value().pool() == QuestPool.FIXED || daily(player.level().getServer()).contains(quest.key());
    }

    public static boolean meetsRequirements(ServerPlayer player, QuestDefinition quest) {
        for (QuestRequirement requirement : quest.requirements()) {
            if (!requirement.test(player)) {
                return false;
            }
        }
        return true;
    }

    /** 悬赏板上给这个玩家显示不显示:已接的总显示;没接的要挂着、前置满足、一次性的没做过 */
    public static boolean isVisible(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
        PlayerQuests data = PlayerQuests.get(player);
        if (data.isActive(quest.key())) {
            return true;
        }
        return isOffered(player, quest) && meetsRequirements(player, quest.value())
                && (quest.value().repeatable() || data.completions(quest.key()) == 0);
    }

    /** 这个玩家在悬赏板上能加入的队伍:没满、他不在里面、他没接这个任务、前置满足 */
    public static List<QuestTeams.Team> joinableTeams(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
        if (quest.isEmpty() || quest.get().value().team().isEmpty() || PlayerQuests.get(player).isActive(key)
                || !meetsRequirements(player, quest.get().value())) {
            return List.of();
        }
        int max = quest.get().value().team().get().max();
        return QuestTeams.get(player.level().getServer()).teamsFor(key).stream()
                .filter(team -> team.members().size() < max && !team.hasMember(player.getUUID()))
                .toList();
    }

    public static AcceptResult accept(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        Optional<Holder.Reference<QuestDefinition>> found = lookup(player.level(), key);
        if (found.isEmpty()) {
            return AcceptResult.UNKNOWN;
        }
        Holder.Reference<QuestDefinition> quest = found.get();
        QuestDefinition definition = quest.value();
        PlayerQuests data = PlayerQuests.get(player);
        if (data.isActive(key)) {
            return AcceptResult.ALREADY_ACTIVE;
        }
        if (!isOffered(player, quest)) {
            return AcceptResult.NOT_OFFERED;
        }
        if (!meetsRequirements(player, definition)) {
            return AcceptResult.LOCKED;
        }
        if (!definition.repeatable() && data.completions(key) > 0) {
            return AcceptResult.ALREADY_COMPLETED;
        }
        if (data.activeCount() >= Config.MAX_ACTIVE_QUESTS.getAsInt()) {
            return AcceptResult.TOO_MANY;
        }
        if (NeoForge.EVENT_BUS.post(new QuestEvent.Accept(player, quest)).isCanceled()) {
            return AcceptResult.CANCELLED;
        }
        int objectives = definition.objectives().size();
        UUID team = definition.team().isPresent()
                ? QuestTeams.get(player.level().getServer()).create(key, player.getUUID(), objectives).id()
                : null;
        data.start(key, objectives, player.level().getGameTime(), team);
        PlayerQuests.sync(player);
        runActions(player, quest, definition.actions().accept());
        changed();
        return AcceptResult.ACCEPTED;
    }

    /** 加入别人的队伍。一次性任务做过的人也能加入,提交时照样拿奖励、记完成。队里原来没人在线的话,他接过发起人 */
    public static JoinResult join(ServerPlayer player, UUID teamId) {
        QuestTeams teams = QuestTeams.get(player.level().getServer());
        QuestTeams.Team team = teams.get(teamId);
        Optional<Holder.Reference<QuestDefinition>> found = team == null ? Optional.empty() : lookup(player.level(), team.quest());
        if (found.isEmpty()) {
            return JoinResult.UNKNOWN_TEAM;
        }
        Holder.Reference<QuestDefinition> quest = found.get();
        QuestDefinition definition = quest.value();
        PlayerQuests data = PlayerQuests.get(player);
        if (data.isActive(team.quest())) {
            return JoinResult.ALREADY_ACTIVE;
        }
        if (team.members().size() >= definition.team().map(QuestTeam::max).orElse(1)) {
            return JoinResult.FULL;
        }
        if (!meetsRequirements(player, definition)) {
            return JoinResult.LOCKED;
        }
        if (data.activeCount() >= Config.MAX_ACTIVE_QUESTS.getAsInt()) {
            return JoinResult.TOO_MANY;
        }
        if (NeoForge.EVENT_BUS.post(new QuestEvent.Join(player, quest, teamId)).isCanceled()) {
            return JoinResult.CANCELLED;
        }
        int objectives = definition.objectives().size();
        team.addMember(player.getUUID());
        ensureOnlineLeader(player.level().getServer(), team, null);
        data.start(team.quest(), objectives, player.level().getGameTime(), teamId).copyFrom(team, objectives);
        PlayerQuests.sync(player);
        runActions(player, quest, definition.actions().accept());
        changed();
        return JoinResult.JOINED;
    }

    /** 这个已接任务的出门名单是哪一组:组队的按队伍,个人的按玩家自己 */
    private static QuestDepartures.Group group(ResourceKey<QuestDefinition> quest, ActiveQuest active, UUID player) {
        return new QuestDepartures.Group(quest, active.team() != null ? active.team() : player);
    }

    /**
     * 准备 / 取消准备:要出发的组队任务里,发起人以外的队员在开局前点。点了就算,走开也算;
     * 发起人出发时带上准备好、在线的人。已经有人出发了就不用准备(自己点出发进去)
     */
    public static ReadyResult toggleReady(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        ActiveQuest active = PlayerQuests.get(player).active(key);
        Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
        if (active == null || quest.isEmpty()) {
            return ReadyResult.NOT_ACTIVE;
        }
        if (!quest.get().value().actions().hasDeparture()) {
            return ReadyResult.NO_DEPARTURE;
        }
        if (active.team() == null) {
            return ReadyResult.NOT_TEAM;
        }
        QuestTeams.Team team = QuestTeams.get(player.level().getServer()).get(active.team());
        if (team == null) {
            return ReadyResult.NOT_ACTIVE;
        }
        if (team.leader().equals(player.getUUID())) {
            return ReadyResult.LEADER;
        }
        if (!QuestDepartures.away(group(key, active, player.getUUID())).isEmpty()) {
            return ReadyResult.IN_PROGRESS;
        }
        boolean ready = QuestDepartures.toggleReady(team.id(), player.getUUID());
        changed();
        return ready ? ReadyResult.READY : ReadyResult.UNREADY;
    }

    /**
     * 出发。这一组还没人在外面时是开局:只有发起人能点,带上他自己和准备好、在线的队员(个人任务就是自己);
     * 已经有人在外面时,队里不在外面的人自己点,只带自己。发出发事件,没被取消就记进出门名单、清空准备、
     * 对每个走的人跑 depart 动作:走成了的关掉他开着的悬赏板;哪个动作返回 false 就是这个人没走成,
     * 后面的动作不再对他跑,把他从出门名单里划掉
     */
    public static DepartResult depart(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        ActiveQuest active = PlayerQuests.get(player).active(key);
        Optional<Holder.Reference<QuestDefinition>> found = lookup(player.level(), key);
        if (active == null || found.isEmpty()) {
            return DepartResult.NOT_ACTIVE;
        }
        Holder.Reference<QuestDefinition> quest = found.get();
        if (!quest.value().actions().hasDeparture()) {
            return DepartResult.NO_DEPARTURE;
        }
        MinecraftServer server = player.level().getServer();
        QuestDepartures.Group group = group(key, active, player.getUUID());
        List<UUID> away = QuestDepartures.away(group);
        if (away.contains(player.getUUID())) {
            return DepartResult.ALREADY_AWAY;
        }
        QuestTeams.Team team = active.team() == null ? null : QuestTeams.get(server).get(active.team());
        boolean opening = away.isEmpty();
        List<ServerPlayer> departing = new ArrayList<>();
        departing.add(player);
        if (opening && team != null) {
            if (!team.leader().equals(player.getUUID())) {
                return DepartResult.NOT_LEADER;
            }
            for (UUID member : team.members()) {
                ServerPlayer online = server.getPlayerList().getPlayer(member);
                if (online != null && online != player && QuestDepartures.isReady(team.id(), member)) {
                    departing.add(online);
                }
            }
        }
        UUID teamId = team == null ? null : team.id();
        if (NeoForge.EVENT_BUS.post(new QuestEvent.Depart(player, quest, departing, teamId, opening)).isCanceled()) {
            return DepartResult.CANCELLED;
        }
        QuestDepartures.addAway(group, departing.stream().map(ServerPlayer::getUUID).toList());
        if (teamId != null) {
            QuestDepartures.clearReady(teamId);
        }
        boolean failed = false;
        for (ServerPlayer going : departing) {
            if (runDepartActions(going, quest)) {
                QuestNetworking.closeBoard(going);
            } else {
                QuestDepartures.removeAway(key, going.getUUID());
                failed |= going == player;
            }
        }
        changed();
        return failed ? DepartResult.FAILED : DepartResult.DEPARTED;
    }

    /** 对这个人跑 depart 动作,碰到返回 false 的就停;返回走没走成 */
    private static boolean runDepartActions(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
        for (QuestAction action : quest.value().actions().depart()) {
            if (!action.run(player, quest)) {
                return false;
            }
        }
        return true;
    }

    /** 回来了:从这个任务的出门名单里划掉;一组人都回来了,下次要发起人重新开局。返回他本来在不在外面 */
    public static boolean markReturned(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        boolean removed = QuestDepartures.removeAway(key, player.getUUID());
        if (removed) {
            changed();
        }
        return removed;
    }

    /** 这个人在不在这个任务的外面(出发了还没回来) */
    public static boolean isAway(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        ActiveQuest active = PlayerQuests.get(player).active(key);
        return active != null && QuestDepartures.isAway(group(key, active, player.getUUID()), player.getUUID());
    }

    /** 跟这个人同一组、出发了还没回来的人(组队的是全队,个人的是他自己),按出发先后;没接是空 */
    public static List<UUID> awayPlayers(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        ActiveQuest active = PlayerQuests.get(player).active(key);
        return active == null ? List.of() : QuestDepartures.away(group(key, active, player.getUUID()));
    }

    /** 叫开着悬赏板的人刷新 */
    private static void changed() {
        QuestNetworking.markDirty();
    }

    /** 放弃:进度清零、没有惩罚;组队任务就是退队。人这时还在外面(出发了没回来)的话召回。没接着返回 false */
    public static boolean abandon(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        return abandon(player, key, QuestEvent.Recall.Reason.ABANDONED);
    }

    private static boolean abandon(ServerPlayer player, ResourceKey<QuestDefinition> key, QuestEvent.Recall.Reason reason) {
        if (!PlayerQuests.get(player).isActive(key)) {
            return false;
        }
        boolean away = isAway(player, key);
        Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
        quest.ifPresent(q -> NeoForge.EVENT_BUS.post(new QuestEvent.Abandon(player, q)));
        ActiveQuest removed = PlayerQuests.get(player).remove(key);
        if (removed == null) {
            return false;
        }
        if (removed.team() != null) {
            leaveTeam(player.level().getServer(), removed.team(), player.getUUID());
        } else {
            QuestDepartures.clear(new QuestDepartures.Group(key, player.getUUID()));
        }
        PlayerQuests.sync(player);
        quest.ifPresent(q -> runActions(player, q, q.value().actions().abandon()));
        if (away) {
            recall(player, quest, reason);
        }
        changed();
        return true;
    }

    /**
     * 召回一个还在外面、却已经不在做这个任务的人:发召回事件,没被取消就送回重生点(做法同原版从末地回来,
     * 不耗重生锚的充能)。正死着的人不传,他复活时本来就在重生点
     */
    private static void recall(ServerPlayer player, Optional<Holder.Reference<QuestDefinition>> quest, QuestEvent.Recall.Reason reason) {
        if (quest.isPresent() && NeoForge.EVENT_BUS.post(new QuestEvent.Recall(player, quest.get(), reason)).isCanceled()) {
            return;
        }
        if (player.isDeadOrDying()) {
            return;
        }
        player.stopRiding();
        player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));
    }

    /**
     * 发起人把一个队员移出队伍,等同那个人自己放弃:在线的当场走一遍放弃(放弃事件、abandon 动作);
     * 不在线的先从队伍里去掉,他下次上线时再走放弃。出发了还没回来的人也能移:从出门名单里划掉,
     * 然后召回(发召回事件,没被取消就送回重生点)
     */
    public static KickResult kick(ServerPlayer leader, ResourceKey<QuestDefinition> key, UUID target) {
        MinecraftServer server = leader.level().getServer();
        ActiveQuest active = PlayerQuests.get(leader).active(key);
        if (active == null) {
            return KickResult.NOT_ACTIVE;
        }
        if (active.team() == null) {
            return KickResult.NOT_TEAM;
        }
        QuestTeams.Team team = QuestTeams.get(server).get(active.team());
        if (team == null) {
            return KickResult.NOT_ACTIVE;
        }
        if (!team.leader().equals(leader.getUUID())) {
            return KickResult.NOT_LEADER;
        }
        if (target.equals(leader.getUUID()) || !team.hasMember(target)) {
            return KickResult.NOT_MEMBER;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(target);
        if (online != null) {
            abandon(online, key, QuestEvent.Recall.Reason.KICKED);
        } else {
            leaveTeam(server, team.id(), target);
            changed();
        }
        return KickResult.KICKED;
    }

    /** 退队:顺带从准备、出门名单里去掉;发起人走了换最早加入、还在线的队员(都不在线就先给最早加入的);队伍空了解散 */
    private static void leaveTeam(MinecraftServer server, UUID teamId, UUID player) {
        QuestTeams teams = QuestTeams.get(server);
        QuestTeams.Team team = teams.get(teamId);
        if (team == null) {
            return;
        }
        QuestDepartures.removeReady(teamId, player);
        QuestDepartures.removeAway(team.quest(), player);
        if (team.removeMember(player)) {
            teams.remove(teamId);
            QuestDepartures.clear(new QuestDepartures.Group(team.quest(), teamId));
        } else {
            ensureOnlineLeader(server, team, null);
            QuestDepartures.removeReady(teamId, team.leader());
        }
    }

    /**
     * 开局只有发起人能点出发,所以队里有人在线时发起人也得在线:发起人不在线(或者正要下线,leaving 是他)时,
     * 换成最早加入、还在线的队员,换上来的人不用准备;没人在线就先不换,等下一个上线的人接过来
     */
    private static void ensureOnlineLeader(MinecraftServer server, QuestTeams.Team team, @Nullable UUID leaving) {
        UUID leader = team.leader();
        if (!leader.equals(leaving) && server.getPlayerList().getPlayer(leader) != null) {
            return;
        }
        UUID next = nextOnlineMember(server, team, leaving);
        if (next != null && !next.equals(leader)) {
            team.setLeader(next);
            QuestDepartures.removeReady(team.id(), next);
        }
    }

    /** 这个已接任务的进度:组队的在队伍里,个人的在玩家身上 */
    private static QuestProgress progressOf(MinecraftServer server, ActiveQuest active) {
        if (active.team() != null) {
            QuestTeams.Team team = QuestTeams.get(server).get(active.team());
            if (team != null) {
                return team;
            }
        }
        return active;
    }

    /** 队伍进度抄给在线成员,并同步 */
    private static void mirror(MinecraftServer server, QuestTeams.Team team, int objectives) {
        for (UUID member : team.members()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player != null) {
                ActiveQuest active = PlayerQuests.get(player).active(team.quest());
                if (active != null) {
                    active.copyFrom(team, objectives);
                    PlayerQuests.sync(player);
                }
            }
        }
    }

    /**
     * 在悬赏板上提交:先把提交物品类目标能交的交上(可以分几次交,组队时交到队伍里),再看是不是全部达成
     * (收集物品数提交人的背包);全部达成就完成任务。组队任务全队一起完成:在线的每人一份奖励、记一次完成,
     * 不在线的没有奖励、上线后补记完成。
     */
    public static SubmitResult submit(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        MinecraftServer server = player.level().getServer();
        ActiveQuest active = PlayerQuests.get(player).active(key);
        Optional<Holder.Reference<QuestDefinition>> found = lookup(player.level(), key);
        if (active == null || found.isEmpty()) {
            return SubmitResult.NOT_ACTIVE;
        }
        Holder.Reference<QuestDefinition> quest = found.get();
        List<QuestObjective> objectives = quest.value().objectives();
        boolean progressed = gain(player, quest, active, (i, objective, current) -> objective.onSubmit(player, objective.count() - current)) > 0;
        QuestProgress progress = progressOf(server, active);
        for (int i = 0; i < objectives.size(); i++) {
            if (!objectives.get(i).isComplete(player, progress.progress(i))) {
                return progressed ? SubmitResult.PROGRESSED : SubmitResult.NOT_COMPLETE;
            }
        }
        if (NeoForge.EVENT_BUS.post(new QuestEvent.Submit(player, quest)).isCanceled()) {
            return SubmitResult.CANCELLED;
        }

        if (progress instanceof QuestTeams.Team team) {
            QuestTeams teams = QuestTeams.get(server);
            for (UUID member : team.members()) {
                ServerPlayer online = server.getPlayerList().getPlayer(member);
                if (online != null) {
                    complete(online, quest);
                } else {
                    teams.addPending(member, key);
                }
            }
            teams.remove(team.id());
            QuestDepartures.clear(new QuestDepartures.Group(key, team.id()));
        } else {
            complete(player, quest);
            QuestDepartures.clear(new QuestDepartures.Group(key, player.getUUID()));
        }
        changed();
        return SubmitResult.COMPLETED;
    }

    /** 给一个人完成任务:去掉已接、记一次完成、发奖励、跑提交动作 */
    private static void complete(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
        PlayerQuests data = PlayerQuests.get(player);
        data.remove(quest.key());
        data.addCompletion(quest.key());
        PlayerQuests.sync(player);
        for (QuestReward reward : quest.value().rewards()) {
            reward.grant(player);
        }
        runActions(player, quest, quest.value().actions().submit());
    }

    /** 接、提交、放弃时的动作:挨个跑完,不看返回值 */
    private static void runActions(ServerPlayer player, Holder.Reference<QuestDefinition> quest, List<QuestAction> actions) {
        for (QuestAction action : actions) {
            action.run(player, quest);
        }
    }

    /**
     * 生物死的时候,算到它「最后被哪个玩家打」的那个玩家头上:原版这个字段已经把玩家射出的弹射物、
     * 驯服的宠物算成玩家;别的模组的召唤物只要也设了这个字段,同样算主人。
     * 一只生物只算给一个玩家,所以组队时同一只怪只算一次;目标达成后多杀的不计。
     */
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !(victim.getLastHurtByPlayer() instanceof ServerPlayer player)) {
            return;
        }
        addProgress(player, objective -> objective.onKill(player, victim));
    }

    /** 给已接任务的第 objective 个目标加进度(别的模组走 QuestApi);返回实际涨了多少 */
    public static int addProgress(ServerPlayer player, ResourceKey<QuestDefinition> key, int objective, int amount) {
        ActiveQuest active = PlayerQuests.get(player).active(key);
        Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
        if (active == null || quest.isEmpty() || amount <= 0) {
            return 0;
        }
        return gain(player, quest.get(), active, (i, definition, current) -> i == objective ? amount : 0);
    }

    /** 给这个玩家全部已接任务里的目标加进度:amount 按目标算该加多少,不相关的返回 0;返回一共涨了多少 */
    public static int addProgress(ServerPlayer player, ToIntFunction<QuestObjective> amount) {
        int total = 0;
        for (Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest> entry : PlayerQuests.get(player).activeInOrder()) {
            Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), entry.getKey());
            if (quest.isPresent()) {
                total += gain(player, quest.get(), entry.getValue(), (i, objective, current) -> amount.applyAsInt(objective));
            }
        }
        return total;
    }

    /** 第 index 个目标(现在进度 current)该涨多少 */
    @FunctionalInterface
    private interface Gain {
        int amount(int index, QuestObjective objective, int current);
    }

    /**
     * 给一个已接任务涨进度:没达成的目标挨个问 gain 要涨多少,封顶到所需数量;有变化就同步
     * (组队的抄给在线队员),然后每个涨了的目标发一次进度事件。返回一共涨了多少
     */
    private static int gain(ServerPlayer player, Holder.Reference<QuestDefinition> quest, ActiveQuest active, Gain gain) {
        MinecraftServer server = player.level().getServer();
        List<QuestObjective> objectives = quest.value().objectives();
        QuestProgress progress = progressOf(server, active);
        List<QuestEvent.Progress> events = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < objectives.size(); i++) {
            QuestObjective objective = objectives.get(i);
            int current = progress.progress(i);
            if (current >= objective.count()) {
                continue;
            }
            int amount = gain.amount(i, objective, current);
            if (amount > 0) {
                int next = Math.min(objective.count(), current + amount);
                progress.setProgress(i, next);
                total += next - current;
                events.add(new QuestEvent.Progress(player, quest, i, current, next, active.team()));
            }
        }
        if (total > 0) {
            if (progress instanceof QuestTeams.Team team) {
                mirror(server, team, objectives.size());
            } else {
                PlayerQuests.sync(player);
            }
            events.forEach(NeoForge.EVENT_BUS::post);
        }
        return total;
    }

    /**
     * 登录时:补记不在线时队伍完成的任务;清掉找不到任务定义、或者队伍已经不在(不再是成员,比如不在线时被移出队伍)的已接任务,
     * 任务定义还在的照放弃走一遍(放弃事件、abandon 动作);还在的组队任务从队伍抄最新进度,队伍的发起人不在线就由他接过来。
     */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        QuestTeams teams = QuestTeams.get(server);
        PlayerQuests data = PlayerQuests.get(player);
        boolean changed = false;

        for (ResourceKey<QuestDefinition> done : teams.takePending(player.getUUID())) {
            data.remove(done);
            data.addCompletion(done);
            changed = true;
        }

        List<ResourceKey<QuestDefinition>> dropped = new ArrayList<>();
        for (Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest> entry : data.activeInOrder()) {
            Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), entry.getKey());
            UUID teamId = entry.getValue().team();
            QuestTeams.Team team = teamId == null ? null : teams.get(teamId);
            if (quest.isEmpty() || teamId != null && (team == null || !team.hasMember(player.getUUID()))) {
                dropped.add(entry.getKey());
            } else if (team != null) {
                entry.getValue().copyFrom(team, quest.get().value().objectives().size());
                ensureOnlineLeader(server, team, null);
                changed = true;
            }
        }
        for (ResourceKey<QuestDefinition> key : dropped) {
            Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
            quest.ifPresent(q -> NeoForge.EVENT_BUS.post(new QuestEvent.Abandon(player, q)));
            ActiveQuest removed = data.remove(key);
            if (removed != null && removed.team() != null) {
                leaveTeam(server, removed.team(), player.getUUID());
            }
            quest.ifPresent(q -> runActions(player, q, q.value().actions().abandon()));
            changed = true;
        }
        if (changed) {
            PlayerQuests.sync(player);
            changed();
        }
    }

    /**
     * 下线时:他当发起人的队伍,换成最早加入、还在线的成员(换上来的人不用准备);没人在线就先不换,
     * 等下一个上线的队员接过来。他从所有准备、出门名单里划掉(人不在线就不算在外面)
     */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        for (QuestTeams.Team team : QuestTeams.get(server).all()) {
            if (team.leader().equals(player.getUUID())) {
                ensureOnlineLeader(server, team, player.getUUID());
            }
        }
        QuestDepartures.removePlayer(player.getUUID());
        changed();
    }

    /** 服务器关掉:准备、出门名单都不存档,清空(单人存档退出再进,不带上一局的) */
    public static void onServerStopped(ServerStoppedEvent event) {
        QuestDepartures.clearAll();
    }

    /** 最早加入、还在线的队员(leaving 是正要下线的人,不算) */
    private static @Nullable UUID nextOnlineMember(MinecraftServer server, QuestTeams.Team team, @Nullable UUID leaving) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            online.add(p.getUUID());
        }
        for (UUID member : team.members()) {
            if (!member.equals(leaving) && online.contains(member)) {
                return member;
            }
        }
        return null;
    }
}
