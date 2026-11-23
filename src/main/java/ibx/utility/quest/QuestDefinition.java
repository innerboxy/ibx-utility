package ibx.utility.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.requirement.QuestRequirement;
import ibx.utility.quest.reward.QuestReward;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

/**
 * 一个任务,存于数据包注册表 {@code ibx_utility:quest}(见 ModRegistries),一个任务一份 JSON。
 * 任务 id 即文件的 Identifier。
 *
 * <pre>
 * data/&lt;命名空间&gt;/ibx_utility/quest/wolf_hunt.json
 * {
 *   "name": { "translate": "quest.example.wolf_hunt" },
 *   "description": { "translate": "quest.example.wolf_hunt.desc" },   // 可选
 *   "icon": "minecraft:bone",                                        // 可选,默认 minecraft:book;也可以写图片路径 "example:textures/quest/wolf.png"
 *   "category": "example:hunting",                                   // 悬赏板分页 id
 *   "pool": "fixed",                                                 // 可选,fixed(默认)/ random
 *   "weight": 1,                                                     // 可选,随机池里的权重,默认 1
 *   "requirements": [ { "type": "ibx_utility:quest_completed", "quest": "example:first_steps" } ],
 *   "repeatable": true,                                              // 可选,默认 false(一次性)
 *   "team": { "max": 4 },                                            // 可选,写了才能组队
 *   "objectives": [ { "type": "ibx_utility:kill", "entity": "minecraft:wolf", "count": 5 } ],
 *   "rewards": [ { "type": "ibx_utility:item", "item": { "id": "minecraft:emerald", "count": 3 } } ],
 *   "actions": { "accept": [], "depart": [], "submit": [], "abandon": [] }   // 可选,别的模组注册的动作;写了 depart 就是要出发的任务
 * }
 * </pre>
 *
 * @param icon     物品 id,或者以 .png 结尾的图片路径(资源包里的完整路径,画的时候整张缩到 16×16)
 * @param category 悬赏板分页({@code category/quest_board} 里的 id);那页不存在(写错了、或配置里关掉了)时整个任务跳过,当作没有
 * @param weight   只对随机池有用
 * @param team     空 = 只能一个人做
 */
public record QuestDefinition(
        Component name,
        Optional<Component> description,
        Identifier icon,
        Identifier category,
        QuestPool pool,
        int weight,
        List<QuestRequirement> requirements,
        boolean repeatable,
        Optional<QuestTeam> team,
        List<QuestObjective> objectives,
        List<QuestReward> rewards,
        QuestActions actions
) {
    /** 未指定 icon 或其物品不存在时的图标 */
    public static final Identifier DEFAULT_ICON = Identifier.withDefaultNamespace("book");

    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(QuestDefinition::name),
            ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(QuestDefinition::description),
            Identifier.CODEC.optionalFieldOf("icon", DEFAULT_ICON).forGetter(QuestDefinition::icon),
            Identifier.CODEC.fieldOf("category").forGetter(QuestDefinition::category),
            QuestPool.CODEC.optionalFieldOf("pool", QuestPool.FIXED).forGetter(QuestDefinition::pool),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(QuestDefinition::weight),
            QuestRequirement.CODEC.listOf().optionalFieldOf("requirements", List.of()).forGetter(QuestDefinition::requirements),
            Codec.BOOL.optionalFieldOf("repeatable", false).forGetter(QuestDefinition::repeatable),
            QuestTeam.CODEC.optionalFieldOf("team").forGetter(QuestDefinition::team),
            ExtraCodecs.nonEmptyList(QuestObjective.CODEC.listOf()).fieldOf("objectives").forGetter(QuestDefinition::objectives),
            QuestReward.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(QuestDefinition::rewards),
            QuestActions.CODEC.optionalFieldOf("actions", QuestActions.NONE).forGetter(QuestDefinition::actions)
    ).apply(instance, QuestDefinition::new));

    /** icon 写的是图片路径(以 .png 结尾)而不是物品 id;客户端画图标走 DataIcons */
    public boolean hasTextureIcon() {
        return icon.getPath().endsWith(".png");
    }

    /** 图标物品;icon 物品不存在(如未装对应模组)、或者写的是图片路径时回退为书 */
    public ItemStack iconStack() {
        Item item = BuiltInRegistries.ITEM.getOptional(icon)
                .filter(i -> i != Items.AIR)
                .orElseGet(() -> BuiltInRegistries.ITEM.getValue(DEFAULT_ICON));
        return new ItemStack(item);
    }
}
