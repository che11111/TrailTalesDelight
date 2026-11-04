package dev.che1s.trailtalesdelight.listener;

import com.huidu.farmersdelight.api.FarmersDelightApi;
import com.huidu.farmersdelight.api.advancement.AdvancementAvailability;
import com.huidu.farmersdelight.api.advancement.FarmersDelightAdvancements;
import com.huidu.farmersdelight.api.effect.FarmersDelightFoodEffects;
import com.huidu.farmersdelight.api.event.FarmersDelightWarmupEvent;
import com.huidu.farmersdelight.api.item.FarmersDelightItems;
import dev.che1s.trailtalesdelight.TrailTalesDelightPlugin;
import dev.che1s.trailtalesdelight.util.AddonLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class TrailContentListener implements Listener {
    public static final String TAB_ID = "trailandtales_delight";
    public static final String ROOT = "root";

    private final TrailTalesDelightPlugin plugin;
    private final Set<String> registeredComfort = new HashSet<>();
    private final Set<String> registeredNourishment = new HashSet<>();
    private final InteractionDeduplicator interactionDeduplicator = new InteractionDeduplicator();

    public TrailContentListener(TrailTalesDelightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCustomBlockInteract(CustomBlockInteractEvent event) {
        if (event.action() != CustomBlockInteractEvent.Action.RIGHT_CLICK) {
            return;
        }
        String id = event.customBlock().id().toString();
        if (!SLICEABLE.containsKey(id)
                && !FEASTS.containsKey(id)
                && !"trailandtales_delight:curd_block".equals(id)) {
            return;
        }
        if (isDuplicateInteraction(event, id)) {
            event.setCancelled(true);
            return;
        }
        ItemStack mainHand = event.player().getInventory().getItemInMainHand();
        ItemStack offHand = event.player().getInventory().getItemInOffHand();
        if ("trailandtales_delight:curd_block".equals(id)
                && holdsKnife(mainHand, offHand)) {
            event.player().sendActionBar(Component.text(AddonLang.get(
                    "curd.cannot_cut",
                    "The curd block cannot be cut. Wait until it ripens into a cheese wheel at light level 8 or below."
            )).color(NamedTextColor.GOLD));
            event.player().swingMainHand();
            event.setCancelled(true);
        } else if (SLICEABLE.containsKey(id)) {
            handleSliceable(event, SLICEABLE.get(id));
        } else if (FEASTS.containsKey(id)) {
            handleFeast(event, FEASTS.get(id));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent event) {
        Block block = event.getBlock();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (block.getType() == Material.CHERRY_LEAVES) {
            double chance = plugin.getConfig().getDouble("vanilla-loot.cherry-leaf-petal-chance", 0.02);
            if (random.nextDouble() < chance) {
                addDrop(event, "trailandtales_delight:cherry_petal",
                        random.nextInt(1, 3), Material.PINK_PETALS);
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSnifferDrop(EntityDropItemEvent event) {
        if (event.getEntityType() != EntityType.SNIFFER) {
            return;
        }
        double chance = plugin.getConfig().getDouble(
                "vanilla-loot.sniffer-dig-lantern-seed-chance", 0.33D);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }
        ItemStack seeds = FarmersDelightItems.create("trailandtales_delight:lantern_fruit_seeds");
        if (seeds != null && !seeds.getType().isAir()) {
            event.getEntity().getWorld().dropItemNaturally(
                    event.getItemDrop().getLocation(), seeds);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArchaeologyLoot(LootGenerateEvent event) {
        if (event.getLootTable().getKey() == null
                || !event.getLootTable().getKey().equals(LootTables.DESERT_WELL_ARCHAEOLOGY.getKey())) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double potChance = plugin.getConfig().getDouble(
                "vanilla-loot.desert-well-pottery-pot-chance", 0.10D);
        double stoveChance = plugin.getConfig().getDouble(
                "vanilla-loot.desert-well-mud-stove-chance", 0.10D);
        if (random.nextDouble() < potChance) {
            event.getLoot().add(FarmersDelightItems.createOrFallback(
                    "trailandtales_delight:pottery_cooking_pot", Material.CAULDRON));
        }
        if (random.nextDouble() < stoveChance) {
            event.getLoot().add(FarmersDelightItems.createOrFallback(
                    "trailandtales_delight:mud_stove", Material.FURNACE));
        }
    }

    @EventHandler
    public void onWarmup(FarmersDelightWarmupEvent event) {
        registerAdvancement();
        registerFoodEffects();
        plugin.refreshRecipeBook();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (FarmersDelightAdvancements.availability() == AdvancementAvailability.AVAILABLE
                && !FarmersDelightAdvancements.has(TAB_ID, player, ROOT)) {
            FarmersDelightAdvancements.award(TAB_ID, player, ROOT);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        interactionDeduplicator.forget(event.getPlayer().getUniqueId());
    }

    public void registerAdvancement() {
        if (!FarmersDelightApi.get().isAvailable()
                || FarmersDelightAdvancements.availability() != AdvancementAvailability.AVAILABLE) {
            return;
        }
        ItemStack icon = FarmersDelightItems.createOrFallback(
                "trailandtales_delight:pottery_cooking_pot", Material.CAULDRON);
        FarmersDelightAdvancements.tree(TAB_ID)
                .root(ROOT, icon,
                        TAB_ID + ".advancement.root",
                        TAB_ID + ".advancement.root.desc",
                        "trailandtales_delight:textures/screen/advancement_background.png")
                .register();
    }

    public void registerFoodEffects() {
        if (!FarmersDelightApi.get().isAvailable()) {
            return;
        }
        unregisterFoodEffects();
        registerFoodEffects("comfort", true);
        registerFoodEffects("nourishment", false);
    }

    public void unregisterFoodEffects() {
        if (!FarmersDelightApi.get().isAvailable()) {
            return;
        }
        for (String id : registeredComfort) {
            FarmersDelightFoodEffects.unregisterComfortFood(id);
        }
        for (String id : registeredNourishment) {
            FarmersDelightFoodEffects.unregisterNourishmentFood(id);
        }
        registeredComfort.clear();
        registeredNourishment.clear();
    }

    private void registerFoodEffects(String sectionName, boolean comfort) {
        var section = plugin.getConfig().getConfigurationSection("food-effects." + sectionName);
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String id = section.getString(key + ".id");
            int duration = section.getInt(key + ".duration", 0);
            if (id == null || duration <= 0) {
                continue;
            }
            if (comfort) {
                FarmersDelightFoodEffects.registerComfortFood(id, duration);
                registeredComfort.add(id);
            } else {
                FarmersDelightFoodEffects.registerNourishmentFood(id, duration);
                registeredNourishment.add(id);
            }
        }
    }

    private void handleSliceable(CustomBlockInteractEvent event, SliceFood food) {
        Player player = event.player();
        ImmutableBlockState state = event.blockState();
        IntegerProperty bitesProperty = (IntegerProperty) state.getProperty("bites");
        if (bitesProperty == null) {
            plugin.getLogger().warning("Block '" + event.customBlock().id()
                    + "' is missing its bites property");
            sendActionBar(player,
                    "interaction.unavailable",
                    "The food block data is unavailable. Please restart the server.",
                    NamedTextColor.RED);
            event.setCancelled(true);
            return;
        }
        int bites = state.get(bitesProperty);
        boolean knife = holdsKnife(
                player.getInventory().getItemInMainHand(),
                player.getInventory().getItemInOffHand());
        if (knife) {
            ItemStack slice = requireItem(player, food.sliceId(),
                    "slice.unavailable",
                    "The sliced item failed to load. Please restart the server.");
            if (slice == null) {
                event.setCancelled(true);
                return;
            }
            dropAtBlock(event.location(), slice);
            advanceSlice(event, state, bitesProperty, bites);
            player.getWorld().playSound(event.location(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1F, 1F);
            player.swingMainHand();
            event.setCancelled(true);
            return;
        }

        if (player.getFoodLevel() >= 20) {
            sendActionBar(player,
                    "slice.food_full",
                    "You are not hungry enough to eat this.",
                    NamedTextColor.GOLD);
            event.setCancelled(true);
            return;
        }
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + food.nutrition()));
        float saturation = Math.min(20F, player.getSaturation() + food.saturation());
        player.setSaturation(saturation);
        if (food.regenerationTicks() > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, food.regenerationTicks(), 0, false, false));
        }
        player.getWorld().playSound(event.location(), Sound.ENTITY_GENERIC_EAT, 0.8F, 1F);
        advanceSlice(event, state, bitesProperty, bites);
        player.swingMainHand();
        event.setCancelled(true);
    }

    private void advanceSlice(CustomBlockInteractEvent event, ImmutableBlockState state,
                              IntegerProperty bitesProperty, int bites) {
        if (bites >= bitesProperty.max) {
            CraftEngineBlocks.remove(event.bukkitBlock(), event.player(), false, false, true);
            return;
        }
        CraftEngineBlocks.place(event.location(), state.with(bitesProperty, bites + 1), false);
    }

    private void handleFeast(CustomBlockInteractEvent event, FeastFood food) {
        Player player = event.player();
        ImmutableBlockState state = event.blockState();
        IntegerProperty servingsProperty = (IntegerProperty) state.getProperty("servings");
        if (servingsProperty == null) {
            plugin.getLogger().warning("Block '" + event.customBlock().id()
                    + "' is missing its servings property");
            sendActionBar(player,
                    "interaction.unavailable",
                    "The food block data is unavailable. Please restart the server.",
                    NamedTextColor.RED);
            event.setCancelled(true);
            return;
        }
        int servings = state.get(servingsProperty);
        ImmutableBlockState replacement = servings <= 1 ? eggshellBlock() : null;
        if (servings <= 1 && replacement == null) {
            plugin.getLogger().warning(
                    "Block 'trailandtales_delight:sniffer_eggshell' is not loaded");
            sendActionBar(player,
                    "interaction.unavailable",
                    "The food block data is unavailable. Please restart the server.",
                    NamedTextColor.RED);
            event.setCancelled(true);
            return;
        }
        if (servings <= 0) {
            CraftEngineBlocks.place(event.location(), replacement, false);
            sendActionBar(player,
                    "feast.empty",
                    "There is no food left here.",
                    NamedTextColor.GOLD);
            event.setCancelled(true);
            return;
        }
        if (food.requiresBowl()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (mainHand.getType() != Material.BOWL && offHand.getType() != Material.BOWL) {
                sendActionBar(player,
                        "feast.bowl_required",
                        "Hold a bowl to take a serving.",
                        NamedTextColor.GOLD);
                event.setCancelled(true);
                return;
            }
        }
        ItemStack serving = requireItem(player, food.servingItem(),
                "feast.unavailable",
                "The serving item failed to load. Please restart the server.");
        if (serving == null) {
            event.setCancelled(true);
            return;
        }
        if (food.requiresBowl()) {
            consumeBowl(player);
        }
        dropAtBlock(event.location(), serving);
        if (servings == 1) {
            CraftEngineBlocks.place(event.location(), replacement, false);
        } else {
            CraftEngineBlocks.place(event.location(), state.with(servingsProperty, servings - 1), false);
        }
        player.getWorld().playSound(event.location(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1F, 1F);
        player.swingMainHand();
        event.setCancelled(true);
    }

    private static ImmutableBlockState eggshellBlock() {
        var definition = CraftEngineBlocks.byId(Key.of("trailandtales_delight:sniffer_eggshell"));
        return definition == null ? null : definition.defaultState();
    }

    private ItemStack requireItem(Player player, String itemId, String messageKey, String fallback) {
        ItemStack item = FarmersDelightItems.create(itemId);
        if (item != null && !item.getType().isAir() && item.getType() != Material.BARRIER) {
            return item;
        }
        plugin.getLogger().warning("Custom item '" + itemId + "' resolved as "
                + (item == null ? "null" : item.getType()));
        sendActionBar(player, messageKey, fallback, NamedTextColor.RED);
        return null;
    }

    private static void consumeBowl(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.BOWL) {
            player.getInventory().setItemInMainHand(decrement(mainHand));
            return;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.BOWL) {
            player.getInventory().setItemInOffHand(decrement(offHand));
        }
    }

    private static ItemStack decrement(ItemStack item) {
        ItemStack remainder = item.clone();
        remainder.setAmount(remainder.getAmount() - 1);
        return remainder.getAmount() <= 0 ? null : remainder;
    }

    private static void sendActionBar(Player player, String key, String fallback, NamedTextColor color) {
        player.sendActionBar(Component.text(AddonLang.get(key, fallback)).color(color));
    }

    private static void dropAtBlock(Location location, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        location.getWorld().dropItemNaturally(
                location.clone().add(0.5, 0.5, 0.5), item.clone());
    }

    private static boolean holdsKnife(ItemStack mainHand, ItemStack offHand) {
        return FarmersDelightItems.isKnife(mainHand)
                || FarmersDelightItems.isKnife(offHand);
    }

    private boolean isDuplicateInteraction(CustomBlockInteractEvent event, String blockId) {
        Player player = event.player();
        Location location = event.location();
        return interactionDeduplicator.isDuplicate(
                player.getUniqueId(),
                blockId,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                player.getTicksLived()
        );
    }

    private static void addDrop(BlockDropItemEvent event, String customId, int amount, Material fallback) {
        ItemStack item = FarmersDelightItems.createOrFallback(customId, fallback);
        item.setAmount(amount);
        event.getItems().add(event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation().add(0.5, 0.5, 0.5), item));
    }

    private record SliceFood(String sliceId, int nutrition, float saturation, int regenerationTicks) {
    }

    private record FeastFood(String servingItem, boolean requiresBowl) {
    }

    private static final Map<String, SliceFood> SLICEABLE = Map.of(
            "trailandtales_delight:cherry_cake",
            new SliceFood("trailandtales_delight:cherry_cake_slice", 4, 4F, 100),
            "trailandtales_delight:cherry_cheese_pie",
            new SliceFood("trailandtales_delight:cherry_cheese_pie_slice", 3, 1.5F, 100),
            "trailandtales_delight:cheese_wheel",
            new SliceFood("trailandtales_delight:cheese_slice", 5, 2.5F, 0)
    );

    private static final Map<String, FeastFood> FEASTS = Map.of(
            "trailandtales_delight:cooked_sniffer_egg_block",
            new FeastFood("trailandtales_delight:fried_sniffer_egg", false),
            "trailandtales_delight:stuffed_sniffer_egg_block",
            new FeastFood("trailandtales_delight:stuffed_sniffer_egg", true)
    );
}
