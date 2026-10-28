# IBX-Utility

[简体中文](README.md) | **English**

A Minecraft **1.21.1** / **NeoForge** mod that gives TavernTales a data-driven equipment forge.

---

## 1. Overview

This mod adds an **Equipment Forge** (`ibx_utility:equipment_forge`) — a crafting block built around "pick a recipe, then craft it" that replaces the fiddly item placement of the vanilla crafting table for gear.

### Key features

- **Materials taken straight from your inventory**: the forge has no crafting grid; materials are deducted from the player's main inventory + offhand, so there is no shuffling items back and forth.
- **List-style interface**: on the left, category tabs + a search box + an item grid; on the right, the required materials and a craft button for the selected recipe.
  - Modeled on the vanilla recipe book: craftable items show as bright slots, items you lack materials for show as dark slots.
  - A search box to filter items by name.
  - Toggle between "show all / show craftable only".
  - Item ordering: first by **rarity**, then by **creative inventory order**; the material list on the right follows the same rule.
- **Data-driven categories**: the left-hand category tabs are defined entirely by the data pack; modpacks are free to add, remove, or change them (see [3.2](#32-category-definitions)).
- **Optionally remove vanilla recipes**: the vanilla crafting-table / smithing recipes superseded by the forge can be removed together via config, enabled by default (see [2](#2-configuration)).
- **No items lost when the inventory is full**: products that don't fit are dropped at the player's feet.

### Loot Bag

The mod also provides a standalone item — the **Loot Bag** (`ibx_utility:loot_bag`) — opened by right-click, whose contents are defined entirely by a data-pack loot table (see [4](#4-loot-bag)).

- **One bag, one table**: the bag's kind is stored in an item component and maps to a loot table of the same name, so you can give every boss its own dedicated bag.
- **Protected as a dropped item**: immune to fire/lava/explosions, never despawns, and has an always-on glowing outline (visible through walls), so it's fine to drop it right on the battlefield.
- **Retextured per kind**: drop one model file into a resource pack to make different bags look different — zero code (see [4.5](#45-retexturing-a-bag)).
- **View drops in JEI**: with JER installed, you can see each bag's possible drops and probabilities (see [4.6](#46-viewing-drops-in-jei)).
- No crafting recipe. An **empty bag** is available at the very front of the "Ingredients" creative tab; **specific kinds** can only be granted via `/give`, commands, or loot tables (kinds are defined by data packs, are open-ended in count, and are not enumerated in the creative tab).

### Fletching Table

Right-clicking the **vanilla fletching table** opens a fletching UI. The block itself is unchanged, and fletcher villagers still use it as their workstation.

- Tail, shaft, tip and potion slots sit in one row; once filled, the result appears on the right. Each craft consumes 1 item from each slot, and shift-clicking the result crafts until the materials run out.
- Each slot only accepts materials that some recipe uses in that position, so shift-clicking from the inventory sends items to the right slot; empty slots show an outline hint of what goes there.
- Recipes are data-driven (see [3.4](#34-fletching-recipes)). Three are built in, all starting from feather + stick + flint and yielding 8 per craft:
  - potion slot empty → arrows;
  - a **lingering potion** in the potion slot → tipped arrows with that potion's effect (the potion is used up; no empty bottle is returned);
  - **glowstone dust** in the potion slot → spectral arrows.
- Whether the vanilla crafting-table recipes for arrows, tipped arrows and spectral arrows are removed is controlled by a separate switch, `removeVanillaArrowRecipes` (see [2](#2-configuration)).
- With JEI installed there is a "Fletching Table" category; open a JEI recipe page from the fletching UI and click the "+" to the right of the result to fill the slots from your inventory.

### Quest Board

The **Quest Board** (`ibx_utility:quest_board`) is a 2-wide, 2-tall wooden board. Right-click it to open a ledger-style quest screen; quests are accepted and handed in here. Quests are defined entirely by data packs (see [6](#6-quest-board-and-quests)).

- **Three objective types**: submit items, kill mobs, collect items. Rewards can be items, a loot table, experience or a command.
- **Fixed quests + daily random**: fixed quests are always posted; the random pool draws a few quests per in-game day for the whole server (5 by default).
- **Hand in at the board**: once the objectives are done, go back to the board and press "Submit" to complete the quest. Rewards that don't fit drop at your feet.
- **Teams**: quests with `team` can be done as a team. Progress is shared, and every online member gets a full reward on submit.
- **Requirements**: quests whose requirements aren't met are simply not shown.
- **Tracker**: lists accepted quests and their progress in the top-left corner, with four styles; hidden by default (see [2](#2-configuration)).
- **Extensible**: other mods can register new objective, reward, requirement and quest-action types, and listen to accept / join / progress / submit / abandon events (see [6.5](#65-api-and-events-for-developers)).

### How to obtain

The Equipment Forge is crafted in the **vanilla crafting table**:

```
Fe Fe Fe          Fe = Iron Ingot
Au CT Au          Au = Gold Ingot
Au    Au          CT = Crafting Table
```

The Quest Board is also crafted in the **vanilla crafting table**:

```
Pl Pl Pl          Pl = any Planks
Pa BQ Pa          Pa = Paper, BQ = Book and Quill
St    St          St = Stick
```

### Dependencies

| Mod | Type | Notes |
| --- | --- | --- |
| NeoForge `21.1.234+` | **Required** | — |
| Minecraft `1.21.1` | **Required** | — |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) `19+` | Optional (client) | Adds "Equipment Forge" and "Fletching Table" recipe-lookup categories; clicking JEI's "+" in the forge UI jumps straight to that recipe with it selected, and in the fletching UI it fills in the materials |
| [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) `1.6+` (JER) | Optional (client) | Adds a "Loot Bag" category showing each bag's possible drops and probabilities (see [4.6](#46-viewing-drops-in-jei)). Requires JEI as well |
| [Beyond Dimensions](https://modrinth.com/mod/beyonddimensions) `0.7+` | Optional | When forging, items stored in the player's **main network** can be consumed directly; the UI's craftable check and material counting include them too |

---

## 2. Configuration

The config type is **COMMON**, located at `config/ibx_utility-common.toml`. The tracker style is a client config; see the end of this section.

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
| `enableDailyQuestCategory` | boolean | `true` | Switch for a Daily Quests page. The mod doesn't ship one; a modpack's own daily page is controlled by it once it carries the matching condition (see [6.2](#62-pages)). |
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
- The three switches are independent; keep only the ones you need. Custom pages: see [6.2](#62-pages).
- Re-enter the world (restart the server) after changing them; `/reload` has no effect.

**`maxActiveQuests` / `dailyQuestCount`**

- Also server-side only, but no `/reload` needed: the limit is read when a quest is accepted; the draw count applies the next time the daily list is refreshed (once per in-game day).

**Client config**

The tracker style is each player's own setting, in `config/ibx_utility-client.toml`; you can also change it from this mod's "Config" button in the mod list:

```toml
#Quest tracker in the top-left corner: TEXT, ICONS, ICONS_WITH_QUEST or HIDDEN (default).
trackerMode = "HIDDEN"
```

| Option | Values | Default | Description |
| --- | --- | --- | --- |
| `trackerMode` | `TEXT` / `ICONS` / `ICONS_WITH_QUEST` / `HIDDEN` | `HIDDEN` | Text: quest name plus one line per objective; Icons: one line per quest, objective icon plus numbers; Icons with quest: the quest icon in front as well; Hidden. Quests with every objective done turn green. |

---

## 3. Data formats

The mod involves four kinds of data files, which modpacks can override or add to directly via data pack.

### 3.1 Forge recipes

- **Location**: `data/<namespace>/recipe/**/*.json` (can go in any subfolder; this mod puts them under `recipe/equipment_forge/`)
- **Type**: `ibx_utility:equipment_forge`

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

| Field | Required | Description |
| --- | --- | --- |
| `category` | Yes | Category id, **must include the full namespace** (e.g. `ibx_utility:melee`). If it references an undefined category, the recipe only appears under the "All" tab and a debug log line is printed. |
| `materials` | Yes | Material list. Each entry is NeoForge's `{ "ingredient": <vanilla ingredient>, "count": <amount> }` (`count` defaults to 1). `ingredient` is an item id (`"minecraft:diamond"`), a `#`-prefixed tag (`"#minecraft:planks"`), or an array of those. The count is the **total amount required**, unrelated to any placement shape.<br>The legacy `{ "item": <item id> }` / `{ "tag": <tag id> }` form is still accepted, but new data packs should use `ingredient`. |
| `result` | Yes | The product, `{ "id": <item id>, "count": <amount> }`. |

> The order of materials doesn't matter — the UI re-sorts them by rarity and creative-inventory order for display.
> Tag-based materials cycle through their candidate items once per second in the UI.

### 3.2 Category definitions

The left-hand category tabs are driven by a data-pack registry.

- **Registry**: `minecraft:category/equipment_forge`
- **Location**: `data/<namespace>/category/equipment_forge/*.json`
- **Category id**: the file's ResourceLocation. For example `data/ibx_utility/category/equipment_forge/melee.json` → `ibx_utility:melee`, and recipes are grouped by this id.

```json
{
  "name": { "translate": "category.ibx_utility.melee" },
  "order": 0,
  "icon": "minecraft:iron_sword"
}
```

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | — | Tab display name, a full Component (usually `{"translate": "..."}`). |
| `order` | No | `0` | Top-to-bottom order; smaller is higher. Ties break by category id alphabetically. |
| `icon` | No | `minecraft:book` | Tab icon item id. May point at an item from an optional mod — if that item doesn't exist, it falls back (see below). |

**Icon fallback**: if the item given by `icon` isn't in the registry (e.g. the corresponding mod isn't installed), the tab falls back to a book.

**The "All" tab is built in**, always pinned to the top, and cannot be configured via data pack.

Built-in categories:

| id | order | Icon |
| --- | --- |------|
| `ibx_utility:melee` | 0 | Iron Sword |
| `ibx_utility:ranged` | 1 | Bow |
| `ibx_utility:magic` | 2 | Knowledge Book |
| `ibx_utility:tool` | 3 | Diamond Pickaxe |
| `ibx_utility:armor` | 4 | Golden Chestplate |
| `ibx_utility:shield` | 5 | Shield |
| `ibx_utility:curio` | 6 | Elytra |

### 3.3 Data-pack conditions

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
| `ibx_utility:daily_quest_category_enabled` | When `enableDailyQuestCategory = true` | The mod ships no daily page; meant for a modpack's own one (see [6.2](#62-pages)) |

> Conditions aren't limited to recipes — NeoForge wires condition parsing into data-pack registries too, so category definitions (3.2), quest-board pages (6.2) and quests (6.3) also support `neoforge:conditions`.

#### Using a condition to remove vanilla recipes

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

### 3.4 Fletching recipes

- **Location**: `data/<namespace>/recipe/**/*.json` (this mod puts them under `recipe/fletching/`)
- **Type**: `ibx_utility:fletching`

```json
{
  "type": "ibx_utility:fletching",
  "tail": "minecraft:feather",
  "shaft": "#c:rods/wooden",
  "tip": "minecraft:flint",
  "result": { "id": "minecraft:arrow", "count": 8 }
}
```

| Field | Required | Description |
| --- | --- | --- |
| `tail` | Yes | Material for the tail slot, in vanilla ingredient form: an item id, a `#`-prefixed tag, or an array of them. |
| `shaft` | Yes | Material for the shaft slot, same form. |
| `tip` | Yes | Material for the tip slot, same form. |
| `additive` | No | Material for the potion slot, same form. If omitted, the potion slot must be empty to craft. |
| `copy_potion` | No | Defaults to `false`. When `true`, the potion effect of the item in the potion slot is copied onto the product (like the vanilla tipped-arrow recipe, `crafting_imbue`). |
| `result` | Yes | The product, `{ "id": <item id>, "count": <count> }`. |

Tipped arrows are written like this (this is the built-in recipe):

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

> Each craft consumes 1 item from each slot (a potion is used up entirely; no empty bottle is returned). Each slot only accepts materials that **some recipe uses in that position**, so adding a new material only takes a recipe — no extra tag needed.

---

## 4. Loot Bag

The Loot Bag (`ibx_utility:loot_bag`) is a "right-click to open" item. It contains no drop content itself — **the bag is just a pointer** to a vanilla loot table, and that table decides everything it drops.

### 4.1 The three parts

A working bag is made of three things, none of which can be missing:

| # | Thing | Location | Example (Iron Golem bag) |
| --- | --- | --- | --- |
| 1 | **Item component**, deciding which kind of bag this is | The item itself | `ibx_utility:loot_bag_type` = `"iron_golem"` |
| 2 | **Loot table**, deciding what drops | Data pack | `data/ibx_utility/loot_table/loot_bag/iron_golem.json` |
| 3 | **Translation**, deciding the tooltip's name and color | Resource pack / mod lang file | `loot_bag.ibx_utility.iron_golem` = `"§fIron Golem"` |

The three are tied together by the **bare string** in the component. A component value of `"iron_golem"` is **fixedly** expanded into:

- Loot table id → `ibx_utility:loot_bag/iron_golem`
- Translation key → `loot_bag.ibx_utility.iron_golem`

> The component value **cannot include a namespace or a directory** (you can't write `mypack:xxx` or `boss/xxx`). Every bag's table must live in the `loot_bag/` folder of the `ibx_utility` namespace — but **any data pack can drop files into this path**; it doesn't have to be this mod's jar.

### 4.2 Step by step: create an "Ender Dragon bag"

**Step 1**, create the table file `data/ibx_utility/loot_table/loot_bag/ender_dragon.json`:

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

**Step 2**, add a translation in the lang file `assets/<any namespace>/lang/en_us.json` (write the color as a § format code at the start of the translation):

```json
{
  "loot_bag.ibx_utility.ender_dragon": "§5Ender Dragon"
}
```

**Step 3**, grant it:

```
/give @s ibx_utility:loot_bag[ibx_utility:loot_bag_type="ender_dragon"] 1
```

After editing the data pack, `/reload` applies it; a new translation requires re-entering the world or pressing F3+T to reload resources.

### 4.3 Component and display

| Component | Type | Description |
| --- | --- | --- |
| `ibx_utility:loot_bag_type` | string | The bag kind. **This is the bag's only component.** Absent, empty string, or missing → empty bag: can't be right-clicked, tooltip shows "empty". |

**The display name and color come entirely from the lang file**, with no automatic derivation:

- Translation found → shown as-is, color decided by the `§` format codes in the translation (no color code means it inherits the default color).
- **Translation not found → the tooltip shows the raw component string in red** (e.g. a red `ender_dragon`). This is a deliberate error hint, warning you that you forgot the translation or mistyped the type.

> Because color goes through `§` codes, **only the 16 vanilla colors** are available; `#RRGGBB` is not supported. And each language's translation must carry its own color code.
>
> The "empty" hint uses the key `loot_bag.ibx_utility.null` — it's not called `.empty` in order to make room for a bag whose name really is `empty`.

### 4.4 Traits of the dropped-item form

While the bag lies on the ground as a dropped item (ItemEntity):

| Trait | Effect |
| --- | --- |
| Fireproof | Fire and lava can't burn it |
| Damage-immune | Explosions, cactus, and all other damage have no effect |
| Never despawns | It never disappears due to timeout |
| Glowing | Always-on glowing outline, visible through walls |

> **The one exception is the void**: an item that falls out the bottom of the world is deleted outright; that path bypasses all damage checks and can't be stopped.

### 4.5 Retexturing a bag

Different kinds of bags can look different. This is **decided by the resource pack — no code changes, no data-pack cooperation needed** — just drop a model file named after the type into this folder:

```
assets/<namespace>/models/item/loot_bag/<type>.json
```

For example, to reskin the Iron Golem bag, create `assets/ibx_utility/models/item/loot_bag/iron_golem.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "ibx_utility:item/loot_bag/iron_golem"
  }
}
```

Types with no matching file (as well as empty bags) all use the **fallback model**, i.e. these two files that ship with the mod:

| File | Purpose |
| --- | --- |
| `models/item/loot_bag.json` | The item's own model. The path is fixed by the item's registry name and **can't be moved**; its content is just one line of `parent` pointing to the file below |
| `models/item/loot_bag/default.json` | The actual definition of the fallback model, textured with `textures/item/loot_bag/default.png` |

> `default` is a **reserved name** and won't be treated as a bag type. Even if there really is a bag called `default`, after falling back it uses exactly this model, so the result is the same.
>
> Type names **cannot contain a slash**; subfolders of `loot_bag/` are not scanned.

**Why does the resource pack decide, not the data pack?** Bag types are defined by the data pack (server), but model baking happens during client resource loading — **long before connecting to a server** — when the client has no idea which types exist. So it has to be the other way around: whichever types the resource pack provides models for are the ones recognized.

> As an aside: 1.21.4's [item model definitions](https://minecraft.wiki/w/Items_model_definition) (`select` + `component` in `assets/<ns>/items/*.json`) do exactly this natively, pure JSON with zero code. 1.21.1 doesn't have it, so this mod implements equivalent logic itself.

### 4.6 Viewing drops in JEI

With [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) (JER) installed, JEI gains a "Loot Bag" category listing what each bag might drop, at what probability, and in what count. Search for the loot bag item in JEI to see all bags.

Drop entries are sorted by **probability, high to low**; ties break by rarity, then by creative-inventory order. The number on a slot is the **minimum count**; the full range is in the tooltip.

**Two necessary conditions:**

- **JER must be installed** (client). Vanilla has no public API to enumerate what a loot table might drop; this parsing is provided by JER. Without JER, the category doesn't appear at all.
- **You must be in a world**. Loot tables are purely server-side and the client doesn't have them; the mod syncs bag tables to the client on login and on `/reload`, giving JEI something to display.

> ⚠️ **Bag contents are synced to every client, whether or not JER is installed.** The server has no way to know what a client has installed, so it always sends them; JER only decides "show or not", while the data itself is always visible to the client. **This is the established cost of the mod enabling JEI display** — not having JER just means you can't see it, not that it's hidden.
>
> ⚠️ **What's shown is an approximation.** Weights, conditions, nested tables, and functions like `set_count` jointly determine the actual drops; no static preview can be fully accurate — conditional entries in particular tend to display inaccurately.

---

## 5. Loot tables in detail

This chapter is a hands-on guide to the `pools` inside a bag. This is the **vanilla loot table**, the same format used by chests and mob drops — if you already know vanilla loot tables, jump straight to [5.7 Context parameters and limits](#57-context-parameters-and-limits), the one place where they differ.

### 5.1 The table skeleton

```json
{
  "type": "ibx_utility:loot_bag",
  "pools": [ ... ],
  "functions": [ ... ],
  "random_sequence": "ibx_utility:loot_bag/iron_golem"
}
```

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `type` | No | `minecraft:generic` | The parameter set. **A bag's table must use `ibx_utility:loot_bag`**; see [5.7](#57-context-parameters-and-limits). |
| `pools` | No | `[]` | The list of pools. Leave it out and you get an empty table (the bag opens but gives nothing). |
| `functions` | No | `[]` | **Table-level functions**, applied to **every** item this table drops. |
| `random_sequence` | No | — | Random-sequence id, used to make drop results reproducible within one save. Usually just fill in the table's own id; leaving it out is fine too. |

> A wrong `type` **does not error**; see the warning at the end of [5.7](#57-context-parameters-and-limits).

### 5.2 Core concept: a pool = one independent draw

This is the most easily misunderstood part of the whole loot table. Remember three sentences first:

1. **Each pool is independent**, resolved in turn, and results **accumulate**.
2. **A single roll of a pool produces one entry** — `entries` is a "candidate pool", not a "checklist".
3. To make a bag "always give A, then randomly give B", write **two pools**, rather than stuffing A and B into one pool's entries.

The Iron Golem bag is written exactly like this: the first pool has only iron ingot as a candidate (so it's guaranteed), and the second pool picks between poppy and iron block by weight.

```
pool[0]: rolls=1, entries=[iron ingot]           → always gives 3-5 iron ingots
pool[1]: rolls=1, entries=[poppy(8), iron block(1)] → 8/9 chance poppy, 1/9 chance iron block
                                                       total: exactly 2 items per opening
```

### 5.3 Pool fields

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `entries` | **Yes** | — | The list of candidate entries. |
| `rolls` | **Yes** | — | Number of draws. Can be a number or a number provider (see [5.4](#54-rolls--how-many-draws)). |
| `bonus_rolls` | No | `0` | Extra draws from luck, multiplied by the player's luck value. |
| `conditions` | No | `[]` | The switch for the **whole pool**. If the conditions fail, the pool is skipped entirely. |
| `functions` | No | `[]` | Applied to every item **this pool** drops. |

The formula for the actual number of draws is:

```
draws = rolls + floor(bonus_rolls × player luck)
```

Luck comes from the player's luck attribute (Luck potion, etc.). Normally it's 0, so `bonus_rolls` has no effect.

### 5.4 rolls — how many draws

Writing a plain number means a fixed count. For a random count, use a **number provider**:

```json
"rolls": 3                                                  // fixed 3 times
"rolls": { "type": "minecraft:uniform", "min": 1, "max": 3 } // 1-3 times, inclusive, uniform
"rolls": { "type": "minecraft:binomial", "n": 3, "p": 0.5 }  // flip 3 coins, each 50% hit, result 0-3
```

| Provider | Description |
| --- | --- |
| `minecraft:constant` | `{"type":"minecraft:constant","value":2}`, equivalent to writing `2` directly |
| `minecraft:uniform` | Uniform random in the inclusive range `min`~`max` |
| `minecraft:binomial` | Binomial distribution, `n` trials each with probability `p`. Good for a bell-shaped "usually few, rarely many" |
| `minecraft:score` | Reads a scoreboard. **This mod can only use `"target": "this"`** (the opening player) |
| `minecraft:storage` | Reads command storage |
| `minecraft:enchantment_level` | **Unavailable in this mod** (needs an enchantment-level parameter) |

> The same syntax also applies to the `count` field in functions like `set_count`.

### 5.5 entries — what to draw

| `type` | Description |
| --- | --- |
| `minecraft:item` | Give one item. `name` is the item id. **Most common.** |
| `minecraft:tag` | An item tag. `name` is the tag id, and `expand` decides behavior (see below). |
| `minecraft:loot_table` | Nest another table. `value` is the table id, or inline a table directly. Good for reusing shared drops. |
| `minecraft:empty` | An empty entry that gives nothing. Used to mix "blanks" into a pool. |
| `minecraft:group` | **All** sub-entries execute. |
| `minecraft:alternatives` | Take the **first** sub-entry whose conditions pass (like if / else if). |
| `minecraft:sequence` | Execute in order until a sub-entry's conditions fail. |
| `minecraft:dynamic` | Block-entity only, **unusable in this mod**. |

**Common fields on every entry:**

| Field | Default | Description |
| --- | --- | --- |
| `weight` | `1` | Weight. The chance of being drawn within a pool = own weight ÷ the sum of all entry weights in the pool. |
| `quality` | `0` | Luck modifier. Actual weight = `max(floor(weight + quality × luck), 0)`. A positive value means "the luckier, the more likely". |
| `conditions` | `[]` | The entry's switch. If it fails, the entry **doesn't take part in this draw**. |
| `functions` | `[]` | Applied only to items this entry produces. |

**`minecraft:tag`'s `expand`** deserves separate note:

- `"expand": true` → **each item in the tag becomes its own entry**, each competing for weight (i.e. "pick one at random from the tag").
- `"expand": false` → **give all items in the tag at once** (i.e. "you get everything in the tag").

**Mixing in blanks with `empty`** is a very common technique:

```json
{
  "rolls": 1,
  "entries": [
    { "type": "minecraft:item", "name": "minecraft:diamond", "weight": 1 },
    { "type": "minecraft:empty", "weight": 9 }
  ]
}
```

This pool has a 10% chance to give one diamond and 90% to give nothing.

> To want "90% chance to give a diamond", besides using `empty` with weights, you can also attach a `minecraft:random_chance` condition to the entry — but the two differ in meaning: weights **compete with each other**, while conditions are **judged independently**.

### 5.6 functions — process what dropped

Functions can attach at three levels: **table level** (every item), **pool level** (every item in the pool), and **entry level** (that entry only). All of the following are usable in this mod:

| Function | Purpose |
| --- | --- |
| `minecraft:set_count` | Set the count. `count` supports number providers; `"add": true` adds to the existing count instead of overwriting |
| `minecraft:limit_count` | Clamp the count into a `{"min":x,"max":y}` range |
| `minecraft:set_components` | Set any item component (replaces the old `set_nbt` in 1.20.5+) |
| `minecraft:set_damage` | Set durability. Note `damage` is the **remaining durability fraction**, not the used fraction — `0.9` means 90% fresh, `0.1` means nearly broken. This is vanilla's counterintuitive design |
| `minecraft:enchant_randomly` | Random enchantment. Use `options` to limit the enchantment pool |
| `minecraft:enchant_with_levels` | Enchant by level, equivalent to the enchanting table |
| `minecraft:set_enchantments` | Specify exact enchantments and levels |
| `minecraft:set_attributes` | Add attribute modifiers |
| `minecraft:set_name` / `set_lore` | Set name/lore. **With an `entity` field, it can only be `"this"`** |
| `minecraft:set_potion` | Set the potion type |
| `minecraft:set_custom_data` | Set custom NBT data |
| `minecraft:furnace_smelt` | Replace the product with its smelting result |
| `minecraft:exploration_map` | Generate an explorer map (only needs the opening position, so it's usable) |
| `minecraft:filtered` / `sequence` / `reference` | Compose and reuse functions |

An entry with "random count + random enchantment" looks like this:

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

> Note the function field name is `"function"`, the condition field name is `"condition"`, and the entry field name is `"type"` — all three differ, and this is the most common slip-up.

### 5.7 Context parameters and limits

This is the **one** place a bag's table differs from an ordinary loot table, and the section that needs the most attention.

The information a loot table can read at runtime is decided by the **parameter set** (the table's `type` field). A chest table can read the chest position; a mob-drop table can read who the killer was and what weapon was used. A bag is opened by a player right-clicking, so the mod registers a dedicated parameter set `ibx_utility:loot_bag` that provides only two parameters:

| Parameter | Content |
| --- | --- |
| `minecraft:origin` | **The opening position** (the player's coordinates at the time) |
| `minecraft:this_entity` | **The player opening the bag** |

Every other parameter (killer, weapon, block, damage source, explosion radius, …) is **absent**, because none of those things exist in the act of opening a bag.

**What happens if you use a parameter that can't be read?** It won't crash, but it will **fail silently** — and that's exactly what makes it dangerous:

| Situation | Consequence |
| --- | --- |
| A condition can't read a parameter | The condition is permanently **false** → that entry **never drops**, or the pool is always skipped |
| A function can't read a parameter | The function becomes a **no-op** → it quietly does nothing |
| At load time | There's a **WARN** in the log: `Parameters [...] are not provided in this context`. **The table still loads** and isn't invalidated |

> The only exception is `minecraft:enchantment_active_check`, which actually throws.
>
> **So: when a bag drops nothing, or a function doesn't work, the first thing to do is search the server startup log for `are not provided in this context`.**

**Unavailable list** — the following conditions/functions depend on parameters a bag doesn't have; writing them is the same as writing nothing:

| Condition | Parameter it needs |
| --- | --- |
| `minecraft:killed_by_player` | Last-damage player |
| `minecraft:damage_source_properties` | Damage source |
| `minecraft:match_tool` | Tool |
| `minecraft:table_bonus` | Tool |
| `minecraft:block_state_property` | Block state |
| `minecraft:survives_explosion` | Explosion radius |
| `minecraft:random_chance_with_enchanted_bonus` | Attacker |
| `minecraft:enchantment_active_check` | Enchantment-active state (**this one throws**) |

| Function | Parameter it needs |
| --- | --- |
| `minecraft:apply_bonus` | Tool (fortune bonus, meaningless in a bag) |
| `minecraft:enchanted_count_increase` | Attacker (looting bonus, meaningless in a bag) |
| `minecraft:copy_state` | Block state |
| `minecraft:copy_components` | Block entity |
| `minecraft:explosion_decay` | Explosion radius |

**Semi-available** — anywhere that points at an entity **can only point at `this` (the opening player)**; filling in `attacker` / `attacking_player` / `direct_attacker` fails silently:

| Condition/function | Usable form |
| --- | --- |
| `minecraft:entity_properties` | `"entity": "this"` |
| `minecraft:entity_scores` | `"entity": "this"` |
| `minecraft:copy_name` | `"source": "this"` |
| `minecraft:set_name` / `set_lore` | `"entity": "this"`, or just omit `entity` |
| `minecraft:fill_player_head` | `"entity": "this"` |
| `minecraft:score` (number provider) | `"target": "this"` |

**Safe to use** (don't depend on any parameter, or only need the opening position / opening player):

`minecraft:random_chance`, `minecraft:value_check`, `minecraft:time_check`, `minecraft:weather_check`, `minecraft:location_check` (checks the opening position), `minecraft:inverted`, `minecraft:any_of`, `minecraft:all_of`, `minecraft:reference`, and all the functions listed in the [5.6](#56-functions--process-what-dropped) table.

> ⚠️ **A wrong `type` doesn't error.** If you misspell `"type"` as something like `ibx_utility:lootbag`, vanilla **won't** report any error and instead silently falls back to the `minecraft:generic` parameter set — which claims to "have every parameter", so the WARN above won't appear either, yet the bag may throw the moment it's opened. **When a table drops nothing, first check whether `type` is spelled correctly.**

### 5.8 Full example: Iron Golem bag

Putting the previous sections together, the complete table for an "Iron Golem bag" at `data/ibx_utility/loot_table/loot_bag/iron_golem.json` looks like this:

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

Breaking it down piece by piece:

- **First pool**: `rolls: 1`, only one candidate → **guaranteed** drop, and `set_count` randomizes the amount to 3-5 iron ingots.
- **Second pool**: `rolls: 1`, two candidates with weights 8 : 1 → 8/9 chance of 1-2 poppies, 1/9 chance of 1 iron block.
- **Total**: **exactly 2 items** per opening (one from each pool).

The matching translation (`§f` = white):

```json
"loot_bag.ibx_utility.iron_golem": "§fIron Golem"
```

The grant command:

```
/give @s ibx_utility:loot_bag[ibx_utility:loot_bag_type="iron_golem"] 1
```

> To verify what a table drops before formally adding a bag, you don't have to open bags repeatedly — roll it directly with the vanilla command:
> ```
> /loot spawn ~ ~ ~ loot ibx_utility:loot_bag/iron_golem
> ```

> The Iron Golem bag above is only a **documentation example**. **The mod itself ships no finished bags** — which bags exist, what they drop, and what they look like is left entirely to the modpack. The mod only provides the item, the parameter set, and that fallback model.

---

## 6. Quest Board and quests

### 6.1 How it plays

1. **Accept**: right-click the board. The ribbon bookmarks on the left are the pages; the single ribbon on the right is the built-in "Accepted Quests". Select a quest and press "Accept". To accept: requirements met, a one-time quest not done yet, a random quest on today's list, and below the active-quest limit (`maxActiveQuests`).
2. **Objectives**:
   - **Kill**: counted live, credited to the player who last hurt the mob — projectiles a player shot and tamed pets count for their owner. Kills before accepting don't count, nor do extra kills after the objective is done.
   - **Submit items**: taken from the inventory when you press "Submit" at the board; can be handed in over several trips.
   - **Collect items**: the inventory is counted at the moment you submit; nothing is taken.
3. **Submit**: once every objective is done, press "Submit" at the board. The quest completes and rewards are given; whatever doesn't fit drops at your feet.
4. **Abandon**: at the board, at any time. Progress is reset; no penalty.
5. **Teams**: quests with `team` can be done as a team.
   - Accepting one opens a team; others can see it at the board and join, up to `max` members.
   - Progress is shared and a mob only counts once; collect objectives count the submitter's inventory.
   - Anyone can submit. Every online member gets a full reward and one completion; offline members get no reward but have the completion recorded when they log in.
   - If the leader leaves or logs out, the earliest remaining member takes over.
   - A player who already finished a one-time quest can still join someone else's team for it, and still gets the reward and another completion.
6. **Daily random**: quests with `pool` set to `random` go into the random pool. Every in-game day (every 24000 ticks of game time; sleeping doesn't skip ahead) the server draws `dailyQuestCount` of them by weight, without repeats. A random quest you already accepted can still be finished after the day changes.
7. **Feedback**: only the tracker in the top-left corner (off by default, see [2](#2-configuration)) — no pop-ups, chat messages or sounds. Without the tracker, check progress on the board's "Accepted Quests" page.

> Accepting, joining, submitting and abandoning all require standing within 8 blocks of the board you opened.

### 6.2 Pages

The bookmarks on the left of the board are driven by a data-pack registry, written the same way as forge categories (3.2).

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
| `icon` | No | `minecraft:book` | Item id of the icon on the bookmark; falls back to a book if the item doesn't exist. |

Built-in pages (each can be turned off in the config, see [2](#2-configuration)):

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

### 6.3 Quests

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
| `icon` | No | `minecraft:book` | Item id of the icon in the list and the tracker; falls back to a book if the item doesn't exist. |
| `category` | **Yes** | — | Which page (page id). **If that page doesn't exist (typo, or turned off in the config), the whole quest is skipped**: not shown, can't be accepted, never drawn; players who already accepted it lose it on their next login. |
| `pool` | No | `fixed` | `fixed` is always posted; `random` goes into the daily random pool and only shows up on days it's drawn. |
| `weight` | No | `1` | Weight in the random pool; only matters for `random`. |
| `requirements` | No | none | Requirements; all must be met to see the quest, accept it or join a team for it (see [6.4](#64-objectives-rewards-and-requirements)). |
| `repeatable` | No | `false` | `false` can be done once; `true` can be done again and again. |
| `team` | No | none | Allows teams; `max` is the team size limit, default 4, minimum 2. |
| `objectives` | **Yes** | — | Objectives, at least one (see [6.4](#64-objectives-rewards-and-requirements)). |
| `rewards` | No | none | Rewards (see [6.4](#64-objectives-rewards-and-requirements)). |
| `actions` | No | none | A list of quest actions for each of `accept` / `submit` / `abandon`. **This mod ships no action types**; other mods register them (see [6.5](#65-api-and-events-for-developers)). |

The smallest quest only needs the three required fields:

```json
{
  "name": "Firewood",
  "category": "ibx_utility:free_quests",
  "objectives": [ { "type": "ibx_utility:submit_item", "item": "#minecraft:logs", "count": 16 } ]
}
```

> **The mod itself ships no quests**, only the two default pages. Which quests exist and how they're arranged is entirely up to the modpack.

### 6.4 Objectives, rewards and requirements

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

### 6.5 API and events for developers

**`ibx.utility.api.QuestApi`**: read methods work on both sides (the client only has its own data); write methods are server-only.

| Method | Description |
| --- | --- |
| `activeQuests(player)` | Accepted quests, in the order they were accepted. |
| `isActive(player, quest)` / `completions(player, quest)` | Whether it's accepted / how many times it was completed. |
| `progress(player, quest, objective)` | Stored progress of objective number `objective` (0-based); for team quests, the team's shared progress. |
| `addProgress(player, quest, objective, amount)` | Adds progress to one objective (capped; team quests add to the team), e.g. when "the boss was defeated". Returns how much was actually added. |
| `addProgress(player, objective -> amount)` | Adds progress to objectives across all of the player's accepted quests, computing the amount per objective; handy for your own objective types. |
| `abandon(player, quest)` | Makes the player abandon a quest, same as abandoning at the board. |

**Events** `ibx.utility.api.event.QuestEvent`, server-only, posted on `NeoForge.EVENT_BUS`; all have `getPlayer()` and `getQuest()`:

| Event | Cancellable | When |
| --- | --- | --- |
| `QuestEvent.Accept` | Yes | Accepting: all checks passed, not yet written to the player's data. |
| `QuestEvent.Join` | Yes | Joining someone's team; `getTeam()` is the team id. |
| `QuestEvent.Progress` | No | An objective's progress went up: `getObjective()`, `getOldProgress()`, `getNewProgress()`, `getTeam()` (null for solo quests). Collect objectives store no progress and don't fire it. |
| `QuestEvent.Submit` | Yes | Every objective is done, rewards not given yet. When cancelled the quest stays, and items handed in this time still count as progress. |
| `QuestEvent.Abandon` | No | Abandoning, before progress is cleared. |

When cancelled, the player sees "You can't do that right now" on the board.

**Registering new types**: objectives, rewards, requirements and quest actions each have a registry, with keys in `ibx.utility.registry.QuestTypeRegistries`. The registered value is the type's `MapCodec`; write its id as `"type"` in quest JSON to use it:

| Registry | Interface to implement |
| --- | --- |
| `ibx_utility:quest_objective_type` (`OBJECTIVE_TYPE_KEY`) | `QuestObjective`: `onKill` / `onSubmit` decide how progress grows, `isComplete` decides when it's done, `display*` decide how it's shown |
| `ibx_utility:quest_reward_type` (`REWARD_TYPE_KEY`) | `QuestReward`: `grant` gives it, `displayIcon` etc. decide how it's shown |
| `ibx_utility:quest_requirement_type` (`REQUIREMENT_TYPE_KEY`) | `QuestRequirement`: `test` |
| `ibx_utility:quest_action_type` (`ACTION_TYPE_KEY`) | `QuestAction`: `run`, executed on accept / submit / abandon |

```java
public static final DeferredRegister<MapCodec<? extends QuestObjective>> OBJECTIVES =
        DeferredRegister.create(QuestTypeRegistries.OBJECTIVE_TYPE_KEY, "examplemod");
public static final DeferredHolder<MapCodec<? extends QuestObjective>, MapCodec<DefeatBossObjective>> DEFEAT_BOSS =
        OBJECTIVES.register("defeat_boss", () -> DefeatBossObjective.CODEC);

// when a boss is defeated
QuestApi.addProgress(player, objective -> objective instanceof DefeatBossObjective o && o.boss().equals(bossId) ? 1 : 0);
```

---

## 7. License

This mod is open-sourced under the [MIT License](LICENSE). You are free to use, modify, and distribute the mod and its source code, or bundle it into modpacks (including commercial ones), as long as you retain the copyright and license notice.

This mod bundles third-party libraries via jarJar; their copyright notices are in [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
