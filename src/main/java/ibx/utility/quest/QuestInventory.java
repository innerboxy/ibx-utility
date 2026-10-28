package ibx.utility.quest;

import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** 任务对背包的数和扣:只看主背包(快捷栏 + 背包 36 格),不碰身上穿的和副手。 */
public final class QuestInventory {
    private QuestInventory() {}

    public static int count(Player player, HolderSet<Item> items) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.is(items::contains)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** 最多扣 max 个,返回实际扣了多少 */
    public static int take(Player player, HolderSet<Item> items, int max) {
        int taken = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (taken >= max) {
                break;
            }
            if (!stack.isEmpty() && stack.is(items::contains)) {
                int n = Math.min(stack.getCount(), max - taken);
                stack.shrink(n);
                taken += n;
            }
        }
        if (taken > 0) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        return taken;
    }
}
