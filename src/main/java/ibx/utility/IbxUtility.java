package ibx.utility;

import com.mojang.logging.LogUtils;
import ibx.utility.block.FletchingTableInteraction;
import ibx.utility.config.Config;
import ibx.utility.lootbag.LootBagItem;
import ibx.utility.network.ModNetworking;
import ibx.utility.registry.ModBlocks;
import ibx.utility.registry.ModConditions;
import ibx.utility.registry.ModDataComponents;
import ibx.utility.registry.ModItems;
import ibx.utility.registry.ModLootContextParamSets;
import ibx.utility.registry.ModMenus;
import ibx.utility.registry.ModRecipes;
import ibx.utility.registry.ModRegistries;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(IbxUtility.MODID)
public class IbxUtility {
    public static final String MODID = "ibx_utility";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IbxUtility(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModConditions.CONDITION_CODECS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        modEventBus.addListener(ModNetworking::register);
        modEventBus.addListener(ModRegistries::register);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(LootBagItem::registerTooltip);

        // 战利品袋的战利品表参数集:没有注册表事件可挂,构造器里直接写静态表,赶在数据包加载之前
        ModLootContextParamSets.register();

        // 战利品表纯服务端,客户端 JEI 要展示袋子掉落只能靠同步(游戏事件总线,非 mod 总线)
        NeoForge.EVENT_BUS.addListener(ModNetworking::onDatapackSync);
        // 右键原版制箭台打开制箭界面
        NeoForge.EVENT_BUS.addListener(FletchingTableInteraction::onRightClickBlock);

        // 通用配置:控制是否移除原版木质配方
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    //创造物品栏位:功能方块分类,同时兼容服务器整合包的 kubejs 专属分类
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // 装备锻造台紧挨在铁砧前面;铁砧若被整合包从该分类移除(insertBefore 找不到目标会抛异常),退回到末尾追加
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ItemStack forge = new ItemStack(ModItems.EQUIPMENT_FORGE.get());
            ItemStack anvil = new ItemStack(Items.ANVIL);
            if (event.getParentEntries().contains(anvil) && event.getSearchEntries().contains(anvil)) {
                event.insertBefore(anvil, forge, TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(forge);
            }
        }
        // 战利品袋放原材料栏最前面。这里给的是空袋(不带 loot_bag_type 组件)——袋子种类由数据包定义,
        // 数量不定且随数据包变化,创造栏不宜穷举;要具体袋子请用 /give 指定组件。
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertFirst(new ItemStack(ModItems.LOOT_BAG.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
