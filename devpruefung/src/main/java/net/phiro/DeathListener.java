package net.phiro;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

// Erzeuge einen Spielerkopf
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

// Setze die Kopftextur auf die des sterbenden Spielers
        skullMeta.setOwningPlayer(player);

// Aktualisiere das Item
        skull.setItemMeta(skullMeta);

// Füge den neuen Kopf den Drops des Spielers hinzu
// Die alten Items werden nicht mehr gelöscht
        event.getDrops().add(skull);

// Erzeuge einen Blitz an der Todesposition des Spielers
        player.getWorld().strikeLightningEffect(player.getLocation());
    }
}