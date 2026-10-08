package ibx.taverntales.forging.menu;

import ibx.taverntales.forging.recipe.FletchingRecipe;
import ibx.taverntales.forging.recipe.FletchingRecipeInput;
import ibx.taverntales.forging.recipe.FletchingRecipes;
import ibx.taverntales.forging.registry.ModMenus;
import ibx.taverntales.forging.registry.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 原版制箭台的界面:箭尾、箭身、箭头、药水四格一排,右侧出成品。
 * 关界面退还物品、Shift 点击等通用行为沿用原版锻造台同款的 {@link ItemCombinerMenu}。
 * 各格只收配方里出现在该位置的材料,Shift 点击背包物品时因此会自动进对的格子。
 */
public class FletchingMenu extends ItemCombinerMenu {
    public static final int TAIL_SLOT = 0;
    public static final int SHAFT_SLOT = 1;
    public static final int TIP_SLOT = 2;
    public static final int ADDITIVE_SLOT = 3;
    public static final int RESULT_SLOT = 4;
    public static final int INPUT_COUNT = 4;

    /** 四个输入格的物品坐标(间距 22,即格子之间留 4px),Screen 底图需与此一致 */
    public static final int INPUT_X = 15;
    public static final int INPUT_STEP = 22;
    public static final int SLOT_Y = 35;
    /** 成品格在 26x26 大格正中,物品坐标比大格左上角各缩进 5px */
    public static final int RESULT_X = 141;

    private final Level level;

    /** 客户端构造 */
    public FletchingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    /** 服务端构造 */
    public FletchingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        this(containerId, inventory, access, inventory.player.level());
    }

    private FletchingMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Level level) {
        super(ModMenus.FLETCHING_TABLE.get(), containerId, inventory, access, slotDefinitions(level));
        this.level = level;
    }

    private static ItemCombinerMenuSlotDefinition slotDefinitions(Level level) {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(TAIL_SLOT, INPUT_X, SLOT_Y, stack -> usedAs(level, stack, r -> Optional.of(r.tail())))
                .withSlot(SHAFT_SLOT, INPUT_X + INPUT_STEP, SLOT_Y, stack -> usedAs(level, stack, r -> Optional.of(r.shaft())))
                .withSlot(TIP_SLOT, INPUT_X + 2 * INPUT_STEP, SLOT_Y, stack -> usedAs(level, stack, r -> Optional.of(r.tip())))
                .withSlot(ADDITIVE_SLOT, INPUT_X + 3 * INPUT_STEP, SLOT_Y, stack -> usedAs(level, stack, FletchingRecipe::additive))
                .withResultSlot(RESULT_SLOT, RESULT_X, SLOT_Y)
                .build();
    }

    /** 物品是否在某条配方里用作该位置的材料;每次现查,/reload 后无需重开界面 */
    private static boolean usedAs(Level level, ItemStack stack, Function<FletchingRecipe, Optional<Ingredient>> part) {
        for (RecipeHolder<FletchingRecipe> holder : FletchingRecipes.all(level)) {
            if (part.apply(holder.value()).map(ingredient -> ingredient.test(stack)).orElse(false)) return true;
        }
        return false;
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(Blocks.FLETCHING_TABLE);
    }

    private FletchingRecipeInput createRecipeInput() {
        return new FletchingRecipeInput(inputSlots.getItem(TAIL_SLOT), inputSlots.getItem(SHAFT_SLOT),
                inputSlots.getItem(TIP_SLOT), inputSlots.getItem(ADDITIVE_SLOT));
    }

    /** 成品只在服务端计算,客户端靠容器同步拿到 */
    @Override
    public void createResult() {
        FletchingRecipeInput input = createRecipeInput();
        Optional<RecipeHolder<FletchingRecipe>> recipe = level instanceof ServerLevel serverLevel
                ? serverLevel.recipeAccess().getRecipeFor(ModRecipes.FLETCHING_TYPE.get(), input, serverLevel)
                : Optional.empty();
        recipe.ifPresentOrElse(holder -> {
            resultSlots.setRecipeUsed(holder);
            resultSlots.setItem(0, holder.value().assemble(input));
        }, () -> {
            resultSlots.setRecipeUsed(null);
            resultSlots.setItem(0, ItemStack.EMPTY);
        });
    }

    @Override
    protected void onTake(Player player, ItemStack carried) {
        carried.onCraftedBy(player, carried.getCount());
        resultSlots.awardUsedRecipes(player, List.of(inputSlots.getItem(TAIL_SLOT), inputSlots.getItem(SHAFT_SLOT),
                inputSlots.getItem(TIP_SLOT), inputSlots.getItem(ADDITIVE_SLOT)));
        // 药水按原版药箭的规矩整瓶消耗,不退空瓶
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            shrinkStackInSlot(slot);
        }
        access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.VILLAGER_WORK_FLETCHER,
                SoundSource.BLOCKS, 1.0f, 1.0f));
    }

    private void shrinkStackInSlot(int slot) {
        ItemStack stack = inputSlots.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            inputSlots.setItem(slot, stack);
        }
    }

    /** 背包里的物品 Shift 点击时:有能收它的输入格才往输入格送,否则照常在背包与快捷栏间移动 */
    @Override
    protected boolean canMoveIntoInputSlots(ItemStack stack) {
        for (int i = 0; i < INPUT_COUNT; i++) {
            Slot slot = getSlot(i);
            if (!slot.mayPlace(stack)) continue;
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < slot.getMaxStackSize(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != resultSlots && super.canTakeItemForPickAll(carried, target);
    }
}
