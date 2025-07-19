package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinListener implements Listener {

    private final Main plugin;

    public FirstJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {

            // Sende die konfigurierte Willkommensnachricht
            player.sendMessage(colorize(plugin.getFirstJoinMessage()));

            // Teleportiere den Spieler zum Spawn
            Location spawnLocation = plugin.getSpawnLocation();
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.sendMessage(colorize(plugin.getSpawnTeleportMessage()));
            } else {
                player.sendMessage(colorize(plugin.getNoSpawnMessage()));
            }
        }
    }

    private String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}