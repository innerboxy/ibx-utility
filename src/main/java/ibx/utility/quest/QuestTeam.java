package ibx.utility.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 任务能组队时的设置:一人接、别人在悬赏板上加入,进度共享。
 *
 * <pre>
 * "team": { "max": 4 }
 * </pre>
 *
 * @param max 人数上限(含发起人),可选,默认 4
 */
public record QuestTeam(int max) {
    public static final int DEFAULT_MAX = 4;

    public static final Codec<QuestTeam> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(2, Integer.MAX_VALUE).optionalFieldOf("max", DEFAULT_MAX).forGetter(QuestTeam::max)
    ).apply(instance, QuestTeam::new));
}
