package ibx.utility.quest.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

/**
 * 固定物品。背包放不下的掉在脚下。{@code item} 同物品叠的写法,也可以只写物品 id。
 * 任务数据加载时物品还没绑定组件,所以存成模板,发奖励时再 {@link ItemStackTemplate#create()}。
 *
 * <pre>
 * { "type": "ibx_utility:item", "item": { "id": "minecraft:emerald", "count": 3 } }
 * { "type": "ibx_utility:item", "item": "minecraft:diamond" }
 * </pre>
 */
public record ItemReward(ItemStackTemplate item) implements QuestReward {
    public static final MapCodec<ItemReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemReward::item)
    ).apply(instance, ItemReward::new));

    @Override
    public MapCodec<ItemReward> codec() {
        return CODEC;
    }

    @Override
    public ItemStack displayIcon() {
        return item.create();
    }

    @Override
    public @Nullable String displayCount() {
        return item.count() > 1 ? String.valueOf(item.count()) : null;
    }

    @Override
    public void grant(ServerPlayer player) {
        player.getInventory().placeItemBackInInventory(item.create());
    }
}
