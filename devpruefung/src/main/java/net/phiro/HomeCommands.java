package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public HomeCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();

        if (command.getName().equalsIgnoreCase("sethome")) {
            if (!player.hasPermission("lolplugin.sethome")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /sethome <Name>");
                return true;
            }

            String homeName = args[0].toLowerCase();
            Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

            int maxHomes = getMaxHomes(player);
            if (homes.size() >= maxHomes) {
                player.sendMessage(ChatColor.RED + "Du hast bereits die maximale Anzahl von " + maxHomes + " Homes.");
                return true;
            }

            plugin.getHomeManager().setHome(player.getUniqueId(), homeName, player.getLocation());
            plugin.getHomeManager().saveHomes();
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' wurde gesetzt.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("home")) {
            if (!player.hasPermission("lolplugin.home")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /home <Name>");
                return true;
            }

            String homeName = args[0].toLowerCase();
            Location home = plugin.getHomeManager().getHome(player.getUniqueId(), homeName);

            if (home == null) {
                player.sendMessage(ChatColor.RED + "Home '" + homeName + "' wurde nicht gefunden.");
                return true;
            }

// Verzögerte Teleportation über den Manager
            plugin.getTeleportationManager().teleportPlayer(player, home, 3);
            return true;
        }

        if (command.getName().equalsIgnoreCase("removehome")) {
            if (!player.hasPermission("lolplugin.removehome")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /removehome <Name>");
                return true;
            }

            String homeName = args[0].toLowerCase();
            Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

            if (!homes.containsKey(homeName)) {
                player.sendMessage(ChatColor.RED + "Home '" + homeName + "' wurde nicht gefunden.");
                return true;
            }

            plugin.getHomeManager().removeHome(player.getUniqueId(), homeName);
            plugin.getHomeManager().saveHomes();
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' wurde entfernt.");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("home") || command.getName().equalsIgnoreCase("removehome")) {
            if (args.length == 1) {
                Set<String> homeNames = plugin.getHomeManager().getHomes(player.getUniqueId()).keySet();
                String partialName = args[0].toLowerCase();
                return homeNames.stream()
                        .filter(name -> name.startsWith(partialName))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private int getMaxHomes(Player player) {
        int maxHomes = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith("lolplugin.maxhomes.")) {
                try {
                    String numberString = permission.substring("lolplugin.maxhomes.".length());
                    int homesFromPerm = Integer.parseInt(numberString);
                    if (homesFromPerm > maxHomes) {
                        maxHomes = homesFromPerm;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        if (maxHomes == 0) {
            maxHomes = plugin.getConfig().getInt("settings.max-homes", 3);
        }

        return maxHomes;
    }
}