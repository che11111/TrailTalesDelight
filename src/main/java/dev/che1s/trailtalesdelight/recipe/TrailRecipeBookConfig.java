package dev.che1s.trailtalesdelight.recipe;

import com.huidu.farmersdelight.api.config.ConfigSectionReader;
import com.huidu.farmersdelight.api.gui.GuiItems;
import com.huidu.farmersdelight.api.recipe.RecipeBookLayout;
import com.huidu.farmersdelight.api.recipe.SimpleRecipeBookLayout;
import com.huidu.farmersdelight.api.text.FarmersDelightText;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 gui.yml 中的 trail-recipes，并向 FarmersDelight 配方书渲染器
 * 提供本附属的列表和详情布局。
 */
public final class TrailRecipeBookConfig {
    private static final List<String> LEGACY_DEFAULT_LIST_LAYOUT = List.of(
            "RRRRRRRRR",
            "RRRRRRRRR",
            "RRRRRRRRR",
            "RRRRRRRRR",
            "RRRRRRRRR",
            "PXXXBXXXN"
    );
    private static final List<List<String>> LEGACY_DEFAULT_DETAIL_LAYOUTS = List.of(
            List.of(
                    "BXXXXXXXX",
                    "XXIIXXRXX",
                    "XXIIXEXXX",
                    "XXXXXTXXX",
                    "XXXXXXXXX",
                    "XXXXXXXXX"
            ),
            List.of(
                    "BXXXXXXXM",
                    "XXXXXXXXX",
                    "XIIIXXXXX",
                    "XIIIXXXXX",
                    "XXTXXCXRX",
                    "XXXXXXXXX"
            )
    );
    private static final String DEFAULT_COOKING_POT_DETAIL_TITLE =
            "<white><shift:-2><image:farmersdelight:cooking_pot_menu>"
                    + "<shift:-163><black><lang:trailandtales_delight.recipe.detail_title>";
    private static final String DEFAULT_CATEGORY_TITLE =
            "<lang:block.trailandtales_delight.pottery_cooking_pot>";
    private static final String LEGACY_CATEGORY_TITLE =
            "<gold><lang:trailandtales_delight.recipe.category>";

    private final RecipeBookLayout list;
    private final RecipeBookLayout detail;
    private final Component categoryTitle;

    private TrailRecipeBookConfig(RecipeBookLayout list, RecipeBookLayout detail, Component categoryTitle) {
        this.list = list;
        this.detail = detail;
        this.categoryTitle = categoryTitle;
    }

    public RecipeBookLayout list() {
        return list;
    }

    public RecipeBookLayout detail() {
        return detail;
    }

    public Component categoryTitle() {
        return categoryTitle;
    }

    public static TrailRecipeBookConfig fromConfig(ConfigurationSection section) {
        ConfigurationSection listSection = section == null ? null : section.getConfigurationSection("list");
        ConfigurationSection detailSection = section == null ? null : section.getConfigurationSection("detail");
        String rawCategory = ConfigSectionReader.optionalString(section, "category-title");
        Component categoryTitle = rawCategory == null || LEGACY_CATEGORY_TITLE.equals(rawCategory)
                ? FarmersDelightText.deserializeGlyphsTitle(DEFAULT_CATEGORY_TITLE)
                : FarmersDelightText.deserializeGlyphsTitle(rawCategory);
        SimpleRecipeBookLayout detail = SimpleRecipeBookLayout.fromConfig(
                detailSection, defaultDetail());
        if (isLegacyDefaultDetail(detailSection)) {
            detail = defaultDetail();
        }
        SimpleRecipeBookLayout list = SimpleRecipeBookLayout.fromConfig(
                listSection, defaultList());
        if (isLegacyDefaultList(listSection)) {
            list = defaultList();
        }
        return new TrailRecipeBookConfig(
                list,
                detail,
                categoryTitle
        );
    }

    private static SimpleRecipeBookLayout defaultList() {
        Map<Character, String> legend = new HashMap<>();
        legend.put('R', "recipe");
        legend.put('P', "prev_page");
        legend.put('N', "next_page");
        legend.put('B', "back");
        legend.put('X', "background");
        return new SimpleRecipeBookLayout(
                FarmersDelightText.deserializeGlyphsTitle(
                        "<white><shift:-2><image:farmersdelight:recipelist>"
                                + "<shift:-163><black><lang:trailandtales_delight.recipe.list_title>"
                                + " <gray>{page}/{total}</gray>"),
                6,
                List.of(
                        "RRRRRRRRR",
                        "RRRRRRRRR",
                        "RRRRRRRRR",
                        "RRRRRRRRR",
                        "RRRRRRRRR",
                        "PXXXBXXXN"
                ),
                legend,
                defaultDecorations(true)
        );
    }

    private static SimpleRecipeBookLayout defaultDetail() {
        Map<Character, String> legend = new HashMap<>();
        legend.put('I', "ingredient");
        legend.put('R', "result");
        legend.put('T', "tool");
        legend.put('C', "return");
        legend.put('B', "back");
        legend.put('X', "background");
        return new SimpleRecipeBookLayout(
                FarmersDelightText.deserializeGlyphsTitle(DEFAULT_COOKING_POT_DETAIL_TITLE),
                6,
                List.of(
                        "BXXXXXXXX",
                        "XXXXXXXXX",
                        "XIIIXXXXX",
                        "XIIIXXXXX",
                        "XXTXXCXRX",
                        "XXXXXXXXX"
                ),
                legend,
                defaultDecorations(false)
        );
    }

    private static Map<String, ItemStack> defaultDecorations(boolean list) {
        YamlConfiguration defaults = new YamlConfiguration();
        String root = "items.";
        defaults.set(root + "background.material",
                list ? "GRAY_STAINED_GLASS_PANE" : "LIGHT_GRAY_STAINED_GLASS_PANE");
        defaults.set(root + "background.item_model", "minecraft:air");
        defaults.set(root + "background.name", " ");
        defaults.set(root + "background.hide-tooltip", true);
        if (list) {
            defaults.set(root + "back.material", "BARRIER");
            defaults.set(root + "back.name", "<red><lang:gui.recipe.close>");
            defaults.set(root + "prev_page.material", "ARROW");
            defaults.set(root + "prev_page.item_model", "minecraft:air");
            defaults.set(root + "prev_page.name", "<green><lang:gui.recipe.prev_page>");
            defaults.set(root + "next_page.material", "ARROW");
            defaults.set(root + "next_page.item_model", "minecraft:air");
            defaults.set(root + "next_page.name", "<green><lang:gui.recipe.next_page>");
        } else {
            defaults.set(root + "back.material", "ARROW");
            defaults.set(root + "back.item_model", "minecraft:air");
            defaults.set(root + "back.name", "<white><lang:gui.recipe.back>");
        }

        Map<String, ItemStack> decorations = new HashMap<>();
        ConfigurationSection items = defaults.getConfigurationSection("items");
        for (String key : items.getKeys(false)) {
            ItemStack item = GuiItems.build(items.getConfigurationSection(key));
            if (item != null) {
                decorations.put(key, item);
            }
        }
        return Map.copyOf(decorations);
    }

    private static boolean isLegacyDefaultDetail(ConfigurationSection section) {
        if (section == null) {
            return false;
        }
        String icon = ConfigSectionReader.optionalString(
                section, "title-layout.craftengine.icon", "");
        List<String> layout = ConfigSectionReader.optionalStringList(section, "layout");
        return ("<image:farmersdelight:recipelist>".equals(icon)
                && LEGACY_DEFAULT_DETAIL_LAYOUTS.contains(layout))
                || ("<image:farmersdelight:cooking_pot_menu>".equals(icon)
                && LEGACY_DEFAULT_DETAIL_LAYOUTS.getLast().equals(layout));
    }

    private static boolean isLegacyDefaultList(ConfigurationSection section) {
        if (section == null) {
            return false;
        }
        return LEGACY_DEFAULT_LIST_LAYOUT.equals(
                ConfigSectionReader.optionalStringList(section, "layout"));
    }

}
