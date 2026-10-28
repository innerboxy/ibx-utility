package ibx.utility.quest.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.registry.ModAttachments;
import ibx.utility.registry.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家的任务数据(附加数据,见 ModAttachments):已接下的任务、每个任务完成过几次。
 * 存盘、死后保留、只同步给自己(追踪栏和悬赏板界面读它)。读随便读,改动请走 QuestService。
 */
public final class PlayerQuests {
    private static final Codec<ResourceKey<QuestDefinition>> KEY = ResourceKey.codec(ModRegistries.QUEST);

    /** 存盘格式:{@code {active: {任务id: {progress, accepted_at, team}}, completed: {任务id: 次数}}}(26.1 起附加数据只接受 MapCodec) */
    public static final MapCodec<PlayerQuests> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(KEY, ActiveQuest.CODEC).optionalFieldOf("active", Map.of()).forGetter(quests -> quests.active),
            Codec.unboundedMap(KEY, ExtraCodecs.POSITIVE_INT).optionalFieldOf("completed", Map.of()).forGetter(quests -> quests.completed)
    ).apply(instance, PlayerQuests::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerQuests> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Map<ResourceKey<QuestDefinition>, ActiveQuest> active;
    private final Map<ResourceKey<QuestDefinition>, Integer> completed;

    public PlayerQuests() {
        this(Map.of(), Map.of());
    }

    private PlayerQuests(Map<ResourceKey<QuestDefinition>, ActiveQuest> active, Map<ResourceKey<QuestDefinition>, Integer> completed) {
        this.active = new HashMap<>(active);
        this.completed = new HashMap<>(completed);
    }

    public static PlayerQuests get(Player player) {
        return player.getData(ModAttachments.PLAYER_QUESTS);
    }

    /** 改完以后同步给玩家自己 */
    public static void sync(ServerPlayer player) {
        player.syncData(ModAttachments.PLAYER_QUESTS);
    }

    public @Nullable ActiveQuest active(ResourceKey<QuestDefinition> quest) {
        return active.get(quest);
    }

    public boolean isActive(ResourceKey<QuestDefinition> quest) {
        return active.containsKey(quest);
    }

    public int activeCount() {
        return active.size();
    }

    /** 已接的任务,按接下的先后排 */
    public List<Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest>> activeInOrder() {
        return active.entrySet().stream()
                .sorted(Comparator.<Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest>>comparingLong(e -> e.getValue().acceptedAt())
                        .thenComparing(e -> e.getKey().identifier()))
                .toList();
    }

    public int completions(ResourceKey<QuestDefinition> quest) {
        return completed.getOrDefault(quest, 0);
    }

    public ActiveQuest start(ResourceKey<QuestDefinition> quest, int objectives, long gameTime, @Nullable UUID team) {
        ActiveQuest started = new ActiveQuest(objectives, gameTime, team);
        active.put(quest, started);
        return started;
    }

    public @Nullable ActiveQuest remove(ResourceKey<QuestDefinition> quest) {
        return active.remove(quest);
    }

    public void addCompletion(ResourceKey<QuestDefinition> quest) {
        completed.merge(quest, 1, Integer::sum);
    }
}
