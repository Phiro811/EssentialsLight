package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportationManager {

    private final Main plugin;
    private final Map<UUID, BukkitTask> activeTeleportTasks = new HashMap<>();
    private final Map<UUID, Location> startLocations = new HashMap<>();
// NEU: Temporäres Flag, um /back Teleports zu markieren
    private final Map<UUID, Boolean> currentlyBackTeleporting = new HashMap<>();

    public TeleportationManager(Main plugin) {
        this.plugin = plugin;
    }

// Überladene Methode ohne Grund
    public void teleportPlayer(Player player, Location destination, int delayInSeconds) {
        teleportPlayer(player, destination, delayInSeconds, "", false); // Standardmäßig nicht von /back ausgelöst
    }

// Überladene Methode mit optionalem Grund für die Nachricht
    public void teleportPlayer(Player player, Location destination, int delayInSeconds, String reasonMessage) {
        teleportPlayer(player, destination, delayInSeconds, reasonMessage, false); // Standardmäßig nicht von /back ausgelöst
    }

// Hauptmethode mit optionalem Grund und optionalem "isBackTeleport" Flag
    public void teleportPlayer(Player player, Location destination, int delayInSeconds, String reasonMessage, boolean isBackTeleport) {
        UUID playerUUID = player.getUniqueId();

        if (activeTeleportTasks.containsKey(playerUUID)) {
            player.sendMessage(plugin.getTeleportBusyMessage());
            return;
        }

// Setze das Flag, wenn dies ein /back Teleport ist, bevor der Teleport ausgeführt wird
// oder der Task gestartet wird. Dies muss erfolgen, bevor der PlayerTeleportEvent feuert.
        if (isBackTeleport) {
            setCurrentlyBackTeleporting(playerUUID, true);
        }

// Handle den Bypass-Fall zuerst, da hier der Teleport direkt erfolgt
        if (player.hasPermission("lolplugin.bypasscooldown")) {
            player.teleport(destination);
            player.sendMessage(plugin.getTeleportBypassMessage());
// Das Flag wird sofort nach dem direkten Teleport zurückgesetzt, falls es gesetzt wurde.
            if (isBackTeleport) {
                setCurrentlyBackTeleporting(playerUUID, false);
            }
            return;
        }

        startLocations.put(playerUUID, player.getLocation());
// Nachricht mit optionalem Grund hinzufügen (Standard ist leer, wenn nicht von /back aufgerufen)
        player.sendMessage(plugin.getTeleportStartMessage(delayInSeconds).replace("[reason]", reasonMessage));

        BukkitTask task = new BukkitRunnable() {
            int remainingSeconds = delayInSeconds;
            final UUID currentPlayerUUID = playerUUID; // Sicherstellen, dass die UUID im Lambda korrekt ist

            @Override
            public void run() {
// Prüfen, ob der Spieler noch online ist
                if (!player.isOnline()) {
                    clearTeleportState(currentPlayerUUID); // Zustand bereinigen (inkl. isBackTeleporting-Flag)
                    this.cancel(); // Eigener Task beenden
                    return;
                }

// Überprüfe auf Bewegung
                Location currentLoc = player.getLocation();
                Location storedStartLoc = startLocations.get(currentPlayerUUID);

                if (storedStartLoc != null &&
                        (currentLoc.getBlockX() != storedStartLoc.getBlockX() ||
                                currentLoc.getBlockY() != storedStartLoc.getBlockY() ||
                                currentLoc.getBlockZ() != storedStartLoc.getBlockZ())) {

                    player.sendMessage(plugin.getTeleportCancelledMessage());
                    cancelTeleport(currentPlayerUUID); // Rufe cancelTeleport auf, um den Task und das Flag zu löschen
                    return; // Der Task wird durch cancelTeleport beendet
                }

                if (remainingSeconds <= 0) {
// Teleportation durchführen
                    player.teleport(destination);
                    player.sendMessage(plugin.getTeleportSuccessMessage());
                    clearTeleportState(currentPlayerUUID); // Zustand bereinigen (inkl. isBackTeleporting-Flag)
                    this.cancel(); // Den Task beenden
                    return;
                }

// Countdown-Nachricht senden
                player.sendMessage(plugin.getTeleportCountdownMessage(remainingSeconds));
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Startet sofort, läuft jede Sekunde (20 Ticks)

        activeTeleportTasks.put(playerUUID, task); // Den Task speichern
    }

// Methode zum Abbrechen des Teleports (von Listenern oder Commands aufgerufen)
    public void cancelTeleport(UUID playerUUID) {
        if (activeTeleportTasks.containsKey(playerUUID)) {
            BukkitTask task = activeTeleportTasks.get(playerUUID);
            if (task != null && !task.isCancelled()) {
                task.cancel(); // Task direkt abbrechen
            }
            clearTeleportState(playerUUID); // Zustand bereinigen (inkl. isBackTeleporting-Flag)
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage(plugin.getTeleportCancelledMessage());
            }
        }
    }

// Private Methode, um den Teleport-Zustand zu löschen (Maps leeren)
    private void clearTeleportState(UUID playerUUID) {
        activeTeleportTasks.remove(playerUUID);
        startLocations.remove(playerUUID);
// Das 'currentlyBackTeleporting' Flag sollte hier auch entfernt werden, um sauber zu bleiben.
        currentlyBackTeleporting.remove(playerUUID);
    }

    public boolean isTeleporting(UUID playerUUID) {
        return activeTeleportTasks.containsKey(playerUUID);
    }

    public Location getStartLocation(UUID playerUUID) {
        return startLocations.get(playerUUID);
    }

// NEU: Getter und Setter für das temporäre isBackTeleporting Flag
// Dieses Flag signalisiert, dass der aktuelle Teleport ein "/back"-Teleport ist
// und seine "from"-Location NICHT als neuen "last-location" Punkt gespeichert werden soll.
    public boolean isCurrentlyBackTeleporting(UUID playerUUID) {
        return currentlyBackTeleporting.getOrDefault(playerUUID, false);
    }

    public void setCurrentlyBackTeleporting(UUID playerUUID, boolean value) {
        if (value) {
            currentlyBackTeleporting.put(playerUUID, true);
        } else {
            currentlyBackTeleporting.remove(playerUUID); // Entferne den Eintrag, wenn false
        }
    }
}