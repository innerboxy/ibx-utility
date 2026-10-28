package ibx.utility.quest.objective;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ibx.utility.quest.QuestDisplay;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * 击杀生物:接了任务以后杀的才算。{@code entity} 可以写生物 id、{@code #标签} 或列表。
 *
 * <pre>
 * { "type": "ibx_utility:kill", "entity": "minecraft:wolf", "count": 5 }
 * { "type": "ibx_utility:kill", "entity": "#minecraft:skeletons", "count": 10, "name": "骷髅" }
 * </pre>
 *
 * @param count 可选,默认 1
 * @param name  可选,显示用的名字;不写就用生物(标签)的名字
 */
public record KillObjective(HolderSet<EntityType<?>> entity, int count, Optional<Component> name) implements QuestObjective {
    public static final MapCodec<KillObjective> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(KillObjective::entity),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(KillObjective::count),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(KillObjective::name)
    ).apply(instance, KillObjective::new));

    @Override
    public int onKill(ServerPlayer player, LivingEntity victim) {
        return entity.contains(victim.typeHolder()) ? 1 : 0;
    }

    @Override
    public ItemStack displayIcon() {
        return QuestDisplay.entityIcon(entity);
    }

    @Override
    public List<ItemStack> displayIcons() {
        return QuestDisplay.entityIcons(entity);
    }

    @Override
    public Component displayTarget() {
        return name.orElseGet(() -> QuestDisplay.entityName(entity));
    }

    @Override
    public Component displayLabel() {
        return Component.translatable("quest.ibx_utility.objective.kill", displayTarget());
    }

    @Override
    public MapCodec<KillObjective> codec() {
        return CODEC;
    }
}
