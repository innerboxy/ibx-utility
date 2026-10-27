package ibx.utility.client;

import ibx.utility.IbxUtility;
import ibx.utility.recipe.EquipmentForgeRecipe;
import ibx.utility.recipe.FletchingRecipes;
import ibx.utility.registry.ModRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.List;

/**
 * 客户端缓存:服务端同步来的锻造配方(制箭配方顺带存进 {@link FletchingRecipes})。
 *
 * 服务端在 OnDatapackSyncEvent 里请求 NeoForge 代发(见 ModNetworking#onDatapackSync),
 * 登录与每次 /reload 后都会触发 {@link RecipesReceivedEvent}。锻造界面与 JEI 都从这里读。
 */
@EventBusSubscriber(modid = IbxUtility.MODID, value = Dist.CLIENT)
public final class ClientForgeRecipes {
    private static List<RecipeHolder<EquipmentForgeRecipe>> recipes = List.of();

    private ClientForgeRecipes() {
    }

    public static List<RecipeHolder<EquipmentForgeRecipe>> all() {
        return recipes;
    }

    /** 高优先级:赶在同样监听这个事件的 JEI 之前填好缓存,免得 JEI 启动时读到空表 */
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = List.copyOf(event.getRecipeMap().byType(ModRecipes.EQUIPMENT_FORGE_TYPE.get()));
        // 制箭配方的缓存放在两端共用的 FletchingRecipes 里,制箭台菜单的格子过滤要读
        FletchingRecipes.setSynced(event.getRecipeMap().byType(ModRecipes.FLETCHING_TYPE.get()));
    }

    /** 退出服务器时清掉,免得带着上一个服务器的配方进下一个 */
    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = List.of();
        FletchingRecipes.setSynced(List.of());
    }
}
