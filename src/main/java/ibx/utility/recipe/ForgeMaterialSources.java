package ibx.utility.recipe;

import ibx.utility.IbxUtility;
import ibx.utility.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 锻造台的相邻材料来源:锻造台六个面上、属于方块标签 {@link #TAG} 的方块,通过 NeoForge 标准物品能力
 * ({@link Capabilities.Item#BLOCK})读取与提取。
 * <p>默认标签含汤姆的简单存储的存储连接器 / 存储接口,以及精妙存储、储物抽屉、功能性存储的控制器、AE2 / RS 的接口等——
 * 它们大多把整个存储网络暴露为一个物品能力(AE2 / RS 的接口只暴露接口自身库存),
 * 所以这里不引用这些模组的任何类,没装的模组在标签里的条目只是被跳过。用白名单而不是"所有相邻容器",
 * 是为了不吃掉熔炉燃料、漏斗中转物品之类;整合包可往标签里加别的存储方块。
 * <p>以下方法只能在服务端调用。
 */
public final class ForgeMaterialSources {
    public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(IbxUtility.MODID, "forge_material_sources"));

    private ForgeMaterialSources() {
    }

    /** 锻造台 pos 周围属于 {@link #TAG} 且提供物品能力的方块 */
    public static List<ResourceHandler<ItemResource>> find(Level level, BlockPos pos) {
        List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (!level.isLoaded(neighbor) || !level.getBlockState(neighbor).is(TAG)) continue;
            ResourceHandler<ItemResource> handler =
                    level.getCapability(Capabilities.Item.BLOCK, neighbor, direction.getOpposite());
            if (handler != null) handlers.add(handler);
        }
        return handlers;
    }

    /**
     * 所有锻造配方材料可能接受的物品,用来筛掉与锻造无关的存储内容(汤姆的网络里常有成千上万种物品)。
     * 按物品粗筛,带组件条件的材料由客户端计数时再用 Ingredient 精确判断。
     * <p>Ingredient#items() 被原版标为过时(原版只拿它做展示),这里用它是因为逐个物品对每种材料调用 test 太慢。
     */
    @SuppressWarnings("deprecation")
    public static Predicate<ItemStack> materialFilter(ServerLevel level) {
        Set<Item> items = new HashSet<>();
        for (var holder : level.recipeAccess().recipeMap().byType(ModRecipes.EQUIPMENT_FORGE_TYPE.get())) {
            for (SizedIngredient material : holder.value().materials()) {
                material.ingredient().items().map(Holder::value).forEach(items::add);
            }
        }
        return stack -> items.contains(stack.getItem());
    }

    /** 各来源中与 filter 匹配的物品,同物品同组件合并为一项(数量裁剪到 int 上限) */
    public static List<ItemStack> snapshot(List<ResourceHandler<ItemResource>> handlers, Predicate<ItemStack> filter) {
        List<ItemStack> merged = new ArrayList<>();
        for (ResourceHandler<ItemResource> handler : handlers) {
            for (int i = 0; i < handler.size(); i++) {
                ItemResource resource = handler.getResource(i);
                if (resource.isEmpty() || !resource.test(filter)) continue;
                long amount = handler.getAmountAsLong(i);
                if (amount > 0) add(merged, resource, amount);
            }
        }
        return merged;
    }

    private static void add(List<ItemStack> merged, ItemResource resource, long amount) {
        for (ItemStack stack : merged) {
            if (resource.matches(stack)) {
                stack.setCount((int) Math.min(stack.getCount() + amount, Integer.MAX_VALUE));
                return;
            }
        }
        merged.add(resource.toStack((int) Math.min(amount, Integer.MAX_VALUE)));
    }

    /** 在 transaction 中从各来源提取至多 amount 个与材料匹配的物品,返回实际提取数 */
    public static int extractMatching(List<ResourceHandler<ItemResource>> handlers, Ingredient ingredient,
                                      int amount, TransactionContext transaction) {
        int extracted = 0;
        for (ResourceHandler<ItemResource> handler : handlers) {
            for (int i = 0; i < handler.size() && extracted < amount; i++) {
                ItemResource resource = handler.getResource(i);
                if (resource.isEmpty() || !resource.test(ingredient)) continue;
                extracted += handler.extract(i, resource, amount - extracted, transaction);
            }
            if (extracted >= amount) break;
        }
        return extracted;
    }
}
