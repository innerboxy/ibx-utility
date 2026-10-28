package ibx.utility.quest.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * 抽一次原版战利品表(战利品袋也可以放在表里发)。组队时每人各抽各的,背包放不下的掉在脚下。
 * 抽表的参数同原版进度奖励(这个玩家、玩家的位置、幸运值)。
 *
 * <pre>
 * { "type": "ibx_utility:loot_table", "table": "example:quest/wolf_hunt" }
 * </pre>
 */
public record LootTableReward(ResourceKey<LootTable> table) implements QuestReward {
    public static final MapCodec<LootTableReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootTable.KEY_CODEC.fieldOf("table").forGetter(LootTableReward::table)
    ).apply(instance, LootTableReward::new));

    @Override
    public MapCodec<LootTableReward> codec() {
        return CODEC;
    }

    @Override
    public ItemStack displayIcon() {
        return new ItemStack(Items.CHEST);
    }

    @Override
    public Component displayName() {
        return Component.translatable("quest.ibx_utility.reward.loot_table");
    }

    @Override
    public void grant(ServerPlayer player) {
        ServerLevel level = player.level();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ADVANCEMENT_REWARD);
        for (ItemStack stack : level.getServer().reloadableRegistries().getLootTable(table).getRandomItems(params)) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }
}
