package ibx.utility.network;

import ibx.utility.IbxUtility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 -> 服务端:关掉了悬赏板界面,之后队伍有变化不用再给他发。 */
public record QuestBoardClosedPayload() implements CustomPacketPayload {
    public static final QuestBoardClosedPayload INSTANCE = new QuestBoardClosedPayload();
    public static final Type<QuestBoardClosedPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_board_closed"));
    public static final StreamCodec<ByteBuf, QuestBoardClosedPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
