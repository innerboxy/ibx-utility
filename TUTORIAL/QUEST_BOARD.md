# 悬赏板与任务

**简体中文** | [English](QUEST_BOARD_en.md)

[← 返回首页](../README.md)

---

## 1. 简介

**悬赏板**(`ibx_utility:quest_board`)是一块宽 2 格、高 2 格的木板,右键打开账本式的任务界面,接任务、交任务都在这里。任务完全由数据包定义(见 [4](#4-任务))。

- **三种目标**:提交物品、击杀生物、收集物品;奖励可以是物品、战利品表、经验或指令。
- **固定任务 + 每日随机**:固定任务一直挂着;随机池的任务每个游戏日全服抽几条(默认 5 条)。
- **回板子提交**:目标达成后回到悬赏板点「提交」才算完成,背包放不下的奖励掉在脚下。
- **可以组队**:任务写了 `team` 就能组队,进度全队共用,提交时在线的队员每人一份奖励。
- **准备和出发**:任务写了 `depart` 动作就是要出发的任务,队员点「准备」、发起人点「出发」一起走,去哪由别的模组决定(见 [2](#2-玩法) 第 6 条)。
- **前置条件**:前置没满足的任务直接不显示。
- **追踪栏**:屏幕左上角列出已接的任务和进度,四种样式可选,默认不显示(见 [配置 2](CONFIG.md#2-客户端配置))。
- **能扩展**:别的模组可以注册新的目标、奖励、前置条件、任务动作类型,也能监听接 / 加入 / 出发 / 进度 / 提交 / 放弃事件(见 [6](#6-给开发者的接口与事件))。

### 获取方式

悬赏板在**原版工作台**中合成:

```
板 板 板          板 = 任意木板
纸 书 纸          书 = 书与笔
棍    棍          棍 = 木棍
```

---

## 2. 玩法

1. **接任务**:右键悬赏板,左边的丝带书签是各个分页,右边一条是自带的「已承接任务」。选中任务点「接受」。要接得下:前置条件满足、一次性任务没做过、随机任务在今天的名单里、已接的没到上限(`maxActiveQuests`)。
2. **做目标**:
   - **击杀**:实时计数,算在生物「最后被哪个玩家打」的那个玩家头上——玩家射出的弹射物、驯服的宠物都算主人。接任务之前杀的不算,目标达成后多杀的也不算。
   - **提交物品**:回悬赏板点「提交」时从背包收走,可以分几次交。
   - **收集物品**:提交那一刻数背包,够了就行,不收走。
3. **提交**:目标全部达成后在悬赏板上点「提交」,任务完成、发奖励,背包放不下的掉在脚下。
4. **放弃**:随时可以在悬赏板上放弃,进度清零,没有惩罚。
5. **组队**:写了 `team` 的任务能组队。
   - 接下就是开了一支队伍,别人在悬赏板上能看到并加入,人数到 `max` 为止。没接的组队任务,能加入的队伍列在右页的最后一页,各带一个「加入」。
   - 进度全队共用,同一只怪只算一次;收集物品数提交人的背包。
   - 谁都可以提交。提交时在线的队员每人一份奖励、各记一次完成;不在线的没有奖励,上线后补记完成。
   - 发起人退队或下线时,换成最早加入、还在线的队员;队里没人在线就先不换,等下一个上线的队员接过来。
   - 自己的队伍在右页的最后一页(翻页的金箭头在蜡封按钮上面):一人一行,名字前一个方框,发起人的方框是填实的。
   - 发起人可以把别的队员移出队伍:队伍那一页里,队员那一行行尾有个小叉,点一下变成红字的「确认移除?」,再点一次才移出,鼠标移开就取消。被移出的人等同自己放弃(不在线的等他上线时再算);出发了还没回来的人也能移,移出后会被送回重生点。
   - 做过的一次性任务也能加入别人的队伍,照样拿奖励、完成次数加一。
6. **准备和出发**:`actions` 里写了 `depart` 的任务是**要出发的任务**,接下后在目标达成前多一个「出发 / 准备」蜡封:组队任务在队伍那一页的名单下面,个人任务在右页内容的最后。右下角一直是「提交」。
   - **开局**:组队任务的队员点「准备」(再点一次「取消准备」),准备好的人名字前的方框打勾;点了就算,走开也算。发起人点「出发」,带上自己和准备好、在线的队员一起走。个人任务没有准备,自己点「出发」。
   - **进行中**:有人出发了还没回来时,右页最上面写「进行中」和在外面的人,队伍那一页里他们的行尾也写「进行中」。队里没跟着走的(没准备的、后加入的、回来了的)自己点「出发」进去。
   - **出发以后**:走成了的人,开着的悬赏板界面会自己关掉。`depart` 动作可以说「没走成」(比如场地放不下),那个人就不算出发,悬赏板上听到一声失败的低音,可以再点。
   - **召回**:人还在外面,却已经不在做这个任务了(被发起人移出队伍,或者在外面时放弃),会被送回重生点——床或重生锚,没有就是世界出生点,不耗重生锚的充能。别的模组可以接管,改成送去别处(见 [6](#6-给开发者的接口与事件) 的 `QuestEvent.Recall`)。
   - **回来**:人去哪、什么时候算回来由挂 `depart` 动作的模组决定;下线的人自动算回来。一组人都回来了,下次又要发起人重新开局。准备和出发的名单不存档,重启或退出存档后都清空。
7. **每日随机**:`pool` 为 `random` 的任务进随机池。每个游戏日(游戏总时长每 24000 tick,睡觉不会提前)全服按权重抽 `dailyQuestCount` 条,不重复。已经接了的随机任务过了一天照样能做完。
8. **提示**:进度只在屏幕左上角的追踪栏里显示(默认关闭,见 [配置 2](CONFIG.md#2-客户端配置)),不开追踪栏时在悬赏板的「已承接任务」页看。在悬赏板上操作只有声音:接受任务、加入队伍、点准备成功响一声落笔声,没做成(太远、到上限、目标没达成、没走成等)响一声低音,都只有自己听得到;界面上不显示结果文字,也没有弹窗和聊天。开着悬赏板时,队友加入、退出、准备、出发、提交,界面都会自己刷新。

> 接、加入、准备、出发、提交、放弃、移出队伍都要站在刚才打开的那块悬赏板 8 格以内。

---

## 3. 分页

悬赏板左边的书签由数据包注册表驱动,写法和锻造台分类([装备锻造台 3](FORGE.md#3-分类定义))一样。

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
| `icon` | 否 | `minecraft:book` | 书签上的图标。写物品 id,物品不存在时回退为书;也可以写图片路径(以 `.png` 结尾,写法同任务的 `icon`,见 [4](#4-任务))。 |

内置分页(每页都能在配置里关掉,见 [配置](CONFIG.md)):

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

---

## 4. 任务

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
| `icon` | 否 | `minecraft:book` | 目录、右页标题和追踪栏里的图标。写物品 id,物品不存在时回退为书;也可以写一张图片的路径(以 `.png` 结尾的完整资源路径,如 `"example:textures/quest/wolf.png"`,对应资源包里的 `assets/example/textures/quest/wolf.png`),整张图缩到 16×16 来画,图片不存在时显示紫黑格。 |
| `category` | **是** | — | 在哪一页(分页 id)。**那页不存在(写错了、或配置里关掉了)时整个任务跳过**:不显示、不能接、不进每日抽取,已接的玩家下次上线时清掉。 |
| `pool` | 否 | `fixed` | `fixed` 一直挂在板上;`random` 进每日随机池,抽中当天才出现。 |
| `weight` | 否 | `1` | 在随机池里被抽中的权重,只对 `random` 有用。 |
| `requirements` | 否 | 无 | 前置条件,全部满足才显示、才能接或加入队伍(见 [5](#5-目标奖励与前置条件))。 |
| `repeatable` | 否 | `false` | `false` 只能做一次;`true` 可以反复做。 |
| `team` | 否 | 无 | 写了就能组队;`max` 是队伍人数上限,默认 4,最少 2。 |
| `objectives` | **是** | — | 目标,至少一个(见 [5](#5-目标奖励与前置条件))。 |
| `rewards` | 否 | 无 | 奖励(见 [5](#5-目标奖励与前置条件))。 |
| `actions` | 否 | 无 | `accept` / `depart` / `submit` / `abandon` 四个时机各执行一串任务动作;写了 `depart` 就是要出发的任务(见 [2](#2-玩法) 第 6 条),出发时对每个走的人执行。**本模组不带动作类型**,由别的模组注册(见 [6](#6-给开发者的接口与事件))。 |

最简单的任务只要必填的三项:

```json
{
  "name": "柴火",
  "category": "ibx_utility:free_quests",
  "objectives": [ { "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16 } ]
}
```

> **模组本体不带任何任务**,只带两个默认分页。任务该有哪些、怎么排,完全交给整合包。

---

## 5. 目标、奖励与前置条件

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

---

## 6. 给开发者的接口与事件

**`ibx.utility.api.QuestApi`**:读的方法两端都能用(客户端只有自己的数据),改的方法只在服务端用。

| 方法 | 说明 |
| --- | --- |
| `activeQuests(player)` | 已接的任务,按接下的先后。 |
| `isActive(player, quest)` / `completions(player, quest)` | 接没接 / 完成过几次。 |
| `progress(player, quest, objective)` | 第 `objective` 个目标(从 0 数)存下的进度;组队任务是全队那份。 |
| `addProgress(player, quest, objective, amount)` | 给某个目标加进度(封顶,组队的加到队伍里),比如判定「Boss 打赢了」。返回实际涨了多少。 |
| `addProgress(player, objective -> amount)` | 给玩家全部已接任务里的目标加进度,按目标算该加多少,适合自己注册的目标类型。 |
| `abandon(player, quest)` | 让玩家放弃一个任务,同在悬赏板上放弃。 |
| `team(player, quest)` | 已接任务的队伍 id;个人任务或没接是空。 |
| `isAway(player, quest)` | (只在服务端)要出发的任务:这个人是不是出发了还没回来。 |
| `awayPlayers(player, quest)` | (只在服务端)跟他同一组、出发了还没回来的人(组队任务是全队的,个人任务只有他自己),按出发先后。 |
| `markReturned(player, quest)` | (只在服务端)这个人回来了:从出门名单里划掉,悬赏板上他又能点「出发」;一组人都回来了,下次要发起人重新开局。下线的人会自动划掉,不用调。 |

**事件** `ibx.utility.api.event.QuestEvent`,只在服务端、发在 `NeoForge.EVENT_BUS` 上,都带 `getPlayer()` 和 `getQuest()`:

| 事件 | 可取消 | 时机 |
| --- | --- | --- |
| `QuestEvent.Accept` | 是 | 接任务:条件都检查过、还没写进玩家数据。 |
| `QuestEvent.Join` | 是 | 加入别人的队伍,`getTeam()` 是队伍 id。 |
| `QuestEvent.Depart` | 是 | 出发:还没记进出门名单、还没执行 `depart` 动作(整队走不走得成在这里查,走不成就取消)。`getDeparting()` 是这次走的人(点的人排第一),`isOpening()` 为真是开局(这一组第一次出发),为假是已经有人在外面、这个人中途进;`getTeam()`(个人任务为 null)。 |
| `QuestEvent.Progress` | 否 | 某个目标的进度涨了:`getObjective()`、`getOldProgress()`、`getNewProgress()`、`getTeam()`(个人任务为 null)。收集物品类目标不存进度,不发。 |
| `QuestEvent.Submit` | 是 | 目标全部达成、还没发奖励。取消后任务留着,这次交上去的物品照样算进度。 |
| `QuestEvent.Abandon` | 否 | 放弃,进度还没清。被发起人移出队伍也发这个(不在线时被移出的,在他上线时发)。 |
| `QuestEvent.Recall` | 是 | 召回:这个人出发了还没回来,却已经不在做这个任务了,人还留在外面。在放弃事件和 `abandon` 动作之后发。`getReason()` 是原因:`KICKED`(被发起人移出队伍)、`ABANDONED`(自己放弃)。**不取消**就由悬赏板把他送回重生点;**取消**后悬赏板什么都不做,由取消的模组自己把人送走(比如送回自己的大厅、顺便清场地)。 |

取消接、加入、出发、提交后,玩家在悬赏板上只会听到一声失败的低音;取消召回没有声音。

**注册新类型**:目标、奖励、前置条件、任务动作各有一个注册表,键在 `ibx.utility.registry.QuestTypeRegistries`。注册的值是类型的 `MapCodec`,任务 JSON 里按 `"type"` 写上就能用:

| 注册表 | 实现的接口 |
| --- | --- |
| `ibx_utility:quest_objective_type`(`OBJECTIVE_TYPE_KEY`) | `QuestObjective`:`onKill` / `onSubmit` 决定怎么涨进度,`isComplete` 决定算不算达成,`display*` 决定界面上怎么显示 |
| `ibx_utility:quest_reward_type`(`REWARD_TYPE_KEY`) | `QuestReward`:`grant` 发奖励,`displayIcon` 等决定界面上怎么显示 |
| `ibx_utility:quest_requirement_type`(`REQUIREMENT_TYPE_KEY`) | `QuestRequirement`:`test` |
| `ibx_utility:quest_action_type`(`ACTION_TYPE_KEY`) | `QuestAction`:`run`,在接 / 出发 / 提交 / 放弃时执行,返回做没做成。只有出发时看返回值:返回 `false` 就是这个人没走成,后面的 `depart` 动作不再对他执行,他不算出发;别的时机不看 |

```java
public static final DeferredRegister<MapCodec<? extends QuestObjective>> OBJECTIVES =
        DeferredRegister.create(QuestTypeRegistries.OBJECTIVE_TYPE_KEY, "examplemod");
public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<DefeatBossObjective>> DEFEAT_BOSS =
        OBJECTIVES.register("defeat_boss", () -> DefeatBossObjective.CODEC);

// Boss 被打败时
QuestApi.addProgress(player, objective -> objective instanceof DefeatBossObjective o && o.boss().equals(bossId) ? 1 : 0);
```
