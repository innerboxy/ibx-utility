package ibx.utility.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** 模组通用配置。 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 是否移除被装备锻造台配方所取代的原版工作台合成配方,使这些物品只能通过锻造台制作。默认开启。
     * 一个开关统一控制所有被锻造台取代的原版配方。
     */
    public static final ModConfigSpec.BooleanValue REMOVE_VANILLA_RECIPES = BUILDER
            .comment("移除被装备锻造台取代的原版工作台合成配方,使这些物品只能通过装备锻造台制作。",
                    "Remove the vanilla crafting-table recipes that are replaced by the Equipment Forge,",
                    "so those items can only be made at the forge. One switch controls all of them.")
            .define("removeVanillaRecipes", true);

    /** 是否移除被制箭台配方所取代的原版工作台合成配方(箭、药箭、光灵箭),使它们只能通过制箭台制作。默认开启。 */
    public static final ModConfigSpec.BooleanValue REMOVE_VANILLA_ARROW_RECIPES = BUILDER
            .comment("移除被制箭台取代的原版工作台合成配方(箭、药箭、光灵箭),使它们只能通过制箭台制作。",
                    "Remove the vanilla crafting-table recipes for arrows, tipped arrows and spectral arrows,",
                    "which are replaced by the fletching table, so they can only be made there.")
            .define("removeVanillaArrowRecipes", true);

    /** 是否加载模组自带的默认分类标签。默认开启。 */
    public static final ModConfigSpec.BooleanValue ENABLE_DEFAULT_CATEGORIES = BUILDER
            .comment("加载模组自带的默认分类标签(近战/远程/魔法/工具/盔甲/盾牌/饰品)。",
                    "关闭后可由数据包完全自定义分类;仍引用这些分类的配方将只出现在「全部」标签中。",
                    "Load the mod's built-in category tabs. Disable to define categories entirely via datapack.")
            .define("enableDefaultCategories", true);

    /** 是否加载模组自带的默认锻造配方。默认开启。 */
    public static final ModConfigSpec.BooleanValue ENABLE_DEFAULT_RECIPES = BUILDER
            .comment("加载模组自带的默认锻造配方(木/石/铜/铁/金/钻石/下界合金装备、矛、弓弩、盾牌等)。",
                    "关闭后可由数据包完全自定义配方表。不影响装备锻造台方块自身的合成配方。",
                    "Load the mod's built-in forge recipes. Disable to define recipes entirely via datapack.",
                    "Does not affect the crafting recipe of the Equipment Forge block itself.")
            .define("enableDefaultRecipes", true);

    /** 是否加载模组自带的默认制箭配方。默认开启。 */
    public static final ModConfigSpec.BooleanValue ENABLE_DEFAULT_FLETCHING_RECIPES = BUILDER
            .comment("加载模组自带的默认制箭配方(箭、药箭、光灵箭)。关闭后可由数据包完全自定义制箭配方。",
                    "Load the mod's built-in fletching recipes (arrow, tipped arrow, spectral arrow).",
                    "Disable to define fletching recipes entirely via datapack.")
            .define("enableDefaultFletchingRecipes", true);

    /** 悬赏板分页的开关:自带的两页(关键任务 / 自由任务)各一个,另一个留给整合包自己加的每日任务页。默认开启 */
    public static final ModConfigSpec.BooleanValue ENABLE_KEY_QUEST_CATEGORY = BUILDER
            .comment("加载悬赏板自带的「关键任务」页。关闭后写在这页的任务也跟着不显示、不能接(下面两个开关同理)。",
                    "Load the quest board's built-in Key Quests page. When disabled, quests on this page are skipped too (same for the two below).")
            .define("enableKeyQuestCategory", true);

    public static final ModConfigSpec.BooleanValue ENABLE_FREE_QUEST_CATEGORY = BUILDER
            .comment("加载悬赏板自带的「自由任务」页。", "Load the quest board's built-in Free Quests page.")
            .define("enableFreeQuestCategory", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DAILY_QUEST_CATEGORY = BUILDER
            .comment("「每日任务」页的开关。模组不带这一页;整合包自己加的每日任务页挂上 ibx_utility:daily_quest_category_enabled 条件,就受这个开关控制。",
                    "Switch for a Daily Quests page. The mod doesn't ship one; a modpack's own daily page that uses the",
                    "ibx_utility:daily_quest_category_enabled condition is controlled by this switch.")
            .define("enableDailyQuestCategory", true);

    /** 一个人同时能接几个任务。默认 5 */
    public static final ModConfigSpec.IntValue MAX_ACTIVE_QUESTS = BUILDER
            .comment("一个人同时能接几个任务。", "How many quests one player can have accepted at the same time.")
            .defineInRange("maxActiveQuests", 5, 1, 64);

    /** 每天从随机池抽几条任务挂上悬赏板。默认 5 */
    public static final ModConfigSpec.IntValue DAILY_QUEST_COUNT = BUILDER
            .comment("每天(游戏总时长每 24000 tick)从随机池抽几条任务挂上悬赏板,全服一份。",
                    "How many random-pool quests are posted each day (every 24000 ticks of game time), shared by the server.")
            .defineInRange("dailyQuestCount", 5, 0, 64);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
