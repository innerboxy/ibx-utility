# 制箭台

**简体中文** | [English](FLETCHING_TABLE_en.md)

[← 返回首页](../README.md)

---

## 1. 简介

右键**原版制箭台**会打开制箭界面。方块本身不变,村民制箭师照常把它当工作站点。

- 箭尾、箭身、箭头、药水四格一排,放齐后右侧出成品;每次合成四格各消耗 1 个,Shift 点击成品一次做到材料用完。
- 每格只接受配方里用在该位置的材料,Shift 点击背包物品会自动进对应格子;空槽有线稿底纹提示放什么。
- 配方由数据包定义(见 [2](#2-制箭配方))。内置三条,都是羽毛 + 木棍 + 燧石打底、每次出 8 支:
  - 药水格留空 → 箭;
  - 药水格放**滞留药水** → 带该药水效果的药箭(药水整瓶消耗,不退空瓶);
  - 药水格放**荧石粉** → 光灵箭。
- 原版工作台的箭、药箭、光灵箭配方由独立开关 `removeVanillaArrowRecipes` 控制是否移除(见 [配置](CONFIG.md))。
- 装了 JEI 可查「制箭台」分类;在制箭台界面里打开 JEI 配方页,点成品右侧的「+」可把背包里的材料自动填进格子。

---

## 2. 制箭配方

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
