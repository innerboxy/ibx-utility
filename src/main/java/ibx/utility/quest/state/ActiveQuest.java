package ibx.utility.quest.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 一个已接下的任务:每个目标的进度、接下时的游戏总时长(追踪栏按它排先后)、所在队伍。
 * 组队任务的进度以队伍里的为准(见 QuestTeams),这里是抄过来给追踪栏看的。改动请走 QuestService。
 */
public final class ActiveQuest implements QuestProgress {
    public static final Codec<ActiveQuest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().fieldOf("progress").forGetter(quest -> IntStream.of(quest.progress).boxed().toList()),
            Codec.LONG.fieldOf("accepted_at").forGetter(ActiveQuest::acceptedAt),
            UUIDUtil.CODEC.optionalFieldOf("team").forGetter(quest -> Optional.ofNullable(quest.team))
    ).apply(instance, ActiveQuest::new));

    private int[] progress;
    private final long acceptedAt;
    private final @Nullable UUID team;

    public ActiveQuest(int objectives, long acceptedAt, @Nullable UUID team) {
        this.progress = new int[objectives];
        this.acceptedAt = acceptedAt;
        this.team = team;
    }

    private ActiveQuest(List<Integer> progress, long acceptedAt, Optional<UUID> team) {
        this.progress = progress.stream().mapToInt(Integer::intValue).toArray();
        this.acceptedAt = acceptedAt;
        this.team = team.orElse(null);
    }

    /** 第 i 个目标的进度;任务数据改过、目标变多时,多出来的从 0 算 */
    @Override
    public int progress(int i) {
        return i < progress.length ? progress[i] : 0;
    }

    @Override
    public void setProgress(int i, int value) {
        if (i >= progress.length) {
            progress = Arrays.copyOf(progress, i + 1);
        }
        progress[i] = value;
    }

    /** 整份抄过来(组队任务从队伍抄进度) */
    public void copyFrom(QuestProgress source, int objectives) {
        for (int i = 0; i < objectives; i++) {
            setProgress(i, source.progress(i));
        }
    }

    public long acceptedAt() {
        return acceptedAt;
    }

    /** 所在队伍;个人任务是 null */
    public @Nullable UUID team() {
        return team;
    }
}
