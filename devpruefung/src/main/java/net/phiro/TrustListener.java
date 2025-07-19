package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.UUID;

public class TrustListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

// Stellen Sie sicher, dass der Empfänger des Schadens ein Spieler ist
        if (!(damaged instanceof Player)) {
            return;
        }

        Player defendingPlayer = (Player) damaged;
        Player attackingPlayer = null;

// Schritt 1: Identifiziere den Angreifer (den Spieler, der den Schaden verursacht hat)
        if (event.getDamager() instanceof Player) {
// Direkter Schaden (z.B. Nahkampfangriff)
            attackingPlayer = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
// Schaden durch ein Wurfgeschoss (z.B. Pfeil, Schneeball)
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource source = projectile.getShooter();

            if (source instanceof Player) {
                attackingPlayer = (Player) source;
            }
        }

// Wenn der Angreifer kein Spieler ist oder der Angreifer und der Verteidiger identisch sind, abbrechen
        if (attackingPlayer == null || attackingPlayer.equals(defendingPlayer)) {
            return;
        }

// Schritt 2: Prüfe, ob die Ursache des Schadens eine Explosion ist
// Hier wird sichergestellt, dass Explosionen NICHT abgebrochen werden
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            return;
        }

// Schritt 3: Führe die Vertrauensprüfung durch
        FileConfiguration config = Main.plugin.getConfig();

        UUID attackerUUID = attackingPlayer.getUniqueId();
        UUID defenderUUID = defendingPlayer.getUniqueId();

// Prüfen, ob beide Spieler sich gegenseitig getrustet haben
        List<String> attackerTrustedList = config.getStringList("trusted_players." + attackerUUID.toString());
        List<String> defenderTrustedList = config.getStringList("trusted_players." + defenderUUID.toString());

        if (attackerTrustedList.contains(defenderUUID.toString()) && defenderTrustedList.contains(attackerUUID.toString())) {
            event.setCancelled(true);
            attackingPlayer.sendMessage(ChatColor.RED + Main.plugin.getConfig().getString("messages.trust-no-pvp"));
        }
    }
}