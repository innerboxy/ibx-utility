package ibx.utility.api.event;

import ibx.utility.quest.QuestDefinition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 任务事件,只在服务端发,发在 {@code NeoForge.EVENT_BUS} 上。
 * 接、加入、提交可以取消(取消后玩家在悬赏板上只听到一声失败的低音);进度、放弃只是通知。
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
}
