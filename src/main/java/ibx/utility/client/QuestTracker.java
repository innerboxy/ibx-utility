package ibx.utility.client;

import ibx.utility.IbxUtility;
import ibx.utility.config.ClientConfig;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.registry.ModRegistries;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 左上角的任务追踪栏:列出全部已接任务和每个目标的进度,不画底板。
 * 样式在客户端配置里选(文字 / 图标 / 图标带任务 / 不显示);这是任务系统唯一的提示,进度一变这里就跟着变。
 */
public final class QuestTracker {
    private static final Identifier LAYER = Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_tracker");

    private static final int MARGIN = 4;
    private static final int QUEST_NAME = 0xFFFFD866;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int DONE = 0xFF7BD66B;
    private static final int DONE_LINE = 0xFFB0B0B0;

    private QuestTracker() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CHAT, LAYER, QuestTracker::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientConfig.TrackerMode mode = ClientConfig.TRACKER_MODE.get();
        Player player = minecraft.player;
        if (mode == ClientConfig.TrackerMode.HIDDEN || player == null || minecraft.level == null
                || minecraft.options.hideGui || minecraft.gui.getDebugOverlay().showDebugScreen()) {
            return;
        }
        List<Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest>> active = PlayerQuests.get(player).activeInOrder();
        if (active.isEmpty()) {
            return;
        }
        HolderLookup.RegistryLookup<QuestDefinition> quests = minecraft.level.registryAccess().lookupOrThrow(ModRegistries.QUEST);
        Font font = minecraft.font;
        int y = MARGIN;
        for (Map.Entry<ResourceKey<QuestDefinition>, ActiveQuest> entry : active) {
            Optional<QuestDefinition> quest = quests.get(entry.getKey()).map(Holder::value);
            if (quest.isEmpty()) {
                continue;
            }
            y = switch (mode) {
                case TEXT -> drawText(graphics, font, player, quest.get(), entry.getValue(), y);
                case ICONS -> drawIcons(graphics, font, player, quest.get(), entry.getValue(), y, false);
                default -> drawIcons(graphics, font, player, quest.get(), entry.getValue(), y, true);
            };
        }
    }

    /** 文字:任务名一行,每个目标一行「  狼 3/5」;目标全够了任务名变绿,够了的目标变灰 */
    private static int drawText(GuiGraphicsExtractor graphics, Font font, Player player, QuestDefinition quest, ActiveQuest active, int y) {
        List<QuestObjective> objectives = quest.objectives();
        int[] shown = progress(player, objectives, active);
        graphics.text(font, quest.name(), MARGIN, y, allDone(objectives, shown) ? DONE : QUEST_NAME, true);
        y += 10;
        for (int i = 0; i < objectives.size(); i++) {
            QuestObjective objective = objectives.get(i);
            boolean done = shown[i] >= objective.count();
            String line = "  " + objective.displayTarget().getString() + " " + shown[i] + "/" + objective.count();
            graphics.text(font, line, MARGIN, y, done ? DONE_LINE : TEXT, true);
            y += 10;
        }
        return y + 3;
    }

    /** 图标:一个任务一行,目标图标 + 数字(够了变绿);目标全够了行尾打勾。withQuest 时行首先画任务图标和冒号 */
    private static int drawIcons(GuiGraphicsExtractor graphics, Font font, Player player, QuestDefinition quest, ActiveQuest active, int y,
                                 boolean withQuest) {
        List<QuestObjective> objectives = quest.objectives();
        int[] shown = progress(player, objectives, active);
        int x = MARGIN;
        if (withQuest) {
            graphics.item(quest.iconStack(), x, y);
            x += 17;
            graphics.text(font, ":", x, y + 4, TEXT, true);
            x += font.width(":") + 3;
        }
        for (int i = 0; i < objectives.size(); i++) {
            QuestObjective objective = objectives.get(i);
            ItemStack icon = objective.displayIcon();
            if (!icon.isEmpty()) {
                graphics.item(icon, x, y);
                x += 17;
            } else {
                String target = objective.displayTarget().getString();
                graphics.text(font, target, x, y + 4, TEXT, true);
                x += font.width(target) + 2;
            }
            String count = shown[i] + "/" + objective.count();
            graphics.text(font, count, x, y + 4, shown[i] >= objective.count() ? DONE : TEXT, true);
            x += font.width(count) + 6;
        }
        if (allDone(objectives, shown)) {
            graphics.text(font, "✔", x, y + 4, DONE, true);
        }
        return y + 18;
    }

    /** 每个目标显示的进度,不超过所需数量(收集物品按背包里现有的算) */
    private static int[] progress(Player player, List<QuestObjective> objectives, ActiveQuest active) {
        int[] shown = new int[objectives.size()];
        for (int i = 0; i < shown.length; i++) {
            QuestObjective objective = objectives.get(i);
            shown[i] = Math.min(objective.count(), objective.displayProgress(player, active.progress(i)));
        }
        return shown;
    }

    private static boolean allDone(List<QuestObjective> objectives, int[] shown) {
        for (int i = 0; i < shown.length; i++) {
            if (shown[i] < objectives.get(i).count()) {
                return false;
            }
        }
        return true;
    }
}
