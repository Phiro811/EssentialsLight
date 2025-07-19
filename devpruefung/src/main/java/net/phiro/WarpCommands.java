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

public class WarpCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public WarpCommands(Main plugin) {
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

        if (command.getName().equalsIgnoreCase("setwarp")) {
            if (!player.hasPermission("lolplugin.setwarp")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /setwarp <Name>");
                return true;
            }

            String warpName = args[0].toLowerCase();
            Map<String, Location> warps = plugin.getWarpManager().getWarps();

            int maxWarps = getMaxWarps(player);
            if (warps.size() >= maxWarps) {
                player.sendMessage(ChatColor.RED + "Du hast bereits die maximale Anzahl von " + maxWarps + " Warps gesetzt.");
                return true;
            }

            plugin.getWarpManager().setWarp(warpName, player.getLocation());
            plugin.getWarpManager().saveWarps();
            player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' wurde gesetzt.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("warp")) {
            if (!player.hasPermission("lolplugin.warp")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /warp <Name>");
                return true;
            }

            String warpName = args[0].toLowerCase();
            Location warp = plugin.getWarpManager().getWarp(warpName);

            if (warp == null) {
                player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' wurde nicht gefunden.");
                return true;
            }

// Verzögerte Teleportation über den Manager
            plugin.getTeleportationManager().teleportPlayer(player, warp, 3);
            return true;
        }

        if (command.getName().equalsIgnoreCase("removewarp")) {
            if (!player.hasPermission("lolplugin.removewarp")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /removewarp <Name>");
                return true;
            }

            String warpName = args[0].toLowerCase();
            Map<String, Location> warps = plugin.getWarpManager().getWarps();

            if (!warps.containsKey(warpName)) {
                player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' wurde nicht gefunden.");
                return true;
            }

            plugin.getWarpManager().removeWarp(warpName);
            plugin.getWarpManager().saveWarps();
            player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' wurde entfernt.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("warpinfo")) {
            if (!player.hasPermission("lolplugin.warpinfo")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /warpinfo <Name>");
                return true;
            }

            String warpName = args[0].toLowerCase();
            Location warp = plugin.getWarpManager().getWarp(warpName);

            if (warp == null) {
                player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' wurde nicht gefunden.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "--- Warp-Informationen: " + warpName + " ---");
            player.sendMessage(ChatColor.YELLOW + "Welt: " + ChatColor.WHITE + warp.getWorld().getName());
            player.sendMessage(ChatColor.YELLOW + "Koordinaten: " + ChatColor.WHITE +
                    "X: " + (int) warp.getX() + ", Y: " + (int) warp.getY() + ", Z: " + (int) warp.getZ());
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("warp") || command.getName().equalsIgnoreCase("removewarp") || command.getName().equalsIgnoreCase("warpinfo")) {
            if (args.length == 1) {
                Set<String> warpNames = plugin.getWarpManager().getWarps().keySet();
                String partialName = args[0].toLowerCase();
                return warpNames.stream()
                        .filter(name -> name.startsWith(partialName))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private int getMaxWarps(Player player) {
        int maxWarps = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith("lolplugin.maxwarps.")) {
                try {
                    String numberString = permission.substring("lolplugin.maxwarps.".length());
                    int warpsFromPerm = Integer.parseInt(numberString);
                    if (warpsFromPerm > maxWarps) {
                        maxWarps = warpsFromPerm;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        if (maxWarps == 0) {
            maxWarps = plugin.getConfig().getInt("settings.max-warps", 3);
        }

        return maxWarps;
    }
}