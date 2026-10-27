package ibx.utility.registry;

import ibx.utility.menu.EquipmentForgeMenu;
import ibx.utility.menu.FletchingMenu;
import ibx.utility.IbxUtility;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IbxUtility.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EquipmentForgeMenu>> EQUIPMENT_FORGE =
            MENUS.register("equipment_forge",
                    () -> new MenuType<>(EquipmentForgeMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** 原版制箭台(方块本身没有界面,由 FletchingTableInteraction 在右键时打开) */
    public static final DeferredHolder<MenuType<?>, MenuType<FletchingMenu>> FLETCHING_TABLE =
            MENUS.register("fletching_table",
                    () -> new MenuType<>(FletchingMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
