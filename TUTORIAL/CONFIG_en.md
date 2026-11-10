# Configuration

[简体中文](CONFIG.md) | **English**

[← Back to README](../README/README_en.md)

---

## 1. Common config

The config type is **COMMON**, located at `config/ibx_utility-common.toml`. The tracker style is a client config; see [2](#2-client-config).

```toml
#Remove vanilla crafting-table recipes superseded by the Equipment Forge, so those items can only be made at the forge.
removeVanillaRecipes = true
#Remove the vanilla crafting-table recipes for arrows, tipped arrows and spectral arrows, which are replaced by the fletching table.
removeVanillaArrowRecipes = true
#Load the mod's built-in default category tabs (melee/ranged/magic/tool/armor/shield/curio).
enableDefaultCategories = true
#Load the mod's built-in default forge recipes (wood/stone/copper/iron/gold/diamond/netherite gear, spears, bow & crossbow, shield, etc.).
enableDefaultRecipes = true
#Load the mod's built-in fletching recipes (arrow, tipped arrow, spectral arrow). Disable to define fletching recipes entirely via datapack.
enableDefaultFletchingRecipes = true
#Load the quest board's built-in Key Quests page. When disabled, quests on this page are skipped too (same for the two below).
enableKeyQuestCategory = true
#Load the quest board's built-in Free Quests page.
enableFreeQuestCategory = true
#Switch for a Daily Quests page. The mod doesn't ship one; a modpack's own daily page that uses the ibx_utility:daily_quest_category_enabled condition is controlled by this switch.
enableDailyQuestCategory = true
#How many quests one player can have accepted at the same time.
maxActiveQuests = 5
#How many random-pool quests are posted each day (every 24000 ticks of game time), shared by the server.
dailyQuestCount = 5
```

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `removeVanillaRecipes` | boolean | `true` | Whether to remove the vanilla recipes superseded by the forge. One switch controls them all. |
| `removeVanillaArrowRecipes` | boolean | `true` | Whether to remove the vanilla recipes superseded by the fletching table (arrows, tipped arrows, spectral arrows). Independent of the option above. |
| `enableDefaultCategories` | boolean | `true` | Whether to load the mod's 7 built-in default category tabs. Turn off to define categories entirely via data pack. |
| `enableDefaultRecipes` | boolean | `true` | Whether to load the mod's built-in default forge recipes. Turn off to define the whole recipe set via data pack. |
| `enableDefaultFletchingRecipes` | boolean | `true` | Whether to load the mod's built-in fletching recipes. Turn off to define fletching recipes entirely via data pack. Independent of the option above. |
| `enableKeyQuestCategory` | boolean | `true` | Whether to load the quest board's built-in Key Quests page. When off, quests on that page are skipped as well. |
| `enableFreeQuestCategory` | boolean | `true` | Whether to load the built-in Free Quests page. Same as above. |
| `enableDailyQuestCategory` | boolean | `true` | Switch for a Daily Quests page. The mod doesn't ship one; a modpack's own daily page is controlled by it once it carries the matching condition (see [Quest Board 3](QUEST_BOARD_en.md#3-pages)). |
| `maxActiveQuests` | int (1–64) | `5` | How many quests one player can have accepted at once (a team quest counts as one). |
| `dailyQuestCount` | int (0–64) | `5` | How many quests are drawn from the random pool each in-game day, one shared list for the server. |

**What the first five and the three quest-page switches have in common:**

- They all take effect **server-side only**. They act during the data-pack loading phase (server), and the result is then synced to clients.
  - Singleplayer: the local config file takes effect (the singleplayer client ships an integrated server).
  - Multiplayer: **only the server's config file matters**; editing the client's local file does nothing.
- When changes take effect depends on the kind:
  - **Recipe switches** (`removeVanillaRecipes`, `removeVanillaArrowRecipes`, `enableDefaultRecipes`, `enableDefaultFletchingRecipes`): `/reload` or re-enter the world.
  - **Category and page switches** (`enableDefaultCategories` and the three quest-page switches): forge categories and quest-board pages are data-pack registries, read once when the world loads; `/reload` does not re-read them. **Re-enter the world** (restart the server) after changing them.

**`removeVanillaRecipes`**

- When enabled, the following vanilla recipes are removed: the tools, swords & spears of every tier (wood/stone/copper/iron/gold/diamond), shield, bow, crossbow, mace, leather/copper/iron/gold/diamond armor sets, turtle shell, and the **smithing upgrade recipes for netherite gear (including the netherite spear)**.
- When disabled, all of the above vanilla recipes are restored, and the forge recipes still work (i.e. both paths coexist).

**`removeVanillaArrowRecipes`**

- When enabled, the vanilla crafting-table recipes for **arrows, tipped arrows and spectral arrows** are removed, so these can only be made at the fletching table.
- When disabled, the vanilla recipes are restored, and the fletching recipes still work.
- Independent of `removeVanillaRecipes`, so you can remove just one group.

**`enableDefaultCategories`**

- When disabled, the 7 built-in categories such as `ibx_utility:melee` are no longer loaded; only the built-in "All" tab remains, and categories can be defined by the data pack.
- Note: if you keep the default recipes at the same time, the categories those recipes reference won't exist, so they **only appear under the "All" tab**. Usually used together with `enableDefaultRecipes = false`.

**`enableDefaultRecipes`**

- When disabled, the mod's 77 built-in forge recipes are no longer loaded, and the whole recipe set can be defined by the data pack.
- **Does not affect the Equipment Forge block's own crafting recipe** (the block is always craftable).
- Also does not affect `removeVanillaRecipes` — to restore vanilla recipes at the same time, turn that off separately.

**`enableDefaultFletchingRecipes`**

- When disabled, the mod's 3 built-in fletching recipes (arrow, tipped arrow, spectral arrow) are no longer loaded, and fletching recipes can be defined by the data pack.
- Independent of `enableDefaultRecipes`.
- Also does not affect `removeVanillaArrowRecipes` — if you remove the vanilla arrow recipes and also disable the default fletching recipes, add fletching recipes via data pack, or there will be no way to make arrows at all.

**Quest-page switches (`enableKeyQuestCategory` / `enableFreeQuestCategory` / `enableDailyQuestCategory`)**

- Turning a page off stops it from loading, and **every quest on that page is skipped too**: not shown, can't be accepted, never drawn as a daily quest; players who already accepted one lose it the next time they log in.
- The three switches are independent; keep only the ones you need. Custom pages: see [Quest Board 3](QUEST_BOARD_en.md#3-pages).
- Re-enter the world (restart the server) after changing them; `/reload` has no effect.

**`maxActiveQuests` / `dailyQuestCount`**

- Also server-side only, but no `/reload` needed: the limit is read when a quest is accepted; the draw count applies the next time the daily list is refreshed (once per in-game day).

---

## 2. Client config

The tracker style is each player's own setting, in `config/ibx_utility-client.toml`; you can also change it from this mod's "Config" button in the mod list:

```toml
#Quest tracker in the top-left corner: TEXT, ICONS, ICONS_WITH_QUEST or HIDDEN (default).
trackerMode = "HIDDEN"
```

| Option | Values | Default | Description |
| --- | --- | --- | --- |
| `trackerMode` | `TEXT` / `ICONS` / `ICONS_WITH_QUEST` / `HIDDEN` | `HIDDEN` | Text: quest name plus one line per objective; Icons: one line per quest, objective icon plus numbers; Icons with quest: the quest icon in front as well; Hidden. Quests with every objective done turn green. |

---

## 3. Data-pack conditions

The mod registers eight data-pack conditions, one per config option. NeoForge's rule is "the data loads only when the condition is `true`", so the condition names all describe the **"loaded"** side of the state:

| Condition id | When it returns `true` | Used on |
| --- | --- | --- |
| `ibx_utility:vanilla_recipe_enabled` | When `removeVanillaRecipes = false` | The mod's override files for vanilla recipes superseded by the forge |
| `ibx_utility:vanilla_arrow_recipe_enabled` | When `removeVanillaArrowRecipes = false` | The mod's override files for the vanilla arrow, tipped arrow and spectral arrow recipes |
| `ibx_utility:default_recipes_enabled` | When `enableDefaultRecipes = true` | The mod's 77 built-in forge recipes |
| `ibx_utility:default_fletching_recipes_enabled` | When `enableDefaultFletchingRecipes = true` | The mod's 3 built-in fletching recipes |
| `ibx_utility:default_categories_enabled` | When `enableDefaultCategories = true` | The mod's 7 built-in category definitions |
| `ibx_utility:key_quest_category_enabled` | When `enableKeyQuestCategory = true` | The quest board's built-in Key Quests page |
| `ibx_utility:free_quest_category_enabled` | When `enableFreeQuestCategory = true` | The quest board's built-in Free Quests page |
| `ibx_utility:daily_quest_category_enabled` | When `enableDailyQuestCategory = true` | The mod ships no daily page; meant for a modpack's own one (see [Quest Board 3](QUEST_BOARD_en.md#3-pages)) |

> Conditions aren't limited to recipes — NeoForge wires condition parsing into data-pack registries too, so category definitions ([Equipment Forge 3](FORGE_en.md#3-category-definitions)), quest-board pages ([Quest Board 3](QUEST_BOARD_en.md#3-pages)) and quests ([Quest Board 4](QUEST_BOARD_en.md#4-quests)) also support `neoforge:conditions`.

### Using a condition to remove vanilla recipes

The semantics of `vanilla_recipe_enabled` are: it returns `true` when the config `removeVanillaRecipes` is `false` (i.e. **do not remove**).

Since NeoForge's rule is "a recipe loads only when the condition is true", attaching it to a vanilla recipe file achieves "when the config is enabled, the vanilla recipe doesn't load". The trick is to **override the vanilla recipe with a file identical to the original**, and add the condition at the top:

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

Placing this at `data/minecraft/recipe/wooden_sword.json` overrides the vanilla Wooden Sword recipe. The condition works with any recipe type (the mod also uses it to override netherite's `smithing_transform` recipes).

> When adding a forge recipe, if you also want to remove the corresponding vanilla recipe, override one this way to reuse the same config switch.
> The original vanilla recipe text can be extracted from `data/minecraft/recipe/`: the ones NeoForge rewrites (e.g. sticks replaced with `#c:rods/wooden`) live in `neoforge-<version>-universal.jar`, the rest in the Minecraft jar. When overriding, use NeoForge's rewritten version.
