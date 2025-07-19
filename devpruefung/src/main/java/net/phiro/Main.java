package net.phiro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class Main extends JavaPlugin {

    public static Main plugin;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private TeleportationManager teleportationManager;
    private BackListener backListener;
// TpaManager ist nicht deklariert, da er nach Absprache nicht existiert und die Logik in TpaCommands liegt.

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

// Initialisierung der Manager
        this.homeManager = new HomeManager(this);
        this.warpManager = new WarpManager(this);
        this.teleportationManager = new TeleportationManager(this);
        this.backListener = new BackListener(this);

// Registrieren der Listener
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new FirstJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TrustListener(), this);
        Bukkit.getPluginManager().registerEvents(this.backListener, this);
        Bukkit.getPluginManager().registerEvents(new TeleportationListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);

// Registrieren der Commands
        Commands commandsExecutor = new Commands();
        getCommand("heal").setExecutor(commandsExecutor);
        getCommand("feed").setExecutor(commandsExecutor);
        getCommand("fly").setExecutor(commandsExecutor);
        getCommand("flyspeed").setExecutor(commandsExecutor);
        getCommand("explode").setExecutor(commandsExecutor);
        getCommand("explode").setTabCompleter(commandsExecutor);
        getCommand("repair").setExecutor(commandsExecutor);

// Registrieren der Spawn- und Home-Befehle
        SpawnCommands spawnCommands = new SpawnCommands(this);
        getCommand("setspawn").setExecutor(spawnCommands);
        getCommand("spawn").setExecutor(spawnCommands);

        HomeCommands homeCommands = new HomeCommands(this);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("home").setExecutor(homeCommands);
        getCommand("home").setTabCompleter(homeCommands);
        getCommand("removehome").setExecutor(homeCommands);
        getCommand("removehome").setTabCompleter(homeCommands);

// Registrieren der Warp-Befehle
        WarpCommands warpCommands = new WarpCommands(this);
        getCommand("setwarp").setExecutor(warpCommands);
        getCommand("warp").setExecutor(warpCommands);
        getCommand("warp").setTabCompleter(warpCommands);
        getCommand("removewarp").setExecutor(warpCommands);
        getCommand("removewarp").setTabCompleter(warpCommands);
        getCommand("warpinfo").setExecutor(warpCommands);
        getCommand("warpinfo").setTabCompleter(warpCommands);

// Registrieren der Trust-Befehle
        TrustCommands trustCommands = new TrustCommands(this);
        getCommand("trust").setExecutor(trustCommands);
        getCommand("trust").setTabCompleter(trustCommands);
        getCommand("untrust").setExecutor(trustCommands);
        getCommand("untrust").setTabCompleter(trustCommands);

// Registrieren des Back-Befehls
        BackCommands backCommands = new BackCommands(this);
        getCommand("back").setExecutor(backCommands);
        getCommand("back").setTabCompleter(backCommands);

// Registrieren der TPA-Befehle (TpaCommands direkt initialisieren, da kein Manager)
        TpaCommands tpaCommands = new TpaCommands(this);
        getCommand("tpa").setExecutor(tpaCommands);
        getCommand("tpa").setTabCompleter(tpaCommands);
        getCommand("tpahere").setExecutor(tpaCommands);
        getCommand("tpahere").setTabCompleter(tpaCommands);
        getCommand("tpaccept").setExecutor(tpaCommands);
        getCommand("tpacancel").setExecutor(tpaCommands);

// Registrieren des RTP-Befehls
        getCommand("rtp").setExecutor(new RtpCommands(this));

// Registrieren der Nickname-Befehle
        NicknameCommands nicknameCommands = new NicknameCommands(this);
        getCommand("nickname").setExecutor(nicknameCommands);
        getCommand("nickname").setTabCompleter(nicknameCommands);

// Registrierung der Invsee/Endersee-Befehle
        InvseeCommands invseeCommands = new InvseeCommands(this);
        getCommand("invsee").setExecutor(invseeCommands);
        getCommand("invsee").setTabCompleter(invseeCommands);
        getCommand("endersee").setExecutor(invseeCommands);
        getCommand("endersee").setTabCompleter(invseeCommands);

// Registrierung des EC-Befehls (und Enderchest-Alias)
        EnderchestCommands ecCommands = new EnderchestCommands(this);
        getCommand("ec").setExecutor(ecCommands);

// Registriere das Custom Recipe
        WardenRecipe.register(this);

// Registriere den neuen Command
        getCommand("recipe").setExecutor(new RecipeCommands());

        getLogger().info("LOL-Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LOL-Plugin disabled!");
    }

// Getter-Methoden für die Manager
    public HomeManager getHomeManager() {
        return homeManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public TeleportationManager getTeleportationManager() {
        return teleportationManager;
    }

    public BackListener getBackListener() {
        return backListener;
    }

    public Location getSpawnLocation() {
        if (!getConfig().contains("spawn.world")) {
            return null;
        }

        World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        double x = getConfig().getDouble("spawn.x");
        double y = getConfig().getDouble("spawn.y");
        double z = getConfig().getDouble("spawn.z");
        float yaw = (float) getConfig().getDouble("spawn.yaw");
        float pitch = (float) getConfig().getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

// Laden des Nicknames aus der config.yml
    public String getNickname(UUID uuid) {
        return getConfig().getString("nicknames." + uuid.toString());
    }

// Getter für die Nachrichten aus der config.yml
    public String getFirstJoinMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.join.first-join", "&aWillkommen auf dem Server, &b%player_name%&a! Viel Spaß!"));
    }

    public String getSpawnTeleportMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.join.spawn-teleport", "&aDu wurdest zum Spawn teleportiert."));
    }

    public String getNoSpawnMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.join.no-spawn", "&cDer Spawn wurde noch nicht gesetzt."));
    }

// Getter für Teleportationsnachrichten
    public String getTeleportBusyMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleport.busy", "&cDu bist bereits in einem Teleportationsprozess."));
    }

    public String getTeleportBypassMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleport.bypass", "&aTeleportation erfolgreich (keine Verzögerung)."));
    }

    public String getTeleportStartMessage(int seconds) {
        String message = getConfig().getString("messages.teleport.start", "&eTeleportation [reason] startet in &6[seconds] &eSekunden. Bitte bewege dich nicht!");
        return ChatColor.translateAlternateColorCodes('&', message.replace("[seconds]", String.valueOf(seconds)));
    }

    public String getTeleportCountdownMessage(int remaining) {
        String message = getConfig().getString("messages.teleport.countdown", "&eTeleportation in... &6[remaining]");
        return ChatColor.translateAlternateColorCodes('&', message.replace("[remaining]", String.valueOf(remaining)));
    }

    public String getTeleportSuccessMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleport.success", "&aTeleportation erfolgreich."));
    }

    public String getTeleportCancelledMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleport.cancelled", "&cDie Teleportation wurde abgebrochen, da du dich bewegt hast oder Schaden genommen hast."));
    }

// Nachrichten für das /back Feature
    public String getBackUsageMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.back.usage", "&cBenutzung: /back [death|last-location]"));
    }

    public String getBackNoLocationFoundMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.back.no-location-found", "&cEs wurde kein passender Rückkehrpunkt gefunden."));
    }

    public String getBackTeleportReasonDeath() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.back.reason-death", "zum Todespunkt"));
    }

    public String getBackTeleportReasonLastLocation() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.back.reason-last-location", "zum letzten Standort"));
    }

    public String getDeathLocationVoidMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.back.death-location-void", "&cDein Todespunkt konnte nicht gespeichert werden, da du in der Void gestorben bist."));
    }

// Allgemeine Nachrichten (Beibehalten)
    public String getNoPermissionMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission", "&cDazu hast du keine Berechtigung."));
    }

    public String getConsoleOnlyMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.console-only", "&cDieser Befehl kann nur von der Konsole ausgeführt werden."));
    }

    public String getPlayerOnlyMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player-only", "&cDieser Befehl kann nur von einem Spieler ausgeführt werden."));
    }

    public String getPlayerNotFoundMessage() { // NEU: Allgemeine Spieler nicht gefunden Nachricht
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player-not-found", "&cSpieler '[player]' wurde nicht gefunden oder ist nicht online."));
    }
}