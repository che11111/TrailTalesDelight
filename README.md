<p align="center">
  <img src="img/TrailTalesDelight-CE.png" alt="TrailTalesDelight-CE Logo" width="128">
</p>

<h1 align="center">TrailTalesDelight-CE</h1>

<p align="center">
  <a href="#installation-and-requirements"><img src="https://img.shields.io/badge/Minecraft-1.21.5%2B-3fb950" alt="Minecraft 1.21.5+"></a>
  <a href="https://github.com/Xiao-MoMi/craft-engine"><img src="https://img.shields.io/badge/CraftEngine-26.9.1%2B_API-5865F2" alt="CraftEngine 26.9.1+ API"></a>
  <a href="https://github.com/IOVEYOUMC0/Farmersdelight-Plugin"><img src="https://img.shields.io/badge/FarmersDelight-1.0.3-E67E22" alt="FarmersDelight 1.0.3"></a>
  <a href="#installation-and-requirements"><img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21"></a>
  <a href="#installation-and-requirements"><img src="https://img.shields.io/badge/Paper-supported-blue" alt="Paper supported"></a>
  <a href="#installation-and-requirements"><img src="https://img.shields.io/badge/Folia-supported-blueviolet" alt="Folia supported"></a>
</p>

<p align="center"><b>English</b> · <a href="README-zh.md">简体中文</a></p>

This is an unofficial port of [Trail-And-Tales-Delight](https://github.com/Vzling/Trail-And-Tales-Delight) for Paper servers using CraftEngine and the FarmersDelight plugin.

Paper, CraftEngine, and the Forge mod API behave differently, so this port cannot reproduce the original mod with 100% fidelity. The known differences are listed below.

The original mod is licensed under MIT. This port is licensed under AGPL-3.0.

## Installation and Requirements

- Server: Paper 1.21.5+ or the corresponding Folia version. Tested on `paper26.3` and `folia26.3`.
- Required: [CraftEngine](https://github.com/Xiao-MoMi/craft-engine) 26.9.1+
- Required: [FarmersDelight-Plugin](https://github.com/IOVEYOUMC0/Farmersdelight-Plugin)
- Optional: [UltimateAdvancementAPI](https://github.com/IOVEYOUMC0/UltimateAdvancementAPI)
- Place the plugin in `plugins/` and fully restart the server.
- CraftEngine resources are installed automatically under `plugins/CraftEngine/resources/trailandtales_delight/`.
- Add `minecraft:pitcher_plant: { style: item }` under `items` in `plugins/FarmersDelight/display-overrides.yml` so the cutting board displays pitcher plants as flat items.

## Item Overview

### 1. Crops, Drops, and Basic Materials

| Item | Acquisition | Item | Acquisition |
|---|---|---|---|
| <img src="img/items/cherry_petal.png" alt="Cherry Petal" width="64" height="64"><br><small>Cherry Petal</small> | 2% extra drop chance when breaking cherry leaves. | <img src="img/items/dried_cherry_petal.png" alt="Dried Cherry Petal" width="64" height="64"><br><small>Dried Cherry Petal</small> | Process cherry petals in a furnace, smoker, or campfire. |
| <img src="img/items/pitcher_taro.png" alt="Pitcher Taro" width="64" height="64"><br><small>Pitcher Taro</small> | Cut vanilla pitcher plants with a knife or unpack a pitcher taro crate. | <img src="img/items/cooked_pitcher_taro.png" alt="Cooked Pitcher Taro" width="64" height="64"><br><small>Cooked Pitcher Taro</small> | Cook pitcher taro in a furnace, smoker, or campfire. |
| <img src="img/items/lantern_fruit.png" alt="Lantern Fruit" width="64" height="64"><br><small>Lantern Fruit</small> | Right-click a mature lantern fruit vine or unpack a lantern fruit crate. | <img src="img/items/golden_lantern_fruit.png" alt="Golden Lantern Fruit" width="64" height="64"><br><small>Golden Lantern Fruit</small> | Craft with one lantern fruit surrounded by eight gold nuggets; harvesting also has a 5% extra drop chance. |
| <img src="img/items/lantern_fruit_seeds.png" alt="Lantern Fruit Seeds" width="64" height="64"><br><small>Lantern Fruit Seeds</small> | About a 33% extra drop chance from sniffer digging; breaking the crop returns seeds. | <img src="img/items/sniffer_eggshell.png" alt="Sniffer Eggshell" width="64" height="64"><br><small>Sniffer Eggshell</small> | Taking the final serving from a cooked or stuffed sniffer egg replaces the feast with an eggshell. |

### 2. Food and Drinks

| Item | Acquisition | Item | Acquisition |
|---|---|---|---|
| <img src="img/items/fried_sniffer_egg.png" alt="Fried Sniffer Egg" width="64" height="64"><br><small>Fried Sniffer Egg</small> | Cook a sniffer egg in a smoker or campfire, or take a serving from a cooked sniffer egg feast. | <img src="img/items/stuffed_sniffer_egg.png" alt="Stuffed Sniffer Egg" width="64" height="64"><br><small>Stuffed Sniffer Egg</small> | Craft fried sniffer egg, cooked pitcher taro, and a bowl, or take a serving from a stuffed sniffer egg feast. |
| <img src="img/items/baked_torchflower_seeds.png" alt="Baked Torchflower Seeds" width="64" height="64"><br><small>Baked Torchflower Seeds</small> | Cook torchflower seeds in a furnace, smoker, or campfire. | <img src="img/items/baked_pitcher_pod.png" alt="Baked Pitcher Pod" width="64" height="64"><br><small>Baked Pitcher Pod</small> | Cook pitcher pods in a furnace, smoker, or campfire. |
| <img src="img/items/ancient_coffee.png" alt="Ancient Coffee" width="64" height="64"><br><small>Ancient Coffee</small> | Cooking pot: water bucket, cocoa beans, and torchflower, served in a glass bottle. | <img src="img/items/torchflower_tea.png" alt="Torchflower Tea" width="64" height="64"><br><small>Torchflower Tea</small> | Cooking pot: water bucket, torchflower, and sugar, served in a glass bottle. |
| <img src="img/items/cherry_petal_tea.png" alt="Cherry Petal Tea" width="64" height="64"><br><small>Cherry Petal Tea</small> | Cooking pot: water bucket, cherry petal, and sugar, served in a glass bottle. | <img src="img/items/pitcher_plant_tea.png" alt="Pitcher Plant Tea" width="64" height="64"><br><small>Pitcher Plant Tea</small> | Cooking pot: water bucket, vanilla pitcher plant, and sugar, served in a glass bottle. |
| <img src="img/items/cheese_slice.png" alt="Cheese Slice" width="64" height="64"><br><small>Cheese Slice</small> | Cut a cheese wheel on a cutting board for four slices. | <img src="img/items/cherry_cheese_slice.png" alt="Cherry Cheese Slice" width="64" height="64"><br><small>Cherry Cheese Slice</small> | Craft a cheese slice with a cherry petal. |
| <img src="img/items/cherry_cake_slice.png" alt="Cherry Cake Slice" width="64" height="64"><br><small>Cherry Cake Slice</small> | Cut a cherry cake on a cutting board for four slices. | <img src="img/items/cherry_cheese_pie_slice.png" alt="Cherry Cheese Pie Slice" width="64" height="64"><br><small>Cherry Cheese Pie Slice</small> | Cut a cherry cheese pie on a cutting board for four slices. |

### 3. Blocks, Tools, and Containers

| Item | Acquisition | Item | Acquisition |
|---|---|---|---|
| <img src="img/items/mud_stove.png" alt="Mud Stove" width="64" height="64"><br><small>Mud Stove</small> | Craft with eight packed mud around a campfire; also a 10% archaeology chance from desert well suspicious sand. Light it with flint and steel. | <img src="img/items/pottery_cooking_pot.png" alt="Pottery Cooking Pot" width="64" height="64"><br><small>Pottery Cooking Pot</small> | Craft with six bricks and a cauldron; also a 10% archaeology chance from desert well suspicious sand. |
| <img src="img/items/lantern_fruit_crate.png" alt="Lantern Fruit Crate" width="64" height="64"><br><small>Lantern Fruit Crate</small> | Craft from nine lantern fruits; unpack back into nine lantern fruits. | <img src="img/items/pitcher_taro_crate.png" alt="Pitcher Taro Crate" width="64" height="64"><br><small>Pitcher Taro Crate</small> | Craft from nine pitcher taros; unpack back into nine pitcher taros. |
| <img src="img/items/pottery_bowl.png" alt="Pottery Bowl" width="64" height="64"><br><small>Pottery Bowl</small> | Craft three bricks in a bowl pattern. | <img src="img/items/cherry_iron_knife.png" alt="Cherry Iron Knife" width="64" height="64"><br><small>Cherry Iron Knife</small> | Smith a FarmersDelight iron knife with cherry planks; no smithing template is required. |
| <img src="img/items/sniffer_eggshell_knife.png" alt="Sniffer Eggshell Knife" width="64" height="64"><br><small>Sniffer Eggshell Knife</small> | Smith a FarmersDelight iron knife with a sniffer egg or eggshell; no smithing template is required. | <img src="img/items/curd_block.png" alt="Curd Block" width="64" height="64"><br><small>Curd Block</small> | Cooking pot: four milk buckets and two sugar or honey; cooking time is 600 ticks. |
| <img src="img/items/cherry_cake.png" alt="Cherry Cake" width="64" height="64"><br><small>Cherry Cake</small> | Craft from cherry petals, milk, sugar, egg, and wheat; four slices can also be combined back into a cake. | <img src="img/items/cherry_cheese_pie.png" alt="Cherry Cheese Pie" width="64" height="64"><br><small>Cherry Cheese Pie</small> | Craft from three cherry cheese slices and a pie crust; four slices can also be combined back into a pie. |
| <img src="img/items/cheese_wheel.png" alt="Cheese Wheel" width="64" height="64"><br><small>Cheese Wheel</small> | Craft from four cheese slices, or ripen a curd block at light level 8 or below. | <img src="img/items/cooked_sniffer_egg_block.png" alt="Cooked Sniffer Egg Block" width="64" height="64"><br><small>Cooked Sniffer Egg Block</small> | Cook a sniffer egg in a furnace. |
| <img src="img/items/stuffed_sniffer_egg_block.png" alt="Stuffed Sniffer Egg Block" width="64" height="64"><br><small>Stuffed Sniffer Egg Block</small> | Craft from four stuffed sniffer eggs, or make it in a cooking pot. | | |

## Mod Comparison

Implemented:

- 33 items, 13 blocks, and all associated models and textures.
- Food effects, potion effects, container returns, slicing, and feast interactions.
- The pottery cooking pot, mud stove, curd aging, lantern fruit crops, and loot integrations.
- Crafting table, furnace, smoker, campfire, or FarmersDelight cooking-pot recipes for every food and drink.

Additional port features:

- Custom blocks render through CraftEngine item-display entities. CraftEngine resources update automatically when the plugin version changes.
- Chinese and English names, plus a dedicated `/fd recipe` category.
- Added survival recipes for the mud stove, pottery cooking pot, both knives, the pottery bowl, and both crop crates.

Known differences:

- Curd ripening uses random ticks rather than the original mod's fixed timer.
- The cooked sniffer egg currently provides four servings; the original changes to an eggshell after one serving.
- The original custom pitcher plant item cannot be placed and has no use beyond cutting-board processing. The port instead lets players cut vanilla pitcher plants on a FarmersDelight cutting board to obtain pitcher taro.

Not ported:

- Lantern fruit vines cannot climb FarmersDelight ropes.
- As of 2026-11-03, the public GitHub source still corresponds to the older `1.3` release, while Modrinth has published `2.4`. This plugin is based on `1.3` and does **not** include the `2.4` crystal tree, crystal crabapple, bamboo basket, bamboo tube rice, ice cream, or other later content.

## Block Presentation

All custom blocks use `item_display` entities for visual rendering.

| Block | CraftEngine ID | Carrier | Collision and State |
|---|---|---|---|
| Mud Stove | `trailandtales_delight:mud_stove` | `barrier` | Full-block collision; facing and lit state |
| Pottery Cooking Pot | `trailandtales_delight:pottery_cooking_pot` | `barrier` | Full-block collision |
| Lantern Fruit Crate | `trailandtales_delight:lantern_fruit_crate` | `barrier` | Full-block collision |
| Pitcher Taro Crate | `trailandtales_delight:pitcher_taro_crate` | `barrier` | Full-block collision |
| Sniffer Eggshell | `trailandtales_delight:sniffer_eggshell` | `barrier` | Full-block collision |
| Budding Lantern Fruit | `trailandtales_delight:budding_lantern_fruit` | `auto_state` | State changes with crop growth |
| Mature Lantern Fruit Vine | `trailandtales_delight:lantern_fruits` | `auto_state` | State changes with vine growth |
| Curd Block | `trailandtales_delight:curd_block` | `structure_void` | No collision; model changes with ripening stage |
| Cheese Wheel | `trailandtales_delight:cheese_wheel` | `structure_void` | No collision; model changes with remaining slices |
| Cherry Cake | `trailandtales_delight:cherry_cake` | `structure_void` | No collision; model changes with remaining slices |
| Cherry Cheese Pie | `trailandtales_delight:cherry_cheese_pie` | `structure_void` | No collision; model changes with remaining slices |
| Cooked Sniffer Egg | `trailandtales_delight:cooked_sniffer_egg_block` | `barrier` | Full-block collision; model changes with remaining servings |
| Stuffed Sniffer Egg | `trailandtales_delight:stuffed_sniffer_egg_block` | `barrier` | Full-block collision; model changes with remaining servings |

## Commands

```text
/fd recipe
/fd recipe trail_recipes
/fd recipe trailandtales_delight:trail_recipes
```

`cooking_pot`, `cutting_board`, and `special` are public FarmersDelight recipe categories. `trail_recipes` shows recipes executed by the addon's pottery cooking-pot station. This plugin does not register a separate command.

## Language

Simplified Chinese is the default server language, and English is bundled. Item, block, advancement, and tooltip names shown to players follow the client language. Resource-pack translations are located under `plugins/CraftEngine/resources/trailandtales_delight/resourcepack/assets/trailandtales_delight/lang/`.

```yaml
# plugins/TrailTalesDelight/config.yml
language: zh_cn # zh_cn or en_us
```

## Sponsorship and Support

If this project helps you, you can support development through Ko-fi or Afdian. Sponsors can request plugin installation, configuration, and troubleshooting.

<p align="center">
  <a href='https://ko-fi.com/J5C529ZWWL' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
  <a href='https://afdian.com/a/sss000' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://img.shields.io/badge/Afdian-Support-946CE6?style=for-the-badge&logo=afdian&logoColor=white' border='0' alt='Support on Afdian' /></a>
</p>
