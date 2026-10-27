package ibx.utility.block;

import ibx.utility.menu.FletchingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 右键原版制箭台打开制箭界面。原版制箭台只是个普通方块(村民制箭师的工作站点),
 * 这里不替换方块,只在交互事件里接管,村民的工作站点逻辑不受影响。
 */
public final class FletchingTableInteraction {
    /** 沿用原版方块名的翻译,各语言自动显示"制箭台 / Fletching Table" */
    private static final Component TITLE = Component.translatable("block.minecraft.fletching_table");

    private FletchingTableInteraction() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!level.getBlockState(event.getPos()).is(Blocks.FLETCHING_TABLE)) return;
        Player player = event.getEntity();
        // 与原版方块交互一致:潜行且手里有东西时不开界面,留给放方块、用物品
        if (player.isSecondaryUseActive()
                && !(player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty())) {
            return;
        }

        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new FletchingMenu(id, inventory, ContainerLevelAccess.create(level, event.getPos())),
                    TITLE
            ));
        }
        // 两端都取消并返回 SUCCESS:客户端挥手且不再尝试副手 / 使用手中物品
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
