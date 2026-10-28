package ibx.utility.registry;

import com.mojang.serialization.MapCodec;
import ibx.utility.IbxUtility;
import ibx.utility.quest.action.QuestAction;
import ibx.utility.quest.objective.CollectItemObjective;
import ibx.utility.quest.objective.KillObjective;
import ibx.utility.quest.objective.QuestObjective;
import ibx.utility.quest.objective.SubmitItemObjective;
import ibx.utility.quest.requirement.AdvancementRequirement;
import ibx.utility.quest.requirement.QuestCompletedRequirement;
import ibx.utility.quest.requirement.QuestRequirement;
import ibx.utility.quest.reward.CommandReward;
import ibx.utility.quest.reward.ExperienceReward;
import ibx.utility.quest.reward.ItemReward;
import ibx.utility.quest.reward.LootTableReward;
import ibx.utility.quest.reward.QuestReward;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 任务里四种可扩展类型的注册表(目标、奖励、前置条件、任务动作)和本模组自带的类型。
 * 别的模组用 {@code DeferredRegister.create(QuestTypeRegistries.XXX_KEY, 模组id)} 注册自己的类型,
 * 注册的值是该类型的 MapCodec,任务 JSON 里用 {@code "type": "模组id:名字"} 引用。
 */
public final class QuestTypeRegistries {
    public static final ResourceKey<Registry<MapCodec<? extends QuestObjective>>> OBJECTIVE_TYPE_KEY = key("quest_objective_type");
    public static final ResourceKey<Registry<MapCodec<? extends QuestReward>>> REWARD_TYPE_KEY = key("quest_reward_type");
    public static final ResourceKey<Registry<MapCodec<? extends QuestRequirement>>> REQUIREMENT_TYPE_KEY = key("quest_requirement_type");
    public static final ResourceKey<Registry<MapCodec<? extends QuestAction>>> ACTION_TYPE_KEY = key("quest_action_type");

    public static final Registry<MapCodec<? extends QuestObjective>> OBJECTIVE_TYPES = new RegistryBuilder<>(OBJECTIVE_TYPE_KEY).sync(true).create();
    public static final Registry<MapCodec<? extends QuestReward>> REWARD_TYPES = new RegistryBuilder<>(REWARD_TYPE_KEY).sync(true).create();
    public static final Registry<MapCodec<? extends QuestRequirement>> REQUIREMENT_TYPES = new RegistryBuilder<>(REQUIREMENT_TYPE_KEY).sync(true).create();
    public static final Registry<MapCodec<? extends QuestAction>> ACTION_TYPES = new RegistryBuilder<>(ACTION_TYPE_KEY).sync(true).create();

    public static final DeferredRegister<MapCodec<? extends QuestObjective>> OBJECTIVES = DeferredRegister.create(OBJECTIVE_TYPE_KEY, IbxUtility.MODID);
    public static final DeferredRegister<MapCodec<? extends QuestReward>> REWARDS = DeferredRegister.create(REWARD_TYPE_KEY, IbxUtility.MODID);
    public static final DeferredRegister<MapCodec<? extends QuestRequirement>> REQUIREMENTS = DeferredRegister.create(REQUIREMENT_TYPE_KEY, IbxUtility.MODID);

    //目标:提交物品 / 击杀 / 收集物品
    public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<SubmitItemObjective>> SUBMIT_ITEM =
            OBJECTIVES.register("submit_item", () -> SubmitItemObjective.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<KillObjective>> KILL =
            OBJECTIVES.register("kill", () -> KillObjective.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<CollectItemObjective>> COLLECT_ITEM =
            OBJECTIVES.register("collect_item", () -> CollectItemObjective.CODEC);

    //奖励:固定物品 / 战利品表 / 经验 / 指令
    public static final DeferredHolder<MapCodec<? extends QuestReward>, MapCodec<ItemReward>> ITEM =
            REWARDS.register("item", () -> ItemReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestReward>, MapCodec<LootTableReward>> LOOT_TABLE =
            REWARDS.register("loot_table", () -> LootTableReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestReward>, MapCodec<ExperienceReward>> EXPERIENCE =
            REWARDS.register("experience", () -> ExperienceReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestReward>, MapCodec<CommandReward>> COMMAND =
            REWARDS.register("command", () -> CommandReward.CODEC);

    //前置条件:完成过某任务 / 拿到某原版进度
    public static final DeferredHolder<MapCodec<? extends QuestRequirement>, MapCodec<QuestCompletedRequirement>> QUEST_COMPLETED =
            REQUIREMENTS.register("quest_completed", () -> QuestCompletedRequirement.CODEC);
    public static final DeferredHolder<MapCodec<? extends QuestRequirement>, MapCodec<AdvancementRequirement>> ADVANCEMENT =
            REQUIREMENTS.register("advancement", () -> AdvancementRequirement.CODEC);

    private QuestTypeRegistries() {}

    private static <T> ResourceKey<Registry<T>> key(String path) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(IbxUtility.MODID, path));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(QuestTypeRegistries::onNewRegistry);
        OBJECTIVES.register(modEventBus);
        REWARDS.register(modEventBus);
        REQUIREMENTS.register(modEventBus);
    }

    private static void onNewRegistry(NewRegistryEvent event) {
        event.register(OBJECTIVE_TYPES);
        event.register(REWARD_TYPES);
        event.register(REQUIREMENT_TYPES);
        event.register(ACTION_TYPES);
    }
}
