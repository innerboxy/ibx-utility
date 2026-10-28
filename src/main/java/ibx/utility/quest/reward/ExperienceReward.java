package ibx.utility.quest.reward;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 经验:经验点和经验等级可以只写一个,也可以都写;至少有一个大于 0。
 *
 * <pre>
 * { "type": "ibx_utility:experience", "points": 50 }
 * { "type": "ibx_utility:experience", "levels": 3 }
 * </pre>
 */
public record ExperienceReward(int points, int levels) implements QuestReward {
    public static final MapCodec<ExperienceReward> CODEC = RecordCodecBuilder.<ExperienceReward>mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("points", 0).forGetter(ExperienceReward::points),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("levels", 0).forGetter(ExperienceReward::levels)
    ).apply(instance, ExperienceReward::new)).validate(reward -> reward.points > 0 || reward.levels > 0
            ? DataResult.success(reward)
            : DataResult.error(() -> "experience reward needs points or levels"));

    @Override
    public MapCodec<ExperienceReward> codec() {
        return CODEC;
    }

    @Override
    public ItemStack displayIcon() {
        return new ItemStack(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public String displayCount() {
        return levels > 0 ? levels + "L" : String.valueOf(points);
    }

    @Override
    public Component displayName() {
        if (levels > 0 && points > 0) {
            return Component.translatable("quest.ibx_utility.reward.experience_both", levels, points);
        }
        return levels > 0
                ? Component.translatable("quest.ibx_utility.reward.experience_levels", levels)
                : Component.translatable("quest.ibx_utility.reward.experience_points", points);
    }

    @Override
    public void grant(ServerPlayer player) {
        if (levels > 0) {
            player.giveExperienceLevels(levels);
        }
        if (points > 0) {
            player.giveExperiencePoints(points);
        }
    }
}
