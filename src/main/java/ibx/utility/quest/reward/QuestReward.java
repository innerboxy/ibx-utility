package ibx.utility.quest.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import ibx.utility.registry.QuestTypeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * 任务奖励。JSON 里按 {@code "type"} 分类型,类型注册在 {@code ibx_utility:quest_reward_type}
 * (见 QuestTypeRegistries),别的模组可以注册自己的奖励类型。组队任务每个在线队员各领一份。
 */
public interface QuestReward {
    Codec<QuestReward> CODEC = Codec.lazyInitialized(() -> QuestTypeRegistries.REWARD_TYPES.byNameCodec()
            .dispatch("type", QuestReward::codec, Function.identity()));

    MapCodec<? extends QuestReward> codec();

    /** 发给这个玩家;给物品的放不下就掉在脚下 */
    void grant(ServerPlayer player);

    /** 显示用:悬赏板奖励栏里的图标;空就不显示(比如指令) */
    default ItemStack displayIcon() {
        return ItemStack.EMPTY;
    }

    /** 显示用:图标右下角的数字;null 不画 */
    default @Nullable String displayCount() {
        return null;
    }

    /** 显示用:鼠标指上去的说明 */
    default Component displayName() {
        return displayIcon().getHoverName();
    }
}
