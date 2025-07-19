package net.phiro;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackListener implements Listener {

    private final Main plugin;
    // Speichert die letzte Location VOR einer Teleportation, die NICHT durch /back oder Enderperle verursacht wurde.
    private final Map<UUID, Location> lastTeleportFromLocations = new HashMap<>();
    // Speichert die letzte Todes-Location
    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();

    public BackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location fromLocation = event.getFrom();
        UUID playerUUID = event.getPlayer().getUniqueId();

        // Überprüfen, ob dies ein Teleport ist, der vom /back-Befehl unseres Plugins ausgelöst wurde.
        // Wenn ja, speichern wir die "from"-Location NICHT, um Rekursion zu vermeiden.
        // Das isCurrentlyBackTeleporting-Flag wird vom TeleportationManager gesetzt und direkt hier zurückgesetzt.
        if (plugin.getTeleportationManager().isCurrentlyBackTeleporting(playerUUID)) {
            plugin.getTeleportationManager().setCurrentlyBackTeleporting(playerUUID, false); // Flag sofort zurücksetzen
            return;
        }

        // Speichere die Location, es sei denn, es ist eine Enderperle.
        // Andere Teleports (z.B. /warp, /home, /tpa) sollen als "last-location" speicherbar sein.
        if (event.getCause() != TeleportCause.ENDER_PEARL) {
            lastTeleportFromLocations.put(playerUUID, fromLocation);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Location deathLocation = event.getEntity().getLocation();
        // Speichere die Position nur, wenn sie nicht in der Void ist (z.B. Y > 0)
        if (deathLocation.getBlockY() > 0) {
            lastDeathLocations.put(event.getEntity().getUniqueId(), deathLocation);
        } else {
            event.getEntity().sendMessage(plugin.getDeathLocationVoidMessage());
        }
    }

    // Liefert die letzte Location vor einem "Nicht-Back/Enderperlen"-Teleport
    public Location getLastTeleportFromLocation(UUID playerUUID) {
        return lastTeleportFromLocations.get(playerUUID);
    }

    // Liefert die letzte Todes-Location
    public Location getLastDeathLocation(UUID playerUUID) {
        return lastDeathLocations.get(playerUUID);
    }

    // Gibt es überhaupt eine "Back"-Location (entweder Tod oder Teleport-From)?
    public boolean hasAnyBackLocation(UUID playerUUID) {
        return lastDeathLocations.containsKey(playerUUID) || lastTeleportFromLocations.containsKey(playerUUID);
    }
}