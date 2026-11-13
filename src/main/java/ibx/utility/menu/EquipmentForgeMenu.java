package ibx.utility.menu;

import ibx.utility.compat.beyonddimensions.BeyondDimensionsCompat;
import ibx.utility.network.NetItemsPayload;
import ibx.utility.recipe.ForgeMaterialSources;
import ibx.utility.registry.ModBlocks;
import ibx.utility.registry.ModMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 装备锻造台菜单:无机器格子,材料读取玩家背包、相邻存储与超越维度网络。
 * 下半部分展示玩家背包(槽位 0-26)与快捷栏(槽位 27-35)。
 */
public class EquipmentForgeMenu extends AbstractContainerMenu {
    /** 背包槽位区在界面中的起始坐标,Screen 绘制底格时需与此保持一致 */
    public static final int INV_X = 8;
    public static final int INV_Y = 144;
    public static final int HOTBAR_Y = 202;
    /** 服务端每隔多少 tick 重算一次背包外材料快照(存储可能被漏斗、其他玩家改动) */
    private static final int SYNC_INTERVAL = 20;

    public final Inventory playerInventory;
    private final ContainerLevelAccess access;
    /**
     * 客户端缓存:背包之外可用材料的快照(相邻存储 + 超越维度网络),由服务端通过 NetItemsPayload 同步。
     * 服务端则记录上次发出的快照,内容不变时不重发。
     */
    private List<ItemStack> netItems = List.of();
    private int syncTimer;

    /** 客户端构造 */
    public EquipmentForgeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    /** 服务端构造 */
    public EquipmentForgeMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.EQUIPMENT_FORGE.get(), containerId);
        this.playerInventory = playerInventory;
        this.access = access;

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    public List<ItemStack> netItems() {
        return netItems;
    }

    public void setNetItems(List<ItemStack> items) {
        this.netItems = items;
    }

    /** 服务端:锻造台相邻的材料存储(见 {@link ForgeMaterialSources}) */
    public List<ResourceHandler<ItemResource>> materialStorages() {
        return access.evaluate(ForgeMaterialSources::find, List.of());
    }

    /**
     * 服务端:重算背包之外可用材料的快照(相邻存储 + 超越维度网络,只保留锻造材料用得到的物品),
     * 与上次发出的不同才发给客户端。
     */
    public void syncNetItems(ServerPlayer player) {
        Predicate<ItemStack> filter = ForgeMaterialSources.materialFilter(player.level());
        List<ItemStack> items = new ArrayList<>(ForgeMaterialSources.snapshot(materialStorages(), filter));
        for (ItemStack stack : BeyondDimensionsCompat.getStoredItems(player)) {
            if (filter.test(stack)) items.add(stack);
        }
        if (sameStacks(items, netItems)) return;
        netItems = items;
        PacketDistributor.sendToPlayer(player, new NetItemsPayload(items));
    }

    /** 两个列表逐项物品、组件、数量都相同 */
    private static boolean sameStacks(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++) {
            if (!ItemStack.matches(left.get(i), right.get(i))) return false;
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (playerInventory.player instanceof ServerPlayer player && ++syncTimer >= SYNC_INTERVAL) {
            syncTimer = 0;
            syncNetItems(player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        // 背包与快捷栏之间互移
        if (index < 27) {
            if (!moveItemStackTo(stack, 27, 36, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 27, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.EQUIPMENT_FORGE.get());
    }
}
