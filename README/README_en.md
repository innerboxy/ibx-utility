# ibx Utility

[简体中文](../README.md) | **English**

---

## Overview

This mod adds a set of data-driven utility blocks and items: the Equipment Forge, the Fletching Table, the Loot Bag and the Quest Board. Recipes, categories, drops and quests are all defined by data packs, so modpacks are free to add, remove, or change them.

### Equipment Forge

The **Equipment Forge** (`ibx_utility:equipment_forge`) is a crafting block built around "pick a recipe, then craft it" that replaces the fiddly item placement of the vanilla crafting table for gear. Materials are taken straight from your inventory, and the interface is a list: category tabs + a search box + an item grid. Recipes and categories are both defined by data packs.

See the [Equipment Forge tutorial](../TUTORIAL/FORGE_en.md).

### Fletching Table

Right-clicking the **vanilla fletching table** opens a fletching UI: tail, shaft, tip and potion slots sit in one row, and once filled the result appears on the right. Recipes are data-driven; arrows, tipped arrows and spectral arrows are built in.

See the [Fletching Table tutorial](../TUTORIAL/FLETCHING_TABLE_en.md).

### Loot Bag

The **Loot Bag** (`ibx_utility:loot_bag`) is opened by right-click, and its contents are defined entirely by a data-pack loot table, so you can give every boss its own dedicated bag. As a dropped item it is immune to fire / lava / explosions, never despawns and has an always-on glowing outline; a resource pack can retexture each kind.

See the [Loot Bag tutorial](../TUTORIAL/LOOT_BAG_en.md).

### Quest Board

The **Quest Board** (`ibx_utility:quest_board`) is a 2-wide, 2-tall wooden board. Right-click it to open a ledger-style quest screen; quests are accepted and handed in here. Quests are defined entirely by data packs: three objective types (submit items, kill mobs, collect items), fixed quests + daily random, and teams, where members get ready and the leader departs with them (where they go is up to other mods); other mods can also register new objective, reward, requirement and quest-action types.

See the [Quest Board tutorial](../TUTORIAL/QUEST_BOARD_en.md).

---

## Tutorials

| Document | Contents |
| --- | --- |
| [Equipment Forge](../TUTORIAL/FORGE_en.md) | Key features, how to obtain, forge recipes, category definitions |
| [Fletching Table](../TUTORIAL/FLETCHING_TABLE_en.md) | Usage, fletching recipes |
| [Loot Bag](../TUTORIAL/LOOT_BAG_en.md) | Creating a bag, component and display, retexturing, viewing drops in JEI, loot tables in detail |
| [Quest Board](../TUTORIAL/QUEST_BOARD_en.md) | How it plays, pages, quests, objectives / rewards / requirements, API and events for developers |
| [Configuration](../TUTORIAL/CONFIG_en.md) | Common config, client config, data-pack conditions |

---

## Dependencies

| Mod | Type | Notes |
| --- | --- | --- |
| NeoForge `21.1.234+` | **Required** | — |
| Minecraft `1.21.1` | **Required** | — |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) `19+` | Optional (client) | Adds "Equipment Forge" and "Fletching Table" recipe-lookup categories; clicking JEI's "+" in the forge UI jumps straight to that recipe with it selected, and in the fletching UI it fills in the materials |
| [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) `1.6+` (JER) | Optional (client) | Adds a "Loot Bag" category showing each bag's possible drops and probabilities (see [Loot Bag 2.6](../TUTORIAL/LOOT_BAG_en.md#26-viewing-drops-in-jei)). Requires JEI as well |
| [Beyond Dimensions](https://modrinth.com/mod/beyonddimensions) `0.7+` | Optional | When forging, items stored in the player's **main network** can be consumed directly; the UI's craftable check and material counting include them too |
| [Tom's Simple Storage](https://modrinth.com/mod/toms-storage) `2.10+` | Optional | When the forge touches an **Inventory Connector** or **Inventory Interface**, forging can consume items from that storage network directly; the UI's craftable check and material counting include them too (see [Equipment Forge · Material sources](../TUTORIAL/FORGE_en.md#material-sources)) |
| Sophisticated Storage / Storage Drawers / Functional Storage / AE2 / Refined Storage / Create | Optional | When the forge touches their controllers or interfaces, their items can be used too (blocks and limits listed in [Equipment Forge · Material sources](../TUTORIAL/FORGE_en.md#material-sources)) |

---

## License

This mod is open-sourced under the [MIT License](../LICENSE). You are free to use, modify, and distribute the mod and its source code, or bundle it into modpacks (including commercial ones), as long as you retain the copyright and license notice.

This mod bundles third-party libraries via jarJar; their copyright notices are in [THIRD-PARTY-NOTICES.md](../THIRD-PARTY-NOTICES.md).
