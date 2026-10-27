package ibx.utility.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.registry.ModRecipes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * 制箭配方:箭尾 + 箭身 + 箭头(+ 可选的附加材料)→ 成品,每次合成四格各消耗 1 个。
 * 不写 additive 的配方要求药水格为空;copy_potion 为 true 时把附加材料的药水效果复制到成品上(药箭,
 * 同原版 crafting_imbue)。内置配方在 data/ibx_utility/recipe/fletching/ 下。
 */
public class FletchingRecipe implements Recipe<FletchingRecipeInput> {
    private final Ingredient tail;
    private final Ingredient shaft;
    private final Ingredient tip;
    private final Optional<Ingredient> additive;
    private final boolean copyPotion;
    private final ItemStackTemplate result;
    /** 产物的只读展示实例。26.1 起 ItemStack 须在注册表加载后才能构造,故懒创建而非在解码时创建 */
    private ItemStack resultView;

    public FletchingRecipe(Ingredient tail, Ingredient shaft, Ingredient tip, Optional<Ingredient> additive,
                           boolean copyPotion, ItemStackTemplate result) {
        this.tail = tail;
        this.shaft = shaft;
        this.tip = tip;
        this.additive = additive;
        this.copyPotion = copyPotion;
        this.result = result;
    }

    public Ingredient tail() {
        return tail;
    }

    public Ingredient shaft() {
        return shaft;
    }

    public Ingredient tip() {
        return tip;
    }

    public Optional<Ingredient> additive() {
        return additive;
    }

    /** 成品是否继承附加材料(药水)的药水效果 */
    public boolean copyPotion() {
        return copyPotion;
    }

    public ItemStackTemplate result() {
        return result;
    }

    /** 产物的只读展示实例(JEI 渲染用),调用方不得修改;要发给玩家请用 {@link #assemble} 拿新副本 */
    public ItemStack resultView() {
        if (resultView == null) resultView = result.create();
        return resultView;
    }

    @Override
    public boolean matches(FletchingRecipeInput input, Level level) {
        return tail.test(input.tail())
                && shaft.test(input.shaft())
                && tip.test(input.tip())
                && additive.map(a -> a.test(input.additive())).orElse(input.additive().isEmpty());
    }

    @Override
    public ItemStack assemble(FletchingRecipeInput input) {
        ItemStack stack = result.create();
        if (copyPotion) {
            stack.set(DataComponents.POTION_CONTENTS, input.additive().get(DataComponents.POTION_CONTENTS));
        }
        return stack;
    }

    @Override
    public boolean isSpecial() {
        return true; // 不进原版配方书
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    /** 不可放置:制箭台不接入原版配方书,自动填充交给 JEI */
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    /** 接口强制要求;配方既 isSpecial 又不可放置,这个分类不会被用到 */
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_EQUIPMENT;
    }

    @Override
    public RecipeSerializer<FletchingRecipe> getSerializer() {
        return ModRecipes.FLETCHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<FletchingRecipe> getType() {
        return ModRecipes.FLETCHING_TYPE.get();
    }

    public static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("tail").forGetter(FletchingRecipe::tail),
            Ingredient.CODEC.fieldOf("shaft").forGetter(FletchingRecipe::shaft),
            Ingredient.CODEC.fieldOf("tip").forGetter(FletchingRecipe::tip),
            Ingredient.CODEC.optionalFieldOf("additive").forGetter(FletchingRecipe::additive),
            Codec.BOOL.optionalFieldOf("copy_potion", false).forGetter(FletchingRecipe::copyPotion),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(FletchingRecipe::result)
    ).apply(instance, FletchingRecipe::new));

    /** 原版已不再把配方整体同步给客户端,这个编解码器供 OnDatapackSyncEvent#sendRecipes 使用 */
    public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, FletchingRecipe::tail,
            Ingredient.CONTENTS_STREAM_CODEC, FletchingRecipe::shaft,
            Ingredient.CONTENTS_STREAM_CODEC, FletchingRecipe::tip,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, FletchingRecipe::additive,
            ByteBufCodecs.BOOL, FletchingRecipe::copyPotion,
            ItemStackTemplate.STREAM_CODEC, FletchingRecipe::result,
            FletchingRecipe::new
    );

    public static final RecipeSerializer<FletchingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
