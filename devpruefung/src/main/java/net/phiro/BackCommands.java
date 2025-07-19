package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BackCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public BackCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Location targetLocation = null;
        String teleportReason = ""; // Wird für die Nachricht verwendet

// Hole beide möglichen Rückkehrpunkte
        Location deathLocation = plugin.getBackListener().getLastDeathLocation(playerUUID);
        Location lastTeleportFromLocation = plugin.getBackListener().getLastTeleportFromLocation(playerUUID);

        if (args.length == 0) {
// Logik für "/back" ohne Argumente: Wähle den "nächsten zu jetzt" (zuletzt gespeicherten)
// Priorität: Todespunkt vor letzter Teleport-Location, da der Tod oft die dringendste Situation ist.
            if (deathLocation != null) {
                targetLocation = deathLocation;
                teleportReason = plugin.getBackTeleportReasonDeath();
            } else if (lastTeleportFromLocation != null) {
                targetLocation = lastTeleportFromLocation;
                teleportReason = plugin.getBackTeleportReasonLastLocation();
            }

        } else if (args.length == 1) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "death":
                    targetLocation = deathLocation;
                    teleportReason = plugin.getBackTeleportReasonDeath();
                    break;
                case "last-location":
                    targetLocation = lastTeleportFromLocation;
                    teleportReason = plugin.getBackTeleportReasonLastLocation();
                    break;
                default:
                    player.sendMessage(plugin.getBackUsageMessage());
                    return true;
            }
        } else {
            player.sendMessage(plugin.getBackUsageMessage());
            return true;
        }

        if (targetLocation == null) {
            player.sendMessage(plugin.getBackNoLocationFoundMessage());
            return true;
        }

// Verzögerte Teleportation über den Manager
        int delay = plugin.getConfig().getInt("settings.teleport-delay-seconds", 3);
// "true" als letztes Argument signalisiert, dass es ein /back Teleport ist,
// damit der BackListener diese Location nicht wieder als "last-location" speichert.
        plugin.getTeleportationManager().teleportPlayer(player, targetLocation, delay, teleportReason, true);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
// Zuerst prüfen, ob der Sender ein Spieler ist
        if (!(sender instanceof Player)) {
            return Collections.emptyList(); // Nur Spieler können Tab-Vervollständigung nutzen
        }

// Dann den Sender zu einem Spieler casten
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId(); // Jetzt können wir sicher auf getUniqueId() zugreifen

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
// Füge "death" hinzu, wenn eine Todeslocation vorhanden ist
            if (plugin.getBackListener().getLastDeathLocation(playerUUID) != null) {
                completions.add("death");
            }
// Füge "last-location" hinzu, wenn eine letzte Teleport-Location vorhanden ist
            if (plugin.getBackListener().getLastTeleportFromLocation(playerUUID) != null) {
                completions.add("last-location");
            }
// Filter für Tab Completion
            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}