package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration; // Beibehalten, falls noch für andere Dinge benötigt, aber nicht direkt für Nachrichten
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Sound;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InvseeCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public InvseeCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPlayerOnlyMessage()); // "Dieser Befehl kann nur von einem Spieler ausgeführt werden."
            return true;
        }

        Player player = (Player) sender;
        // FileConfiguration config = Main.plugin.getConfig(); // Nicht mehr direkt für Nachrichten genutzt

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Benutzung: /" + label + " <Spieler>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (command.getName().equalsIgnoreCase("invsee")) {
            if (!player.hasPermission("lolplugin.invsee")) {
                player.sendMessage(plugin.getNoPermissionMessage());
                return true;
            }

            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getPlayerNotFoundMessage().replace("[player]", targetName)); // Allgemeine Spieler-nicht-gefunden-Nachricht
                return true;
            }

            PlayerInventory targetInv = target.getInventory();
            Inventory invseeGui = Bukkit.createInventory(null, 5 * 9, ChatColor.DARK_GRAY + "Inventar von " + target.getName());

            for (int i = 0; i < 36; i++) {
                invseeGui.setItem(i, targetInv.getItem(i));
            }

            invseeGui.setItem(36, targetInv.getHelmet());
            invseeGui.setItem(37, targetInv.getChestplate());
            invseeGui.setItem(38, targetInv.getLeggings());
            invseeGui.setItem(39, targetInv.getBoots());

            ItemStack fillerItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = fillerItem.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(ChatColor.RESET + "");
                fillerItem.setItemMeta(fillerMeta);
            }

            for (int i = 40; i <= 43; i++) {
                invseeGui.setItem(i, fillerItem);
            }

            invseeGui.setItem(44, targetInv.getItemInOffHand());

            player.openInventory(invseeGui);
            // NEU: Angepasste Erfolgsnachricht für /invsee
            player.sendMessage(ChatColor.WHITE + "Du siehst nun das " + ChatColor.GOLD + "Inventar" + ChatColor.WHITE + " von " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + ".");
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            return true;
        }

        if (command.getName().equalsIgnoreCase("endersee")) {
            if (!player.hasPermission("lolplugin.endersee")) {
                player.sendMessage(plugin.getNoPermissionMessage());
                return true;
            }

            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getPlayerNotFoundMessage().replace("[player]", targetName)); // Allgemeine Spieler-nicht-gefunden-Nachricht
                return true;
            }

            try {
                Inventory enderChest = target.getEnderChest();
                player.openInventory(enderChest);
                // NEU: Angepasste Erfolgsnachricht für /endersee
                player.sendMessage(ChatColor.WHITE + "Du siehst nun die " + ChatColor.LIGHT_PURPLE + "Enderchest" + ChatColor.WHITE + " von " + ChatColor.GREEN + target.getName() + ChatColor.WHITE + ".");
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Ein Fehler ist aufgetreten: " + e.getMessage()); // Allgemeine Fehlermeldung
                plugin.getLogger().severe("Error opening Ender Chest for " + target.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}