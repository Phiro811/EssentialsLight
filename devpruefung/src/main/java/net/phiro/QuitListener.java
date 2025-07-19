package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Deaktiviere die Standard-Leave-Nachricht
        event.setQuitMessage(null);

        // Sende die neue Nachricht mit rotem "-1" und gelbem Rest
        Bukkit.broadcastMessage(ChatColor.RED + "-1" + ChatColor.YELLOW + " | " + player.getDisplayName() + " left the game");
    }
}