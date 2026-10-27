package ibx.utility.registry;

import ibx.utility.IbxUtility;
import ibx.utility.lootbag.LootBagItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IbxUtility.MODID);

    //装备锻造台。1.21.2 起 Properties 必须带上注册 id,registerSimpleBlockItem / registerItem 会代为 setId
    public static final DeferredItem<BlockItem> EQUIPMENT_FORGE =
            ITEMS.registerSimpleBlockItem("equipment_forge", ModBlocks.EQUIPMENT_FORGE);

    //悬赏板
    public static final DeferredItem<BlockItem> QUEST_BOARD =
            ITEMS.registerSimpleBlockItem("quest_board", ModBlocks.QUEST_BOARD);

    //战利品袋:史诗品质,掉落物形态不会被岩浆/火烧毁,也不会被爆炸炸掉(后者见 LootBagItem#canBeHurtBy)
    public static final DeferredItem<LootBagItem> LOOT_BAG =
            ITEMS.registerItem("loot_bag", LootBagItem::new, properties -> properties
                    .rarity(Rarity.EPIC)
                    .fireResistant());
}
