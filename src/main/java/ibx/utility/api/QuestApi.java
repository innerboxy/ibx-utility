package ibx.utility.api;

import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.QuestService;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.PlayerQuests;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * 给别的模组用的任务接口。读的方法两端都能用(客户端只有自己的数据);改的方法只在服务端用。
 * <ul>
 *     <li>事件:{@link ibx.utility.api.event.QuestEvent}(接 / 加入 / 出发 / 进度 / 提交 / 放弃),发在 NeoForge.EVENT_BUS 上。</li>
 *     <li>新的目标、奖励、前置条件、任务动作类型:注册到 {@link ibx.utility.registry.QuestTypeRegistries} 的四个注册表,
 *     任务 JSON 里按 {@code "type"} 写上就能用。</li>
 * </ul>
 */
public final class QuestApi {
    private QuestApi() {}

    /** 已接的任务,按接下的先后 */
    public static List<ResourceKey<QuestDefinition>> activeQuests(Player player) {
        return PlayerQuests.get(player).activeInOrder().stream().map(Map.Entry::getKey).toList();
    }

    public static boolean isActive(Player player, ResourceKey<QuestDefinition> quest) {
        return PlayerQuests.get(player).isActive(quest);
    }

    /** 已接任务第 objective 个目标(从 0 数)存下的进度;组队任务是全队共用的那份。没接是 0。收集物品类目标不存进度,要数背包 */
    public static int progress(Player player, ResourceKey<QuestDefinition> quest, int objective) {
        ActiveQuest active = PlayerQuests.get(player).active(quest);
        return active == null ? 0 : active.progress(objective);
    }

    /** 完成过几次 */
    public static int completions(Player player, ResourceKey<QuestDefinition> quest) {
        return PlayerQuests.get(player).completions(quest);
    }

    /**
     * 给已接任务的第 objective 个目标加进度(封顶到所需数量;组队任务加到队伍里)。比如判定「Boss 打赢了」。
     * 会发进度事件。返回实际涨了多少;没接这个任务、目标已经满了返回 0
     */
    public static int addProgress(ServerPlayer player, ResourceKey<QuestDefinition> quest, int objective, int amount) {
        return QuestService.addProgress(player, quest, objective, amount);
    }

    /**
     * 给这个玩家全部已接任务里的目标加进度:amount 按目标算该加多少,不相关的返回 0。
     * 适合自己注册的目标类型,比如 {@code objective -> objective instanceof MyObjective my && my.matches(...) ? 1 : 0}。
     * 返回一共涨了多少
     */
    public static int addProgress(ServerPlayer player, ToIntFunction<QuestObjective> amount) {
        return QuestService.addProgress(player, amount);
    }

    /** 让玩家放弃一个任务,同在悬赏板上放弃(组队任务就是退队,会发放弃事件)。没接着返回 false */
    public static boolean abandon(ServerPlayer player, ResourceKey<QuestDefinition> quest) {
        return QuestService.abandon(player, quest);
    }

    /** 已接任务的队伍 id;个人任务或没接是空 */
    public static Optional<UUID> team(Player player, ResourceKey<QuestDefinition> quest) {
        ActiveQuest active = PlayerQuests.get(player).active(quest);
        return active == null ? Optional.empty() : Optional.ofNullable(active.team());
    }

    // ---------------------------------------------------------------- 出发(要出发的任务:actions 里写了 depart),只在服务端用

    /** 这个人是不是在这个任务的外面(出发了还没回来) */
    public static boolean isAway(ServerPlayer player, ResourceKey<QuestDefinition> quest) {
        return QuestService.isAway(player, quest);
    }

    /** 跟这个人同一组、出发了还没回来的人(组队任务是全队的,个人任务只有他自己),按出发先后 */
    public static List<UUID> awayPlayers(ServerPlayer player, ResourceKey<QuestDefinition> quest) {
        return QuestService.awayPlayers(player, quest);
    }

    /**
     * 这个人回来了:从出门名单里划掉,悬赏板上他又能点出发。一组人都回来了就不再算进行中,下次要发起人重新开局。
     * 下线的人悬赏板自己会划掉,不用调;准备、出门名单不存档,重启后也是空的。返回他本来在不在外面
     */
    public static boolean markReturned(ServerPlayer player, ResourceKey<QuestDefinition> quest) {
        return QuestService.markReturned(player, quest);
    }
}
