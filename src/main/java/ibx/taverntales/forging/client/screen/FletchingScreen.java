package ibx.taverntales.forging.client.screen;

import ibx.taverntales.forging.TavernTalesEquipmentForge;
import ibx.taverntales.forging.menu.FletchingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * 制箭台界面:原版风格的灰色面板,四格一排 → 箭头 → 26x26 成品大格。
 * 槽位框、箭头、大格,以及装饰(左上角的箭图标、四格外的砂轮式凸起边框)都烘焙在 fletching_table.png 里;
 * 空槽的线稿底纹由 {@link CyclingSlotBackground} 绘制,与原版锻造台空槽图标同一套机制(药水格在水瓶与火药之间轮换)。
 */
public class FletchingScreen extends ItemCombinerScreen<FletchingMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            TavernTalesEquipmentForge.MODID, "textures/gui/fletching_table.png");
    /** 标题放在左上角箭图标的右侧(图标占 8..37),位置同原版锻造台 */
    private static final int TITLE_X = 44;
    private static final int TITLE_Y = 15;

    private static final List<Identifier> TAIL_ICONS = List.of(sprite("feather"));
    private static final List<Identifier> SHAFT_ICONS = List.of(sprite("stick"));
    private static final List<Identifier> TIP_ICONS = List.of(sprite("flint"));
    /** 药水格只作提示:原版药水空槽图标 + 火药(与红石粉同形,借用原版红石粉的空槽图标) */
    private static final List<Identifier> ADDITIVE_ICONS = List.of(
            Identifier.withDefaultNamespace("container/slot/potion"),
            Identifier.withDefaultNamespace("container/slot/redstone_dust"));

    private final CyclingSlotBackground tailIcon = new CyclingSlotBackground(FletchingMenu.TAIL_SLOT);
    private final CyclingSlotBackground shaftIcon = new CyclingSlotBackground(FletchingMenu.SHAFT_SLOT);
    private final CyclingSlotBackground tipIcon = new CyclingSlotBackground(FletchingMenu.TIP_SLOT);
    private final CyclingSlotBackground additiveIcon = new CyclingSlotBackground(FletchingMenu.ADDITIVE_SLOT);

    public FletchingScreen(FletchingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND);
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(TavernTalesEquipmentForge.MODID, "fletching_table/" + name);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        tailIcon.tick(TAIL_ICONS);
        shaftIcon.tick(SHAFT_ICONS);
        tipIcon.tick(TIP_ICONS);
        additiveIcon.tick(ADDITIVE_ICONS);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        tailIcon.extractRenderState(menu, graphics, a, leftPos, topPos);
        shaftIcon.extractRenderState(menu, graphics, a, leftPos, topPos);
        tipIcon.extractRenderState(menu, graphics, a, leftPos, topPos);
        additiveIcon.extractRenderState(menu, graphics, a, leftPos, topPos);
    }

    /** 制箭台没有"放了材料却合不出来"的错误图标 */
    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int xo, int yo) {
    }
}
