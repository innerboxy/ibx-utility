# Equipment Forge

[简体中文](FORGE.md) | **English**

[← Back to README](../README/README_en.md)

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
- **Data-driven categories**: the left-hand category tabs are defined entirely by the data pack; modpacks are free to add, remove, or change them (see [3](#3-category-definitions)).
- **Optionally remove vanilla recipes**: the vanilla crafting-table / smithing recipes superseded by the forge can be removed together via config, enabled by default (see [Configuration](CONFIG_en.md)).
- **No items lost when the inventory is full**: products that don't fit are dropped at the player's feet.

### How to obtain

The Equipment Forge is crafted in the **vanilla crafting table**:

```
Fe Fe Fe          Fe = Iron Ingot
Au CT Au          Au = Gold Ingot
Au    Au          CT = Crafting Table
```

---

## 2. Forge recipes

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

---

## 3. Category definitions

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
