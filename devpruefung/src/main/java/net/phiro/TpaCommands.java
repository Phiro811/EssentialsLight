package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TpaCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final Map<UUID, UUID> teleportRequests = new HashMap<>();
    private final Map<UUID, Long> requestCooldowns = new HashMap<>();
    private final Map<UUID, Integer> requestTimers = new HashMap<>();

    public TpaCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tpa")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /tpa <Spieler>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Der Spieler ist nicht online.");
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Du kannst dich nicht zu dir selbst teleportieren.");
                return true;
            }

            long cooldownTime = 60000;
            if (requestCooldowns.containsKey(player.getUniqueId())) {
                long timeLeft = requestCooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    player.sendMessage(ChatColor.RED + "Bitte warte " + TimeUnit.MILLISECONDS.toSeconds(timeLeft) + " Sekunden, bevor du eine neue TPA-Anfrage sendest.");
                    return true;
                }
            }

            teleportRequests.put(target.getUniqueId(), player.getUniqueId());
            requestCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);

            target.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " möchte sich zu dir teleportieren.");
            target.sendMessage(ChatColor.YELLOW + "Akzeptiere mit " + ChatColor.GREEN + "/tpaccept" + ChatColor.YELLOW + " oder lehne ab mit " + ChatColor.RED + "/tpacancel" + ChatColor.YELLOW + ".");
            player.sendMessage(ChatColor.GREEN + "TPA-Anfrage an " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + " gesendet.");

            requestTimers.put(target.getUniqueId(), Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (teleportRequests.containsKey(target.getUniqueId())) {
                    teleportRequests.remove(target.getUniqueId());
                    target.sendMessage(ChatColor.RED + "Die TPA-Anfrage von " + player.getName() + " ist abgelaufen.");
                    player.sendMessage(ChatColor.RED + "Deine TPA-Anfrage an " + target.getName() + " ist abgelaufen.");
                }
            }, 20L * 30).getTaskId()); // 30 Sekunden

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpahere")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Benutzung: /tpahere <Spieler>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Der Spieler ist nicht online.");
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Du kannst dich nicht zu dir selbst teleportieren.");
                return true;
            }

            long cooldownTime = 60000;
            if (requestCooldowns.containsKey(player.getUniqueId())) {
                long timeLeft = requestCooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    player.sendMessage(ChatColor.RED + "Bitte warte " + TimeUnit.MILLISECONDS.toSeconds(timeLeft) + " Sekunden, bevor du eine neue TP-Anfrage sendest.");
                    return true;
                }
            }

            teleportRequests.put(target.getUniqueId(), player.getUniqueId());
            requestCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);

            target.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " möchte, dass du dich zu ihm teleportierst.");
            target.sendMessage(ChatColor.YELLOW + "Akzeptiere mit " + ChatColor.GREEN + "/tpaccept" + ChatColor.YELLOW + " oder lehne ab mit " + ChatColor.RED + "/tpacancel" + ChatColor.YELLOW + ".");
            player.sendMessage(ChatColor.GREEN + "TP-Anfrage an " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + " gesendet.");

            requestTimers.put(target.getUniqueId(), Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (teleportRequests.containsKey(target.getUniqueId())) {
                    teleportRequests.remove(target.getUniqueId());
                    target.sendMessage(ChatColor.RED + "Die TP-Anfrage von " + player.getName() + " ist abgelaufen.");
                    player.sendMessage(ChatColor.RED + "Deine TP-Anfrage an " + target.getName() + " ist abgelaufen.");
                }
            }, 20L * 30).getTaskId()); // 30 Sekunden

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpaccept")) {
            if (teleportRequests.containsKey(player.getUniqueId())) {
                Player requester = Bukkit.getPlayer(teleportRequests.get(player.getUniqueId()));

                if (requester != null && requester.isOnline()) {
// NEU: Teleportation über den Manager
                    plugin.getTeleportationManager().teleportPlayer(requester, player.getLocation(), 3);
                    player.sendMessage(ChatColor.GREEN + "Teleportationsanfrage von " + requester.getName() + " akzeptiert.");
                    teleportRequests.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "Die Teleportationsanfrage ist abgelaufen oder der Spieler ist nicht mehr online.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Du hast keine ausstehende Teleportationsanfrage.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpacancel")) {
            if (teleportRequests.containsKey(player.getUniqueId())) {
                Player requester = Bukkit.getPlayer(teleportRequests.get(player.getUniqueId()));
                if (requester != null && requester.isOnline()) {
                    requester.sendMessage(ChatColor.RED + "Deine Teleportationsanfrage wurde von " + player.getName() + " abgelehnt.");
                }
                player.sendMessage(ChatColor.RED + "Teleportationsanfrage abgelehnt.");
                teleportRequests.remove(player.getUniqueId());
            } else {
                player.sendMessage(ChatColor.RED + "Du hast keine ausstehende Teleportationsanfrage.");
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