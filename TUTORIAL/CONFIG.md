# 配置

**简体中文** | [English](CONFIG_en.md)

[← 返回首页](../README.md)

---

## 1. 通用配置

配置类型为 **COMMON**,文件位于 `config/ibx_utility-common.toml`。追踪栏样式是客户端配置,见 [2](#2-客户端配置)。

```toml
#移除被装备锻造台取代的原版工作台合成配方,使这些物品只能通过装备锻造台制作。
removeVanillaRecipes = true
#移除被制箭台取代的原版工作台合成配方(箭、药箭、光灵箭),使它们只能通过制箭台制作。
removeVanillaArrowRecipes = true
#加载模组自带的默认分类标签(近战/远程/魔法/工具/盔甲/盾牌/饰品)。
enableDefaultCategories = true
#加载模组自带的默认锻造配方(木/石/铜/铁/金/钻石/下界合金装备、矛、弓弩、盾牌等)。
enableDefaultRecipes = true
#加载模组自带的默认制箭配方(箭、药箭、光灵箭)。关闭后可由数据包完全自定义制箭配方。
enableDefaultFletchingRecipes = true
#加载悬赏板自带的「关键任务」页。关闭后写在这页的任务也跟着不显示、不能接(下面两页同理)。
enableKeyQuestCategory = true
#加载悬赏板自带的「自由任务」页。
enableFreeQuestCategory = true
#「每日任务」页的开关。模组不带这一页;整合包自己加的每日任务页挂上 ibx_utility:daily_quest_category_enabled 条件,就受这个开关控制。
enableDailyQuestCategory = true
#一个人同时能接几个任务。
maxActiveQuests = 5
#每天(游戏总时长每 24000 tick)从随机池抽几条任务挂上悬赏板,全服一份。
dailyQuestCount = 5
```

| 选项 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `removeVanillaRecipes` | boolean | `true` | 是否移除被锻造台取代的原版配方。一个开关统一控制全部。 |
| `removeVanillaArrowRecipes` | boolean | `true` | 是否移除被制箭台取代的原版配方(箭、药箭、光灵箭)。与上一项互不影响。 |
| `enableDefaultCategories` | boolean | `true` | 是否加载模组自带的 7 个默认分类标签。关闭后可由数据包完全自定义分类。 |
| `enableDefaultRecipes` | boolean | `true` | 是否加载模组自带的默认锻造配方。关闭后可由数据包完全自定义配方表。 |
| `enableDefaultFletchingRecipes` | boolean | `true` | 是否加载模组自带的默认制箭配方。关闭后可由数据包完全自定义制箭配方。与上一项互不影响。 |
| `enableKeyQuestCategory` | boolean | `true` | 是否加载悬赏板自带的「关键任务」页。关闭后写在这页的任务一起跳过。 |
| `enableFreeQuestCategory` | boolean | `true` | 是否加载悬赏板自带的「自由任务」页。同上。 |
| `enableDailyQuestCategory` | boolean | `true` | 「每日任务」页的开关。模组不带这一页,整合包自己加的每日任务页挂上对应条件后受它控制(见 [悬赏板 3](QUEST_BOARD.md#3-分页))。 |
| `maxActiveQuests` | int(1~64) | `5` | 一个人同时最多接几个任务(组队任务也算一个)。 |
| `dailyQuestCount` | int(0~64) | `5` | 每个游戏日从随机池抽几条任务,全服共用一份。 |

**前五项和三个悬赏板分页开关的共同点:**

- 都**只在服务端生效**。它们作用于数据包加载阶段(服务端),结果随后同步给客户端。
  - 单人游戏:本地配置文件即生效(单人客户端自带集成服务端)。
  - 多人游戏:**只有服务器的配置文件生效**,客户端改本地文件无效。
- 生效时机分两类:
  - **配方类**(`removeVanillaRecipes`、`removeVanillaArrowRecipes`、`enableDefaultRecipes`、`enableDefaultFletchingRecipes`):改完 `/reload` 或重进世界。
  - **分类、分页类**(`enableDefaultCategories` 和三个悬赏板分页开关):锻造台分类、悬赏板分页都是数据包注册表,只在进世界时读一次,`/reload` 不会重读。改完要**重进世界**(服务器要重启)。

**`removeVanillaRecipes`**

- 开启时,以下原版配方会被移除:木/石/铜/铁/金/钻石各阶的工具、剑与矛、盾牌、弓、弩、重锤、皮革/铜/铁/金/钻石套装、海龟壳,以及**下界合金装备(含下界合金矛)的锻造台升级(smithing)配方**。
- 关闭时,以上原版配方全部恢复,锻造台配方仍然可用(即两种途径并存)。

**`removeVanillaArrowRecipes`**

- 开启时,原版工作台的**箭、药箭、光灵箭**配方会被移除,这三种箭只能在制箭台制作。
- 关闭时,原版配方恢复,制箭台配方仍然可用。
- 与 `removeVanillaRecipes` 相互独立,可以只移除其中一类。

**`enableDefaultCategories`**

- 关闭后,`ibx_utility:melee` 等 7 个内置分类不再加载,左侧只剩内置的「全部」标签,可由数据包自行定义分类。
- 注意:若同时保留了默认配方,那些配方引用的分类将不存在,于是**只会出现在「全部」标签中**。通常与 `enableDefaultRecipes = false` 搭配使用。

**`enableDefaultRecipes`**

- 关闭后,模组自带的 77 个锻造配方不再加载,可由数据包自行定义整套配方表。
- **不影响装备锻造台方块自身的合成配方**(该方块始终可合成)。
- 也不影响 `removeVanillaRecipes`——如需同时恢复原版配方,请另行关闭它。

**`enableDefaultFletchingRecipes`**

- 关闭后,模组自带的 3 个制箭配方(箭、药箭、光灵箭)不再加载,可由数据包自行定义制箭配方。
- 与 `enableDefaultRecipes` 相互独立。
- 也不影响 `removeVanillaArrowRecipes`——关闭默认制箭配方而原版箭配方仍被移除(该项默认开启)时,记得用数据包补上制箭配方,否则将没有任何途径做箭。

**悬赏板分页开关(`enableKeyQuestCategory` / `enableFreeQuestCategory` / `enableDailyQuestCategory`)**

- 关掉某一页,这一页不再加载;**写在这页的任务一起跳过**:不显示、不能接、不进每日抽取,已经接了的玩家下次上线时清掉。
- 三个开关互不影响,可以只留需要的;自定义分页见 [悬赏板 3](QUEST_BOARD.md#3-分页)。
- 改完要重进世界(服务器要重启),`/reload` 不生效。

**`maxActiveQuests` / `dailyQuestCount`**

- 同样只看服务端的配置,但不用 `/reload`:上限在接任务时读;抽取数量在下一次刷新每日任务时生效(每个游戏日一次)。

---

## 2. 客户端配置

追踪栏样式是每个玩家自己的设置,文件位于 `config/ibx_utility-client.toml`,也可以在模组列表里点本模组的「配置」改:

```toml
#左上角任务追踪栏的样式:文字 / 图标 / 图标带任务 / 不显示(默认)。
trackerMode = "HIDDEN"
```

| 选项 | 可选值 | 默认 | 说明 |
| --- | --- | --- | --- |
| `trackerMode` | `TEXT` / `ICONS` / `ICONS_WITH_QUEST` / `HIDDEN` | `HIDDEN` | 文字:任务名 + 每个目标一行;图标:一个任务一行,目标图标 + 数字;图标带任务:前面再加任务图标;不显示。目标全部达成的任务变绿。 |

---

## 3. 数据包条件

模组注册了八个数据包条件,分别对应八个配置项。NeoForge 的规则是「条件为 `true` 时该条数据才加载」,所以条件名描述的都是"**加载**"这一侧的状态:

| 条件 id | 返回 `true` 的时机 | 用在哪 |
| --- | --- | --- |
| `ibx_utility:vanilla_recipe_enabled` | `removeVanillaRecipes = false` 时 | 模组对被锻造台取代的原版配方的覆盖文件 |
| `ibx_utility:vanilla_arrow_recipe_enabled` | `removeVanillaArrowRecipes = false` 时 | 模组对原版箭、药箭、光灵箭配方的覆盖文件 |
| `ibx_utility:default_recipes_enabled` | `enableDefaultRecipes = true` 时 | 模组自带的 77 个锻造配方 |
| `ibx_utility:default_fletching_recipes_enabled` | `enableDefaultFletchingRecipes = true` 时 | 模组自带的 3 个制箭配方 |
| `ibx_utility:default_categories_enabled` | `enableDefaultCategories = true` 时 | 模组自带的 7 个分类定义 |
| `ibx_utility:key_quest_category_enabled` | `enableKeyQuestCategory = true` 时 | 悬赏板自带的「关键任务」页 |
| `ibx_utility:free_quest_category_enabled` | `enableFreeQuestCategory = true` 时 | 悬赏板自带的「自由任务」页 |
| `ibx_utility:daily_quest_category_enabled` | `enableDailyQuestCategory = true` 时 | 模组不带每日任务页,留给整合包自己加的那一页(见 [悬赏板 3](QUEST_BOARD.md#3-分页)) |

> 条件不仅能用在配方上——NeoForge 给数据包注册表也接上了条件解析,所以分类定义([装备锻造台 3](FORGE.md#3-分类定义))、悬赏板分页([悬赏板 3](QUEST_BOARD.md#3-分页))和任务([悬赏板 4](QUEST_BOARD.md#4-任务))同样支持 `neoforge:conditions`。

### 用条件移除原版配方

`vanilla_recipe_enabled` 的语义是:当配置 `removeVanillaRecipes` 为 `false`(即**不移除**)时返回 `true`。

由于 NeoForge 的规则是「条件为 true 时配方才加载」,把它挂在原版配方文件上,即可实现"配置开启时原版配方不加载"。做法是**用与原版内容一致的文件覆盖原版配方**,并在顶部加上条件:

```json
{
  "neoforge:conditions": [
    { "type": "ibx_utility:vanilla_recipe_enabled" }
  ],
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "key": {
    "#": "#c:rods/wooden",
    "X": "#minecraft:wooden_tool_materials"
  },
  "pattern": ["X", "X", "#"],
  "result": { "id": "minecraft:wooden_sword" }
}
```

放到 `data/minecraft/recipe/wooden_sword.json` 即可覆盖原版木剑配方。该条件对任意配方类型都适用(本模组也用它覆盖了下界合金的 `smithing_transform` 配方)。

> 新增锻造配方时,若希望同时移除对应的原版配方,照此覆盖一份即可复用同一个配置开关。
> 原版配方的原文可从 `data/minecraft/recipe/` 中提取:NeoForge 改写过的(如把木棍换成 `#c:rods/wooden`)在 `neoforge-<版本>-universal.jar` 里,其余在 Minecraft 本体 jar 里。覆盖时应以 NeoForge 改写版为准。
