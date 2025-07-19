package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender; // <-- DIESE ZEILE WURDE HINZUGEFÜGT
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpawnCommands implements CommandExecutor {

    private final Main plugin;

    public SpawnCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("lolplugin.setspawn")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            Location playerLocation = player.getLocation();
            config.set("spawn.world", playerLocation.getWorld().getName());
            config.set("spawn.x", playerLocation.getX());
            config.set("spawn.y", playerLocation.getY());
            config.set("spawn.z", playerLocation.getZ());
            config.set("spawn.yaw", playerLocation.getYaw());
            config.set("spawn.pitch", playerLocation.getPitch());
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Spawn wurde gesetzt!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("lolplugin.spawn")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            Location spawnLocation = plugin.getSpawnLocation();
            if (spawnLocation == null) {
                player.sendMessage(ChatColor.RED + "Es wurde noch kein Spawn gesetzt.");
                return true;
            }

// Verzögerte Teleportation über den Manager
            plugin.getTeleportationManager().teleportPlayer(player, spawnLocation, 3);
            return true;
        }
        return false;
    }
}