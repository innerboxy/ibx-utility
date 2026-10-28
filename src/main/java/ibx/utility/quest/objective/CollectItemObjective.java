package ibx.utility.quest.objective;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.QuestDisplay;
import ibx.utility.quest.QuestInventory;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * 收集物品:提交时背包里够数就算,不扣。组队任务数的是点「提交」那个人的背包。
 *
 * <pre>
 * { "type": "ibx_utility:collect_item", "item": "#minecraft:logs", "count": 32, "name": "原木" }
 * </pre>
 *
 * @param count 可选,默认 1
 * @param name  可选,显示用的名字;不写就用物品(标签)的名字
 */
public record CollectItemObjective(HolderSet<Item> item, int count, Optional<Component> name) implements QuestObjective {
    public static final MapCodec<CollectItemObjective> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("item").forGetter(CollectItemObjective::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CollectItemObjective::count),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(CollectItemObjective::name)
    ).apply(instance, CollectItemObjective::new));

    /** 不看进度,提交时数背包 */
    @Override
    public boolean isComplete(ServerPlayer player, int progress) {
        return QuestInventory.count(player, item) >= count;
    }

    @Override
    public ItemStack displayIcon() {
        return QuestDisplay.itemIcon(item);
    }

    @Override
    public List<ItemStack> displayIcons() {
        return QuestDisplay.itemIcons(item);
    }

    @Override
    public Component displayTarget() {
        return name.orElseGet(() -> QuestDisplay.itemName(item));
    }

    @Override
    public Component displayLabel() {
        return Component.translatable("quest.ibx_utility.objective.collect_item", displayTarget());
    }

    @Override
    public int displayProgress(Player player, int progress) {
        return QuestInventory.count(player, item);
    }

    @Override
    public MapCodec<CollectItemObjective> codec() {
        return CODEC;
    }
}
