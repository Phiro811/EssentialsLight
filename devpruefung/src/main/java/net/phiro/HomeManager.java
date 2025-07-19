package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {

    private final Main plugin;
    private final File homesFile;
    private FileConfiguration homesConfig;

    public HomeManager(Main plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        this.homesConfig = YamlConfiguration.loadConfiguration(this.homesFile);
        loadHomes();
    }

    public Map<String, Location> getHomes(UUID playerUUID) {
        Map<String, Location> homes = new HashMap<>();
        if (homesConfig.contains(playerUUID.toString())) {
            for (String key : homesConfig.getConfigurationSection(playerUUID.toString()).getKeys(false)) {
                homes.put(key, homesConfig.getLocation(playerUUID.toString() + "." + key));
            }
        }
        return homes;
    }

    public Location getHome(UUID playerUUID, String homeName) {
        return homesConfig.getLocation(playerUUID.toString() + "." + homeName);
    }

    // NEU: Methode zum Hinzufügen eines Homes
    public void setHome(UUID playerUUID, String homeName, Location location) {
        homesConfig.set(playerUUID.toString() + "." + homeName, location);
    }

    // NEU: Methode zum Entfernen eines Homes
    public void removeHome(UUID playerUUID, String homeName) {
        homesConfig.set(playerUUID.toString() + "." + homeName, null);
    }

    public void saveHomes() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                homesConfig.save(homesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadHomes() {
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        this.homesConfig = YamlConfiguration.loadConfiguration(this.homesFile);
    }
}