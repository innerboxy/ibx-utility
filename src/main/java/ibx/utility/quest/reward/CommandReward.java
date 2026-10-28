package ibx.utility.quest.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

/**
 * 执行一条指令。照原版进度奖励里的函数来跑:执行者是玩家、位置在玩家处、2 级(游戏管理员)权限、
 * 不往聊天里输出。写数据包的人本来就有这个权限,不会借奖励再提权。
 *
 * <pre>
 * { "type": "ibx_utility:command", "command": "function example:quest/wolf_hunt_done" }
 * </pre>
 */
public record CommandReward(String command) implements QuestReward {
    public static final MapCodec<CommandReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("command").forGetter(CommandReward::command)
    ).apply(instance, CommandReward::new));

    @Override
    public MapCodec<CommandReward> codec() {
        return CODEC;
    }

    @Override
    public void grant(ServerPlayer player) {
        CommandSourceStack source = player.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(LevelBasedPermissionSet.GAMEMASTER);
        player.level().getServer().getCommands().performPrefixedCommand(source, command);
    }
}
