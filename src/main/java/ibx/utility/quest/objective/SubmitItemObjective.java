package ibx.utility.quest.objective;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.QuestDisplay;
import ibx.utility.quest.QuestInventory;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 提交物品:回悬赏板提交时从背包扣掉,可以分几次交。{@code item} 可以写物品 id、{@code #标签} 或列表。
 *
 * <pre>
 * { "type": "ibx_utility:submit_item", "item": "minecraft:bone", "count": 10 }
 * </pre>
 *
 * @param count 可选,默认 1
 */
public record SubmitItemObjective(HolderSet<Item> item, int count) implements QuestObjective {
    public static final MapCodec<SubmitItemObjective> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("item").forGetter(SubmitItemObjective::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SubmitItemObjective::count)
    ).apply(instance, SubmitItemObjective::new));

    @Override
    public int onSubmit(ServerPlayer player, int remaining) {
        return QuestInventory.take(player, item, remaining);
    }

    @Override
    public ItemStack displayIcon() {
        return QuestDisplay.itemIcon(item);
    }

    @Override
    public Component displayTarget() {
        return QuestDisplay.itemName(item);
    }

    @Override
    public Component displayLabel() {
        return Component.translatable("quest.ibx_utility.objective.submit_item", displayTarget());
    }

    @Override
    public MapCodec<SubmitItemObjective> codec() {
        return CODEC;
    }
}
