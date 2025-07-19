package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class RtpCommands implements CommandExecutor {

    private final Main plugin;
    private final Random random = new Random();

    public RtpCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("lolplugin.rtp")) {
            int maxAttempts = 10;
            int currentAttempt = 0;
            int range = 5000;
            Location randomLoc = null;

            while (currentAttempt < maxAttempts) {
                int x = random.nextInt(2 * range) - range;
                int z = random.nextInt(2 * range) - range;
                int y = player.getWorld().getHighestBlockYAt(x, z);

                if (y > 0) { // Stelle sicher, dass die Location auf festem Untergrund ist
                    randomLoc = new Location(player.getWorld(), x + 0.5, y + 1, z + 0.5);
                    break;
                }
                currentAttempt++;
            }

            if (randomLoc != null) {
// NEU: Verzögerte Teleportation über den Manager
                plugin.getTeleportationManager().teleportPlayer(player, randomLoc, 3);
            } else {
                player.sendMessage(ChatColor.RED + "Konnte keinen sicheren Ort für die Teleportation finden. Bitte versuche es erneut.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
        }
        return true;
    }
}