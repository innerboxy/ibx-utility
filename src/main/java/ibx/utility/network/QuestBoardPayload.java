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

import java.util.List;
import java.util.UUID;

/**
 * 服务端 -> 客户端:悬赏板上给这个玩家看的内容。右键悬赏板时发一次(OPEN,客户端打开界面);
 * 之后队伍、准备、出门名单、已接任务有变化,给开着悬赏板的人再发(REFRESH,刷新界面);
 * 他出发了就发一个不带内容的 CLOSE,客户端把界面关掉。
 * 任务定义本身随数据包注册表同步,自己的进度随附加数据同步;这里只发服务端才算得出的东西。
 *
 * @param quests 这个玩家能看到的任务(挂着的、前置满足的、已接的)
 * @param teams  这些任务里跟他有关的队伍:他自己的队伍,和他能加入的队伍
 * @param away   他自己在外面的个人任务(出发了还没回来;组队任务的在 TeamInfo 里)
 */
public record QuestBoardPayload(Mode mode, List<ResourceKey<QuestDefinition>> quests, List<TeamInfo> teams,
                                List<ResourceKey<QuestDefinition>> away) implements CustomPacketPayload {
    public static final Type<QuestBoardPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_board"));

    /** 客户端收到后做什么 */
    public enum Mode {
        /** 打开界面 */
        OPEN,
        /** 界面开着就刷新 */
        REFRESH,
        /** 界面开着就关掉(不带内容) */
        CLOSE;

        static final StreamCodec<RegistryFriendlyByteBuf, Mode> STREAM_CODEC =
                ByteBufCodecs.VAR_INT.map(i -> values()[i], Mode::ordinal).cast();
    }

    /** 叫客户端关掉悬赏板界面 */
    public static QuestBoardPayload close() {
        return new QuestBoardPayload(Mode.CLOSE, List.of(), List.of(), List.of());
    }

    /**
     * 一个队员
     *
     * @param leader 是不是发起人
     * @param ready  点了准备(要出发的任务开局前)
     * @param away   出发了还没回来
     */
    public record Member(UUID id, String name, boolean leader, boolean ready, boolean away) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Member> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Member::id,
                ByteBufCodecs.STRING_UTF8, Member::name,
                ByteBufCodecs.BOOL, Member::leader,
                ByteBufCodecs.BOOL, Member::ready,
                ByteBufCodecs.BOOL, Member::away,
                Member::new);
    }

    /**
     * @param members 成员,按加入先后
     * @param leading 看的这个人是不是发起人
     * @param mine    是不是这个玩家自己的队伍(否则是他能加入的)
     */
    public record TeamInfo(UUID id, ResourceKey<QuestDefinition> quest, String leader, List<Member> members, int max, boolean leading,
                           boolean mine) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TeamInfo> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, TeamInfo::id,
                ResourceKey.streamCodec(ModRegistries.QUEST), TeamInfo::quest,
                ByteBufCodecs.STRING_UTF8, TeamInfo::leader,
                Member.STREAM_CODEC.apply(ByteBufCodecs.list()), TeamInfo::members,
                ByteBufCodecs.VAR_INT, TeamInfo::max,
                ByteBufCodecs.BOOL, TeamInfo::leading,
                ByteBufCodecs.BOOL, TeamInfo::mine,
                TeamInfo::new);

        /** 有人出发了还没回来 */
        public boolean inProgress() {
            return members.stream().anyMatch(Member::away);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestBoardPayload> STREAM_CODEC = StreamCodec.composite(
            Mode.STREAM_CODEC, QuestBoardPayload::mode,
            ResourceKey.streamCodec(ModRegistries.QUEST).apply(ByteBufCodecs.list()), QuestBoardPayload::quests,
            TeamInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestBoardPayload::teams,
            ResourceKey.streamCodec(ModRegistries.QUEST).apply(ByteBufCodecs.list()), QuestBoardPayload::away,
            QuestBoardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
