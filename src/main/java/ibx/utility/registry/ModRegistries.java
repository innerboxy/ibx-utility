package ibx.utility.registry;

import ibx.utility.recipe.EquipmentCategoryDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/** 自定义数据包注册表:装备分类。 */
public class ModRegistries {
    // 注册表键放在 minecraft 命名空间,数据包加载路径为 data/<包>/category/equipment_forge/;
    // 非 minecraft 命名空间会多一层 data/<包>/<注册表命名空间>/。
    // 分类条目 id 仍取各自文件的 Identifier(如 ibx_utility:melee),不受此影响。
    public static final ResourceKey<Registry<EquipmentCategoryDefinition>> EQUIPMENT_CATEGORY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("category/equipment_forge"));

    /** 传入网络编解码器,使定义随登录同步到客户端(标签渲染 / 物品 tooltip 需要) */
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EQUIPMENT_CATEGORY,
                EquipmentCategoryDefinition.CODEC, EquipmentCategoryDefinition.CODEC);
    }
}
