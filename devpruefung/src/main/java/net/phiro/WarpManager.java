package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpManager {

    private final Main plugin;
    private final File warpsFile;
    private FileConfiguration warpsConfig;

    public WarpManager(Main plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.warpsConfig = YamlConfiguration.loadConfiguration(this.warpsFile);
        loadWarps();
    }

    public Map<String, Location> getWarps() {
        Map<String, Location> warps = new HashMap<>();
        if (warpsConfig.contains("warps")) {
            for (String key : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
                warps.put(key, warpsConfig.getLocation("warps." + key));
            }
        }
        return warps;
    }

    public Location getWarp(String warpName) {
        return warpsConfig.getLocation("warps." + warpName);
    }

// NEU: Methode zum Hinzufügen eines Warps
    public void setWarp(String warpName, Location location) {
        warpsConfig.set("warps." + warpName, location);
    }

// NEU: Methode zum Entfernen eines Warps
    public void removeWarp(String warpName) {
        warpsConfig.set("warps." + warpName, null);
    }

    public void saveWarps() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                warpsConfig.save(warpsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadWarps() {
        if (!warpsFile.exists()) {
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(this.warpsFile);
    }
}