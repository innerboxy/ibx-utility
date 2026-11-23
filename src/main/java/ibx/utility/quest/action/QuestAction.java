package ibx.utility.quest.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.registry.QuestTypeRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

/**
 * 任务动作:接任务、出发、提交、放弃时额外做的事。框架不带类型,留给别的模组注册到
 * {@code ibx_utility:quest_action_type}(见 QuestTypeRegistries),任务 JSON 的 {@code actions} 里写上就会执行。
 */
public interface QuestAction {
    Codec<QuestAction> CODEC = Codec.lazyInitialized(() -> QuestTypeRegistries.ACTION_TYPES.byNameCodec()
            .dispatch("type", QuestAction::codec, Function.identity()));

    MapCodec<? extends QuestAction> codec();

    /**
     * 对这个玩家执行;quest 是触发它的任务。返回做没做成,只有出发({@code depart})时看:
     * 返回 false 就是这个人没走成——后面的 depart 动作不再对他跑,他不算出发(悬赏板上又能点「出发」);
     * 接、提交、放弃时的返回值不看
     */
    boolean run(ServerPlayer player, Holder.Reference<QuestDefinition> quest);
}
