# 装备锻造台

**简体中文** | [English](FORGE_en.md)

[← 返回首页](../README.md)

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
- **数据驱动的分类**:左侧分类标签完全由数据包定义,整合包可自由增删改(见 [3](#3-分类定义))。
- **可选地移除原版配方**:被锻造台取代的原版工作台/锻造台配方可由配置统一移除,默认开启(见 [配置](CONFIG.md))。
- **背包满不丢物品**:制作时背包放不下的产物会掉落在玩家脚下。

### 获取方式

装备锻造台可在**原版工作台**中合成:

```
铁 铁 铁          铁 = 铁锭
金 台 金          金 = 金锭
金    金          台 = 工作台
```

---

## 2. 锻造配方

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

---

## 3. 分类定义

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
