# Fletching Table

[简体中文](FLETCHING_TABLE.md) | **English**

[← Back to README](../README/README_en.md)

---

## 1. Overview

Right-clicking the **vanilla fletching table** opens a fletching UI. The block itself is unchanged, and fletcher villagers still use it as their workstation.

- Tail, shaft, tip and potion slots sit in one row; once filled, the result appears on the right. Each craft consumes 1 item from each slot, and shift-clicking the result crafts until the materials run out.
- Each slot only accepts materials that some recipe uses in that position, so shift-clicking from the inventory sends items to the right slot; empty slots show an outline hint of what goes there.
- Recipes are data-driven (see [2](#2-fletching-recipes)). Three are built in, all starting from feather + stick + flint and yielding 8 per craft:
  - potion slot empty → arrows;
  - a **lingering potion** in the potion slot → tipped arrows with that potion's effect (the potion is used up; no empty bottle is returned);
  - **glowstone dust** in the potion slot → spectral arrows.
- Whether the vanilla crafting-table recipes for arrows, tipped arrows and spectral arrows are removed is controlled by a separate switch, `removeVanillaArrowRecipes` (see [Configuration](CONFIG_en.md)).
- With JEI installed there is a "Fletching Table" category; open a JEI recipe page from the fletching UI and click the "+" to the right of the result to fill the slots from your inventory.

---

## 2. Fletching recipes

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
