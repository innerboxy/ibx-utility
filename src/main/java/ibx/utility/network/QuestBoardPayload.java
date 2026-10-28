package ibx.utility.network;

import ibx.utility.IbxUtility;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.registry.ModRegistries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 服务端 -> 客户端:悬赏板上给这个玩家看的内容。右键悬赏板时发一次(open = true,客户端打开界面),
 * 在界面里每做一个操作再发一次(open = false,刷新界面)。
 * 任务定义本身随数据包注册表同步,自己的进度随附加数据同步;这里只发服务端才算得出的东西。
 *
 * @param quests  这个玩家能看到的任务(挂着的、前置满足的、已接的)
 * @param teams   这些任务里跟他有关的队伍:他自己的队伍,和他能加入的队伍
 * @param message 刚才那个操作的结果,显示在界面里
 */
public record QuestBoardPayload(boolean open, List<ResourceKey<QuestDefinition>> quests, List<TeamInfo> teams,
                                Optional<Component> message) implements CustomPacketPayload {
    public static final Type<QuestBoardPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_board"));

    /**
     * @param members 成员名字,按加入先后
     * @param mine    是不是这个玩家自己的队伍(否则是他能加入的)
     */
    public record TeamInfo(UUID id, ResourceKey<QuestDefinition> quest, String leader, List<String> members, int max, boolean mine) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TeamInfo> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, TeamInfo::id,
                ResourceKey.streamCodec(ModRegistries.QUEST), TeamInfo::quest,
                ByteBufCodecs.STRING_UTF8, TeamInfo::leader,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), TeamInfo::members,
                ByteBufCodecs.VAR_INT, TeamInfo::max,
                ByteBufCodecs.BOOL, TeamInfo::mine,
                TeamInfo::new);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestBoardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, QuestBoardPayload::open,
            ResourceKey.streamCodec(ModRegistries.QUEST).apply(ByteBufCodecs.list()), QuestBoardPayload::quests,
            TeamInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestBoardPayload::teams,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional), QuestBoardPayload::message,
            QuestBoardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
