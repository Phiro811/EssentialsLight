package net.phiro;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Sound;

public class EnderchestCommands implements CommandExecutor {

    private final Main plugin;

    public EnderchestCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPlayerOnlyMessage()); // "Dieser Befehl kann nur von einem Spieler ausgeführt werden."
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lolplugin.ec")) {
            player.sendMessage(plugin.getNoPermissionMessage());
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(ChatColor.RED + "Benutzung: /ec"); // Direkte Nutzung, da es keinen Spieler-Parameter gibt
            return true;
        }

        Inventory enderChest = player.getEnderChest();
        player.openInventory(enderChest);

        // NEU: Angepasste Erfolgsnachricht für /ec
        player.sendMessage(ChatColor.WHITE + "Du siehst nun deine " + ChatColor.LIGHT_PURPLE + "Enderchest" + ChatColor.WHITE + ".");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        return true;
    }
}