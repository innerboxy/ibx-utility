# Quest Board and quests

[简体中文](QUEST_BOARD.md) | **English**

[← Back to README](../README/README_en.md)

---

## 1. Overview

The **Quest Board** (`ibx_utility:quest_board`) is a 2-wide, 2-tall wooden board. Right-click it to open a ledger-style quest screen; quests are accepted and handed in here. Quests are defined entirely by data packs (see [4](#4-quests)).

- **Three objective types**: submit items, kill mobs, collect items. Rewards can be items, a loot table, experience or a command.
- **Fixed quests + daily random**: fixed quests are always posted; the random pool draws a few quests per in-game day for the whole server (5 by default).
- **Hand in at the board**: once the objectives are done, go back to the board and press "Submit" to complete the quest. Rewards that don't fit drop at your feet.
- **Teams**: quests with `team` can be done as a team. Progress is shared, and every online member gets a full reward on submit.
- **Ready and depart**: a quest with a `depart` action is a departing quest. Members press "Ready", the leader presses "Depart" and they leave together; where they go is up to other mods (see item 6 of [2](#2-how-it-plays)).
- **Requirements**: quests whose requirements aren't met are simply not shown.
- **Tracker**: lists accepted quests and their progress in the top-left corner, with four styles; hidden by default (see [Configuration 2](CONFIG_en.md#2-client-config)).
- **Extensible**: other mods can register new objective, reward, requirement and quest-action types, and listen to accept / join / depart / progress / submit / abandon events (see [6](#6-api-and-events-for-developers)).

### How to obtain

The Quest Board is crafted in the **vanilla crafting table**:

```
Pl Pl Pl          Pl = any Planks
Pa BQ Pa          Pa = Paper, BQ = Book and Quill
St    St          St = Stick
```

---

## 2. How it plays

1. **Accept**: right-click the board. The ribbon bookmarks on the left are the pages; the single ribbon on the right is the built-in "Accepted Quests". Select a quest and press "Accept". To accept: requirements met, a one-time quest not done yet, a random quest on today's list, and below the active-quest limit (`maxActiveQuests`).
2. **Objectives**:
   - **Kill**: counted live, credited to the player who last hurt the mob — projectiles a player shot and tamed pets count for their owner. Kills before accepting don't count, nor do extra kills after the objective is done.
   - **Submit items**: taken from the inventory when you press "Submit" at the board; can be handed in over several trips.
   - **Collect items**: the inventory is counted at the moment you submit; nothing is taken.
3. **Submit**: once every objective is done, press "Submit" at the board. The quest completes and rewards are given; whatever doesn't fit drops at your feet.
4. **Abandon**: at the board, at any time. Progress is reset; no penalty.
5. **Teams**: quests with `team` can be done as a team.
   - Accepting one opens a team; others can see it at the board and join, up to `max` members. For a team quest you haven't accepted, the teams you can join are listed on the last page of the right page, each with a "Join" seal.
   - Progress is shared and a mob only counts once; collect objectives count the submitter's inventory.
   - Anyone can submit. Every online member gets a full reward and one completion; offline members get no reward but have the completion recorded when they log in.
   - If the leader leaves or logs out, the earliest-joined member who is still online takes over; if nobody in the team is online, the next member to log in takes over.
   - Your own team is on the last page of the right page (the gold page arrows sit above the seal buttons): one member per line with a box in front of the name; the leader's box is filled.
   - The leader can remove other members from the team: on the team page each member's line ends with a small cross; click it and it turns into a red "Remove?", click again to remove, move the mouse away to cancel. A removed member is treated as if they abandoned the quest themselves (for offline members, when they next log in); members who have departed and not come back can be removed too, and are then sent back to their respawn point.
   - A player who already finished a one-time quest can still join someone else's team for it, and still gets the reward and another completion.
6. **Ready and depart**: a quest whose `actions` has `depart` is a **departing quest**. Once accepted, it has an extra "Depart / Ready" seal until the objectives are done: on a team quest it sits under the member list on the team page, on a solo quest at the end of the right page's content. The bottom-right button is always "Submit".
   - **Starting**: on a team quest, members press "Ready" (press again for "Unready"), and the box in front of a ready member's name gets a check mark; it counts as soon as it's pressed, even if they walk away. The leader presses "Depart" and leaves together with every ready, online member. Solo quests have no ready step; just press "Depart".
   - **In progress**: while someone has departed and not come back, the top of the right page shows "In Progress" and who is out, and their lines on the team page end with "In Progress" too. Members who didn't go (not ready, joined later, already came back) press "Depart" to go in on their own.
   - **After departing**: for players who did leave, an open board screen closes by itself. A `depart` action can report that a player couldn't go (for example the arena can't be placed); that player doesn't count as departed, hears the low failure note at the board, and can press again.
   - **Recall**: a player who is still out but no longer on the quest (removed by the leader, or abandoned while out) is sent back to their respawn point: bed or respawn anchor, otherwise the world spawn; no respawn anchor charge is used. Other mods can take over and send them elsewhere (see `QuestEvent.Recall` in [6](#6-api-and-events-for-developers)).
   - **Coming back**: where people go and when they count as back is up to the mod that provides the `depart` action; players who log out count as back automatically. Once everyone in a group is back, the leader has to start again next time. The ready and departed lists aren't saved; they are empty after a restart or reloading the world.
7. **Daily random**: quests with `pool` set to `random` go into the random pool. Every in-game day (every 24000 ticks of game time; sleeping doesn't skip ahead) the server draws `dailyQuestCount` of them by weight, without repeats. A random quest you already accepted can still be finished after the day changes.
8. **Feedback**: progress only shows in the tracker in the top-left corner (off by default, see [Configuration 2](CONFIG_en.md#2-client-config)); without the tracker, check the board's "Accepted Quests" page. Actions on the board only give sounds: a pen scratch when a quest is accepted, a team joined or "Ready" pressed, a low bass note when an action fails (too far, at the limit, objectives not done, couldn't depart, etc.), heard only by that player. No result text, pop-ups or chat messages. While the board is open, it refreshes by itself when teammates join, leave, get ready, depart or submit.

> Accepting, joining, getting ready, departing, submitting, abandoning and removing a member all require standing within 8 blocks of the board you opened.

---

## 3. Pages

The bookmarks on the left of the board are driven by a data-pack registry, written the same way as forge categories ([Equipment Forge 3](FORGE_en.md#3-category-definitions)).

- **Registry**: `minecraft:category/quest_board`
- **Location**: `data/<namespace>/category/quest_board/*.json`
- **Page id**: the file's ResourceLocation; quests use it to say which page they're on.
- **Re-enter the world after editing** (restart the server): pages are a data-pack registry, and `/reload` does not re-read them.

```json
{
  "name": { "translate": "category.example.hunting" },
  "order": 0,
  "icon": "minecraft:iron_sword"
}
```

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | — | Bookmark name (shown on hover), a full Component. |
| `order` | No | `0` | Top-to-bottom order, smaller is higher. Ties are sorted by page id. |
| `icon` | No | `minecraft:book` | The icon on the bookmark. Write an item id (falls back to a book if the item doesn't exist), or an image path ending in `.png` (same as a quest's `icon`, see [4](#4-quests)). |

Built-in pages (each can be turned off in the config, see [Configuration](CONFIG_en.md)):

| id | order | Icon |
| --- | --- | --- |
| `ibx_utility:key_quests` | 0 | Book and Quill |
| `ibx_utility:free_quests` | 1 | Map |

- "Accepted Quests" is built in, always the ribbon on the right of the book, and can't be configured by data packs.
- Bookmark colors cycle in order: red, blue, green, purple, teal, orange.

Want a "Daily Quests" page? The mod doesn't ship one; add your own page with the condition `ibx_utility:daily_quest_category_enabled` (so the `enableDailyQuestCategory` config switch controls it), and you can reuse the mod's lang key `category.ibx_utility.daily_quests`:

```json
{
  "neoforge:conditions": [ { "type": "ibx_utility:daily_quest_category_enabled" } ],
  "name": { "translate": "category.ibx_utility.daily_quests" },
  "order": 2,
  "icon": "minecraft:clock"
}
```

Random-pool quests (`"pool": "random"`) don't have to be on this page; they show up on whichever page they name.

---

## 4. Quests

- **Registry**: `ibx_utility:quest`
- **Location**: `data/<namespace>/ibx_utility/quest/*.json`
- **Quest id**: the file's ResourceLocation. For example `data/example/ibx_utility/quest/wolf_pack.json` → `example:wolf_pack`.
- **Re-enter the world after editing** (restart the server): quests are a data-pack registry too, and `/reload` does not re-read them.

With every field filled in:

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
    { "type": "ibx_utility:command", "command": "say The wolves are gone" }
  ]
}
```

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | — | Quest name. A plain string, or `{ "translate": "..." }` for a lang key. |
| `description` | No | none | Description, written like `name`. Fewer lines are shown when the page is full. |
| `icon` | No | `minecraft:book` | The icon in the list, the right page's title and the tracker. Write an item id (falls back to a book if the item doesn't exist), or the path of an image (a full resource path ending in `.png`, e.g. `"example:textures/quest/wolf.png"`, which is `assets/example/textures/quest/wolf.png` in a resource pack); the whole image is drawn scaled to 16×16, and a missing image shows the purple-and-black checkerboard. |
| `category` | **Yes** | — | Which page (page id). **If that page doesn't exist (typo, or turned off in the config), the whole quest is skipped**: not shown, can't be accepted, never drawn; players who already accepted it lose it on their next login. |
| `pool` | No | `fixed` | `fixed` is always posted; `random` goes into the daily random pool and only shows up on days it's drawn. |
| `weight` | No | `1` | Weight in the random pool; only matters for `random`. |
| `requirements` | No | none | Requirements; all must be met to see the quest, accept it or join a team for it (see [5](#5-objectives-rewards-and-requirements)). |
| `repeatable` | No | `false` | `false` can be done once; `true` can be done again and again. |
| `team` | No | none | Allows teams; `max` is the team size limit, default 4, minimum 2. |
| `objectives` | **Yes** | — | Objectives, at least one (see [5](#5-objectives-rewards-and-requirements)). |
| `rewards` | No | none | Rewards (see [5](#5-objectives-rewards-and-requirements)). |
| `actions` | No | none | A list of quest actions for each of `accept` / `depart` / `submit` / `abandon`. Having `depart` makes it a departing quest (see item 6 of [2](#2-how-it-plays)); those actions run for each player who departs. **This mod ships no action types**; other mods register them (see [6](#6-api-and-events-for-developers)). |

The smallest quest only needs the three required fields:

```json
{
  "name": "Firewood",
  "category": "ibx_utility:free_quests",
  "objectives": [ { "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16 } ]
}
```

> **The mod itself ships no quests**, only the two default pages. Which quests exist and how they're arranged is entirely up to the modpack.

---

## 5. Objectives, rewards and requirements

`item` / `entity` accept a single id, a `#` tag, or an array of them.

- **Name**: tags show their translated tag name (`#id` when there is none, e.g. `#minecraft:logs`); arrays use the first entry's name. All three objective types accept an optional `name` (a string or `{ "translate": "..." }`) that replaces the automatic name: `{ "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16, "name": "Logs" }`.
- **Icon**: for tags and arrays the icon cycles through the entries once per second (for mobs, the ones that have a spawn egg), on the board and in the tracker alike (same as tag materials in the Equipment Forge).

**Objectives** (`objectives`):

| type | Fields | Description |
| --- | --- | --- |
| `ibx_utility:kill` | `entity`, `count` (default 1), `name` (optional) | Kill mobs. Its icon is the mob's spawn egg (none if there isn't one). |
| `ibx_utility:submit_item` | `item`, `count` (default 1), `name` (optional) | Hand the items in at the board; they are taken, and can be handed in over several trips. |
| `ibx_utility:collect_item` | `item`, `count` (default 1), `name` (optional) | Just have them in your inventory when you submit; nothing is taken. The tracker counts the inventory live. |

**Rewards** (`rewards`):

| type | Fields | Description |
| --- | --- | --- |
| `ibx_utility:item` | `item`: an item id, or `{ "id": ..., "count": ... }` | Goes into the inventory; whatever doesn't fit drops at the player's feet. |
| `ibx_utility:loot_table` | `table`: loot table id | Rolled once with the player's luck; shown as a chest on the screen. |
| `ibx_utility:experience` | `levels`, `points`, at least one | Gives experience levels / points. |
| `ibx_utility:command` | `command` | Run as the completing player with permission level 2, output suppressed; the leading `/` is optional. Not shown on the screen. |

**Requirements** (`requirements`):

| type | Fields | Description |
| --- | --- | --- |
| `ibx_utility:quest_completed` | `quest`: quest id | Has completed this quest. |
| `ibx_utility:advancement` | `advancement`: advancement id | Has this advancement. |

---

## 6. API and events for developers

**`ibx.utility.api.QuestApi`**: read methods work on both sides (the client only has its own data); write methods are server-only.

| Method | Description |
| --- | --- |
| `activeQuests(player)` | Accepted quests, in the order they were accepted. |
| `isActive(player, quest)` / `completions(player, quest)` | Whether it's accepted / how many times it was completed. |
| `progress(player, quest, objective)` | Stored progress of objective number `objective` (0-based); for team quests, the team's shared progress. |
| `addProgress(player, quest, objective, amount)` | Adds progress to one objective (capped; team quests add to the team), e.g. when "the boss was defeated". Returns how much was actually added. |
| `addProgress(player, objective -> amount)` | Adds progress to objectives across all of the player's accepted quests, computing the amount per objective; handy for your own objective types. |
| `abandon(player, quest)` | Makes the player abandon a quest, same as abandoning at the board. |
| `team(player, quest)` | Team id of an accepted quest; empty for solo quests or if not accepted. |
| `isAway(player, quest)` | (Server-only) For departing quests: whether this player has departed and not come back. |
| `awayPlayers(player, quest)` | (Server-only) Players in the same group who have departed and not come back (the whole team for team quests, just them for solo quests), in departure order. |
| `markReturned(player, quest)` | (Server-only) This player is back: removed from the departed list, so they can press "Depart" again on the board; once everyone in a group is back, the leader has to start again. Players who log out are removed automatically, no need to call this. |

**Events** `ibx.utility.api.event.QuestEvent`, server-only, posted on `NeoForge.EVENT_BUS`; all have `getPlayer()` and `getQuest()`:

| Event | Cancellable | When |
| --- | --- | --- |
| `QuestEvent.Accept` | Yes | Accepting: all checks passed, not yet written to the player's data. |
| `QuestEvent.Join` | Yes | Joining someone's team; `getTeam()` is the team id. |
| `QuestEvent.Depart` | Yes | Departing: not yet added to the departed list, `depart` actions not run yet (check here whether the whole group can go, and cancel if it can't). `getDeparting()` is who leaves this time (the player who pressed first); `isOpening()` is true when the group starts (first departure) and false when someone is already out and this player goes in mid-way; `getTeam()` (null for solo quests). |
| `QuestEvent.Progress` | No | An objective's progress went up: `getObjective()`, `getOldProgress()`, `getNewProgress()`, `getTeam()` (null for solo quests). Collect objectives store no progress and don't fire it. |
| `QuestEvent.Submit` | Yes | Every objective is done, rewards not given yet. When cancelled the quest stays, and items handed in this time still count as progress. |
| `QuestEvent.Abandon` | No | Abandoning, before progress is cleared. Also fired when the leader removes a member from the team (for a member removed while offline, when they next log in). |
| `QuestEvent.Recall` | Yes | Recall: this player departed and hasn't come back, but is no longer on the quest, and is still out there. Fired after the abandon event and the `abandon` actions. `getReason()` says why: `KICKED` (removed by the leader) or `ABANDONED` (abandoned it themselves). **Not cancelled**: the board sends them back to their respawn point. **Cancelled**: the board does nothing, and the cancelling mod moves the player itself (for example back to its own lobby, cleaning up its arena). |

When cancelled, the player only hears the low "failed" note on the board.

**Registering new types**: objectives, rewards, requirements and quest actions each have a registry, with keys in `ibx.utility.registry.QuestTypeRegistries`. The registered value is the type's `MapCodec`; write its id as `"type"` in quest JSON to use it:

| Registry | Interface to implement |
| --- | --- |
| `ibx_utility:quest_objective_type` (`OBJECTIVE_TYPE_KEY`) | `QuestObjective`: `onKill` / `onSubmit` decide how progress grows, `isComplete` decides when it's done, `display*` decide how it's shown |
| `ibx_utility:quest_reward_type` (`REWARD_TYPE_KEY`) | `QuestReward`: `grant` gives it, `displayIcon` etc. decide how it's shown |
| `ibx_utility:quest_requirement_type` (`REQUIREMENT_TYPE_KEY`) | `QuestRequirement`: `test` |
| `ibx_utility:quest_action_type` (`ACTION_TYPE_KEY`) | `QuestAction`: `run`, executed on accept / depart / submit / abandon; returns whether it succeeded. The result only matters on depart: `false` means this player couldn't go, the remaining `depart` actions are skipped for them and they don't count as departed; it is ignored at the other times |

```java
public static final DeferredRegister<MapCodec<? extends QuestObjective>> OBJECTIVES =
        DeferredRegister.create(QuestTypeRegistries.OBJECTIVE_TYPE_KEY, "examplemod");
public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<DefeatBossObjective>> DEFEAT_BOSS =
        OBJECTIVES.register("defeat_boss", () -> DefeatBossObjective.CODEC);

// when a boss is defeated
QuestApi.addProgress(player, objective -> objective instanceof DefeatBossObjective o && o.boss().equals(bossId) ? 1 : 0);
```
