package ibx.utility;

import ibx.utility.client.screen.EquipmentForgeScreen;
import ibx.utility.client.screen.FletchingScreen;
import ibx.utility.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = IbxUtility.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IbxUtility.MODID, value = Dist.CLIENT)
public class IbxUtilityClient {

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.EQUIPMENT_FORGE.get(), EquipmentForgeScreen::new);
        event.register(ModMenus.FLETCHING_TABLE.get(), FletchingScreen::new);
    }
}
