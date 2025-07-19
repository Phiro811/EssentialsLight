package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound; // Import für Sounds
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack; // Für Item-Handling
import org.bukkit.inventory.meta.Damageable; // Für Haltbarkeit von Items
import org.bukkit.inventory.meta.ItemMeta; // Für ItemMeta
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

// Cooldown Maps
    private final Map<UUID, Long> healCooldowns = new HashMap<>();
    private final Map<UUID, Long> feedCooldowns = new HashMap<>();
    private final Map<UUID, Long> repairCooldowns = new HashMap<>(); // NEU: Cooldown für Repair
    private static final long COOLDOWN_MILLIS = 30 * 1000L; // 30 Sekunden in Millisekunden

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId(); // Spieler-UUID für Cooldowns

// --- HEAL COMMAND ---
        if (command.getName().equalsIgnoreCase("heal")) {
            if (!player.hasPermission("lolplugin.heal")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            long now = System.currentTimeMillis();
            if (healCooldowns.containsKey(playerUUID)) {
                long lastUse = healCooldowns.get(playerUUID);
                long timeLeft = (lastUse + COOLDOWN_MILLIS) - now; // Verbleibende Zeit in Millisekunden
                if (timeLeft > 0) {
                    double secondsLeft = timeLeft / 1000.0;
                    player.sendMessage(ChatColor.RED + "Du musst noch " + String.format("%.1f", secondsLeft) + " Sekunden warten, um dich zu heilen.");
                    return true;
                }
            }
            healCooldowns.put(playerUUID, now); // Cooldown aktualisieren

            if (args.length == 0) {
// Heile dich selbst
                double initialHealth = player.getHealth();
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
                player.setFireTicks(0); // Feuer löschen

// Berechne die geheilten HALBEN Herzen
                int healedHalfHearts = (int) (20.0 - initialHealth);
                if (healedHalfHearts < 0) healedHalfHearts = 0; // Negative Werte vermeiden

                player.sendMessage(ChatColor.RED + "+" + healedHalfHearts + "❤" + ChatColor.WHITE + " | Du hast dich geheilt!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            } else if (args.length == 1) {
// Heile anderen Spieler
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Spieler '" + args[0] + "' wurde nicht gefunden oder ist nicht online.");
                    return true;
                }
                if (!player.hasPermission("lolplugin.heal.other")) { // Eigene Berechtigung für das Heilen anderer
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, andere Spieler zu heilen.");
                    return true;
                }

                double initialHealth = target.getHealth();
                target.setHealth(20.0);
                target.setFoodLevel(20);
                target.setSaturation(20.0f);
                target.setFireTicks(0); // Feuer löschen

                int healedHalfHearts = (int) (20.0 - initialHealth);
                if (healedHalfHearts < 0) healedHalfHearts = 0;

                player.sendMessage(ChatColor.RED + "+" + healedHalfHearts + "❤" + ChatColor.WHITE + " | Du hast Spieler " + target.getName() + " geheilt!");
                target.sendMessage(ChatColor.RED + "+" + healedHalfHearts + "❤" + ChatColor.WHITE + " | " + player.getName() + " hat dich geheilt!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            } else {
                player.sendMessage(ChatColor.RED + "Verwendung: /heal [Spielername]");
            }
            return true;
        }

// --- FEED COMMAND ---
        if (command.getName().equalsIgnoreCase("feed")) {
            if (!player.hasPermission("lolplugin.feed")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            long now = System.currentTimeMillis();
            if (feedCooldowns.containsKey(playerUUID)) {
                long lastUse = feedCooldowns.get(playerUUID);
                long timeLeft = (lastUse + COOLDOWN_MILLIS) - now;
                if (timeLeft > 0) {
                    double secondsLeft = timeLeft / 1000.0;
                    player.sendMessage(ChatColor.RED + "Du musst noch " + String.format("%.1f", secondsLeft) + " Sekunden warten, um dich zu sättigen.");
                    return true;
                }
            }
            feedCooldowns.put(playerUUID, now); // Cooldown aktualisieren

            if (args.length == 0) {
                int initialFood = player.getFoodLevel();
                player.setFoodLevel(20);
                player.setSaturation(20.0f);

                int restoredHunger = 20 - initialFood;
                if (restoredHunger < 0) restoredHunger = 0;

                player.sendMessage(ChatColor.GOLD + "+" + restoredHunger + "🍖" + ChatColor.WHITE + " | Du hast dich gesättigt!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Spieler '" + args[0] + "' wurde nicht gefunden oder ist nicht online.");
                    return true;
                }
                if (!player.hasPermission("lolplugin.feed.other")) { // Eigene Berechtigung für das Sättigen anderer
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, andere Spieler zu sättigen.");
                    return true;
                }

                int initialFood = target.getFoodLevel();
                target.setFoodLevel(20);
                target.setSaturation(20.0f);

                int restoredHunger = 20 - initialFood;
                if (restoredHunger < 0) restoredHunger = 0;

                player.sendMessage(ChatColor.GOLD + "+" + restoredHunger + "🍖" + ChatColor.WHITE + " | Du hast Spieler " + target.getName() + " gesättigt!");
                target.sendMessage(ChatColor.GOLD + "+" + restoredHunger + "🍖" + ChatColor.WHITE + " | " + player.getName() + " hat dich gesättigt!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            } else {
                player.sendMessage(ChatColor.RED + "Verwendung: /feed [Spielername]");
            }
            return true;
        }

// --- FLY COMMAND ---
        if (command.getName().equalsIgnoreCase("fly")) {
            if (!player.hasPermission("lolplugin.fly")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage(ChatColor.WHITE + "Fly = " + ChatColor.RED + "Off"); // Angepasste Nachricht
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage(ChatColor.WHITE + "Fly = " + ChatColor.GREEN + "On"); // Angepasste Nachricht
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
            }
            return true;
        }

// --- FLYSPEED COMMAND ---
        if (command.getName().equalsIgnoreCase("flyspeed")) {
            if (!player.hasPermission("lolplugin.flyspeed")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            if (args.length == 1) {
                try {
                    int rawSpeed = Integer.parseInt(args[0]);
                    if (rawSpeed >= 1 && rawSpeed <= 10) {
                        float convertedSpeed = rawSpeed / 10.0F;
                        player.setFlySpeed(convertedSpeed);
                        player.sendMessage(ChatColor.WHITE + "Flyspeed: " + ChatColor.GREEN + rawSpeed);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F); // Sound hinzufügen
                    } else {
                        player.sendMessage(ChatColor.RED + "Geschwindigkeit muss zwischen 1 und 10 liegen.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Ungültige Geschwindigkeitsangabe. Bitte eine Zahl von 1 bis 10 eingeben.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Verwendung: /flyspeed <Geschwindigkeit (1-10)>");
            }
            return true;
        }

// --- EXPLODE COMMAND ---
        if (command.getName().equalsIgnoreCase("explode")) {
            if (!player.hasPermission("lolplugin.explode")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            boolean enableFire = false;
            boolean enableBlockBreaking = false;
            Player target = null;
            int booleanStartIndex = 0;

            if (args.length > 0) {
// Versuche, den ersten Parameter als Spielernamen zu parsen
                target = Bukkit.getPlayer(args[0]);

                if (target != null) {
                    booleanStartIndex = 1; // Wenn erster Parameter ein Spieler ist, beginnen Booleans ab dem zweiten
                } else {
                    target = player; // Wenn kein Spieler gefunden, ist der Ausführende das Ziel
                    booleanStartIndex = 0; // Booleans könnten der erste Parameter sein
                }

// Block-Breaking Parameter
                if (args.length > booleanStartIndex) {
                    try {
                        enableBlockBreaking = Boolean.parseBoolean(args[booleanStartIndex]);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Ungültiger Wert für 'Block-Explosion'. Verwende 'true' oder 'false'.");
                        player.sendMessage(ChatColor.RED + "Verwendung: /explode [Spieler] [Block-Explosion (true/false)] [Feuer (true/false)]");
                        return true;
                    }
                }
// Feuer Parameter
                if (args.length > booleanStartIndex + 1) {
                    try {
                        enableFire = Boolean.parseBoolean(args[booleanStartIndex + 1]);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Ungültiger Wert für 'Feuer'. Verwende 'true' oder 'false'.");
                        player.sendMessage(ChatColor.RED + "Verwendung: /explode [Spieler] [Block-Explosion (true/false)] [Feuer (true/false)]");
                        return true;
                    }
                }
            } else {
                target = player; // Kein Argument, explodiert sich selbst
            }

            if (target == null) {
                player.sendMessage(ChatColor.RED + "Interner Fehler: Zielspieler konnte nicht bestimmt werden.");
                return true;
            }

            target.getWorld().createExplosion(target.getLocation(), 4.0F, enableFire, enableBlockBreaking); // Stärke 4.0

            if (target == player) {
                player.sendMessage(ChatColor.GREEN + "Booooom!");
            } else {
                player.sendMessage(ChatColor.GREEN + "Du hast Spieler " + target.getName() + " explodieren lassen!");
                target.sendMessage(ChatColor.RED + "Du wurdest von " + player.getName() + " explodieren gelassen! BOOOOM!");
            }
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F); // Explosionssound hinzufügen
            return true;
        }

// --- REPAIR COMMAND (NEU) ---
        if (command.getName().equalsIgnoreCase("repair")) {
            if (!player.hasPermission("lolplugin.repair")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
                return true;
            }

            long now = System.currentTimeMillis();
            if (repairCooldowns.containsKey(playerUUID)) {
                long lastUse = repairCooldowns.get(playerUUID);
                long timeLeft = (lastUse + COOLDOWN_MILLIS) - now;
                if (timeLeft > 0) {
                    double secondsLeft = timeLeft / 1000.0;
                    player.sendMessage(ChatColor.RED + "Du musst noch " + String.format("%.1f", secondsLeft) + " Sekunden warten, um deine Items zu reparieren.");
                    return true;
                }
            }
            repairCooldowns.put(playerUUID, now); // Cooldown aktualisieren

            int repairedItemsCount = 0;
            int totalDamageRepaired = 0; // Summe der reparierten Haltbarkeitspunkte

// Targets: player (self) or other player
            Player repairTarget = null;
            if (args.length == 0) {
                repairTarget = player;
            } else if (args.length == 1) {
                repairTarget = Bukkit.getPlayer(args[0]);
                if (repairTarget == null) {
                    player.sendMessage(ChatColor.RED + "Spieler '" + args[0] + "' wurde nicht gefunden oder ist nicht online.");
                    return true;
                }
                if (!player.hasPermission("lolplugin.repair.other")) { // Eigene Berechtigung für das Reparieren anderer
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, die Items anderer Spieler zu reparieren.");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Verwendung: /repair [Spielername]");
                return true;
            }

// Repariere Items im Inventar des Ziels
            for (ItemStack item : repairTarget.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof Damageable) {
                        Damageable damageable = (Damageable) meta;
                        if (damageable.hasDamage()) { // Wenn das Item beschädigt ist
                            totalDamageRepaired += damageable.getDamage(); // Gesamtschaden vor der Reparatur
                            damageable.setDamage(0); // Repariere das Item vollständig
                            item.setItemMeta(damageable); // Setze die geänderte Meta zurück
                            repairedItemsCount++;
                        }
                    }
                }
            }

            if (repairedItemsCount > 0) {
// Nachrichten an den Ausführenden und das Ziel anpassen
                if (repairTarget == player) {
// Eigene Reparatur
                    player.sendMessage(ChatColor.GRAY + "+" + totalDamageRepaired + ChatColor.GRAY + "🛡 " + ChatColor.WHITE + "| Du hast deine Items repariert.");
                } else {
// Reparatur für anderen Spieler
                    player.sendMessage(ChatColor.GRAY + "+" + totalDamageRepaired + ChatColor.GRAY + "🛡 " + ChatColor.WHITE + "| Du hast " + repairTarget.getName() + "s Items repariert.");
                    repairTarget.sendMessage(ChatColor.GRAY + "+" + totalDamageRepaired + ChatColor.GRAY + "🛡 " + ChatColor.WHITE + "| Deine Items wurden von " + player.getName() + " repariert.");
                }
                repairTarget.playSound(repairTarget.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F); // Sound für Reparatur
            } else {
                if (repairTarget == player) {
                    player.sendMessage(ChatColor.RED + "Du hast keine Items, die repariert werden müssen.");
                } else {
                    player.sendMessage(ChatColor.RED + repairTarget.getName() + " hat keine Items, die repariert werden müssen.");
                }
            }
            return true;
        }

        return false; // Befehl nicht erkannt
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        List<String> booleanOptions = Arrays.asList("true", "false");

        if (command.getName().equalsIgnoreCase("heal") ||
                command.getName().equalsIgnoreCase("feed") ||
                command.getName().equalsIgnoreCase("repair")) { // Auch für Repair Tab-Vervollständigung hinzufügen
            if (args.length == 1) {
// Wenn der Spieler die Berechtigung hat, andere zu heilen/sättigen/reparieren
                String permissionPrefix = "lolplugin." + command.getName().toLowerCase() + ".other";
                if (sender.hasPermission(permissionPrefix)) {
                    return StringUtil.copyPartialMatches(args[0], playerNames, completions);
                }
// Ansonsten keine Vorschläge
                return Collections.emptyList();
            }
        } else if (command.getName().equalsIgnoreCase("explode")) {
            if (args.length == 1) {
// Erste Argument: Spielername ODER Block-Explosion (true/false)
                completions.addAll(playerNames);
                completions.addAll(booleanOptions);
                return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
            } else if (args.length == 2) {
// Zweite Argument: Block-Explosion (true/false) oder Feuer (true/false)
// Wenn erstes Argument ein Spieler ist, dann ist args[1] Block-Explosion
// Wenn erstes Argument kein Spieler ist, ist args[0] Block-Explosion, args[1] Feuer
                Player potentialTarget = Bukkit.getPlayer(args[0]);
                if (potentialTarget != null) { // args[0] ist ein Spielername
                    return StringUtil.copyPartialMatches(args[1], booleanOptions, completions);
                } else { // args[0] ist wahrscheinlich ein Boolean für Block-Explosion
                    return StringUtil.copyPartialMatches(args[1], booleanOptions, completions);
                }
            } else if (args.length == 3) {
// Drittes Argument (nur wenn args[0] ein Spieler ist und args[1] ein Boolean): Feuer (true/false)
                Player potentialTarget = Bukkit.getPlayer(args[0]);
                if (potentialTarget != null) {
                    return StringUtil.copyPartialMatches(args[2], booleanOptions, completions);
                }
            }
        } else if (command.getName().equalsIgnoreCase("flyspeed")) { // Nur für Flyspeed
            if (args.length == 1) {
// Vorschläge für Geschwindigkeit
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), completions);
            }
        }
        return Collections.emptyList();
    }
}