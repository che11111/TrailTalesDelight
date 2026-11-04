package dev.che1s.trailtalesdelight.config;

import com.huidu.farmersdelight.api.config.ConfigFileUpdater;
import com.huidu.farmersdelight.api.config.ConfigUpdatePolicy;
import dev.che1s.trailtalesdelight.util.AddonLang;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 将新设置合并到现有 config.yml。Bukkit 只会在文件不存在时写出内置配置，
 * 因此必须在加载文件之后、读取当前版本设置之前执行。
 */
public final class TrailConfigBootstrap {
    private static final ConfigUpdatePolicy POLICY = ConfigUpdatePolicy.builder()
            .registrySection("food-effects.comfort", "food-effects.nourishment")
            .build();

    private final JavaPlugin plugin;

    public TrailConfigBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        try {
            ConfigFileUpdater.updateMainConfig(plugin, POLICY);
        } catch (Exception exception) {
            plugin.getLogger().warning(AddonLang.get(
                    "plugin.config_update_failed",
                    "Could not update config.yml: {error}",
                    "error", String.valueOf(exception.getMessage())
            ));
        }
    }
}
