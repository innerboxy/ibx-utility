package ibx.utility.client.screen;

import ibx.utility.IbxUtility;
import ibx.utility.client.ClientQuestBoard;
import ibx.utility.network.QuestActionPayload;
import ibx.utility.network.QuestBoardPayload;
import ibx.utility.quest.QuestCategoryDefinition;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.quest.QuestDisplay;
import ibx.utility.quest.QuestPool;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.reward.QuestReward;
import ibx.utility.quest.state.ActiveQuest;
import ibx.utility.quest.state.PlayerQuests;
import ibx.utility.registry.ModRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * 悬赏板:账本式界面。左边丝带书签是数据定义的分页,右边一条是自带的【已承接任务】;
 * 左页是这一页的任务目录,右页是选中任务的详情和按钮。
 * 书是一整张贴图,左页每行底下的账本线和标题下的双线都画在上面,目录的行距要和它对齐;
 * 丝带是一张浅灰贴图(右边用左右镜像的那张),按分页颜色乘色;按钮是蜡封,翻页是金箭头(都在 gui/sprites/quest_board/)。
 * 分页多了左边书签上下翻页(同锻造台的页签);右页内容一页放不下也分页,页码在蜡封按钮上面。
 */
public class QuestBoardScreen extends Screen {
    private static final int BOOK_W = 300;
    private static final int BOOK_H = 192;
    private static final int PAGE_W = (BOOK_W - 12) / 2;
    private static final int PAGE_H = BOOK_H - 12;
    private static final int ROWS = 6;
    private static final int ROW_H = 24;
    private static final int TAB_SIZE = 20;
    private static final int TAB_GAP = 2;
    /** 左边每页最多几条书签;再多就翻页:上面一个翻页按钮,中间书签,下面页码和另一个按钮 */
    private static final int TAB_PAGE_SIZE = 6;
    /** 没选中的书签露在书外的宽度,翻页按钮在这一列里居中 */
    private static final int TAB_COLUMN = 19;
    private static final int TAB_BUTTON_W = 14;
    private static final int TAB_BUTTON_H = 10;
    /** 书签页码那一行:上下各空 4,数字 7 高 */
    private static final int TAB_LABEL_H = 4 + 7 + 4;
    /** 翻页时左边整列(上翻按钮、一页书签、页码、下翻按钮,连按钮投影)的高度,这一列在书的高度里上下居中 */
    private static final int TAB_COLUMN_H = TAB_BUTTON_H + TAB_GAP + TAB_PAGE_SIZE * (TAB_SIZE + TAB_GAP) - TAB_GAP
            + TAB_LABEL_H + TAB_BUTTON_H + 1;
    private static final int TAB_BUTTON_Y = (BOOK_H - TAB_COLUMN_H) / 2;
    /** 第一条书签离书顶的距离,左右两边一样;上面空出来的地方翻页时放上翻按钮 */
    private static final int TAB_TOP = TAB_BUTTON_Y + TAB_BUTTON_H + TAB_GAP;
    /** 右页分页时,页码那一行(金箭头 + 「1/2」)占的高度 */
    private static final int PAGER_H = 12;
    private static final int RIBBON_W = 28;
    private static final int SEAL = 14;
    private static final int ARROW_W = 13;
    private static final int ARROW_H = 9;
    private static final int TEAM_ROW_H = 16;

    private static final int INK = 0xFF3A2A1C;
    private static final int INK_LIGHT = 0xFF6E5A3E;
    private static final int RED = 0xFF9C2A22;
    private static final int GREEN = 0xFF3F7F34;
    private static final int GOLD = 0xFF9B6E1A;
    private static final int FADED = 0xFFA8987A;
    /** 右页任务名下面的双横线,同书页贴图里左页标题下那两道 */
    private static final int RULE = 0xFFB9A57E;
    private static final int RULE_LIGHT = 0xFFD4BF94;
    /** 小标题压着的小丝带签:上沿亮一道、中间正红、下沿暗一道,字用纸色 */
    private static final int TAG = 0xFF9C2A22;
    private static final int TAG_LIGHT = 0xFFC04A3E;
    private static final int TAG_DARK = 0xFF6E1A14;
    private static final int TAG_TEXT = 0xFFF4E8CC;
    /** 书外面(暗下来的世界上)的字:书签页码 */
    private static final int PARCHMENT = 0xFFF2E6C8;
    /** 丝带颜色:贴图是浅灰的,乘上这些颜色正好是红 / 蓝 / 绿 / 紫 / 青 / 橙 */
    private static final int[] RIBBONS = {0xFFA92E25, 0xFF4467A9, 0xFF448A38, 0xFF844498, 0xFF338A8A, 0xFFA97325};
    private static final int RIBBON_ACCEPTED = 0xFFC78C44;

    private static final Identifier BOOK = Identifier.fromNamespaceAndPath(IbxUtility.MODID, "textures/gui/quest_board.png");
    private static final Identifier RIBBON = sprite("ribbon");
    private static final Identifier RIBBON_RIGHT = sprite("ribbon_right");
    private static final Identifier POINTER = sprite("pointer");
    /** 【已承接任务】书签上的图标:盖了红蜡封的契约 */
    private static final Identifier ACCEPTED_ICON = sprite("accepted");
    private static final Identifier PAGE_FORWARD = sprite("page_forward");
    private static final Identifier PAGE_FORWARD_HIGHLIGHTED = sprite("page_forward_highlighted");
    private static final Identifier PAGE_BACKWARD = sprite("page_backward");
    private static final Identifier PAGE_BACKWARD_HIGHLIGHTED = sprite("page_backward_highlighted");
    private static final Identifier TAB_UP = sprite("tab_up");
    private static final Identifier TAB_UP_HIGHLIGHTED = sprite("tab_up_highlighted");
    private static final Identifier TAB_DOWN = sprite("tab_down");
    private static final Identifier TAB_DOWN_HIGHLIGHTED = sprite("tab_down_highlighted");

    private static final Identifier ACCEPTED_TAB = Identifier.fromNamespaceAndPath(IbxUtility.MODID, "accepted");

    /** 蜡封按钮的颜色:接受红、提交绿、放弃棕、加入蓝 */
    private enum Wax {
        RED("red"), GREEN("green"), BROWN("brown"), BLUE("blue");

        final Identifier sprite;
        final Identifier highlighted;

        Wax(String name) {
            sprite = sprite("seal_" + name);
            highlighted = sprite("seal_" + name + "_highlighted");
        }
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(IbxUtility.MODID, "quest_board/" + name);
    }

    /** 一条书签;icon 是物品图标,sprite 不为空时画这张贴图(自带的【已承接任务】) */
    private record Tab(Identifier id, Component name, ItemStack icon, @Nullable Identifier sprite, int color) {}

    /**
     * 右页的一段内容:高度、是不是小标题、属于哪个小标题底下(小标题自己没有)、怎么画。
     * 小标题不留在页底,跟着下一段走;一段内容接着上一页时,新的一页开头把它的小标题再写一遍
     */
    private record Block(int height, boolean heading, @Nullable Block section, BlockDrawer drawer) {}

    @FunctionalInterface
    private interface BlockDrawer {
        void draw(GuiGraphicsExtractor graphics, int y, int mouseX, int mouseY);
    }

    /** 这一帧画出来的可点区域;点击时按它找 */
    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private List<Tab> tabs = List.of();
    private Identifier selectedTab;
    private @Nullable ResourceKey<QuestDefinition> selectedQuest;
    private int listPage;
    /** 左边书签翻到第几页(分页多到要翻页时才用) */
    private int tabPage;
    /** 右页详情翻到第几页;换了任务回到第一页 */
    private int detailPage;
    private final List<Hit> hits = new ArrayList<>();

    public QuestBoardScreen() {
        super(Component.translatable("block.ibx_utility.quest_board"));
    }

    @Override
    protected void init() {
        refresh();
    }

    /** 服务端发来新的内容时调用:重排书签,选中的书签 / 任务还在就保留 */
    public void refresh() {
        tabs = buildTabs();
        if (selectedTab == null || tabs.stream().noneMatch(tab -> tab.id.equals(selectedTab))) {
            selectedTab = tabs.getFirst().id;
            listPage = 0;
            tabPage = 0;
        }
        tabPage = Math.min(tabPage, tabPageCount() - 1);
        List<ResourceKey<QuestDefinition>> shown = questsIn(selectedTab);
        if (selectedQuest == null || !shown.contains(selectedQuest)) {
            select(shown.isEmpty() ? null : shown.getFirst());
        }
        listPage = Math.min(listPage, Math.max(0, (shown.size() - 1) / ROWS));
    }

    /** 选中一个任务;换了任务右页回到第一页 */
    private void select(@Nullable ResourceKey<QuestDefinition> quest) {
        if (!Objects.equals(quest, selectedQuest)) {
            detailPage = 0;
        }
        selectedQuest = quest;
    }

    /** 左边的书签:数据定义的分页(【已承接任务】在右边,不算) */
    private List<Tab> leftTabs() {
        return tabs.stream().filter(tab -> !tab.id.equals(ACCEPTED_TAB)).toList();
    }

    private int tabPageCount() {
        int n = leftTabs().size();
        return Math.max(1, (n + TAB_PAGE_SIZE - 1) / TAB_PAGE_SIZE);
    }

    /** 书签翻页,dir 为 -1 上翻、+1 下翻;已经到头翻不动时返回 false */
    private boolean turnTabPage(int dir) {
        int page = Math.clamp(tabPage + dir, 0, tabPageCount() - 1);
        if (page == tabPage) {
            return false;
        }
        tabPage = page;
        return true;
    }

    private int bookX() {
        return (width - BOOK_W) / 2;
    }

    private int bookY() {
        return (height - BOOK_H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- 数据

    private HolderLookup.RegistryLookup<QuestDefinition> quests() {
        return minecraft.level.registryAccess().lookupOrThrow(ModRegistries.QUEST);
    }

    private Optional<QuestDefinition> definition(ResourceKey<QuestDefinition> key) {
        return quests().get(key).map(Holder::value);
    }

    private PlayerQuests playerQuests() {
        return PlayerQuests.get(minecraft.player);
    }

    private List<Tab> buildTabs() {
        List<Tab> result = new ArrayList<>();
        List<Holder.Reference<QuestCategoryDefinition>> categories = minecraft.level.registryAccess()
                .lookupOrThrow(ModRegistries.QUEST_CATEGORY).listElements()
                .sorted(Comparator.<Holder.Reference<QuestCategoryDefinition>>comparingInt(c -> c.value().order())
                        .thenComparing(c -> c.key().identifier()))
                .toList();
        for (int i = 0; i < categories.size(); i++) {
            QuestCategoryDefinition category = categories.get(i).value();
            result.add(new Tab(categories.get(i).key().identifier(), category.name(), category.iconStack(), null, RIBBONS[i % RIBBONS.length]));
        }
        result.add(new Tab(ACCEPTED_TAB, Component.translatable("quest.ibx_utility.board.accepted"), ItemStack.EMPTY, ACCEPTED_ICON, RIBBON_ACCEPTED));
        return result;
    }

    /** 这一页的任务:【已承接任务】按接下先后;其余是服务端发来的、写在这一页的(已按 id 排好) */
    private List<ResourceKey<QuestDefinition>> questsIn(Identifier tab) {
        if (tab.equals(ACCEPTED_TAB)) {
            return playerQuests().activeInOrder().stream().map(entry -> entry.getKey())
                    .filter(key -> definition(key).isPresent()).toList();
        }
        return ClientQuestBoard.current().quests().stream()
                .filter(key -> definition(key).filter(quest -> quest.category().equals(tab)).isPresent())
                .toList();
    }

    private boolean achieved(QuestDefinition quest, ActiveQuest active) {
        List<QuestObjective> objectives = quest.objectives();
        for (int i = 0; i < objectives.size(); i++) {
            if (objectives.get(i).displayProgress(minecraft.player, active.progress(i)) < objectives.get(i).count()) {
                return false;
            }
        }
        return true;
    }

    /** 做过的一次性组队任务:不能再接,只能加入别人的队伍 */
    private boolean joinOnly(ResourceKey<QuestDefinition> key, QuestDefinition quest) {
        return !quest.repeatable() && playerQuests().completions(key) > 0;
    }

    private Optional<QuestBoardPayload.TeamInfo> ownTeam(ResourceKey<QuestDefinition> key) {
        return ClientQuestBoard.current().teams().stream().filter(team -> team.mine() && team.quest().equals(key)).findFirst();
    }

    // ---------------------------------------------------------------- 画

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        hits.clear();
        int x = bookX();
        int y = bookY();
        drawTabs(graphics, x, y, mouseX, mouseY);
        drawBook(graphics, x, y);
        int px = x + 6;
        int py = y + 6;
        drawList(graphics, px, py, mouseX, mouseY);
        if (selectedQuest != null) {
            definition(selectedQuest).ifPresent(quest -> drawDetail(graphics, quest, px + PAGE_W + 10, py + 6, PAGE_W - 18, mouseX, mouseY));
        }
    }

    /**
     * 分页的丝带从书的左边一条条往下排;【已承接任务】单独一条在右边,和左边第一条对齐。
     * 左边多于 TAB_PAGE_SIZE 条时翻页(同锻造台的页签):上下各一个翻页按钮,常驻,到头了点了只响声音;下面的按钮上方写页码
     */
    private void drawTabs(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        List<Tab> left = leftTabs();
        int pages = tabPageCount();
        boolean paged = pages > 1;
        int first = tabPage * TAB_PAGE_SIZE;
        int count = Math.min(TAB_PAGE_SIZE, left.size() - first);
        int ty0 = y + TAB_TOP;
        for (int i = 0; i < count; i++) {
            drawTab(graphics, left.get(first + i), false, x, ty0 + i * (TAB_SIZE + TAB_GAP), mouseX, mouseY);
        }
        tabs.stream().filter(tab -> tab.id.equals(ACCEPTED_TAB)).findFirst()
                .ifPresent(tab -> drawTab(graphics, tab, true, x + BOOK_W, ty0, mouseX, mouseY));
        if (!paged) {
            return;
        }
        int bx = x - TAB_COLUMN + (TAB_COLUMN - TAB_BUTTON_W) / 2;
        int labelY = ty0 + TAB_PAGE_SIZE * (TAB_SIZE + TAB_GAP) - TAB_GAP + 4;
        String label = (tabPage + 1) + "/" + pages;
        graphics.text(font, label, x - TAB_COLUMN + (TAB_COLUMN - font.width(label)) / 2, labelY, PARCHMENT, true);
        tabButton(graphics, TAB_UP, TAB_UP_HIGHLIGHTED, bx, y + TAB_BUTTON_Y, -1, mouseX, mouseY);
        tabButton(graphics, TAB_DOWN, TAB_DOWN_HIGHLIGHTED, bx, labelY + 7 + 4, 1, mouseX, mouseY);
    }

    /** 书签翻页按钮:黄铜片,贴图带 1 像素投影 */
    private void tabButton(GuiGraphicsExtractor graphics, Identifier sprite, Identifier highlighted, int x, int y, int dir,
                           int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + TAB_BUTTON_W && mouseY >= y && mouseY < y + TAB_BUTTON_H;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hover ? highlighted : sprite, x, y, TAB_BUTTON_W + 1, TAB_BUTTON_H + 1);
        hits.add(new Hit(x, y, TAB_BUTTON_W, TAB_BUTTON_H, () -> {
            turnTabPage(dir);
            play(SoundEvents.UI_BUTTON_CLICK.value());
        }));
    }

    /** 一条丝带:从书边 edge 往外伸出 out 像素(选中的伸得更长),其余压在书下面 */
    private void drawTab(GuiGraphicsExtractor graphics, Tab tab, boolean right, int edge, int ty, int mouseX, int mouseY) {
        int out = tab.id.equals(selectedTab) ? 24 : 19;
        int x0 = right ? edge : edge - out;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, right ? RIBBON_RIGHT : RIBBON, right ? edge + out - RIBBON_W : x0, ty,
                RIBBON_W, TAB_SIZE, tab.color);
        int ix = right ? edge + out - 19 : x0 + 3;
        if (tab.sprite != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, tab.sprite, ix, ty + 2, 16, 16);
        } else {
            graphics.item(tab.icon, ix, ty + 2);
        }
        Identifier id = tab.id;
        hits.add(new Hit(x0, ty, out, TAB_SIZE, () -> {
            if (!id.equals(selectedTab)) {
                selectedTab = id;
                listPage = 0;
                List<ResourceKey<QuestDefinition>> shown = questsIn(id);
                select(shown.isEmpty() ? null : shown.getFirst());
                play(SoundEvents.BOOK_PAGE_TURN);
            }
        }));
        if (mouseX >= x0 && mouseX < x0 + out && mouseY >= ty && mouseY < ty + TAB_SIZE) {
            graphics.setTooltipForNextFrame(font, tab.name, mouseX, mouseY);
        }
    }

    private void drawBook(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, 0, 0, BOOK_W, BOOK_H, BOOK_W, BOOK_H);
    }

    private void drawList(GuiGraphicsExtractor graphics, int px, int py, int mouseX, int mouseY) {
        Tab tab = tabs.stream().filter(t -> t.id.equals(selectedTab)).findFirst().orElse(null);
        if (tab != null) {
            String title = fit(tab.name.getString(), PAGE_W - 40);
            graphics.text(font, title, px + (PAGE_W - font.width(title)) / 2, py + 6, INK, false);
        }
        List<ResourceKey<QuestDefinition>> shown = questsIn(selectedTab);
        if (shown.isEmpty()) {
            // 放在第一、二道账本线(书页贴图里每行底下那道)的正中间
            Component empty = Component.translatable("quest.ibx_utility.board.no_quests");
            int firstLine = py + 22 + ROW_H - 2;
            graphics.text(font, empty, px + (PAGE_W - font.width(empty)) / 2, firstLine + ROW_H / 2 - 4, FADED, false);
            return;
        }
        int pages = (shown.size() + ROWS - 1) / ROWS;
        for (int r = 0; r < ROWS; r++) {
            int index = listPage * ROWS + r;
            if (index >= shown.size()) {
                break;
            }
            ResourceKey<QuestDefinition> key = shown.get(index);
            QuestDefinition quest = definition(key).orElseThrow();
            int ry = py + 22 + r * ROW_H;
            graphics.item(quest.iconStack(), px + 8, ry + 2);
            graphics.text(font, fit(quest.name().getString(), PAGE_W - 34), px + 27, ry + 2, INK, false);
            Component status = status(key, quest);
            graphics.text(font, fit(status.getString(), PAGE_W - 34), px + 27, ry + 11, statusColor(key, quest), false);
            if (key.equals(selectedQuest)) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, POINTER, px + 1, ry + 5, 4, 7);
                graphics.fill(px + 4, ry + 22, px + PAGE_W - 8, ry + 23, RED);
            }
            hits.add(new Hit(px + 2, ry, PAGE_W - 8, ROW_H, () -> {
                select(key);
                play(SoundEvents.UI_BUTTON_CLICK.value());
            }));
        }
        if (pages > 1) {
            drawPager(graphics, px + PAGE_W / 2, py + 22 + ROWS * ROW_H + 2, listPage, pages, mouseX, mouseY, dir -> listPage += dir);
        }
    }

    /** 页码「1/2」,两边金箭头;到头那边不画箭头 */
    private void drawPager(GuiGraphicsExtractor graphics, int cx, int y, int page, int pages, int mouseX, int mouseY, IntConsumer turn) {
        String label = (page + 1) + "/" + pages;
        graphics.text(font, label, cx - font.width(label) / 2, y, INK_LIGHT, false);
        if (page > 0) {
            spriteButton(graphics, PAGE_BACKWARD, PAGE_BACKWARD_HIGHLIGHTED, cx - 12 - ARROW_W, y, ARROW_W, ARROW_H,
                    mouseX, mouseY, () -> turn.accept(-1));
        }
        if (page < pages - 1) {
            spriteButton(graphics, PAGE_FORWARD, PAGE_FORWARD_HIGHLIGHTED, cx + 12, y, ARROW_W, ARROW_H,
                    mouseX, mouseY, () -> turn.accept(1));
        }
    }

    private Component status(ResourceKey<QuestDefinition> key, QuestDefinition quest) {
        ActiveQuest active = playerQuests().active(key);
        if (active != null) {
            if (achieved(quest, active)) {
                return Component.translatable("quest.ibx_utility.board.status.ready");
            }
            Optional<QuestBoardPayload.TeamInfo> team = ownTeam(key);
            return team.isPresent()
                    ? Component.translatable("quest.ibx_utility.board.status.team", team.get().members().size(), team.get().max())
                    : Component.translatable("quest.ibx_utility.board.status.accepted");
        }
        if (joinOnly(key, quest)) {
            return Component.translatable("quest.ibx_utility.board.status.join_only");
        }
        if (quest.pool() == QuestPool.RANDOM) {
            return Component.translatable("quest.ibx_utility.board.status.daily");
        }
        return quest.team().isPresent()
                ? Component.translatable("quest.ibx_utility.board.status.team_open")
                : Component.translatable("quest.ibx_utility.board.status.open");
    }

    private int statusColor(ResourceKey<QuestDefinition> key, QuestDefinition quest) {
        ActiveQuest active = playerQuests().active(key);
        if (active != null) {
            return achieved(quest, active) ? GOLD : GREEN;
        }
        return quest.pool() == QuestPool.RANDOM ? RED : INK_LIGHT;
    }

    /**
     * 右页:图标和名字(下面两道横线),再往下是简介、目标、奖励、队伍,最底下一排蜡封按钮(位置固定,和左页翻页那行齐底)。
     * 中间的内容一页放不下就分页:每页都留着图标和名字,页码和金箭头在蜡封按钮上面。
     */
    private void drawDetail(GuiGraphicsExtractor graphics, QuestDefinition quest, int x, int y, int w, int mouseX, int mouseY) {
        ResourceKey<QuestDefinition> key = selectedQuest;
        ActiveQuest active = playerQuests().active(key);
        int buttonY = y + PAGE_H - 23;
        graphics.item(quest.iconStack(), x, y);
        graphics.text(font, fit(quest.name().getString(), w - 20), x + 20, y + 4, INK, false);
        graphics.fill(x, y + 18, x + w, y + 19, RULE);
        graphics.fill(x, y + 20, x + w, y + 21, RULE_LIGHT);

        List<Block> blocks = detailBlocks(key, quest, active, x, w);
        int top = y + 24;
        int room = buttonY - 2 - top;
        List<List<Block>> pages = paginate(blocks, room);
        if (pages.size() > 1) {
            pages = paginate(blocks, room - PAGER_H);
        }
        detailPage = Math.min(detailPage, pages.size() - 1);
        int yy = top;
        for (Block block : pages.get(detailPage)) {
            block.drawer.draw(graphics, yy, mouseX, mouseY);
            yy += block.height;
        }
        if (pages.size() > 1) {
            drawPager(graphics, x + w / 2, buttonY - PAGER_H, detailPage, pages.size(), mouseX, mouseY, dir -> detailPage += dir);
        }

        // 往前推的(接受、提交)在右边,放弃在左边
        if (active != null) {
            sealButton(graphics, Wax.BROWN, Component.translatable("quest.ibx_utility.board.abandon"), x, buttonY, INK, mouseX, mouseY,
                    () -> send(QuestActionPayload.Action.ABANDON, key, null));
            Component submit = Component.translatable("quest.ibx_utility.board.submit");
            sealButton(graphics, Wax.GREEN, submit, x + w - sealWidth(submit), buttonY, achieved(quest, active) ? INK : INK_LIGHT,
                    mouseX, mouseY, () -> send(QuestActionPayload.Action.SUBMIT, key, null));
        } else if (!joinOnly(key, quest)) {
            Component accept = Component.translatable("quest.ibx_utility.board.accept");
            sealButton(graphics, Wax.RED, accept, x + w - sealWidth(accept), buttonY, INK, mouseX, mouseY,
                    () -> send(QuestActionPayload.Action.ACCEPT, key, null));
        }
    }

    /** 按高度把右页内容分页 */
    private static List<List<Block>> paginate(List<Block> blocks, int height) {
        List<List<Block>> pages = new ArrayList<>();
        List<Block> page = new ArrayList<>();
        int used = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            int need = block.height + (block.heading && i + 1 < blocks.size() ? blocks.get(i + 1).height : 0);
            if (!page.isEmpty() && used + need > height) {
                pages.add(page);
                page = new ArrayList<>();
                used = 0;
                if (block.section != null) {
                    page.add(block.section);
                    used += block.section.height;
                }
            }
            page.add(block);
            used += block.height;
        }
        pages.add(page);
        return pages;
    }

    /** 右页名字下面的内容,一段一段排好:简介每行一段,目标每个一段,奖励每排一段,队伍每行一段 */
    private List<Block> detailBlocks(ResourceKey<QuestDefinition> key, QuestDefinition quest, @Nullable ActiveQuest active, int x, int w) {
        List<Block> blocks = new ArrayList<>();
        quest.description().ifPresent(description -> {
            Block descriptionHeading = heading(Component.translatable("quest.ibx_utility.board.description"), x, 0);
            blocks.add(descriptionHeading);
            List<FormattedCharSequence> lines = font.split(description, w);
            for (int i = 0; i < lines.size(); i++) {
                FormattedCharSequence line = lines.get(i);
                blocks.add(new Block(i == lines.size() - 1 ? 14 : 10, false, descriptionHeading,
                        (graphics, y, mouseX, mouseY) -> graphics.text(font, line, x, y, INK_LIGHT, false)));
            }
        });

        Block objectivesHeading = heading(Component.translatable("quest.ibx_utility.board.objectives"), x, 0);
        blocks.add(objectivesHeading);
        List<QuestObjective> objectives = quest.objectives();
        for (int i = 0; i < objectives.size(); i++) {
            QuestObjective objective = objectives.get(i);
            int progress = Math.min(objective.count(), objective.displayProgress(minecraft.player, active == null ? 0 : active.progress(i)));
            blocks.add(new Block(18, false, objectivesHeading, (graphics, y, mouseX, mouseY) -> {
                ItemStack icon = QuestDisplay.cycle(objective.displayIcons());
                int tx = x;
                if (!icon.isEmpty()) {
                    graphics.item(icon, x, y);
                    tx += 19;
                }
                String count = progress + "/" + objective.count();
                graphics.text(font, fit(objective.displayLabel().getString(), w - (tx - x) - font.width(count) - 4), tx, y + 4, INK, false);
                graphics.text(font, count, x + w - font.width(count), y + 4, progress >= objective.count() ? GREEN : INK, false);
            }));
        }

        List<QuestReward> rewards = quest.rewards().stream().filter(reward -> !reward.displayIcon().isEmpty()).toList();
        if (!rewards.isEmpty()) {
            Block rewardsHeading = heading(Component.translatable("quest.ibx_utility.board.rewards"), x, 2);
            blocks.add(rewardsHeading);
            int perRow = Math.max(1, (w + 4) / 20);
            for (int from = 0; from < rewards.size(); from += perRow) {
                List<QuestReward> row = rewards.subList(from, Math.min(rewards.size(), from + perRow));
                blocks.add(new Block(20, false, rewardsHeading, (graphics, y, mouseX, mouseY) -> {
                    int rx = x;
                    for (QuestReward reward : row) {
                        ItemStack icon = reward.displayIcon();
                        graphics.item(icon, rx, y);
                        graphics.itemDecorations(font, icon, rx, y, reward.displayCount());
                        if (mouseX >= rx && mouseX < rx + 16 && mouseY >= y && mouseY < y + 16) {
                            graphics.setTooltipForNextFrame(font, reward.displayName(), mouseX, mouseY);
                        }
                        rx += 20;
                    }
                }));
            }
        }

        if (quest.team().isPresent()) {
            teamBlocks(blocks, key, quest, x, w);
        }
        return blocks;
    }

    /** 小标题(「简介」「目标」「奖励」「队伍」):上面空 gap,自己 11 高,字压在一小段红丝带上 */
    private Block heading(Component text, int x, int gap) {
        return new Block(gap + 11, true, null, (graphics, y, mouseX, mouseY) -> drawTag(graphics, text, x, y + gap));
    }

    /** 小丝带签:从字左边 2 像素起,到字右边多出 6 像素,上下各多 1 像素;右头一个 3 像素深的燕尾缺口 */
    private void drawTag(GuiGraphicsExtractor graphics, Component text, int x, int y) {
        int width = font.width(text) + 8;
        for (int row = -1; row <= 9; row++) {
            int end = Math.min(width, width - 3 + Math.abs(row - 4));
            int color = row == -1 ? TAG_LIGHT : row == 9 ? TAG_DARK : TAG;
            graphics.fill(x - 2, y + row, x + end, y + row + 1, color);
        }
        graphics.text(font, text, x + 1, y, TAG_TEXT, false);
    }

    /** 队伍:自己的队伍列出成员;没接的列出能加入的队伍,各带一个「加入」蜡封 */
    private void teamBlocks(List<Block> blocks, ResourceKey<QuestDefinition> key, QuestDefinition quest, int x, int w) {
        int max = quest.team().get().max();
        Optional<QuestBoardPayload.TeamInfo> own = ownTeam(key);
        if (own.isPresent()) {
            Block teamHeading = heading(Component.translatable("quest.ibx_utility.board.team", own.get().members().size(), max), x, 0);
            blocks.add(teamHeading);
            for (FormattedCharSequence line : font.split(Component.literal(String.join("、", own.get().members())), w)) {
                blocks.add(new Block(10, false, teamHeading, (graphics, y, mouseX, mouseY) -> graphics.text(font, line, x, y, INK_LIGHT, false)));
            }
            return;
        }
        if (playerQuests().isActive(key)) {
            return;
        }
        Block joinHeading = heading(Component.translatable("quest.ibx_utility.board.team_join"), x, 0);
        blocks.add(joinHeading);
        List<QuestBoardPayload.TeamInfo> joinable = ClientQuestBoard.current().teams().stream()
                .filter(team -> !team.mine() && team.quest().equals(key)).toList();
        if (joinable.isEmpty()) {
            blocks.add(new Block(10, false, joinHeading, (graphics, y, mouseX, mouseY) -> graphics.text(font,
                    fit(Component.translatable("quest.ibx_utility.board.no_teams").getString(), w), x, y, FADED, false)));
            return;
        }
        Component join = Component.translatable("quest.ibx_utility.board.join");
        for (QuestBoardPayload.TeamInfo team : joinable) {
            String label = Component.translatable("quest.ibx_utility.board.team_of", team.leader(), team.members().size(), max).getString();
            UUID id = team.id();
            blocks.add(new Block(TEAM_ROW_H, false, joinHeading, (graphics, y, mouseX, mouseY) -> {
                graphics.text(font, fit(label, w - sealWidth(join) - 4), x, y + 3, INK_LIGHT, false);
                sealButton(graphics, Wax.BLUE, join, x + w - sealWidth(join), y, INK, mouseX, mouseY,
                        () -> send(QuestActionPayload.Action.JOIN, key, id));
            }));
        }
    }

    // ---------------------------------------------------------------- 小件

    private int sealWidth(Component label) {
        return SEAL + 3 + font.width(label);
    }

    /** 蜡封按钮:左边一枚蜡封、右边写字;指上去蜡封变亮、字加下划线 */
    private void sealButton(GuiGraphicsExtractor graphics, Wax wax, Component label, int x, int y, int color, int mouseX, int mouseY,
                            Runnable action) {
        int w = sealWidth(label);
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + SEAL;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hover ? wax.highlighted : wax.sprite, x, y, SEAL, SEAL);
        int tx = x + SEAL + 3;
        graphics.text(font, label, tx, y + 3, color, false);
        if (hover) {
            graphics.fill(tx, y + 12, tx + font.width(label) - 1, y + 13, color);
        }
        hits.add(new Hit(x, y, w, SEAL, action));
    }

    /** 贴图按钮(翻页箭头):指上去换亮的那张 */
    private void spriteButton(GuiGraphicsExtractor graphics, Identifier sprite, Identifier highlighted, int x, int y, int w, int h,
                              int mouseX, int mouseY, Runnable action) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hover ? highlighted : sprite, x, y, w, h);
        hits.add(new Hit(x, y, w, h, action));
    }

    private String fit(String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }

    private void send(QuestActionPayload.Action action, ResourceKey<QuestDefinition> quest, @Nullable UUID team) {
        ClientQuestBoard.send(new QuestActionPayload(action, quest, Optional.ofNullable(team)));
        play(SoundEvents.UI_BUTTON_CLICK.value());
    }

    private void play(SoundEvent sound) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    /** 滚轮在左边书签那一列上时翻书签页(同锻造台),翻动了才响 */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = bookX();
        int y = bookY();
        if (tabPageCount() > 1 && mouseX >= x - TAB_COLUMN - 5 && mouseX < x && mouseY >= y && mouseY < y + BOOK_H) {
            if (turnTabPage(-(int) Math.signum(scrollY))) {
                play(SoundEvents.UI_BUTTON_CLICK.value());
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (Hit hit : List.copyOf(hits)) {
                if (hit.contains(event.x(), event.y())) {
                    hit.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
