package ibx.utility.registry;

import ibx.utility.recipe.EquipmentForgeRecipe;
import ibx.utility.recipe.FletchingRecipe;
import ibx.utility.IbxUtility;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, IbxUtility.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, IbxUtility.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EquipmentForgeRecipe>> EQUIPMENT_FORGE_TYPE =
            RECIPE_TYPES.register("equipment_forge",
                    () -> RecipeType.simple(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "equipment_forge")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EquipmentForgeRecipe>> EQUIPMENT_FORGE_SERIALIZER =
            RECIPE_SERIALIZERS.register("equipment_forge", () -> EquipmentForgeRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FletchingRecipe>> FLETCHING_TYPE =
            RECIPE_TYPES.register("fletching",
                    () -> RecipeType.simple(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "fletching")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fletching", () -> FletchingRecipe.SERIALIZER);
}
