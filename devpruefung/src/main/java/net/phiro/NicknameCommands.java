package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NicknameCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public NicknameCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Verwendung: /nickname <neuer_name>");
            player.sendMessage(ChatColor.YELLOW + "Um deinen Nicknamen zurückzusetzen: /nickname reset");
            return true;
        }

        String nickname = args[0];
        if (nickname.equalsIgnoreCase("reset")) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());

            // Lösche den Nickname aus der Konfiguration
            plugin.getConfig().set("nicknames." + player.getUniqueId().toString(), null);
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Dein Nickname wurde zurückgesetzt.");
            return true;
        }

        // Farbcode-Unterstützung
        nickname = ChatColor.translateAlternateColorCodes('&', nickname);

        player.setDisplayName(nickname);
        player.setPlayerListName(nickname);

        // Speichere den Nickname in der Konfiguration
        plugin.getConfig().set("nicknames." + player.getUniqueId().toString(), nickname);
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "Dein Nickname wurde zu " + ChatColor.RESET + nickname + ChatColor.GREEN + " geändert.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reset");
        }
        return Collections.emptyList();
    }
}