package ibx.utility.registry;

import ibx.utility.IbxUtility;
import ibx.utility.quest.QuestCategoryDefinition;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.recipe.EquipmentCategoryDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/** 自定义数据包注册表:装备分类、悬赏板分页、任务。 */
public class ModRegistries {
    // 注册表键放在 minecraft 命名空间,数据包加载路径为 data/<包>/category/equipment_forge/;
    // 非 minecraft 命名空间会多一层 data/<包>/<注册表命名空间>/。
    // 分类条目 id 仍取各自文件的 Identifier(如 ibx_utility:melee),不受此影响。
    public static final ResourceKey<Registry<EquipmentCategoryDefinition>> EQUIPMENT_CATEGORY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("category/equipment_forge"));

    /** 悬赏板分页,和锻造台分类并排:data/<包>/category/quest_board/ */
    public static final ResourceKey<Registry<QuestCategoryDefinition>> QUEST_CATEGORY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("category/quest_board"));

    /** 任务,注册表键带本模组命名空间,不会和别的模组撞名:data/<包>/ibx_utility/quest/ */
    public static final ResourceKey<Registry<QuestDefinition>> QUEST =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest"));

    /** 传入网络编解码器,使定义随登录同步到客户端(标签渲染 / 物品 tooltip / 悬赏板界面需要) */
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EQUIPMENT_CATEGORY,
                EquipmentCategoryDefinition.CODEC, EquipmentCategoryDefinition.CODEC);
        event.dataPackRegistry(QUEST_CATEGORY,
                QuestCategoryDefinition.CODEC, QuestCategoryDefinition.CODEC);
        event.dataPackRegistry(QUEST, QuestDefinition.CODEC, QuestDefinition.CODEC);
    }
}
