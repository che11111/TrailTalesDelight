# README 物品图标

- 尺寸：256×256 PNG。
- 二维图标：由原 16×16 图标使用最近邻算法进行 16 倍放大。
- 三维图标：`pottery_cooking_pot`、`mud_stove`、`lantern_fruit_crate`、`pitcher_taro_crate` 根据实际方块模型渲染。
- 文件命名与 CraftEngine 物品 ID 一致。

重新生成：

```bash
python3 tools/generate_readme_icons.py
```
