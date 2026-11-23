package ibx.utility.network;

import ibx.utility.IbxUtility;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.registry.ModRegistries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.UUID;

/**
 * 客户端 -> 服务端:在悬赏板界面里点的操作。服务端先确认人还在悬赏板旁边,再走 QuestService。
 *
 * @param target 加入队伍时是队伍 id,移出队伍时是被移出的人;别的操作不用
 */
public record QuestActionPayload(Action action, ResourceKey<QuestDefinition> quest, Optional<UUID> target) implements CustomPacketPayload {
    public static final Type<QuestActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_action"));

    public enum Action {
        ACCEPT,
        JOIN,
        SUBMIT,
        ABANDON,
        /** 准备 / 取消准备(要出发的组队任务,发起人以外的队员) */
        READY,
        /** 出发 */
        DEPART,
        /** 发起人把一个队员移出队伍 */
        KICK;

        static final StreamCodec<RegistryFriendlyByteBuf, Action> STREAM_CODEC =
                ByteBufCodecs.VAR_INT.map(i -> values()[i], Action::ordinal).cast();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestActionPayload> STREAM_CODEC = StreamCodec.composite(
            Action.STREAM_CODEC, QuestActionPayload::action,
            ResourceKey.streamCodec(ModRegistries.QUEST), QuestActionPayload::quest,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), QuestActionPayload::target,
            QuestActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
