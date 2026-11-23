package ibx.utility.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.action.QuestAction;

import java.util.List;

/**
 * 任务在接、出发、提交、放弃时额外执行的动作,四个列表都可选。
 * 写了 {@code depart} 的任务是「要出发」的任务:接下后悬赏板上多出准备 / 出发按钮,出发时对每个走的人跑这些动作。
 *
 * <pre>
 * "actions": { "accept": [ { "type": "其他模组:某动作", ... } ], "depart": [], "submit": [], "abandon": [] }
 * </pre>
 */
public record QuestActions(List<QuestAction> accept, List<QuestAction> depart, List<QuestAction> submit, List<QuestAction> abandon) {
    public static final QuestActions NONE = new QuestActions(List.of(), List.of(), List.of(), List.of());

    public static final Codec<QuestActions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            QuestAction.CODEC.listOf().optionalFieldOf("accept", List.of()).forGetter(QuestActions::accept),
            QuestAction.CODEC.listOf().optionalFieldOf("depart", List.of()).forGetter(QuestActions::depart),
            QuestAction.CODEC.listOf().optionalFieldOf("submit", List.of()).forGetter(QuestActions::submit),
            QuestAction.CODEC.listOf().optionalFieldOf("abandon", List.of()).forGetter(QuestActions::abandon)
    ).apply(instance, QuestActions::new));

    /** 是不是要出发的任务(写了 depart 动作) */
    public boolean hasDeparture() {
        return !depart.isEmpty();
    }
}
