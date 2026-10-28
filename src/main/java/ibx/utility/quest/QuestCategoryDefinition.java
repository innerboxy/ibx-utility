package ibx.utility.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 悬赏板的一页,存于数据包注册表 {@code minecraft:category/quest_board}(见 ModRegistries),
 * 和锻造台分类 {@code category/equipment_forge} 并排、写法相同。分页 id 即文件的 Identifier,任务的 {@code category} 引用它。
 *
 * <pre>
 * data/&lt;命名空间&gt;/category/quest_board/hunting.json
 * {
 *   "name": { "translate": "category.example.hunting" },
 *   "order": 0,                          // 可选,默认 0
 *   "icon": "minecraft:iron_sword"       // 可选,默认 minecraft:book
 * }
 * </pre>
 *
 * @param name  书签的显示名
 * @param order 书签从上到下的顺序,越小越靠上;相同则按分页 id 字母序。可选,默认 0
 * @param icon  书签上的图标物品 id。可选,默认书本;物品不存在时同样回退为书本
 */
public record QuestCategoryDefinition(Component name, int order, Identifier icon) {
    /** 未指定 icon 或其物品不存在时的图标 */
    public static final Identifier DEFAULT_ICON = Identifier.withDefaultNamespace("book");

    public static final Codec<QuestCategoryDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(QuestCategoryDefinition::name),
            Codec.INT.optionalFieldOf("order", 0).forGetter(QuestCategoryDefinition::order),
            Identifier.CODEC.optionalFieldOf("icon", DEFAULT_ICON).forGetter(QuestCategoryDefinition::icon)
    ).apply(instance, QuestCategoryDefinition::new));

    /** 书签图标;icon 物品不存在(如未装对应模组)时回退为书 */
    public ItemStack iconStack() {
        Item item = BuiltInRegistries.ITEM.getOptional(icon)
                .filter(i -> i != Items.AIR)
                .orElseGet(() -> BuiltInRegistries.ITEM.getValue(DEFAULT_ICON));
        return new ItemStack(item);
    }
}
