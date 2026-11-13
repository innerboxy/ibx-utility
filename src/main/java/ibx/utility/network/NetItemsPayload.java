package ibx.utility.network;

import ibx.utility.IbxUtility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** 服务端 -> 客户端:背包之外可用材料的快照(锻造台相邻存储 + 玩家超越维度网络),供锻造界面的可制作检测与材料计数使用 */
public record NetItemsPayload(List<ItemStack> items) implements CustomPacketPayload {
    public static final Type<NetItemsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "net_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NetItemsPayload> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(NetItemsPayload::new, NetItemsPayload::items);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
