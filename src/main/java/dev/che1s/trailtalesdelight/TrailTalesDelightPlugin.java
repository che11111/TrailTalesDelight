package dev.che1s.trailtalesdelight;

import com.huidu.farmersdelight.api.FarmersDelightApi;
import com.huidu.farmersdelight.api.registry.ContentRegistration;
import com.huidu.farmersdelight.api.util.PluginManagerGuard;
import dev.che1s.trailtalesdelight.block.BuddingTransitionBehavior;
import dev.che1s.trailtalesdelight.block.CurdAgingBehavior;
import dev.che1s.trailtalesdelight.config.TrailConfigBootstrap;
import dev.che1s.trailtalesdelight.listener.TrailContentListener;
import dev.che1s.trailtalesdelight.listener.TrailReloadListener;
import dev.che1s.trailtalesdelight.recipe.TrailRecipeBookConfig;
import dev.che1s.trailtalesdelight.recipe.TrailRecipeType;
import dev.che1s.trailtalesdelight.util.AddonLang;
import dev.che1s.trailtalesdelight.util.AddonResourceInstaller;
import net.momirealms.craftengine.core.util.Key;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TrailTalesDelightPlugin extends JavaPlugin {
    public static final String NAMESPACE = "trailandtales_delight";
    private static final int BSTATS_PLUGIN_ID = 35102;
    private static final String RELOAD_GUARD = "trailandtalesdelight.enabled.in.this.jvm";

    private final TrailConfigBootstrap configBootstrap = new TrailConfigBootstrap(this);
    private TrailContentListener contentListener;
    private TrailRecipeType recipeType;
    private YamlConfiguration guiConfig;
    private Metrics metrics;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        AddonLang.init(this);

        Key budding = Key.of(NAMESPACE + ":budding_transition");
        if (!ContentRegistration.isRegistered(budding)) {
            ContentRegistration.registerBlockBehavior(budding, BuddingTransitionBehavior.FACTORY);
        }
        Key curd = Key.of(NAMESPACE + ":curd_aging");
        if (!ContentRegistration.isRegistered(curd)) {
            ContentRegistration.registerBlockBehavior(curd, CurdAgingBehavior.FACTORY);
        }

        String version = getPluginMeta().getVersion();
        AddonResourceInstaller.InstallResult installed = AddonResourceInstaller.install(
                this,
                NAMESPACE,
                version,
                getConfig().getBoolean("craftengine-resources.auto-completion", true),
                getConfig().getBoolean("craftengine-resources.update-on-version-change", true)
        );
        if (installed.changed()) {
            getLogger().info(AddonLang.get(
                    "plugin.resources_installed",
                    "Wrote {count} CraftEngine resource files and removed {removed} stale files for {namespace} {version}.",
                    "count", installed.copied(),
                    "removed", installed.removed(),
                    "namespace", NAMESPACE,
                    "version", version
            ));
        }
    }

    @Override
    public void onEnable() {
        if (System.getProperty(RELOAD_GUARD) != null) {
            getLogger().severe(AddonLang.get(
                    "plugin.unsafe-reload",
                    "Do not /reload this plugin; stop and start the server."
            ));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        System.setProperty(RELOAD_GUARD, "1");

        FarmersDelightApi api = FarmersDelightApi.get();
        if (api == null || !api.isAvailable()) {
            getLogger().warning(AddonLang.get(
                    "plugin.farmersdelight-unavailable",
                    "FarmersDelight is unavailable."
            ));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configBootstrap.updateConfig();
        reloadConfig();
        AddonLang.reload();

        if (!api.hasFeature("recipes")) {
            getLogger().warning(AddonLang.get(
                    "plugin.recipes-unsupported",
                    "This FarmersDelight build does not support addon recipe books."
            ));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        api.registerAddonBlockNamespace(NAMESPACE);
        guiConfig = loadGuiConfig();
        recipeType = new TrailRecipeType();
        recipeType.setBook(TrailRecipeBookConfig.fromConfig(
                guiConfig == null ? null : guiConfig.getConfigurationSection("trail-recipes")));
        api.registerRecipeType(recipeType);
        contentListener = new TrailContentListener(this);
        getServer().getPluginManager().registerEvents(contentListener, this);
        getServer().getPluginManager().registerEvents(new TrailReloadListener(this), this);
        getServer().getPluginManager().registerEvents(new PluginManagerGuard(getName()), this);
        contentListener.registerFoodEffects();

        getLogger().info(AddonLang.get(
                "plugin.enabled_summary",
                "TrailTalesDelight loaded: {items} items, {blocks} custom blocks, {recipes} station recipes.",
                "items", 33,
                "blocks", 13,
                "recipes", 10
        ));
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (contentListener != null && FarmersDelightApi.get().isAvailable()) {
            contentListener.unregisterFoodEffects();
            if (recipeType != null) {
                FarmersDelightApi.get().unregisterRecipeType(recipeType.id());
            }
            com.huidu.farmersdelight.api.advancement.FarmersDelightAdvancements.unregister(
                    TrailContentListener.TAB_ID);
        }
        getLogger().info(AddonLang.get("plugin.disabled", "TrailTalesDelight disabled."));
    }

    public void reloadAddon(String reason) {
        reloadConfig();
        AddonLang.reload();
        refreshRecipeBook();
        if (contentListener != null) {
            contentListener.registerFoodEffects();
        }
        getLogger().info(AddonLang.get(
                "plugin.reloaded_with_reason",
                "TrailTalesDelight reloaded: {reason}",
                "reason", reason
        ));
    }

    /**
     * 在 CraftEngine 加载字体和图片后重建配方书。
     * 配方标题会立即解析图片字形，因此只在 onEnable 中执行会丢失
     * 资源包仍在加载时的 FarmersDelight 彩绘配方列表标题。
     */
    public void refreshRecipeBook() {
        if (recipeType == null) {
            return;
        }
        guiConfig = loadGuiConfig();
        recipeType.setBook(TrailRecipeBookConfig.fromConfig(
                guiConfig == null ? null : guiConfig.getConfigurationSection("trail-recipes")));
        FarmersDelightApi.get().refreshRecipeType(recipeType.id());
    }

    private YamlConfiguration loadGuiConfig() {
        File file = new File(getDataFolder(), "gui.yml");
        if (!file.isFile()) {
            try {
                saveResource("gui.yml", false);
            } catch (IllegalArgumentException exception) {
                getLogger().warning(AddonLang.get(
                        "plugin.gui_missing",
                        "Bundled gui.yml is missing; using built-in recipe layouts."
                ));
                return null;
            }
        }
        try {
            return YamlConfiguration.loadConfiguration(file);
        } catch (RuntimeException exception) {
            getLogger().warning(AddonLang.get(
                    "plugin.gui_load_failed",
                    "Could not load gui.yml: {error}",
                    "error", String.valueOf(exception.getMessage())
            ));
            return null;
        }
    }
}
