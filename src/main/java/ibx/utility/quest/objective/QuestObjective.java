package ibx.utility.quest.objective;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import ibx.utility.registry.QuestTypeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

/**
 * 任务目标。JSON 里按 {@code "type"} 分类型,类型注册在 {@code ibx_utility:quest_objective_type}
 * (见 QuestTypeRegistries),别的模组可以注册自己的目标类型。
 * 进度是一个整数,由框架存在玩家身上;下面三个钩子决定这个类型怎么涨进度、怎么算达成。
 */
public interface QuestObjective {
    Codec<QuestObjective> CODEC = Codec.lazyInitialized(() -> QuestTypeRegistries.OBJECTIVE_TYPES.byNameCodec()
            .dispatch("type", QuestObjective::codec, Function.identity()));

    /** 这个目标类型的编解码器(即注册表里的那一项) */
    MapCodec<? extends QuestObjective> codec();

    /** 达成需要的数量 */
    int count();

    /** 生物算这个玩家杀的时候调用,返回这个目标涨多少进度;不相关返回 0 */
    default int onKill(ServerPlayer player, LivingEntity victim) {
        return 0;
    }

    /** 在悬赏板上提交时调用:还差 remaining,从玩家身上收走能收的,返回收了多少 */
    default int onSubmit(ServerPlayer player, int remaining) {
        return 0;
    }

    /** 显示用:目标对象的图标(生物用刷怪蛋,物品用第一个物品);空就不画 */
    default ItemStack displayIcon() {
        return ItemStack.EMPTY;
    }

    /** 显示用:目标对象的全部图标(标签、列表里的每一个),悬赏板和追踪栏每秒轮换一个;默认只有 displayIcon 那一个 */
    default List<ItemStack> displayIcons() {
        ItemStack icon = displayIcon();
        return icon.isEmpty() ? List.of() : List.of(icon);
    }

    /** 显示用:目标对象的名字(「狼」「骨头」「原木」),追踪栏用 */
    default Component displayTarget() {
        return Component.empty();
    }

    /** 显示用:完整的一句(「击杀 狼」),悬赏板详情用 */
    default Component displayLabel() {
        return displayTarget();
    }

    /** 显示用:当前进度;默认就是存下来的进度,收集物品改成数背包(客户端也能算) */
    default int displayProgress(Player player, int progress) {
        return progress;
    }

    /** 这个目标算不算达成;默认看进度够不够数 */
    default boolean isComplete(ServerPlayer player, int progress) {
        return progress >= count();
    }
}
