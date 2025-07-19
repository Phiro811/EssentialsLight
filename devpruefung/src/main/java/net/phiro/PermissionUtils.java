package net.phiro;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionUtils {

    /**
     * Sucht die höchste Ganzzahl (Integer) in den Permissions eines Spielers für ein bestimmtes Muster.
     * Beispiel: findHighestIntValue(player, "lolplugin.home.max.") findet den Wert in "lolplugin.home.max.5".
     *
     * @param player der Spieler
     * @param permissionBase das Präfix der Permission (z.B. "lolplugin.home.max.")
     * @param defaultValue der Wert, der zurückgegeben wird, wenn keine passende Permission gefunden wird
     * @return die höchste gefundene Zahl oder der Standardwert
     */
    public static int findHighestIntValue(Player player, String permissionBase, int defaultValue) {
        int highestValue = defaultValue;
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith(permissionBase)) {
                try {
                    String valueString = perm.substring(permissionBase.length());
                    int value = Integer.parseInt(valueString);
                    if (value > highestValue) {
                        highestValue = value;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignoriere Permissions, die nicht mit einer Zahl enden
                }
            }
        }
        return highestValue;
    }

    /**
     * Sucht die höchste Ganzzahl (Long) in den Permissions eines Spielers für ein bestimmtes Muster.
     * Nützlich für Cooldowns.
     *
     * @param player der Spieler
     * @param permissionBase das Präfix der Permission (z.B. "lolplugin.home.cooldown.")
     * @param defaultValue der Wert, der zurückgegeben wird, wenn keine passende Permission gefunden wird
     * @return die höchste gefundene Zahl oder der Standardwert
     */
    public static long findHighestLongValue(Player player, String permissionBase, long defaultValue) {
        long highestValue = defaultValue;
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith(permissionBase)) {
                try {
                    String valueString = perm.substring(permissionBase.length());
                    long value = Long.parseLong(valueString);
                    if (value > highestValue) {
                        highestValue = value;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignoriere Permissions, die nicht mit einer Zahl enden
                }
            }
        }
        return highestValue;
    }
}