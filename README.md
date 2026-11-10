# ibx Utility

**简体中文** | [English](README/README_en.md)

---

## 简介

本模组添加一组数据驱动的功能方块与物品:装备锻造台、制箭台、战利品袋、悬赏板。配方、分类、掉落和任务都由数据包定义,整合包可自由增删改。

### 装备锻造台

**装备锻造台**(`ibx_utility:equipment_forge`)是一个以"选中配方即可制作"为核心的合成方块,用来取代原版工作台里繁琐的装备摆放。材料直接从背包扣除,界面是分类标签 + 搜索框 + 物品网格的列表式;配方和分类都由数据包定义。

详见 [装备锻造台教程](TUTORIAL/FORGE.md)。

### 制箭台

右键**原版制箭台**会打开制箭界面:箭尾、箭身、箭头、药水四格一排,放齐后右侧出成品。配方由数据包定义,内置箭、药箭、光灵箭三条。

详见 [制箭台教程](TUTORIAL/FLETCHING_TABLE.md)。

### 战利品袋

**战利品袋**(`ibx_utility:loot_bag`)右键即开,内容完全由数据包的战利品表定义,可以给每个 Boss 配一个专属袋子。掉落物形态免疫火焰 / 岩浆 / 爆炸、永不消失并常亮发光轮廓;资源包可以按种类换材质。

详见 [战利品袋教程](TUTORIAL/LOOT_BAG.md)。

### 悬赏板

**悬赏板**(`ibx_utility:quest_board`)是一块宽 2 格、高 2 格的木板,右键打开账本式的任务界面,接任务、交任务都在这里。任务完全由数据包定义:提交物品、击杀生物、收集物品三种目标,固定任务 + 每日随机,可以组队;别的模组还能注册新的目标、奖励和前置条件类型。

详见 [悬赏板教程](TUTORIAL/QUEST_BOARD.md)。

---

## 教程

| 文档 | 内容 |
| --- | --- |
| [装备锻造台](TUTORIAL/FORGE.md) | 界面特点、获取方式、锻造配方、分类定义 |
| [制箭台](TUTORIAL/FLETCHING_TABLE.md) | 用法、制箭配方 |
| [战利品袋](TUTORIAL/LOOT_BAG.md) | 新建袋子、组件与显示、换材质、在 JEI 中查看掉落、战利品表详解 |
| [悬赏板](TUTORIAL/QUEST_BOARD.md) | 玩法、分页、任务、目标 / 奖励 / 前置条件、给开发者的接口与事件 |
| [配置](TUTORIAL/CONFIG.md) | 通用配置、客户端配置、数据包条件 |

---

## 依赖

| 模组 | 类型 | 说明 |
| --- | --- | --- |
| NeoForge `21.1.234+` | **必需** | — |
| Minecraft `1.21.1` | **必需** | — |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) `19+` | 可选(客户端) | 提供「装备锻造」「制箭台」配方查询分类;在锻造台界面点击 JEI 的「+」可直接跳转选中该配方,在制箭台界面点击「+」可自动填入材料 |
| [JustEnoughResources](https://modrinth.com/mod/just-enough-resources-jer) `1.6+`(JER) | 可选(客户端) | 提供「战利品袋」分类,展示每个袋子的可能掉落与概率(见 [战利品袋 2.6](TUTORIAL/LOOT_BAG.md#26-在-jei-中查看掉落))。需同时装 JEI |
| [Beyond Dimensions](https://modrinth.com/mod/beyonddimensions) `0.7+`(超越维度) | 可选 | 锻造时可直接消耗玩家**主网络**中存储的物品,界面的可合成判定与材料计数也会计入 |

> 拼音检索所需的 pinyin4j 已通过 jarJar 打包进模组,无需额外安装。

---

## 许可证

本模组以 [MIT 许可证](LICENSE)开源。你可以自由地使用、修改、分发本模组及其源码,或将其打包进整合包(含商业整合包),只需保留版权与许可声明。

本模组通过 jarJar 打包了第三方库 **pinyin4j**(BSD 许可)。其版权声明见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。
