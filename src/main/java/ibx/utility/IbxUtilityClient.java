package ibx.utility;

import ibx.utility.client.QuestTracker;
import ibx.utility.client.screen.EquipmentForgeScreen;
import ibx.utility.client.screen.FletchingScreen;
import ibx.utility.config.ClientConfig;
import ibx.utility.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = IbxUtility.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IbxUtility.MODID, value = Dist.CLIENT)
public class IbxUtilityClient {

    public IbxUtilityClient(ModContainer modContainer) {
        // 客户端配置(追踪栏样式);模组列表里「配置」按钮用 NeoForge 自带的配置界面
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.EQUIPMENT_FORGE.get(), EquipmentForgeScreen::new);
        event.register(ModMenus.FLETCHING_TABLE.get(), FletchingScreen::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        QuestTracker.register(event);
    }
}
