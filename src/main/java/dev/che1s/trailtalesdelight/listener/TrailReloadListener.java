package dev.che1s.trailtalesdelight.listener;

import com.huidu.farmersdelight.api.event.FarmersDelightReloadEvent;
import dev.che1s.trailtalesdelight.TrailTalesDelightPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** 通过 FarmersDelight 的重载生命周期刷新本附属的全部运行时状态。 */
public final class TrailReloadListener implements Listener {
    private final TrailTalesDelightPlugin plugin;

    public TrailReloadListener(TrailTalesDelightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFarmersDelightReload(FarmersDelightReloadEvent event) {
        plugin.reloadAddon(event.getReason());
    }
}
