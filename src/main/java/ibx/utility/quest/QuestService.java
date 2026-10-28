package ibx.utility.quest;

import ibx.utility.api.event.QuestEvent;
import ibx.utility.config.Config;
import ibx.utility.quest.action.QuestAction;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.requirement.QuestRequirement;
import ibx.utility.quest.reward.QuestReward;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.DailyQuests;
import ibx.utility.quest.state.PlayerQuests;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
 * 任务的服务端逻辑:接、加入队伍、放弃、提交、加进度(击杀计数也走这里)、每日随机,沿路发 QuestEvent。
 * 悬赏板界面和对外接口 QuestApi 都走这里。
 * 组队任务的进度以队伍里的为准(QuestTeams),每次变化抄给在线成员,追踪栏统一只读玩家自己的数据。
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
        return AcceptResult.ACCEPTED;
    }

    /** 加入别人的队伍。一次性任务做过的人也能加入,提交时照样拿奖励、记完成 */
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
        data.start(team.quest(), objectives, player.level().getGameTime(), teamId).copyFrom(team, objectives);
        PlayerQuests.sync(player);
        runActions(player, quest, definition.actions().accept());
        return JoinResult.JOINED;
    }

    /** 放弃:进度清零、没有惩罚;组队任务就是退队。没接着返回 false */
    public static boolean abandon(ServerPlayer player, ResourceKey<QuestDefinition> key) {
        if (!PlayerQuests.get(player).isActive(key)) {
            return false;
        }
        Optional<Holder.Reference<QuestDefinition>> quest = lookup(player.level(), key);
        quest.ifPresent(q -> NeoForge.EVENT_BUS.post(new QuestEvent.Abandon(player, q)));
        ActiveQuest removed = PlayerQuests.get(player).remove(key);
        if (removed == null) {
            return false;
        }
        if (removed.team() != null) {
            leaveTeam(player.level().getServer(), removed.team(), player.getUUID());
        }
        PlayerQuests.sync(player);
        quest.ifPresent(q -> runActions(player, q, q.value().actions().abandon()));
        return true;
    }

    private static void leaveTeam(MinecraftServer server, UUID teamId, UUID player) {
        QuestTeams teams = QuestTeams.get(server);
        QuestTeams.Team team = teams.get(teamId);
        if (team != null && team.removeMember(player)) {
            teams.remove(teamId);
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
        } else {
            complete(player, quest);
        }
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
     * 登录时:补记不在线时队伍完成的任务;清掉找不到任务定义、或者队伍已经不在(不再是成员)的已接任务;
     * 还在的组队任务从队伍抄最新进度。
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
                changed = true;
            }
        }
        for (ResourceKey<QuestDefinition> key : dropped) {
            ActiveQuest removed = data.remove(key);
            if (removed != null && removed.team() != null) {
                leaveTeam(server, removed.team(), player.getUUID());
            }
            changed = true;
        }
        if (changed) {
            PlayerQuests.sync(player);
        }
    }

    /** 下线时:他当发起人的队伍,换成最早加入、还在线的成员;没人在线就先不换 */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        for (QuestTeams.Team team : QuestTeams.get(server).all()) {
            if (team.leader().equals(player.getUUID())) {
                UUID next = nextOnlineMember(server, team, player.getUUID());
                if (next != null) {
                    team.setLeader(next);
                }
            }
        }
    }

    private static @Nullable UUID nextOnlineMember(MinecraftServer server, QuestTeams.Team team, UUID leaving) {
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
