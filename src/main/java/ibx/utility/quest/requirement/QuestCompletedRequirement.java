package ibx.utility.quest.requirement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.registry.ModRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

/**
 * 完成过某个任务(组队完成的,每个队员都算完成过)。
 *
 * <pre>
 * { "type": "ibx_utility:quest_completed", "quest": "example:first_steps" }
 * </pre>
 */
public record QuestCompletedRequirement(ResourceKey<QuestDefinition> quest) implements QuestRequirement {
    public static final MapCodec<QuestCompletedRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(ModRegistries.QUEST).fieldOf("quest").forGetter(QuestCompletedRequirement::quest)
    ).apply(instance, QuestCompletedRequirement::new));

    @Override
    public MapCodec<QuestCompletedRequirement> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ServerPlayer player) {
        return PlayerQuests.get(player).completions(quest) > 0;
    }
}
