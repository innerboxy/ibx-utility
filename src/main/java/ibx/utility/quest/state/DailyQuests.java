package ibx.utility.quest.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.IbxUtility;
import ibx.utility.config.Config;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.QuestPool;
import ibx.utility.quest.QuestService;
import ibx.utility.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * 每日随机任务(世界存档,全服一份):今天是第几天、抽中了哪些随机池任务。
 * 一天 = 主世界游戏总时长 24000 tick(睡觉、/time set 不会提前刷新)。已经接下的随机任务不受刷新影响。
 */
public final class DailyQuests extends SavedData {
    public static final long DAY_TICKS = 24000L;

    private static final Codec<DailyQuests> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("day").forGetter(daily -> daily.day),
            ResourceKey.codec(ModRegistries.QUEST).listOf().fieldOf("quests").forGetter(daily -> daily.quests)
    ).apply(instance, DailyQuests::new));

    public static final SavedDataType<DailyQuests> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IbxUtility.MODID, "daily_quests"), DailyQuests::new, CODEC);

    /** -1 = 还没抽过 */
    private long day;
    private final List<ResourceKey<QuestDefinition>> quests;

    private DailyQuests() {
        this(-1, List.of());
    }

    private DailyQuests(long day, List<ResourceKey<QuestDefinition>> quests) {
        this.day = day;
        this.quests = new ArrayList<>(quests);
    }

    public static DailyQuests get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static long currentDay(MinecraftServer server) {
        return server.overworld().getGameTime() / DAY_TICKS;
    }

    public long day() {
        return day;
    }

    /** 今天抽中的任务(可能有任务定义已经不在的,用的时候自己查) */
    public List<ResourceKey<QuestDefinition>> quests() {
        return List.copyOf(quests);
    }

    public boolean contains(ResourceKey<QuestDefinition> quest) {
        return quests.contains(quest);
    }

    /** 到了新的一天(或者还没抽过)就重抽。返回抽没抽 */
    public boolean refreshIfNewDay(MinecraftServer server) {
        long today = currentDay(server);
        if (today == day) {
            return false;
        }
        draw(server, today);
        return true;
    }

    /** 从随机池按权重抽,不重复;数量看配置,池子不够就全上 */
    public void draw(MinecraftServer server, long today) {
        ServerLevel overworld = server.overworld();
        List<Holder.Reference<QuestDefinition>> pool = new ArrayList<>(QuestService.all(overworld.registryAccess())
                .filter(quest -> quest.value().pool() == QuestPool.RANDOM)
                .toList());
        RandomSource random = overworld.getRandom();
        quests.clear();
        int count = Math.min(Config.DAILY_QUEST_COUNT.getAsInt(), pool.size());
        for (int n = 0; n < count; n++) {
            int total = pool.stream().mapToInt(quest -> quest.value().weight()).sum();
            int roll = random.nextInt(total);
            for (int i = 0; i < pool.size(); i++) {
                roll -= pool.get(i).value().weight();
                if (roll < 0) {
                    quests.add(pool.remove(i).key());
                    break;
                }
            }
        }
        day = today;
        setDirty();
    }
}
