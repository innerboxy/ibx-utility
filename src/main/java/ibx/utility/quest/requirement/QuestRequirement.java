package ibx.utility.quest.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import ibx.utility.registry.QuestTypeRegistries;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

/**
 * 任务前置条件。全部满足才在悬赏板上显示、才能接;不满足的任务不显示。
 * JSON 里按 {@code "type"} 分类型,类型注册在 {@code ibx_utility:quest_requirement_type}(见 QuestTypeRegistries)。
 */
public interface QuestRequirement {
    Codec<QuestRequirement> CODEC = Codec.lazyInitialized(() -> QuestTypeRegistries.REQUIREMENT_TYPES.byNameCodec()
            .dispatch("type", QuestRequirement::codec, Function.identity()));

    MapCodec<? extends QuestRequirement> codec();

    boolean test(ServerPlayer player);
}
