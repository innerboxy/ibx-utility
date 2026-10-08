package ibx.taverntales.forging.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** 制箭台的四个输入格:箭尾、箭身、箭头、药水(附加材料,可空) */
public record FletchingRecipeInput(ItemStack tail, ItemStack shaft, ItemStack tip, ItemStack additive) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> tail;
            case 1 -> shaft;
            case 2 -> tip;
            case 3 -> additive;
            default -> throw new IllegalArgumentException("Recipe does not contain slot " + index);
        };
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return tail.isEmpty() && shaft.isEmpty() && tip.isEmpty() && additive.isEmpty();
    }
}
