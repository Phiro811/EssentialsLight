package net.phiro;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

public class WardenRecipe {

    private static ItemStack recipeResult;
    private static NamespacedKey key;

    public static void register(Plugin plugin) {
        // Erstelle den NamespacedKey für das Rezept
        key = new NamespacedKey(plugin, "warden_recipe");

        // Definiere das Ergebnis des Rezepts (ein unsichtbarer Platzhalter)
        recipeResult = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = recipeResult.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Warden Beschwörungsblock");
            meta.setLore(Collections.singletonList("§7Kombiniere 9 Spielerköpfe."));
        }
        recipeResult.setItemMeta(meta);

        // Erstelle das geformte Rezept
        ShapedRecipe recipe = new ShapedRecipe(key, recipeResult);

        // Setze die Form des Rezepts
        recipe.shape("HHH", "HHH", "HHH");
        recipe.setIngredient('H', Material.PLAYER_HEAD);

        // Registriere das Rezept
        plugin.getServer().addRecipe(recipe);

        plugin.getLogger().info("Warden Beschwörungsrezept registriert.");
    }

    public static NamespacedKey getKey() {
        return key;
    }

    public static ItemStack getRecipeResult() {
        return recipeResult;
    }
}