package ibx.utility.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.action.QuestAction;

import java.util.List;

/**
 * 任务在接、提交、放弃时额外执行的动作,三个列表都可选。
 *
 * <pre>
 * "actions": { "accept": [ { "type": "其他模组:某动作", ... } ], "submit": [], "abandon": [] }
 * </pre>
 */
public record QuestActions(List<QuestAction> accept, List<QuestAction> submit, List<QuestAction> abandon) {
    public static final QuestActions NONE = new QuestActions(List.of(), List.of(), List.of());

    public static final Codec<QuestActions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            QuestAction.CODEC.listOf().optionalFieldOf("accept", List.of()).forGetter(QuestActions::accept),
            QuestAction.CODEC.listOf().optionalFieldOf("submit", List.of()).forGetter(QuestActions::submit),
            QuestAction.CODEC.listOf().optionalFieldOf("abandon", List.of()).forGetter(QuestActions::abandon)
    ).apply(instance, QuestActions::new));
}
