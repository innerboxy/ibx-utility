package ibx.utility.compat.jei;

import ibx.utility.recipe.FletchingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 展示:与制箭台界面同一套排布——箭尾、箭身、箭头、药水四格一排 → 箭头 → 成品大格。
 * 四个输入格按菜单槽位 0-3 的顺序添加,JEI 的「+」据此把材料原样填进对应格子;
 * 「+」用 JEI 默认位置,即成品大格右侧。
 */
public class FletchingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<FletchingRecipe>> {
    /** 输入格物品坐标:背景 18x18 自动向左上偏移 1px,与界面一样格间留 4px */
    private static final int INPUT_X = 1;
    private static final int INPUT_STEP = 22;
    private static final int SLOT_Y = 5;
    /** JEI 箭头 22x16,与输入格、成品格各隔 8px */
    private static final int ARROW_X = 92;
    private static final int ARROW_Y = 5;
    /** 成品格物品坐标:26x26 背景向左上偏移 5px,即背景占 (122,0)-(148,26) */
    private static final int RESULT_X = 127;

    private static final int WIDTH = 148;
    private static final int HEIGHT = 26;

    private final IDrawableStatic arrow;

    public FletchingRecipeCategory(IGuiHelper guiHelper) {
        super(EquipmentForgeJeiPlugin.FLETCHING,
                Component.translatable("block.minecraft.fletching_table"),
                guiHelper.createDrawableItemLike(Items.FLETCHING_TABLE),
                WIDTH, HEIGHT);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FletchingRecipe> holder, IFocusGroup focuses) {
        FletchingRecipe recipe = holder.value();
        builder.addInputSlot(INPUT_X, SLOT_Y).setStandardSlotBackground().add(recipe.tail());
        builder.addInputSlot(INPUT_X + INPUT_STEP, SLOT_Y).setStandardSlotBackground().add(recipe.shaft());
        builder.addInputSlot(INPUT_X + 2 * INPUT_STEP, SLOT_Y).setStandardSlotBackground().add(recipe.tip());
        // 药水格始终占位(保持与菜单槽位一一对应),配方没有附加材料时留空
        IRecipeSlotBuilder additive = builder.addInputSlot(INPUT_X + 3 * INPUT_STEP, SLOT_Y).setStandardSlotBackground();
        IRecipeSlotBuilder output = builder.addOutputSlot(RESULT_X, SLOT_Y).setOutputSlotBackground();

        if (recipe.copyPotion() && recipe.additive().isPresent()) {
            // 药箭:按药水种类逐一展开,药水格与成品格联动——翻到哪种药水就显示对应的药箭,
            // 在 JEI 里查某种药箭时也只显示配它的那种药水
            List<ItemStack> sources = recipe.additive().get().display().resolveForStacks(additive.getContextMap());
            List<ItemStack> potions = new ArrayList<>();
            List<ItemStack> results = new ArrayList<>();
            BuiltInRegistries.POTION.listElements().forEach(potion -> {
                for (ItemStack source : sources) {
                    potions.add(PotionContents.createItemStack(source.getItem(), potion));
                    ItemStack result = recipe.resultView().copy();
                    result.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    results.add(result);
                }
            });
            additive.addItemStacks(potions);
            output.addItemStacks(results);
            builder.createFocusLink(additive, output);
        } else {
            recipe.additive().ifPresent(additive::add);
            output.add(recipe.resultView());
        }
    }

    @Override
    public void draw(RecipeHolder<FletchingRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, ARROW_X, ARROW_Y);
    }
}
