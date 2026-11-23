package ibx.utility.api.event;

import ibx.utility.quest.QuestDefinition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 任务事件,只在服务端发,发在 {@code NeoForge.EVENT_BUS} 上。
 * 接、加入、出发、提交可以取消(取消后玩家在悬赏板上只听到一声失败的低音);进度、放弃只是通知;
 * 召回取消的是「送回重生点」这个默认处理。
 */
public abstract class QuestEvent extends Event {
    private final ServerPlayer player;
    private final Holder.Reference<QuestDefinition> quest;

    protected QuestEvent(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
        this.player = player;
        this.quest = quest;
    }

    /** 做这件事的玩家 */
    public ServerPlayer getPlayer() {
        return player;
    }

    public Holder.Reference<QuestDefinition> getQuest() {
        return quest;
    }

    /** 接任务:条件都检查过了、还没写进玩家数据。组队任务接下就是新建一支队伍。可以取消 */
    public static final class Accept extends QuestEvent implements ICancellableEvent {
        public Accept(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
            super(player, quest);
        }
    }

    /** 加入别人的队伍:条件都检查过了、还没加进去。可以取消 */
    public static final class Join extends QuestEvent implements ICancellableEvent {
        private final UUID team;

        public Join(ServerPlayer player, Holder.Reference<QuestDefinition> quest, UUID team) {
            super(player, quest);
            this.team = team;
        }

        public UUID getTeam() {
            return team;
        }
    }

    /**
     * 出发(要出发的任务,即 actions 里写了 depart 的):条件都检查过了、还没记进出门名单、还没跑 depart 动作。可以取消。
     * <ul>
     *     <li>开局(这一组还没人在外面):发起人点的,带上他自己和点了准备、在线的队员;个人任务就是他自己。</li>
     *     <li>中途进(这一组已经有人在外面):没跟着走的队员自己点的,只有他一个。</li>
     * </ul>
     * 之后对每个走的人跑一遍 depart 动作。人去哪、什么时候算回来由挂动作的模组决定,回来时调 QuestApi.markReturned。
     */
    public static final class Depart extends QuestEvent implements ICancellableEvent {
        private final List<ServerPlayer> departing;
        private final @Nullable UUID team;
        private final boolean opening;

        public Depart(ServerPlayer player, Holder.Reference<QuestDefinition> quest, List<ServerPlayer> departing, @Nullable UUID team,
                      boolean opening) {
            super(player, quest);
            this.departing = List.copyOf(departing);
            this.team = team;
            this.opening = opening;
        }

        /** 这次走的人(点的人排第一个) */
        public List<ServerPlayer> getDeparting() {
            return departing;
        }

        /** 组队任务的队伍;个人任务是 null */
        public @Nullable UUID getTeam() {
            return team;
        }

        /** 是不是开局:true = 这一组第一次出发;false = 已经有人在外面,这个人中途进 */
        public boolean isOpening() {
            return opening;
        }
    }

    /**
     * 某个目标的进度涨了(击杀、在悬赏板上交了一部分物品、别的模组调 QuestApi.addProgress)。
     * 组队任务的进度是全队共用的,这里的玩家是让它涨的那个人。收集物品类目标不存进度,不发这个事件。
     */
    public static final class Progress extends QuestEvent {
        private final int objective;
        private final int oldProgress;
        private final int newProgress;
        private final @Nullable UUID team;

        public Progress(ServerPlayer player, Holder.Reference<QuestDefinition> quest, int objective, int oldProgress, int newProgress,
                        @Nullable UUID team) {
            super(player, quest);
            this.objective = objective;
            this.oldProgress = oldProgress;
            this.newProgress = newProgress;
            this.team = team;
        }

        /** 第几个目标(从 0 数,同任务 JSON 里 objectives 的顺序) */
        public int getObjective() {
            return objective;
        }

        public int getOldProgress() {
            return oldProgress;
        }

        public int getNewProgress() {
            return newProgress;
        }

        /** 组队任务的队伍;个人任务是 null */
        public @Nullable UUID getTeam() {
            return team;
        }
    }

    /**
     * 提交:目标全部达成、还没发奖励。可以取消——取消后任务留着,这次交上去的物品照样算进度、不退回。
     * 组队任务只对提交的那个人发一次,取消就是全队都没完成。
     */
    public static final class Submit extends QuestEvent implements ICancellableEvent {
        public Submit(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
            super(player, quest);
        }
    }

    /** 放弃(在悬赏板上,或别的模组调 QuestApi.abandon):进度还没清、还没退队。只是通知,不能取消 */
    public static final class Abandon extends QuestEvent {
        public Abandon(ServerPlayer player, Holder.Reference<QuestDefinition> quest) {
            super(player, quest);
        }
    }

    /**
     * 召回:这个人出发了还没回来,却已经不在做这个任务了(被发起人移出队伍,或者在外面时放弃),人还留在外面。
     * 在放弃事件、abandon 动作之后发,这时他已经不在队伍和出门名单里。
     * 没被取消:悬赏板把他送回重生点(同从末地回来——床 / 重生锚,没有就是世界出生点;不耗重生锚的充能;
     * 正死着的人不传,他复活时本来就在重生点)。可以取消:取消后悬赏板什么都不做,由取消的模组自己把人送走。
     */
    public static final class Recall extends QuestEvent implements ICancellableEvent {
        /** 为什么召回 */
        public enum Reason {
            /** 被发起人移出队伍 */
            KICKED,
            /** 自己放弃(悬赏板上,或别的模组调 QuestApi.abandon) */
            ABANDONED
        }

        private final Reason reason;

        public Recall(ServerPlayer player, Holder.Reference<QuestDefinition> quest, Reason reason) {
            super(player, quest);
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }
    }
}
