package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final Main plugin;

    public JoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Lade den gespeicherten Nickname
        String nickname = plugin.getNickname(player.getUniqueId());
        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }

        // Deaktiviere die Standard-Join-Nachricht
        event.setJoinMessage(null);

        // Sende die neue Nachricht mit grünem "+1" und gelbem Rest
        Bukkit.broadcastMessage(ChatColor.GREEN + "+1" + ChatColor.YELLOW + " | " + player.getDisplayName() + " joined the game");
    }
}