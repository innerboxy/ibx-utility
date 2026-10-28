package ibx.utility.registry;

import ibx.utility.IbxUtility;
import ibx.utility.quest.state.PlayerQuests;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** 附加数据。 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IbxUtility.MODID);

    /** 玩家的任务:已接的、完成过几次。存盘、死后保留、只同步给自己 */
    public static final Supplier<AttachmentType<PlayerQuests>> PLAYER_QUESTS =
            ATTACHMENT_TYPES.register("player_quests",
                    () -> AttachmentType.builder(() -> new PlayerQuests())
                            .serialize(PlayerQuests.CODEC)
                            .sync((holder, player) -> holder == player, PlayerQuests.STREAM_CODEC)
                            .copyOnDeath()
                            .build());
}
