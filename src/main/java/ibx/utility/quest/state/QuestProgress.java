package ibx.utility.quest.state;

/** 一个任务每个目标的进度,下标同任务里 objectives 的顺序。个人任务存在玩家身上,组队任务存在队伍里。 */
public interface QuestProgress {
    int progress(int i);

    void setProgress(int i, int value);
}
