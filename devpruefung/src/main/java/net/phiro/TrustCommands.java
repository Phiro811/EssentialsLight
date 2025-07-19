package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TrustCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final HashMap<UUID, Long> trustCooldown = new HashMap<>();

    public TrustCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = Main.plugin.getConfig();
        long TRUST_COOLDOWN = PermissionUtils.findHighestLongValue(player, "lolplugin.trust.cooldown.", config.getLong("settings.trust-cooldown", 3));

        if (trustCooldown.containsKey(player.getUniqueId()) && (System.currentTimeMillis() - trustCooldown.get(player.getUniqueId()) < TimeUnit.SECONDS.toMillis(TRUST_COOLDOWN))) {
            long timeLeft = (TimeUnit.SECONDS.toMillis(TRUST_COOLDOWN) - (System.currentTimeMillis() - trustCooldown.get(player.getUniqueId()))) / 1000;
            player.sendMessage(ChatColor.RED + config.getString("messages.cooldown").replace("[time]", String.valueOf(timeLeft)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen eines Spielers an.");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + config.getString("messages.target-not-found").replace("[target]", args[0]));
            return true;
        }

// NEU: Prüfung auf Selbst-Vertrauen
        if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.RED + config.getString("messages.self-trust-error"));
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = targetPlayer.getUniqueId();
        String trustedPath = "trusted_players." + playerUUID.toString();

        if (command.getName().equalsIgnoreCase("trust")) {
            if (!player.hasPermission("lolplugin.trust")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            List<String> trustedList = config.getStringList(trustedPath);
            if (trustedList.contains(targetUUID.toString())) {
                player.sendMessage(ChatColor.RED + "Du traust diesem Spieler bereits.");
                return true;
            }

            trustedList.add(targetUUID.toString());
            config.set(trustedPath, trustedList);
            Main.plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + config.getString("messages.trust-success-self").replace("[target]", targetPlayer.getName()));
            targetPlayer.sendMessage(ChatColor.GREEN + config.getString("messages.trust-success-target").replace("[player]", player.getName()));
            trustCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        }

        if (command.getName().equalsIgnoreCase("untrust")) {
            if (!player.hasPermission("lolplugin.untrust")) {
                player.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
                return true;
            }

            List<String> trustedList = config.getStringList(trustedPath);
            if (!trustedList.contains(targetUUID.toString())) {
                player.sendMessage(ChatColor.RED + "Du traust diesem Spieler nicht.");
                return true;
            }

            trustedList.remove(targetUUID.toString());
            config.set(trustedPath, trustedList);
            Main.plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + config.getString("messages.untrust-success-self").replace("[target]", targetPlayer.getName()));
            targetPlayer.sendMessage(ChatColor.GREEN + config.getString("messages.untrust-success-target").replace("[player]", player.getName()));
            trustCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
            return playerNames;
        }
        return Collections.emptyList();
    }
}