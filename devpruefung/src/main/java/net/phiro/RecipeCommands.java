package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecipeCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("recipe")) {
            player.sendMessage("§a--- Warden Rezept ---");
            player.sendMessage("§fUm den Warden zu beschwören, lege §b9 Spielerköpfe §fin einem 3x3-Gitter in die Werkbank.");
            player.sendMessage("§fDie Köpfe können in beliebiger Reihenfolge angeordnet werden.");
            return true;
        }
        return false;
    }
}