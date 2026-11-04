package dev.che1s.trailtalesdelight.recipe;

import com.huidu.farmersdelight.api.item.FarmersDelightItems;
import com.huidu.farmersdelight.api.recipe.FarmersDelightRecipes;
import com.huidu.farmersdelight.api.recipe.RecipeBookLayout;
import com.huidu.farmersdelight.api.recipe.RecipeInfo;
import com.huidu.farmersdelight.api.recipe.RecipeType;
import com.huidu.farmersdelight.api.recipe.ViewableRecipe;
import dev.che1s.trailtalesdelight.util.AddonLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 由 FarmersDelight 配方注册表提供数据的独立附属配方页。
 * 烹饪锅和砧板配方仍由 CraftEngine 资源包中的 YAML 管理。
 */
public final class TrailRecipeType implements RecipeType {
    public static final String ID = "trailandtales_delight:trail_recipes";
    private static final String NS = "trailandtales_delight:";

    private volatile TrailRecipeBookConfig book;

    public void setBook(TrailRecipeBookConfig book) {
        this.book = book;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Component title() {
        TrailRecipeBookConfig current = book;
        return current == null
                ? Component.translatable("block.trailandtales_delight.pottery_cooking_pot")
                : current.categoryTitle();
    }

    @Override
    public ItemStack icon() {
        ItemStack icon = FarmersDelightItems.createOrFallback(
                "trailandtales_delight:pottery_cooking_pot", Material.DECORATED_POT);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.lore(null);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    @Override
    public List<ViewableRecipe> recipes() {
        List<ViewableRecipe> result = new ArrayList<>();
        for (String recipeId : FarmersDelightRecipes.cookingPotRecipeIds()) {
            if (belongsToThisAddon(recipeId)) {
                RecipeInfo info = FarmersDelightRecipes.cookingPotRecipe(recipeId);
                if (info != null) {
                    result.add(new TrailViewableRecipe(info));
                }
            }
        }
        return List.copyOf(result);
    }

    @Override
    public RecipeBookLayout listLayout() {
        TrailRecipeBookConfig current = book;
        return current == null ? null : current.list();
    }

    @Override
    public RecipeBookLayout detailLayout() {
        TrailRecipeBookConfig current = book;
        return current == null ? null : current.detail();
    }

    private static boolean belongsToThisAddon(String id) {
        if (id == null) {
            return false;
        }
        return id.startsWith(NS);
    }

    private record TrailViewableRecipe(RecipeInfo recipe) implements ViewableRecipe {
        @Override
        public String id() {
            return recipe.id();
        }

        @Override
        public List<ItemStack> inputs() {
            List<ItemStack> items = new ArrayList<>();
            for (String ingredient : recipe.ingredients()) {
                String firstChoice = ingredient.split("\\|", 2)[0];
                if (firstChoice.startsWith("#")) {
                    continue;
                }
                ItemStack item = FarmersDelightItems.create(firstChoice);
                if (item != null && !item.getType().isAir()) {
                    items.add(item);
                }
            }
            return List.copyOf(items);
        }

        @Override
        public List<ItemStack> listInputs() {
            return inputs();
        }

        @Override
        public ItemStack result() {
            if (recipe.results().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            return recipe.results().getFirst().clone();
        }

        @Override
        public Map<String, List<ItemStack>> displaySlots() {
            Map<String, List<ItemStack>> slots = new LinkedHashMap<>();
            if (recipe.container() != null && !recipe.container().getType().isAir()) {
                slots.put("return", List.of(recipe.container().clone()));
            }
            if (!recipe.tools().isEmpty()) {
                ItemStack knife = FarmersDelightItems.create("trailandtales_delight:cherry_iron_knife");
                if (knife != null && !knife.getType().isAir()) {
                    slots.put("tool", List.of(knife));
                }
            }
            return Map.copyOf(slots);
        }

        @Override
        public List<Component> infoLines(Player viewer) {
            String station = RecipeInfo.TYPE_COOKING_POT.equals(recipe.type())
                    ? AddonLang.get("recipe.station.cooking_pot", "烹饪锅")
                    : AddonLang.get("recipe.station.cutting_board", "砧板");
            String time = recipe.cookTimeTicks() > 0
                    ? AddonLang.get("recipe.cook_time", "烹饪时间：{seconds} 秒",
                    "seconds", String.format(Locale.ROOT, "%.1f", recipe.cookTimeTicks() / 20.0))
                    : "";
            List<Component> lines = new ArrayList<>(2);
            lines.add(Component.text(station).decoration(TextDecoration.ITALIC, false));
            if (!time.isBlank()) {
                lines.add(Component.text(time).decoration(TextDecoration.ITALIC, false));
            }
            return List.copyOf(lines);
        }
    }
}
