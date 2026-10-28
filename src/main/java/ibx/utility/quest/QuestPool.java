package ibx.utility.quest;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** 任务挂在悬赏板上的方式:固定挂着,还是进随机池、被每日刷新抽中才出现。 */
public enum QuestPool implements StringRepresentable {
    FIXED("fixed"),
    RANDOM("random");

    public static final Codec<QuestPool> CODEC = StringRepresentable.fromEnum(QuestPool::values);

    private final String name;

    QuestPool(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
