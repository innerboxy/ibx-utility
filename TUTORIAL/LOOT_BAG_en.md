# Loot Bag

[简体中文](LOOT_BAG.md) | **English**

[← Back to README](../README/README_en.md)

---

## 1. Overview

The Loot Bag (`ibx_utility:loot_bag`) is a "right-click to open" item. It contains no drop content itself — **the bag is just a pointer** to a vanilla loot table, and that table decides everything it drops.

- **One bag, one table**: the bag's kind is stored in an item component and maps to a loot table of the same name, so you can give every boss its own dedicated bag.
- **Protected as a dropped item**: immune to fire/lava/explosions, never despawns, and has an always-on glowing outline (visible through walls), so it's fine to drop it right on the battlefield.
- **Retextured per kind**: drop one model file into a resource pack to make different bags look different — zero code (see [2.5](#25-retexturing-a-bag)).
- **View drops in JEI**: with JER installed, you can see each bag's possible drops and probabilities (see [2.6](#26-viewing-drops-in-jei)).
- No crafting recipe. An **empty bag** is available at the very front of the "Ingredients" creative tab; **specific kinds** can only be granted via `/give`, commands, or loot tables (kinds are defined by data packs, are open-ended in count, and are not enumerated in the creative tab).

---

## 2. Defining a bag

### 2.1 The three parts

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

### 2.2 Step by step: create an "Ender Dragon bag"

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

### 2.3 Component and display

| Component | Type | Description |
| --- | --- | --- |
| `ibx_utility:loot_bag_type` | string | The bag kind. **This is the bag's only component.** Absent, empty string, or missing → empty bag: can't be right-clicked, tooltip shows "empty". |

**The display name and color come entirely from the lang file**, with no automatic derivation:

- Translation found → shown as-is, color decided by the `§` format codes in the translation (no color code means it inherits the default color).
- **Translation not found → the tooltip shows the raw component string in red** (e.g. a red `ender_dragon`). This is a deliberate error hint, warning you that you forgot the translation or mistyped the type.

> Because color goes through `§` codes, **only the 16 vanilla colors** are available; `#RRGGBB` is not supported. And each language's translation must carry its own color code.
>
> The "empty" hint uses the key `loot_bag.ibx_utility.null` — it's not called `.empty` in order to make room for a bag whose name really is `empty`.

### 2.4 Traits of the dropped-item form

While the bag lies on the ground as a dropped item (ItemEntity):

| Trait | Effect |
| --- | --- |
| Fireproof | Fire and lava can't burn it |
| Damage-immune | Explosions, cactus, and all other damage have no effect |
| Never despawns | It never disappears due to timeout |
| Glowing | Always-on glowing outline, visible through walls |

> **The one exception is the void**: an item that falls out the bottom of the world is deleted outright; that path bypasses all damage checks and can't be stopped.

### 2.5 Retexturing a bag

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

### 2.6 Viewing drops in JEI

With [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) (JER) installed, JEI gains a "Loot Bag" category listing what each bag might drop, at what probability, and in what count. Search for the loot bag item in JEI to see all bags.

Drop entries are sorted by **probability, high to low**; ties break by rarity, then by creative-inventory order. The number on a slot is the **minimum count**; the full range is in the tooltip.

**Two necessary conditions:**

- **JER must be installed** (client). Vanilla has no public API to enumerate what a loot table might drop; this parsing is provided by JER. Without JER, the category doesn't appear at all.
- **You must be in a world**. Loot tables are purely server-side and the client doesn't have them; the mod syncs bag tables to the client on login and on `/reload`, giving JEI something to display.

> ⚠️ **Bag contents are synced to every client, whether or not JER is installed.** The server has no way to know what a client has installed, so it always sends them; JER only decides "show or not", while the data itself is always visible to the client. **This is the established cost of the mod enabling JEI display** — not having JER just means you can't see it, not that it's hidden.
>
> ⚠️ **What's shown is an approximation.** Weights, conditions, nested tables, and functions like `set_count` jointly determine the actual drops; no static preview can be fully accurate — conditional entries in particular tend to display inaccurately.

---

## 3. Loot tables in detail

This chapter is a hands-on guide to the `pools` inside a bag. This is the **vanilla loot table**, the same format used by chests and mob drops — if you already know vanilla loot tables, jump straight to [3.7 Context parameters and limits](#37-context-parameters-and-limits), the one place where they differ.

### 3.1 The table skeleton

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
| `type` | No | `minecraft:generic` | The parameter set. **A bag's table must use `ibx_utility:loot_bag`**; see [3.7](#37-context-parameters-and-limits). |
| `pools` | No | `[]` | The list of pools. Leave it out and you get an empty table (the bag opens but gives nothing). |
| `functions` | No | `[]` | **Table-level functions**, applied to **every** item this table drops. |
| `random_sequence` | No | — | Random-sequence id, used to make drop results reproducible within one save. Usually just fill in the table's own id; leaving it out is fine too. |

> A wrong `type` **does not error**; see the warning at the end of [3.7](#37-context-parameters-and-limits).

### 3.2 Core concept: a pool = one independent draw

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

### 3.3 Pool fields

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `entries` | **Yes** | — | The list of candidate entries. |
| `rolls` | **Yes** | — | Number of draws. Can be a number or a number provider (see [3.4](#34-rolls--how-many-draws)). |
| `bonus_rolls` | No | `0` | Extra draws from luck, multiplied by the player's luck value. |
| `conditions` | No | `[]` | The switch for the **whole pool**. If the conditions fail, the pool is skipped entirely. |
| `functions` | No | `[]` | Applied to every item **this pool** drops. |

The formula for the actual number of draws is:

```
draws = rolls + floor(bonus_rolls × player luck)
```

Luck comes from the player's luck attribute (Luck potion, etc.). Normally it's 0, so `bonus_rolls` has no effect.

### 3.4 rolls — how many draws

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

### 3.5 entries — what to draw

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

### 3.6 functions — process what dropped

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

### 3.7 Context parameters and limits

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

`minecraft:random_chance`, `minecraft:value_check`, `minecraft:time_check`, `minecraft:weather_check`, `minecraft:location_check` (checks the opening position), `minecraft:inverted`, `minecraft:any_of`, `minecraft:all_of`, `minecraft:reference`, and all the functions listed in the [3.6](#36-functions--process-what-dropped) table.

> ⚠️ **A wrong `type` doesn't error.** If you misspell `"type"` as something like `ibx_utility:lootbag`, vanilla **won't** report any error and instead silently falls back to the `minecraft:generic` parameter set — which claims to "have every parameter", so the WARN above won't appear either, yet the bag may throw the moment it's opened. **When a table drops nothing, first check whether `type` is spelled correctly.**

### 3.8 Full example: Iron Golem bag

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
