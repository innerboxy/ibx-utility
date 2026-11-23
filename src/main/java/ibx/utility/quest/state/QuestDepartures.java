package ibx.utility.quest.state;

import ibx.utility.quest.QuestDefinition;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 要出发的任务(actions 里写了 depart)的准备名单和出门名单。只在内存里、不存档:重启或退出存档就清空;
 * 下线的人由 QuestService 划掉。组队任务按队伍记,个人任务按「这个人的这个任务」记。改动请走 QuestService。
 */
public final class QuestDepartures {
    /**
     * 一份出门名单属于谁
     *
     * @param owner 组队任务是队伍 id,个人任务是玩家自己的 UUID
     */
    public record Group(ResourceKey<QuestDefinition> quest, UUID owner) {}

    /** 队伍 -> 点了准备的队员,按点的先后 */
    private static final Map<UUID, Set<UUID>> READY = new HashMap<>();
    /** 出发了、还没回来的人,按出发先后;空了就删掉这一项 */
    private static final Map<Group, Set<UUID>> AWAY = new HashMap<>();

    private QuestDepartures() {}

    public static boolean isReady(UUID team, UUID player) {
        Set<UUID> ready = READY.get(team);
        return ready != null && ready.contains(player);
    }

    /** 点一下准备、再点取消;返回现在是不是准备好了 */
    public static boolean toggleReady(UUID team, UUID player) {
        Set<UUID> ready = READY.computeIfAbsent(team, t -> new LinkedHashSet<>());
        if (ready.remove(player)) {
            if (ready.isEmpty()) {
                READY.remove(team);
            }
            return false;
        }
        ready.add(player);
        return true;
    }

    public static void removeReady(UUID team, UUID player) {
        Set<UUID> ready = READY.get(team);
        if (ready != null && ready.remove(player) && ready.isEmpty()) {
            READY.remove(team);
        }
    }

    public static void clearReady(UUID team) {
        READY.remove(team);
    }

    /** 这一组出发了还没回来的人,按出发先后 */
    public static List<UUID> away(Group group) {
        Set<UUID> away = AWAY.get(group);
        return away == null ? List.of() : List.copyOf(away);
    }

    public static boolean isAway(Group group, UUID player) {
        Set<UUID> away = AWAY.get(group);
        return away != null && away.contains(player);
    }

    public static void addAway(Group group, List<UUID> players) {
        AWAY.computeIfAbsent(group, g -> new LinkedHashSet<>()).addAll(players);
    }

    /** 把这个人从这个任务的出门名单里划掉(不管是哪一组);返回他本来在不在外面 */
    public static boolean removeAway(ResourceKey<QuestDefinition> quest, UUID player) {
        boolean removed = false;
        Iterator<Map.Entry<Group, Set<UUID>>> it = AWAY.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Group, Set<UUID>> entry = it.next();
            if (entry.getKey().quest().equals(quest) && entry.getValue().remove(player)) {
                removed = true;
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            }
        }
        return removed;
    }

    /** 这一组整个不算了(任务完成、队伍散了、个人放弃) */
    public static void clear(Group group) {
        AWAY.remove(group);
        READY.remove(group.owner());
    }

    /** 下线:这个人从所有准备、出门名单里划掉;返回有没有变 */
    public static boolean removePlayer(UUID player) {
        boolean changed = false;
        Iterator<Set<UUID>> ready = READY.values().iterator();
        while (ready.hasNext()) {
            Set<UUID> set = ready.next();
            if (set.remove(player)) {
                changed = true;
                if (set.isEmpty()) {
                    ready.remove();
                }
            }
        }
        Iterator<Set<UUID>> away = AWAY.values().iterator();
        while (away.hasNext()) {
            Set<UUID> set = away.next();
            if (set.remove(player)) {
                changed = true;
                if (set.isEmpty()) {
                    away.remove();
                }
            }
        }
        return changed;
    }

    /** 服务器关掉时清空(单人存档退出再进,不带上一局的名单) */
    public static void clearAll() {
        READY.clear();
        AWAY.clear();
    }
}
