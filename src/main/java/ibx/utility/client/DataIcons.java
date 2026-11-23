package ibx.utility.client;

import ibx.utility.quest.QuestCategoryDefinition;
import ibx.utility.quest.QuestDefinition;
import ibx.utility.recipe.EquipmentCategoryDefinition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 画数据包里写的图标,占 16×16:任务(悬赏板目录、右页标题、追踪栏)、悬赏板分页书签、锻造台分类标签(和 JEI 里的分类角标)。
 * icon 写的是图片路径(以 .png 结尾)就画那张图,整张缩到 16×16;否则画物品。图片不存在时是原版的紫黑格。
 */
public final class DataIcons {
    private static final int SIZE = 16;

    private DataIcons() {}

    public static void draw(GuiGraphicsExtractor graphics, QuestDefinition quest, int x, int y) {
        if (quest.hasTextureIcon()) {
            texture(graphics, quest.icon(), x, y);
        } else {
            graphics.item(quest.iconStack(), x, y);
        }
    }

    public static void draw(GuiGraphicsExtractor graphics, QuestCategoryDefinition category, int x, int y) {
        if (category.hasTextureIcon()) {
            texture(graphics, category.icon(), x, y);
        } else {
            graphics.item(category.iconStack(), x, y);
        }
    }

    public static void draw(GuiGraphicsExtractor graphics, EquipmentCategoryDefinition category, int x, int y) {
        if (category.hasTextureIcon()) {
            texture(graphics, category.icon(), x, y);
        } else {
            graphics.item(category.iconStack(), x, y);
        }
    }

    /** 把一整张图缩到 16×16 画出来 */
    private static void texture(GuiGraphicsExtractor graphics, Identifier image, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, image, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }
}
