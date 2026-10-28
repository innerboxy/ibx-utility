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
 * @param team 只有加入队伍时用
 */
public record QuestActionPayload(Action action, ResourceKey<QuestDefinition> quest, Optional<UUID> team) implements CustomPacketPayload {
    public static final Type<QuestActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_action"));

    public enum Action {
        ACCEPT,
        JOIN,
        SUBMIT,
        ABANDON;

        static final StreamCodec<RegistryFriendlyByteBuf, Action> STREAM_CODEC =
                ByteBufCodecs.VAR_INT.map(i -> values()[i], Action::ordinal).cast();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestActionPayload> STREAM_CODEC = StreamCodec.composite(
            Action.STREAM_CODEC, QuestActionPayload::action,
            ResourceKey.streamCodec(ModRegistries.QUEST), QuestActionPayload::quest,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), QuestActionPayload::team,
            QuestActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
