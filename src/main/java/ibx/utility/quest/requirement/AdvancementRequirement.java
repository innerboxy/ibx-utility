package ibx.utility.quest.requirement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * 拿到了某个原版进度。进度不存在(写错 id、对应模组没装)时当作没拿到。
 *
 * <pre>
 * { "type": "ibx_utility:advancement", "advancement": "minecraft:story/mine_diamond" }
 * </pre>
 */
public record AdvancementRequirement(Identifier advancement) implements QuestRequirement {
    public static final MapCodec<AdvancementRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("advancement").forGetter(AdvancementRequirement::advancement)
    ).apply(instance, AdvancementRequirement::new));

    @Override
    public MapCodec<AdvancementRequirement> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ServerPlayer player) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(advancement);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }
}
