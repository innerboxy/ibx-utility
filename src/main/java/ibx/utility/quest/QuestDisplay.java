package ibx.utility.quest;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.Tags;

import java.util.Optional;

/** 目标里「物品 / 生物,或它们的标签」显示成什么名字和图标。 */
public final class QuestDisplay {
    private QuestDisplay() {}

    /** 标签用标签的翻译(没有就显示 #id);单个用它自己的名字;多个用第一个的名字 */
    public static Component itemName(HolderSet<Item> items) {
        Optional<TagKey<Item>> tag = items.unwrapKey();
        if (tag.isPresent()) {
            return tagName(tag.get());
        }
        return items.stream().findFirst().map(item -> new ItemStack(item).getHoverName()).orElse(Component.empty());
    }

    public static ItemStack itemIcon(HolderSet<Item> items) {
        return items.stream().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public static Component entityName(HolderSet<EntityType<?>> entities) {
        Optional<TagKey<EntityType<?>>> tag = entities.unwrapKey();
        if (tag.isPresent()) {
            return tagName(tag.get());
        }
        return entities.stream().findFirst().map(type -> type.value().getDescription()).orElse(Component.empty());
    }

    /** 生物用它的刷怪蛋,没有刷怪蛋就不画 */
    public static ItemStack entityIcon(HolderSet<EntityType<?>> entities) {
        return entities.stream().findFirst()
                .flatMap(type -> SpawnEggItem.byId(type.value()))
                .map(egg -> new ItemStack((Holder<Item>) egg))
                .orElse(ItemStack.EMPTY);
    }

    private static Component tagName(TagKey<?> tag) {
        return Component.translatableWithFallback(Tags.getTagTranslationKey(tag), "#" + tag.location());
    }
}
