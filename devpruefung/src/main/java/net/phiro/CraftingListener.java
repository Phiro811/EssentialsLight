package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;

public class CraftingListener implements Listener {

    private final Main plugin;
    private final NamespacedKey wardenKey;

    public CraftingListener(Main plugin) {
        this.plugin = plugin;
        this.wardenKey = new NamespacedKey(plugin, "custom_warden");
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe.getResult().getItemMeta() != null && recipe.getResult().getItemMeta().getDisplayName().equals("§5Warden Beschwörungsblock")) {

            Player crafter = (Player) event.getWhoClicked();

            event.setCancelled(true);

            for (int i = 1; i <= 9; i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null && item.getType() == Material.PLAYER_HEAD) {
                    event.getInventory().setItem(i, null);
                }
            }

            Location spawnLocation = crafter.getLocation();
            LivingEntity warden = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.WARDEN);

            // Setze den Namen des Wardens und mache ihn sichtbar
            warden.setCustomName(ChatColor.BOLD + "Trollden");
            warden.setCustomNameVisible(true);

            // Verhindere, dass der Warden despawnt oder sich eingräbt
            warden.setRemoveWhenFarAway(false);

            // Markiere den Warden
            warden.getPersistentDataContainer().set(wardenKey, PersistentDataType.STRING, "true");

            // Optimiere den Partikel-Task, indem Partikel nur bei sichtbaren Spielern gespawnt werden
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (warden.isValid() && warden.getNearbyEntities(50, 50, 50).stream().anyMatch(e -> e instanceof Player)) {
                    warden.getWorld().spawnParticle(Particle.CLOUD, warden.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0);
                }
            }, 0L, 10L);

            crafter.sendMessage(ChatColor.RED + "Du hast den Warden beschworen!");
            crafter.sendMessage(ChatColor.RED + "Viel Glück...");

            plugin.getServer().broadcastMessage(ChatColor.DARK_RED + crafter.getName() + " hat den Warden beschworen!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (event.getEntity() instanceof Warden) {
                Warden warden = (Warden) event.getEntity();

                if (warden.getPersistentDataContainer().has(wardenKey, PersistentDataType.STRING)) {
                    event.setCancelled(true);

                    warden.getWorld().playSound(warden.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);

                    if (projectile.getShooter() instanceof Player) {
                        Player shooter = (Player) projectile.getShooter();
                        shooter.sendMessage(ChatColor.GOLD + "Dieses Entity ist immun gegen Projektile!");
                    }
                }
            }
        }
    }
}