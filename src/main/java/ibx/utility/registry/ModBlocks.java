package ibx.utility.registry;

import ibx.utility.block.EquipmentForgeBlock;
import ibx.utility.IbxUtility;
import ibx.utility.block.QuestBoardBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IbxUtility.MODID);

    //装备锻造台。1.21.2 起 Properties 必须带上注册 id,registerBlock 会代为 setId
    public static final DeferredBlock<EquipmentForgeBlock> EQUIPMENT_FORGE =
            BLOCKS.registerBlock("equipment_forge", EquipmentForgeBlock::new, properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());

    //悬赏板:宽 2 × 高 2 的四格组合方块,活塞推不动
    public static final DeferredBlock<QuestBoardBlock> QUEST_BOARD =
            BLOCKS.registerBlock("quest_board", QuestBoardBlock::new, properties -> properties
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
                    .pushReaction(PushReaction.BLOCK)
                    .noOcclusion());
}
