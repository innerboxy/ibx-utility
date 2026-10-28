# IBX-Utility

**简体中文** | [English](README_en.md)

Minecraft **1.21.1** / **NeoForge** 模组,为 TavernTales 提供一个数据驱动的装备锻造台。

---

## 1. 简介

本模组添加**装备锻造台**(`ibx_utility:equipment_forge`)——一个以"选中配方即可制作"为核心的合成方块,用来取代原版工作台里繁琐的装备摆放。

### 核心特点

- **材料直接取自背包**:锻造台没有合成格子,材料从玩家主背包 + 副手中扣除,不需要来回搬运物品。
- **列表式界面**:左侧是分类标签 + 搜索框 + 物品网格,右侧显示所选配方的所需材料与制作按钮。
  - 参照原版配方书:可合成的物品显示为亮色槽位,材料不足显示为暗色槽位。
  - 搜索框支持**拼音检索**(全拼与首字母,如 `zuanshijian` / `zsj` 均可搜到钻石剑)。
  - 可切换「显示全部 / 仅显示可合成」。
  - 物品排序:先按**稀有度**,再按**创造模式物品栏顺序**;右侧材料列表同理。
- **数据驱动的分类**:左侧分类标签完全由数据包定义,整合包可自由增删改(见 [3.2](#32-分类定义))。
- **可选地移除原版配方**:被锻造台取代的原版工作台/锻造台配方可由配置统一移除,默认开启(见 [2](#2-配置))。
- **背包满不丢物品**:制作时背包放不下的产物会掉落在玩家脚下。

### 战利品袋

模组另外提供一个独立物品——**战利品袋**(`ibx_utility:loot_bag`),右键即开,内容完全由数据包的战利品表定义(见 [4](#4-战利品袋))。

- **一袋一表**:袋子的种类写在物品组件里,对应一张同名的战利品表,可以给每个 Boss 配一个专属袋子。
- **掉落物形态受保护**:免疫火焰/岩浆/爆炸,永不消失,并常亮发光轮廓(隔墙可见),适合直接扔在战场上。
- **可按种类换材质**:资源包丢一个模型文件就能让不同袋子长得不一样,零代码(见 [4.5](#45-给袋子换材质))。
- **可在 JEI 中查看掉落**:装了 JER 后能看到每个袋子的可能掉落与概率(见 [4.6](#46-在-jei-中查看掉落))。
- 没有合成配方。**空袋**可在创造物品栏的「原材料」分类最前面取得;**具体种类的袋子**只能通过 `/give`、命令或战利品表给予(种类由数据包定义,数量不定,创造栏不穷举)。

### 制箭台

右键**原版制箭台**会打开制箭界面。方块本身不变,村民制箭师照常把它当工作站点。

- 箭尾、箭身、箭头、药水四格一排,放齐后右侧出成品;每次合成四格各消耗 1 个,Shift 点击成品一次做到材料用完。
- 每格只接受配方里用在该位置的材料,Shift 点击背包物品会自动进对应格子;空槽有线稿底纹提示放什么。
- 配方由数据包定义(见 [3.4](#34-制箭配方))。内置三条,都是羽毛 + 木棍 + 燧石打底、每次出 8 支:
  - 药水格留空 → 箭;
  - 药水格放**滞留药水** → 带该药水效果的药箭(药水整瓶消耗,不退空瓶);
  - 药水格放**荧石粉** → 光灵箭。
- 原版工作台的箭、药箭、光灵箭配方由独立开关 `removeVanillaArrowRecipes` 控制是否移除(见 [2](#2-配置))。
- 装了 JEI 可查「制箭台」分类;在制箭台界面里打开 JEI 配方页,点成品右侧的「+」可把背包里的材料自动填进格子。

### 悬赏板

**悬赏板**(`ibx_utility:quest_board`)是一块宽 2 格、高 2 格的木板,右键打开账本式的任务界面,接任务、交任务都在这里。任务完全由数据包定义(见 [6](#6-悬赏板与任务))。

- **三种目标**:提交物品、击杀生物、收集物品;奖励可以是物品、战利品表、经验或指令。
- **固定任务 + 每日随机**:固定任务一直挂着;随机池的任务每个游戏日全服抽几条(默认 5 条)。
- **回板子提交**:目标达成后回到悬赏板点「提交」才算完成,背包放不下的奖励掉在脚下。
- **可以组队**:任务写了 `team` 就能组队,进度全队共用,提交时在线的队员每人一份奖励。
- **前置条件**:前置没满足的任务直接不显示。
- **追踪栏**:屏幕左上角列出已接的任务和进度,四种样式可选,默认不显示(见 [2](#2-配置))。
- **能扩展**:别的模组可以注册新的目标、奖励、前置条件、任务动作类型,也能监听接 / 加入 / 进度 / 提交 / 放弃事件(见 [6.5](#65-给开发者的接口与事件))。

### 获取方式

装备锻造台可在**原版工作台**中合成:

```
铁 铁 铁          铁 = 铁锭
金 台 金          金 = 金锭
金    金          台 = 工作台
```

悬赏板同样在**原版工作台**中合成:

```
板 板 板          板 = 任意木板
纸 书 纸          书 = 书与笔
棍    棍          棍 = 木棍
```

### 依赖

| 模组 | 类型 | 说明 |
| --- | --- | --- |
| NeoForge `21.1.234+` | **必需** | — |
| Minecraft `1.21.1` | **必需** | — |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) `19+` | 可选(客户端) | 提供「装备锻造」「制箭台」配方查询分类;在锻造台界面点击 JEI 的「+」可直接跳转选中该配方,在制箭台界面点击「+」可自动填入材料 |
| [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) `1.6+`(JER) | 可选(客户端) | 提供「战利品袋」分类,展示每个袋子的可能掉落与概率(见 [4.6](#46-在-jei-中查看掉落))。需同时装 JEI |
| [Beyond Dimensions](https://modrinth.com/mod/beyonddimensions) `0.7+`(超越维度) | 可选 | 锻造时可直接消耗玩家**主网络**中存储的物品,界面的可合成判定与材料计数也会计入 |

> 拼音检索所需的 pinyin4j 已通过 jarJar 打包进模组,无需额外安装。

---

## 2. 配置

配置类型为 **COMMON**,文件位于 `config/ibx_utility-common.toml`。追踪栏样式是客户端配置,见本节末尾。

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
| `enableDailyQuestCategory` | boolean | `true` | 「每日任务」页的开关。模组不带这一页,整合包自己加的每日任务页挂上对应条件后受它控制(见 [6.2](#62-分页))。 |
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
- 三个开关互不影响,可以只留需要的;自定义分页见 [6.2](#62-分页)。
- 改完要重进世界(服务器要重启),`/reload` 不生效。

**`maxActiveQuests` / `dailyQuestCount`**

- 同样只看服务端的配置,但不用 `/reload`:上限在接任务时读;抽取数量在下一次刷新每日任务时生效(每个游戏日一次)。

**客户端配置**

追踪栏样式是每个玩家自己的设置,文件位于 `config/ibx_utility-client.toml`,也可以在模组列表里点本模组的「配置」改:

```toml
#左上角任务追踪栏的样式:文字 / 图标 / 图标带任务 / 不显示(默认)。
trackerMode = "HIDDEN"
```

| 选项 | 可选值 | 默认 | 说明 |
| --- | --- | --- | --- |
| `trackerMode` | `TEXT` / `ICONS` / `ICONS_WITH_QUEST` / `HIDDEN` | `HIDDEN` | 文字:任务名 + 每个目标一行;图标:一个任务一行,目标图标 + 数字;图标带任务:前面再加任务图标;不显示。目标全部达成的任务变绿。 |

---

## 3. 数据格式

模组共涉及四类数据文件,整合包可通过数据包直接覆盖或新增。

### 3.1 锻造配方

- **位置**:`data/<命名空间>/recipe/**/*.json`(可放在任意子目录;本模组放在 `recipe/equipment_forge/` 下)
- **类型**:`ibx_utility:equipment_forge`

```json
{
  "type": "ibx_utility:equipment_forge",
  "category": "ibx_utility:melee",
  "materials": [
    { "ingredient": "#minecraft:planks", "count": 2 },
    { "ingredient": "minecraft:diamond", "count": 3 }
  ],
  "result": { "id": "minecraft:diamond_sword", "count": 1 }
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `category` | 是 | 分类 id,**必须写完整命名空间**(如 `ibx_utility:melee`)。引用了未定义的分类时,该配方只会出现在「全部」标签中,并打印一条调试日志。 |
| `materials` | 是 | 材料列表,每项为 NeoForge 的 `{ "ingredient": <原版 ingredient>, "count": <数量> }`(`count` 默认 1)。`ingredient` 写物品 id(`"minecraft:diamond"`)、带 `#` 的标签(`"#minecraft:planks"`)或它们组成的数组。数量指**所需总数**,与摆放形状无关。<br>旧版的 `{ "item": <物品id> }` / `{ "tag": <标签id> }` 写法仍可读取,新数据包建议改用 `ingredient`。 |
| `result` | 是 | 产物,`{ "id": <物品id>, "count": <数量> }`。 |

> 材料按列表顺序无关紧要——界面会自动按稀有度、创造物品栏顺序重新排序显示。
> 标签类材料在界面上会每秒轮换展示其可选物品。

### 3.2 分类定义

左侧分类标签由数据包注册表驱动。

- **注册表**:`minecraft:category/equipment_forge`
- **位置**:`data/<命名空间>/category/equipment_forge/*.json`
- **分类 id**:即该文件的 ResourceLocation。例如 `data/ibx_utility/category/equipment_forge/melee.json` → `ibx_utility:melee`,配方通过这个 id 归类。

```json
{
  "name": { "translate": "category.ibx_utility.melee" },
  "order": 0,
  "icon": "minecraft:iron_sword"
}
```

| 字段 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| `name` | 是 | — | 标签显示名,完整的 Component(通常用 `{"translate": "..."}`)。 |
| `order` | 否 | `0` | 从上到下的顺序,越小越靠上。相同则按分类 id 字母序。 |
| `icon` | 否 | `minecraft:book` | 标签图标物品 id。可以指向可选模组的物品——该物品不存在时会回退(见下)。 |

**图标回退**:若 `icon` 指定的物品在注册表中不存在(如未安装对应模组),一律回退为书。

**「全部」标签是内置的**,永远置顶且不可通过数据包配置。

内置分类:

| id | order | 图标   |
| --- | --- |------|
| `ibx_utility:melee` | 0 | 铁剑   |
| `ibx_utility:ranged` | 1 | 弓    |
| `ibx_utility:magic` | 2 | 知识之书 |
| `ibx_utility:tool` | 3 | 钻石镐  |
| `ibx_utility:armor` | 4 | 黄金胸甲 |
| `ibx_utility:shield` | 5 | 盾牌   |
| `ibx_utility:curio` | 6 | 鞘翅   |

### 3.3 数据包条件

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
| `ibx_utility:daily_quest_category_enabled` | `enableDailyQuestCategory = true` 时 | 模组不带每日任务页,留给整合包自己加的那一页(见 [6.2](#62-分页)) |

> 条件不仅能用在配方上——NeoForge 给数据包注册表也接上了条件解析,所以分类定义(3.2)、悬赏板分页(6.2)和任务(6.3)同样支持 `neoforge:conditions`。

#### 用条件移除原版配方

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

### 3.4 制箭配方

- **位置**:`data/<命名空间>/recipe/**/*.json`(本模组放在 `recipe/fletching/` 下)
- **类型**:`ibx_utility:fletching`

```json
{
  "type": "ibx_utility:fletching",
  "tail": "minecraft:feather",
  "shaft": "#c:rods/wooden",
  "tip": "minecraft:flint",
  "result": { "id": "minecraft:arrow", "count": 8 }
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `tail` | 是 | 箭尾格材料,原版 ingredient 写法:物品 id、带 `#` 的标签或它们组成的数组。 |
| `shaft` | 是 | 箭身格材料,写法同上。 |
| `tip` | 是 | 箭头格材料,写法同上。 |
| `additive` | 否 | 药水格材料,写法同上。不写时,药水格必须为空才能合成。 |
| `copy_potion` | 否 | 默认 `false`。为 `true` 时把药水格物品的药水效果复制到产物上(同原版药箭配方 `crafting_imbue`)。 |
| `result` | 是 | 产物,`{ "id": <物品id>, "count": <数量> }`。 |

药箭的写法(内置配方即如此):

```json
{
  "type": "ibx_utility:fletching",
  "tail": "minecraft:feather",
  "shaft": "#c:rods/wooden",
  "tip": "minecraft:flint",
  "additive": "minecraft:lingering_potion",
  "copy_potion": true,
  "result": { "id": "minecraft:tipped_arrow", "count": 8 }
}
```

> 每次合成四格各消耗 1 个(药水整瓶消耗,不退空瓶)。各格只接受**某条配方里用在该位置**的材料,所以新增材料只需加配方,不必另配标签。

---

## 4. 战利品袋

战利品袋(`ibx_utility:loot_bag`)是一个"右键即开"的物品。它自己不含任何掉落内容——**袋子只是个指针**,指向一张原版战利品表,掉什么全由那张表说了算。

### 4.1 三个组成部分

一个能用的袋子由三样东西凑成,缺一不可:

| # | 东西 | 位置 | 例(铁傀儡袋) |
| --- | --- | --- | --- |
| 1 | **物品组件**,决定这是哪种袋子 | 物品自身 | `ibx_utility:loot_bag_type` = `"iron_golem"` |
| 2 | **战利品表**,决定掉什么 | 数据包 | `data/ibx_utility/loot_table/loot_bag/iron_golem.json` |
| 3 | **译文**,决定 tooltip 显示什么名字、什么颜色 | 资源包/模组语言文件 | `loot_bag.ibx_utility.iron_golem` = `"§f铁傀儡"` |

三者靠组件里那个**裸字符串**串起来。组件值 `"iron_golem"` 会被**固定**拼成:

- 战利品表 id → `ibx_utility:loot_bag/iron_golem`
- 翻译键 → `loot_bag.ibx_utility.iron_golem`

> 组件值**不能带命名空间,也不能带目录**(不能写 `mypack:xxx` 或 `boss/xxx`)。所有袋子的表都必须放在 `ibx_utility` 命名空间的 `loot_bag/` 目录下——但**任何数据包都可以往这个路径里塞文件**,不需要是本模组的 jar。

### 4.2 手把手:新建一个"末影龙袋"

**第一步**,建表文件 `data/ibx_utility/loot_table/loot_bag/ender_dragon.json`:

```json
{
  "type": "ibx_utility:loot_bag",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "minecraft:dragon_egg" }
      ]
    }
  ]
}
```

**第二步**,在语言文件 `assets/<任意命名空间>/lang/zh_cn.json` 里加译文(颜色用 § 格式码写在译文开头):

```json
{
  "loot_bag.ibx_utility.ender_dragon": "§5末影龙"
}
```

**第三步**,给予:

```
/give @s ibx_utility:loot_bag[ibx_utility:loot_bag_type="ender_dragon"] 1
```

改完数据包后 `/reload` 即可生效;新增译文需要重进世界或按 F3+T 重载资源。

### 4.3 组件与显示

| 组件 | 类型 | 说明 |
| --- | --- | --- |
| `ibx_utility:loot_bag_type` | 字符串 | 袋子种类。**这是袋子唯一的组件**。不写、写空串、或没有该组件 → 空袋:不可右键,tooltip 显示"空"。 |

**显示名与颜色一律来自语言文件**,没有任何自动推导:

- 查得到译文 → 原样显示,颜色由译文里的 `§` 格式码决定(不写颜色码就继承默认色)。
- **查不到译文 → tooltip 显示红色的组件原文**(如红色的 `ender_dragon`)。这是故意的报错提示,提醒你漏配了译文或者 type 拼错了。

> 因为颜色走 `§` 码,**只能用原版的 16 种颜色**,不支持 `#RRGGBB`。且每种语言的译文都要各自带上颜色码。
>
> "空"这个提示用的键是 `loot_bag.ibx_utility.null`——之所以不叫 `.empty`,是为了给一个名字真叫 `empty` 的袋子让路。

### 4.4 掉落物形态的特性

袋子作为掉落物(ItemEntity)躺在地上时:

| 特性 | 效果 |
| --- | --- |
| 防火 | 火焰、岩浆烧不掉 |
| 免伤 | 爆炸、仙人掌等一切伤害都无效 |
| 不消失 | 永远不会因为超时而消失 |
| 发光 | 常亮发光轮廓,隔墙可见 |

> **唯一的例外是虚空**:掉出世界底部的物品会被直接删除,这条路径绕过了所有伤害判定,拦不住。

### 4.5 给袋子换材质

不同种类的袋子可以长得不一样。这件事**由资源包决定,不需要改代码,也不需要数据包配合**——往下面这个目录丢一个以类型命名的模型文件即可:

```
assets/<命名空间>/models/item/loot_bag/<类型>.json
```

例如给铁傀儡袋换皮,建 `assets/ibx_utility/models/item/loot_bag/iron_golem.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "ibx_utility:item/loot_bag/iron_golem"
  }
}
```

没有对应文件的类型(以及空袋)一律用**回退模型**,也就是模组自带的这两个文件:

| 文件 | 作用 |
| --- | --- |
| `models/item/loot_bag.json` | 物品自身的模型。路径由物品注册名决定,**不能挪走**,内容只是一行 `parent` 指向下面那个 |
| `models/item/loot_bag/default.json` | 回退模型的实际定义,贴图为 `textures/item/loot_bag/default.png` |

> `default` 是**保留名**,不会被当作袋子类型。就算真有个叫 `default` 的袋子,它回退后用的也正是这个模型,结果一样。
>
> 类型名里**不能有斜杠**,`loot_bag/` 的子目录不会被扫描。

**为什么是资源包说了算,而不是数据包?** 袋子类型是数据包(服务端)定义的,而模型烘焙发生在客户端资源加载时——**远早于连上服务器**,那时客户端根本不知道有哪些类型。所以只能反过来:资源包提供了哪些类型的模型,就认哪些。

> 顺带一提:1.21.4 的[物品模型映射](https://zh.minecraft.wiki/w/物品模型映射)(`assets/<ns>/items/*.json` 的 `select` + `component`)原生就干这件事,纯 json 零代码。1.21.1 没有,故本模组自行实现了等效逻辑。

### 4.6 在 JEI 中查看掉落

装了 [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer)(JER)后,JEI 里会多出一个「战利品袋」分类,列出每个袋子可能掉什么、概率多少、几个到几个。在 JEI 里搜战利品袋这个物品即可看到全部袋子。

掉落项按**概率从大到小**排列;概率相同则按稀有度,再相同则按创造物品栏顺序。格子上的数字是**最小数量**,完整区间看 tooltip。

**两个必要条件:**

- **必须装 JER**(客户端)。原版没有任何公开 API 能枚举一张战利品表可能掉出什么,这套解析是 JER 提供的。没装 JER 则该分类整个不出现。
- **必须进入世界**。战利品表是纯服务端的,客户端手里没有;模组会在登录和 `/reload` 时把袋子表同步给客户端,JEI 才有东西可显示。

> ⚠️ **袋子内容会同步给所有客户端,与装没装 JER 无关。** 服务端无从得知客户端装了什么,所以一律下发;JER 只决定"显示不显示",数据本身对客户端始终可见。**这是本模组启用 JEI 展示后的既定代价**——不装 JER 只是看不到,不等于藏住了。
>
> ⚠️ **显示的是近似值**。权重、条件、嵌套表、`set_count` 之类的函数共同决定实际掉落,任何静态预览都做不到完全准确——带条件的条目尤其容易显示得不准。

---

## 5. 战利品表详解

本章是袋子里 `pools` 的手把手用法。这就是**原版战利品表**,和箱子、怪物掉落用的是同一套格式——已经熟悉原版战利品表的话,只需直接跳到 [5.7 上下文参数与限制](#57-上下文参数与限制),那里是唯一的不同之处。

### 5.1 表的骨架

```json
{
  "type": "ibx_utility:loot_bag",
  "pools": [ ... ],
  "functions": [ ... ],
  "random_sequence": "ibx_utility:loot_bag/iron_golem"
}
```

| 字段 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| `type` | 否 | `minecraft:generic` | 参数集。**袋子的表必须写 `ibx_utility:loot_bag`**,原因见 [5.7](#57-上下文参数与限制)。 |
| `pools` | 否 | `[]` | 池列表。不写就是个空表(袋子能开,但什么都不给)。 |
| `functions` | 否 | `[]` | **表级函数**,作用于本表掉出的**每一件**物品。 |
| `random_sequence` | 否 | — | 随机序列 id,用于让掉落结果在同一存档内可复现。一般填表自己的 id 即可,不填也无妨。 |

> `type` 写错了**不会报错**,详见 [5.7](#57-上下文参数与限制) 末尾的警告。

### 5.2 核心概念:池 = 一次独立的抽奖

这是整个战利品表最容易误解的地方,先记住三句话:

1. **每个 pool 相互独立**,依次结算,结果**累加**。
2. **一个 pool 的一次 roll,只产出一个条目**——`entries` 是"候选池",不是"清单"。
3. 想让袋子"必给 A,再随机给 B",就写**两个 pool**,而不是把 A、B 塞进同一个 pool 的 entries。

铁傀儡袋正是这么写的:第一个 pool 只有铁锭一个候选(所以必给),第二个 pool 在虞美人和铁块之间按权重二选一。

```
pool[0]: rolls=1, entries=[铁锭]              → 必定给 3-5 个铁锭
pool[1]: rolls=1, entries=[虞美人(8), 铁块(1)] → 8/9 概率给虞美人, 1/9 概率给铁块
                                                 合计:每次开袋恰好 2 件物品
```

### 5.3 pool 的字段

| 字段 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| `entries` | **是** | — | 候选条目列表。 |
| `rolls` | **是** | — | 抽取次数。可以是数字,也可以是数值提供器(见 [5.4](#54-rolls-抽几次))。 |
| `bonus_rolls` | 否 | `0` | 幸运加成的额外抽取次数,会乘以玩家幸运值。 |
| `conditions` | 否 | `[]` | **整个池**的开关。条件不通过则本池完全跳过。 |
| `functions` | 否 | `[]` | 作用于**本池**掉出的每件物品。 |

实际抽取次数的公式是:

```
次数 = rolls + floor(bonus_rolls × 玩家幸运值)
```

幸运值来自玩家的 luck 属性(幸运药水等)。普通情况下为 0,于是 `bonus_rolls` 不起作用。

### 5.4 rolls 抽几次

直接写数字就是固定次数。要随机次数则用**数值提供器**:

```json
"rolls": 3                                                  // 固定 3 次
"rolls": { "type": "minecraft:uniform", "min": 1, "max": 3 } // 1~3 次,闭区间,均匀
"rolls": { "type": "minecraft:binomial", "n": 3, "p": 0.5 }  // 抛 3 次硬币,每次 50% 命中,结果 0~3
```

| 提供器 | 说明 |
| --- | --- |
| `minecraft:constant` | `{"type":"minecraft:constant","value":2}`,等价于直接写 `2` |
| `minecraft:uniform` | `min`~`max` 闭区间均匀随机 |
| `minecraft:binomial` | 二项分布,`n` 次尝试每次 `p` 概率。适合"大概率少、小概率多"的钟形分布 |
| `minecraft:score` | 读记分板。**本模组只能用 `"target": "this"`**(开袋玩家) |
| `minecraft:storage` | 读命令存储 |
| `minecraft:enchantment_level` | **本模组不可用**(需要附魔等级参数) |

> 同样的写法也适用于 `set_count` 等函数里的 `count` 字段。

### 5.5 entries 抽什么

| `type` | 说明 |
| --- | --- |
| `minecraft:item` | 给一件物品。`name` 填物品 id。**最常用**。 |
| `minecraft:tag` | 物品标签。`name` 填标签 id,`expand` 决定行为(见下)。 |
| `minecraft:loot_table` | 嵌套另一张表。`value` 填表 id,或直接内联一张表。适合复用公共掉落。 |
| `minecraft:empty` | 空条目,什么都不给。用来给池子掺"空奖"。 |
| `minecraft:group` | 子条目**全部**执行。 |
| `minecraft:alternatives` | 按顺序取**第一个**条件通过的子条目(类似 if / else if)。 |
| `minecraft:sequence` | 按顺序执行,直到某个子条目条件失败为止。 |
| `minecraft:dynamic` | 方块实体专用,**本模组用不了**。 |

**每个条目的通用字段:**

| 字段 | 默认 | 说明 |
| --- | --- | --- |
| `weight` | `1` | 权重。同池内被抽中的概率 = 自身 weight ÷ 池内所有条目 weight 之和。 |
| `quality` | `0` | 幸运修正。实际权重 = `max(floor(weight + quality × 幸运值), 0)`。正数表示"越幸运越容易出"。 |
| `conditions` | `[]` | 本条目的开关。不通过则该条目**不参与本次抽取**。 |
| `functions` | `[]` | 只作用于本条目产出的物品。 |

**`minecraft:tag` 的 `expand`** 值得单独说明:

- `"expand": true` → 标签里的**每个物品各自成为一个条目**,各自参与权重竞争(即"从标签里随机挑一个")。
- `"expand": false` → **一次性给出标签里的全部物品**(即"标签里的都给你")。

**用 `empty` 掺空奖**是很常用的技巧:

```json
{
  "rolls": 1,
  "entries": [
    { "type": "minecraft:item", "name": "minecraft:diamond", "weight": 1 },
    { "type": "minecraft:empty", "weight": 9 }
  ]
}
```

这个池有 10% 概率给一颗钻石,90% 什么都不给。

> 想要"90% 概率给钻石",除了用 `empty` 配权重,也可以给条目挂 `minecraft:random_chance` 条件——但两者语义不同:权重是**互相竞争**,条件是**各自独立判定**。

### 5.6 functions 给掉出来的东西加工

函数可以挂在三个层级:**表级**(每件物品)、**池级**(本池的每件物品)、**条目级**(仅该条目)。以下在本模组中都可用:

| 函数 | 用途 |
| --- | --- |
| `minecraft:set_count` | 设置数量。`count` 支持数值提供器;`"add": true` 表示在原数量上累加而非覆盖 |
| `minecraft:limit_count` | 把数量夹在 `{"min":x,"max":y}` 区间内 |
| `minecraft:set_components` | 设置任意物品组件(1.20.5+ 取代了旧的 `set_nbt`) |
| `minecraft:set_damage` | 设置耐久。注意 `damage` 是**剩余耐久比例**而非损耗比例——`0.9` 表示九成新,`0.1` 表示快断了。这是原版的反直觉设计 |
| `minecraft:enchant_randomly` | 随机附魔。可用 `options` 限定附魔池 |
| `minecraft:enchant_with_levels` | 按等级附魔,等同附魔台的效果 |
| `minecraft:set_enchantments` | 精确指定附魔与等级 |
| `minecraft:set_attributes` | 添加属性修饰符 |
| `minecraft:set_name` / `set_lore` | 设置名字/描述。**带 `entity` 字段时只能填 `"this"`** |
| `minecraft:set_potion` | 设置药水类型 |
| `minecraft:set_custom_data` | 设置自定义 NBT 数据 |
| `minecraft:furnace_smelt` | 把产物换成其熔炼结果 |
| `minecraft:exploration_map` | 生成探险地图(只需开袋位置,可用) |
| `minecraft:filtered` / `sequence` / `reference` | 组合与复用函数 |

一个"随机数量 + 随机附魔"的条目长这样:

```json
{
  "type": "minecraft:item",
  "name": "minecraft:diamond_sword",
  "functions": [
    {
      "function": "minecraft:enchant_with_levels",
      "levels": { "type": "minecraft:uniform", "min": 20, "max": 30 }
    },
    {
      "function": "minecraft:set_components",
      "components": { "minecraft:rarity": "epic" }
    }
  ]
}
```

> 注意函数字段名是 `"function"`,条件字段名是 `"condition"`,而条目字段名是 `"type"`——三者不一样,这是最常见的手滑点。

### 5.7 上下文参数与限制

这是袋子的表**唯一**区别于普通战利品表的地方,也是最需要注意的一节。

战利品表运行时能读到的信息由**参数集**(即表的 `type` 字段)决定。箱子表能读到箱子位置,怪物掉落表能读到凶手是谁、用的什么武器。袋子是玩家右键打开的,所以模组注册了一个专属参数集 `ibx_utility:loot_bag`,它只提供两个参数:

| 参数 | 内容 |
| --- | --- |
| `minecraft:origin` | **开袋位置**(玩家当时的坐标) |
| `minecraft:this_entity` | **开袋的玩家** |

其它参数(凶手、武器、方块、伤害来源、爆炸半径……)**一概没有**,因为开袋这个场景里根本不存在这些东西。

**用了读不到的参数会怎样?** 不会崩,但会**静默失效**——这正是它危险的地方:

| 情形 | 后果 |
| --- | --- |
| 条件读不到参数 | 条件恒为 **false** → 该条目**永远不掉**,或该池永远跳过 |
| 函数读不到参数 | 函数变成**空操作** → 悄悄地什么都没做 |
| 加载时 | 日志里有一条 **WARN**:`Parameters [...] are not provided in this context`。**表照常加载**,不会作废 |

> 只有 `minecraft:enchantment_active_check` 是例外,它会真的抛异常。
>
> **所以:袋子不掉东西、或者函数不生效时,第一件事是去服务端启动日志里搜 `are not provided in this context`。**

**不可用清单**——以下条件/函数依赖袋子拿不到的参数,写了等于没写:

| 条件 | 它需要的参数 |
| --- | --- |
| `minecraft:killed_by_player` | 最后伤害玩家 |
| `minecraft:damage_source_properties` | 伤害来源 |
| `minecraft:match_tool` | 工具 |
| `minecraft:table_bonus` | 工具 |
| `minecraft:block_state_property` | 方块状态 |
| `minecraft:survives_explosion` | 爆炸半径 |
| `minecraft:random_chance_with_enchanted_bonus` | 攻击者 |
| `minecraft:enchantment_active_check` | 附魔激活状态(**这个会抛异常**) |

| 函数 | 它需要的参数 |
| --- | --- |
| `minecraft:apply_bonus` | 工具(时运加成,袋子里没意义) |
| `minecraft:enchanted_count_increase` | 攻击者(抢夺加成,袋子里没意义) |
| `minecraft:copy_state` | 方块状态 |
| `minecraft:copy_components` | 方块实体 |
| `minecraft:explosion_decay` | 爆炸半径 |

**半可用**——凡是带实体指向的地方,**只能指向 `this`(开袋玩家)**,填 `attacker` / `attacking_player` / `direct_attacker` 都会静默失效:

| 条件/函数 | 可用写法 |
| --- | --- |
| `minecraft:entity_properties` | `"entity": "this"` |
| `minecraft:entity_scores` | `"entity": "this"` |
| `minecraft:copy_name` | `"source": "this"` |
| `minecraft:set_name` / `set_lore` | `"entity": "this"`,或干脆不写 `entity` |
| `minecraft:fill_player_head` | `"entity": "this"` |
| `minecraft:score`(数值提供器) | `"target": "this"` |

**放心用的**(不依赖任何参数,或只需开袋位置/开袋玩家):

`minecraft:random_chance`、`minecraft:value_check`、`minecraft:time_check`、`minecraft:weather_check`、`minecraft:location_check`(检查开袋位置)、`minecraft:inverted`、`minecraft:any_of`、`minecraft:all_of`、`minecraft:reference`,以及 [5.6](#56-functions-给掉出来的东西加工) 表格里列出的全部函数。

> ⚠️ **`type` 写错不会报错。** 如果把 `"type"` 拼成了 `ibx_utility:lootbag` 之类,原版**不会**报任何错,而是静默退回 `minecraft:generic` 参数集——那个集合声称"什么参数都有",于是上面的 WARN 也不会出现,袋子却可能在开的瞬间抛异常。**表不掉东西时,先检查 `type` 有没有拼对。**

### 5.8 完整示例:铁傀儡袋

把前面几节拼起来,一个"铁傀儡袋"的完整表 `data/ibx_utility/loot_table/loot_bag/iron_golem.json` 长这样:

```json
{
  "type": "ibx_utility:loot_bag",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:iron_ingot",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": { "type": "minecraft:uniform", "min": 3, "max": 5 }
            }
          ]
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:poppy",
          "weight": 8,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": { "type": "minecraft:uniform", "min": 1, "max": 2 }
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:iron_block",
          "weight": 1
        }
      ]
    }
  ]
}
```

逐段拆解:

- **第一个池**:`rolls: 1`、只有一个候选 → **必定**掉落,`set_count` 把数量随机成 3~5 个铁锭。
- **第二个池**:`rolls: 1`、两个候选,权重 8 : 1 → 8/9 概率掉 1~2 朵虞美人,1/9 概率掉 1 个铁块。
- **合计**:每次开袋**恰好 2 件**物品(每个池各出 1 件)。

配套的译文(`§f` = 白色):

```json
"loot_bag.ibx_utility.iron_golem": "§f铁傀儡"
```

给予指令:

```
/give @s ibx_utility:loot_bag[ibx_utility:loot_bag_type="iron_golem"] 1
```

> 想在正式加袋子前先验证一张表掉什么,不必反复开袋——用原版命令直接掷它:
> ```
> /loot spawn ~ ~ ~ loot ibx_utility:loot_bag/iron_golem
> ```

> 上面的铁傀儡袋只是**文档示例**。**模组本体不带任何成品袋子**——袋子该有哪些、掉什么、长什么样,完全交给整合包决定。模组只提供物品、参数集与那份回退模型。

---

## 6. 悬赏板与任务

### 6.1 玩法

1. **接任务**:右键悬赏板,左边的丝带书签是各个分页,右边一条是自带的「已承接任务」。选中任务点「接受」。要接得下:前置条件满足、一次性任务没做过、随机任务在今天的名单里、已接的没到上限(`maxActiveQuests`)。
2. **做目标**:
   - **击杀**:实时计数,算在生物「最后被哪个玩家打」的那个玩家头上——玩家射出的弹射物、驯服的宠物都算主人。接任务之前杀的不算,目标达成后多杀的也不算。
   - **提交物品**:回悬赏板点「提交」时从背包收走,可以分几次交。
   - **收集物品**:提交那一刻数背包,够了就行,不收走。
3. **提交**:目标全部达成后在悬赏板上点「提交」,任务完成、发奖励,背包放不下的掉在脚下。
4. **放弃**:随时可以在悬赏板上放弃,进度清零,没有惩罚。
5. **组队**:写了 `team` 的任务能组队。
   - 接下就是开了一支队伍,别人在悬赏板上能看到并加入,人数到 `max` 为止。
   - 进度全队共用,同一只怪只算一次;收集物品数提交人的背包。
   - 谁都可以提交。提交时在线的队员每人一份奖励、各记一次完成;不在线的没有奖励,上线后补记完成。
   - 发起人退队或下线时,换成最早加入的队员。
   - 做过的一次性任务也能加入别人的队伍,照样拿奖励、完成次数加一。
6. **每日随机**:`pool` 为 `random` 的任务进随机池。每个游戏日(游戏总时长每 24000 tick,睡觉不会提前)全服按权重抽 `dailyQuestCount` 条,不重复。已经接了的随机任务过了一天照样能做完。
7. **提示**:进度只在屏幕左上角的追踪栏里显示(默认关闭,见 [2](#2-配置)),不开追踪栏时在悬赏板的「已承接任务」页看。在悬赏板上操作只有声音:接受任务、加入队伍成功响一声落笔声,没做成(太远、到上限、目标没达成等)响一声低音,都只有自己听得到;界面上不显示结果文字,也没有弹窗和聊天。

> 接、加入、提交、放弃都要站在刚才打开的那块悬赏板 8 格以内。

### 6.2 分页

悬赏板左边的书签由数据包注册表驱动,写法和锻造台分类(3.2)一样。

- **注册表**:`minecraft:category/quest_board`
- **位置**:`data/<命名空间>/category/quest_board/*.json`
- **分页 id**:即该文件的 ResourceLocation,任务用它写自己在哪页。
- **改了要重进世界**(服务器要重启):分页是数据包注册表,`/reload` 不会重读。

```json
{
  "name": { "translate": "category.example.hunting" },
  "order": 0,
  "icon": "minecraft:iron_sword"
}
```

| 字段 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| `name` | 是 | — | 书签名字(指上去显示),完整的 Component。 |
| `order` | 否 | `0` | 从上到下的顺序,越小越靠上。相同则按分页 id 字母序。 |
| `icon` | 否 | `minecraft:book` | 书签上的图标物品 id;物品不存在时回退为书。 |

内置分页(每页都能在配置里关掉,见 [2](#2-配置)):

| id | order | 图标 |
| --- | --- | --- |
| `ibx_utility:key_quests` | 0 | 书与笔 |
| `ibx_utility:free_quests` | 1 | 地图 |

- 「已承接任务」是自带的,永远是书右边那一条,不能通过数据包配置。
- 书签颜色按顺序轮换:红、蓝、绿、紫、青、橙。

想要「每日任务」页:模组不带,自己加一份分页,挂上条件 `ibx_utility:daily_quest_category_enabled`(就受配置开关 `enableDailyQuestCategory` 控制),译名可以直接用模组带的 `category.ibx_utility.daily_quests`:

```json
{
  "neoforge:conditions": [ { "type": "ibx_utility:daily_quest_category_enabled" } ],
  "name": { "translate": "category.ibx_utility.daily_quests" },
  "order": 2,
  "icon": "minecraft:clock"
}
```

随机池的任务(`"pool": "random"`)不一定要放在这一页,写在哪页就在哪页出现。

### 6.3 任务

- **注册表**:`ibx_utility:quest`
- **位置**:`data/<命名空间>/ibx_utility/quest/*.json`
- **任务 id**:即该文件的 ResourceLocation。例如 `data/example/ibx_utility/quest/wolf_pack.json` → `example:wolf_pack`。
- **改了要重进世界**(服务器要重启):任务也是数据包注册表,`/reload` 不会重读。

所有字段都写上的样子:

```json
{
  "name": { "translate": "quest.example.wolf_pack" },
  "description": { "translate": "quest.example.wolf_pack.desc" },
  "icon": "minecraft:bone",
  "category": "ibx_utility:free_quests",
  "pool": "fixed",
  "weight": 1,
  "requirements": [
    { "type": "ibx_utility:quest_completed", "quest": "example:first_steps" },
    { "type": "ibx_utility:advancement", "advancement": "minecraft:story/mine_stone" }
  ],
  "repeatable": true,
  "team": { "max": 3 },
  "objectives": [
    { "type": "ibx_utility:kill", "entity": "minecraft:wolf", "count": 4 },
    { "type": "ibx_utility:submit_item", "item": "minecraft:bone", "count": 6 },
    { "type": "ibx_utility:collect_item", "item": "#minecraft:logs", "count": 16 }
  ],
  "rewards": [
    { "type": "ibx_utility:item", "item": { "id": "minecraft:emerald", "count": 3 } },
    { "type": "ibx_utility:loot_table", "table": "minecraft:chests/simple_dungeon" },
    { "type": "ibx_utility:experience", "levels": 2, "points": 30 },
    { "type": "ibx_utility:command", "command": "say 狼群清掉了" }
  ]
}
```

| 字段 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| `name` | 是 | — | 任务名。直接写字符串,或 `{ "translate": "..." }` 走语言文件。 |
| `description` | 否 | 无 | 说明,写法同 `name`。界面放不下时少显示几行。 |
| `icon` | 否 | `minecraft:book` | 目录和追踪栏里的图标物品 id;物品不存在时回退为书。 |
| `category` | **是** | — | 在哪一页(分页 id)。**那页不存在(写错了、或配置里关掉了)时整个任务跳过**:不显示、不能接、不进每日抽取,已接的玩家下次上线时清掉。 |
| `pool` | 否 | `fixed` | `fixed` 一直挂在板上;`random` 进每日随机池,抽中当天才出现。 |
| `weight` | 否 | `1` | 在随机池里被抽中的权重,只对 `random` 有用。 |
| `requirements` | 否 | 无 | 前置条件,全部满足才显示、才能接或加入队伍(见 [6.4](#64-目标奖励与前置条件))。 |
| `repeatable` | 否 | `false` | `false` 只能做一次;`true` 可以反复做。 |
| `team` | 否 | 无 | 写了就能组队;`max` 是队伍人数上限,默认 4,最少 2。 |
| `objectives` | **是** | — | 目标,至少一个(见 [6.4](#64-目标奖励与前置条件))。 |
| `rewards` | 否 | 无 | 奖励(见 [6.4](#64-目标奖励与前置条件))。 |
| `actions` | 否 | 无 | `accept` / `submit` / `abandon` 三个时机各执行一串任务动作。**本模组不带动作类型**,由别的模组注册(见 [6.5](#65-给开发者的接口与事件))。 |

最简单的任务只要必填的三项:

```json
{
  "name": "柴火",
  "category": "ibx_utility:free_quests",
  "objectives": [ { "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16 } ]
}
```

> **模组本体不带任何任务**,只带两个默认分页。任务该有哪些、怎么排,完全交给整合包。

### 6.4 目标、奖励与前置条件

`item` / `entity` 都可以写单个 id、带 `#` 的标签,或它们组成的数组。

- **名字**:标签用标签的译名(没有译名就显示 `#id`,比如 `#minecraft:logs`),数组用第一个的名字。三种目标都可以加可选的 `name` 自己定显示名(字符串或 `{ "translate": "..." }`),写了就代替自动取的名字:`{ "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16, "name": "原木" }`。
- **图标**:标签、数组里的物品(生物是有刷怪蛋的那些)每秒轮换一个,悬赏板和追踪栏都一样(同装备锻造台的标签材料)。

**目标**(`objectives`):

| type | 字段 | 说明 |
| --- | --- | --- |
| `ibx_utility:kill` | `entity`,`count`(默认 1),`name`(可选) | 击杀生物。目标图标是它的刷怪蛋(没有就不画)。 |
| `ibx_utility:submit_item` | `item`,`count`(默认 1),`name`(可选) | 在悬赏板上把物品交上去,会被收走,可以分几次交。 |
| `ibx_utility:collect_item` | `item`,`count`(默认 1),`name`(可选) | 提交那一刻背包里有就行,不收走。追踪栏实时数背包。 |

**奖励**(`rewards`):

| type | 字段 | 说明 |
| --- | --- | --- |
| `ibx_utility:item` | `item`:物品 id,或 `{ "id": ..., "count": ... }` | 放进背包,放不下的掉在脚下。 |
| `ibx_utility:loot_table` | `table`:战利品表 id | 按玩家的幸运值抽一次,界面上显示为一个箱子。 |
| `ibx_utility:experience` | `levels`、`points`,至少写一个 | 给经验等级 / 经验点。 |
| `ibx_utility:command` | `command` | 以完成任务的玩家身份、2 级权限执行,不显示输出;开头的 `/` 写不写都行。界面上不显示。 |

**前置条件**(`requirements`):

| type | 字段 | 说明 |
| --- | --- | --- |
| `ibx_utility:quest_completed` | `quest`:任务 id | 完成过这个任务。 |
| `ibx_utility:advancement` | `advancement`:进度 id | 拿到了这个进度。 |

### 6.5 给开发者的接口与事件

**`ibx.utility.api.QuestApi`**:读的方法两端都能用(客户端只有自己的数据),改的方法只在服务端用。

| 方法 | 说明 |
| --- | --- |
| `activeQuests(player)` | 已接的任务,按接下的先后。 |
| `isActive(player, quest)` / `completions(player, quest)` | 接没接 / 完成过几次。 |
| `progress(player, quest, objective)` | 第 `objective` 个目标(从 0 数)存下的进度;组队任务是全队那份。 |
| `addProgress(player, quest, objective, amount)` | 给某个目标加进度(封顶,组队的加到队伍里),比如判定「Boss 打赢了」。返回实际涨了多少。 |
| `addProgress(player, objective -> amount)` | 给玩家全部已接任务里的目标加进度,按目标算该加多少,适合自己注册的目标类型。 |
| `abandon(player, quest)` | 让玩家放弃一个任务,同在悬赏板上放弃。 |

**事件** `ibx.utility.api.event.QuestEvent`,只在服务端、发在 `NeoForge.EVENT_BUS` 上,都带 `getPlayer()` 和 `getQuest()`:

| 事件 | 可取消 | 时机 |
| --- | --- | --- |
| `QuestEvent.Accept` | 是 | 接任务:条件都检查过、还没写进玩家数据。 |
| `QuestEvent.Join` | 是 | 加入别人的队伍,`getTeam()` 是队伍 id。 |
| `QuestEvent.Progress` | 否 | 某个目标的进度涨了:`getObjective()`、`getOldProgress()`、`getNewProgress()`、`getTeam()`(个人任务为 null)。收集物品类目标不存进度,不发。 |
| `QuestEvent.Submit` | 是 | 目标全部达成、还没发奖励。取消后任务留着,这次交上去的物品照样算进度。 |
| `QuestEvent.Abandon` | 否 | 放弃,进度还没清。 |

取消后,玩家在悬赏板上只会听到一声失败的低音。

**注册新类型**:目标、奖励、前置条件、任务动作各有一个注册表,键在 `ibx.utility.registry.QuestTypeRegistries`。注册的值是类型的 `MapCodec`,任务 JSON 里按 `"type"` 写上就能用:

| 注册表 | 实现的接口 |
| --- | --- |
| `ibx_utility:quest_objective_type`(`OBJECTIVE_TYPE_KEY`) | `QuestObjective`:`onKill` / `onSubmit` 决定怎么涨进度,`isComplete` 决定算不算达成,`display*` 决定界面上怎么显示 |
| `ibx_utility:quest_reward_type`(`REWARD_TYPE_KEY`) | `QuestReward`:`grant` 发奖励,`displayIcon` 等决定界面上怎么显示 |
| `ibx_utility:quest_requirement_type`(`REQUIREMENT_TYPE_KEY`) | `QuestRequirement`:`test` |
| `ibx_utility:quest_action_type`(`ACTION_TYPE_KEY`) | `QuestAction`:`run`,在接 / 提交 / 放弃时执行 |

```java
public static final DeferredRegister<MapCodec<? extends QuestObjective>> OBJECTIVES =
        DeferredRegister.create(QuestTypeRegistries.OBJECTIVE_TYPE_KEY, "examplemod");
public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<DefeatBossObjective>> DEFEAT_BOSS =
        OBJECTIVES.register("defeat_boss", () -> DefeatBossObjective.CODEC);

// Boss 被打败时
QuestApi.addProgress(player, objective -> objective instanceof DefeatBossObjective o && o.boss().equals(bossId) ? 1 : 0);
```

---

## 7. 许可证

本模组以 [MIT 许可证](LICENSE)开源。你可以自由地使用、修改、分发本模组及其源码,或将其打包进整合包(含商业整合包),只需保留版权与许可声明。

本模组通过 jarJar 打包了第三方库 **pinyin4j**(BSD 许可)。其版权声明见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。
