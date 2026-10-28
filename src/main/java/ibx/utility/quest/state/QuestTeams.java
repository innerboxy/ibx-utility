package ibx.utility.quest.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.IbxUtility;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.registry.ModRegistries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 组队任务(世界存档,全服一份):每支队伍的任务、发起人、成员(按加入先后)、共享进度;
 * 还有队伍提交时不在线、上线后要补记的完成次数。改动请走 QuestService。
 */
public final class QuestTeams extends SavedData {
    private static final Codec<ResourceKey<QuestDefinition>> KEY = ResourceKey.codec(ModRegistries.QUEST);

    private static final Codec<QuestTeams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Team.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(teams -> List.copyOf(teams.teams.values())),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, KEY.listOf()).optionalFieldOf("pending_completions", Map.of())
                    .forGetter(teams -> teams.pending)
    ).apply(instance, QuestTeams::new));

    public static final SavedDataType<QuestTeams> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_teams"), QuestTeams::new, CODEC);

    private final Map<UUID, Team> teams = new LinkedHashMap<>();
    private final Map<UUID, List<ResourceKey<QuestDefinition>>> pending = new HashMap<>();

    private QuestTeams() {}

    private QuestTeams(List<Team> teams, Map<UUID, List<ResourceKey<QuestDefinition>>> pending) {
        teams.forEach(team -> {
            team.owner = this;
            this.teams.put(team.id, team);
        });
        pending.forEach((player, quests) -> this.pending.put(player, new ArrayList<>(quests)));
    }

    public static QuestTeams get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public @Nullable Team get(UUID team) {
        return teams.get(team);
    }

    /** 做这个任务的队伍,按建队先后 */
    public List<Team> teamsFor(ResourceKey<QuestDefinition> quest) {
        return teams.values().stream().filter(team -> team.quest.equals(quest)).toList();
    }

    public List<Team> all() {
        return List.copyOf(teams.values());
    }

    public Team create(ResourceKey<QuestDefinition> quest, UUID leader, int objectives) {
        Team team = new Team(UUID.randomUUID(), quest, leader, List.of(leader), new int[objectives]);
        team.owner = this;
        teams.put(team.id, team);
        setDirty();
        return team;
    }

    public void remove(UUID team) {
        if (teams.remove(team) != null) {
            setDirty();
        }
    }

    public void addPending(UUID player, ResourceKey<QuestDefinition> quest) {
        pending.computeIfAbsent(player, p -> new ArrayList<>()).add(quest);
        setDirty();
    }

    /** 取走这个玩家待补记的完成(取完就清) */
    public List<ResourceKey<QuestDefinition>> takePending(UUID player) {
        List<ResourceKey<QuestDefinition>> quests = pending.remove(player);
        if (quests == null) {
            return List.of();
        }
        setDirty();
        return quests;
    }

    /** 一支队伍。成员按加入先后排,第一个不一定是发起人(发起人离开后换人) */
    public static final class Team implements QuestProgress {
        private static final Codec<Team> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(team -> team.id),
                KEY.fieldOf("quest").forGetter(team -> team.quest),
                UUIDUtil.CODEC.fieldOf("leader").forGetter(team -> team.leader),
                UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(team -> team.members),
                Codec.INT.listOf().fieldOf("progress").forGetter(team -> IntStream.of(team.progress).boxed().toList())
        ).apply(instance, (id, quest, leader, members, progress) ->
                new Team(id, quest, leader, members, progress.stream().mapToInt(Integer::intValue).toArray())));

        private final UUID id;
        private final ResourceKey<QuestDefinition> quest;
        private UUID leader;
        private final List<UUID> members;
        private int[] progress;
        private @Nullable QuestTeams owner;

        private Team(UUID id, ResourceKey<QuestDefinition> quest, UUID leader, List<UUID> members, int[] progress) {
            this.id = id;
            this.quest = quest;
            this.leader = leader;
            this.members = new ArrayList<>(members);
            this.progress = progress;
        }

        public UUID id() {
            return id;
        }

        public ResourceKey<QuestDefinition> quest() {
            return quest;
        }

        public UUID leader() {
            return leader;
        }

        public List<UUID> members() {
            return List.copyOf(members);
        }

        public boolean hasMember(UUID player) {
            return members.contains(player);
        }

        public void addMember(UUID player) {
            members.add(player);
            markDirty();
        }

        /** 退队;发起人走了换最早加入的那个。返回队伍空没空 */
        public boolean removeMember(UUID player) {
            members.remove(player);
            if (player.equals(leader) && !members.isEmpty()) {
                leader = members.getFirst();
            }
            markDirty();
            return members.isEmpty();
        }

        public void setLeader(UUID player) {
            leader = player;
            markDirty();
        }

        private void markDirty() {
            if (owner != null) {
                owner.setDirty();
            }
        }

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
            markDirty();
        }
    }
}
