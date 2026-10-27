package ibx.utility.recipe;

import ibx.utility.registry.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

/**
 * 制箭配方的读取入口,服务端与客户端共用(菜单的格子过滤两端都要算,结果才一致)。
 * 服务端直接读配方管理器;客户端没有完整配方,读 ClientForgeRecipes 收到同步时存进来的副本。
 */
public final class FletchingRecipes {
    private static List<RecipeHolder<FletchingRecipe>> synced = List.of();

    private FletchingRecipes() {
    }

    public static Collection<RecipeHolder<FletchingRecipe>> all(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().recipeMap().byType(ModRecipes.FLETCHING_TYPE.get());
        }
        return synced;
    }

    /** 客户端:服务端同步来的配方(JEI 也从这里读) */
    public static List<RecipeHolder<FletchingRecipe>> synced() {
        return synced;
    }

    /** 客户端:登录与每次 /reload 收到配方时更新;退出服务器时传空表 */
    public static void setSynced(Collection<RecipeHolder<FletchingRecipe>> recipes) {
        synced = List.copyOf(recipes);
    }
}
