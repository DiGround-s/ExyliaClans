package net.diground.exylia;

import net.diground.exylia.api.ClanAPI;
import net.diground.exylia.api.ClanAPIImpl;
import net.diground.exylia.cache.ClanItemCache;
import net.diground.exylia.cache.LeaderboardCache;
import net.diground.exylia.dependencies.PlaceholderAPI;
import net.diground.exylia.commands.main.ClanAdminCommand;
import net.diground.exylia.commands.main.ClanCommand;
import net.diground.exylia.commands.extras.TeamLocalizator;
import net.diground.exylia.listeners.*;
import net.diground.exylia.managers.*;
import net.diground.exylia.menus.listeners.MenuListener;
import net.diground.exylia.menus.managers.MainMenuManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.*;
import java.io.File;
import java.util.*;


public final class ExyliaClans extends JavaPlugin {

    private Connection connection;
    private String databaseType;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private File configFile;
    private ClanChestManager chestManager;
    private ClanChatManager chatManager;
    private ClanManager clanManager;
    private net.diground.exylia.clan.ClanManager newClanManager;
    private RankManager rankManager;
    private Economy economy;
    private LimitManager limitManager;
    private final DatabaseManager databaseConnection;
    private final Map<ItemStack, String> menuCommands = new HashMap<>();
    private final Map<String, Inventory> menuInventories = new HashMap<>();
    public static final Map<UUID, BukkitTask> refreshTasks = new HashMap<>();
    private MainMenuManager mainMenuManager;
    private ClanAPI clanAPI;
    private LeaderboardCache leaderboardCache;
    private ClanItemCache clanItemCache;

    public ExyliaClans() {
        databaseConnection = new DatabaseManager(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        long start = System.currentTimeMillis();
        setupConfig();
        long end = System.currentTimeMillis();
        Bukkit.getLogger().info("Config actualizado en " + (end - start) + "ms");


        long start2 = System.currentTimeMillis();
        setupMessages();
        long end2 = System.currentTimeMillis();
        Bukkit.getLogger().info("Mensajes actualizado en " + (end2 - start2) + "ms");


        long start3 = System.currentTimeMillis();
        setupMenus();
        long end3 = System.currentTimeMillis();
        Bukkit.getLogger().info("Menus actualizados en " + (end3 - start3) + "ms");

        long start4 = System.currentTimeMillis();
        leaderboardCache = new LeaderboardCache(this);
        clanItemCache = new ClanItemCache(this);
        long end4 = System.currentTimeMillis();
        Bukkit.getLogger().info("Caché de leaderboard actualizado en " + (end4 - start4) + "ms");


        long start5 = System.currentTimeMillis();
        databaseConnection.setupDatabase();
        databaseConnection.updateDatabase();
        long end5 = System.currentTimeMillis();
        Bukkit.getLogger().info("Base de datos actualizada en " + (end5 - start5) + "ms");

        long start6 = System.currentTimeMillis();
        this.clanAPI = new ClanAPIImpl(this);
        long end6 = System.currentTimeMillis();
        Bukkit.getLogger().info("API de Clanes actualizada en " + (end6 - start6) + "ms");

        long start7 = System.currentTimeMillis();
        registerCommands();
        long end7 = System.currentTimeMillis();
        Bukkit.getLogger().info("Comandos actualizados en " + (end7 - start7) + "ms");

        long start8 = System.currentTimeMillis();
        registerListeners();
        long end8 = System.currentTimeMillis();
        Bukkit.getLogger().info("Listeners actualizados en " + (end8 - start8) + "ms");

        long start9 = System.currentTimeMillis();
        registerManagers();
        long end9 = System.currentTimeMillis();
        Bukkit.getLogger().info("Managers actualizados en " + (end9 - start9) + "ms");

        databaseType = this.getConfig().getString("database.type");
        getLogger().info("FenixClans has been enabled!");
        if (!setupEconomy()) {
            getLogger().warning("Vault plugin not found! Clan bank feature will be disabled.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        long start = System.currentTimeMillis();
        databaseConnection.closeDatabaseConnection();
        long end = System.currentTimeMillis();
        getLogger().info("Base de datos cerrada en " + (end - start) + "ms");

        getLogger().info("FenixClans has been disabled!");
    }

    public Connection getConnection() {
        return databaseConnection.getConnection();
    }

    public String getDatabaseType() {
        return databaseType;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }


    public ClanAPI getClanAPI() {
        return clanAPI;
    }

    private void setupConfig() {
        saveDefaultConfig();
    }

    private void setupMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    public void reloadMessagesConfig() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    public void reloadConfig() {
        super.reloadConfig();
    }










    public void setupMenus() {
        List<String> menuFilesName = Arrays.asList(
                "allies.yml", "enemies.yml", "info.yml",
                "leaderboard.yml", "main.yml", "main_noclan.yml", "members.yml", "banner.yml"
        );

        for (String menuFileName : menuFilesName) {
            File menuFile = new File(getDataFolder() + File.separator + "menus", menuFileName);
            if (!menuFile.exists()) {
                saveResource("menus/" + menuFileName, false);
            }
        }
    }


    public FileConfiguration getMenuConfig(String menuFileName) {
        File menuFile = new File(getDataFolder() + File.separator + "menus", menuFileName);
        if (!menuFile.exists()) {
            saveResource("menus/" + menuFileName, false);
            throw new IllegalArgumentException("El archivo de menú " + menuFileName + " no existe.");
        }
        return YamlConfiguration.loadConfiguration(menuFile);
    }


    public boolean isNotificationEnabled(String key) {
        return messagesConfig.getBoolean("notifications." + key + ".enabled", false);
    }

    public boolean isNotificationChatEnabled(String key) {
        return messagesConfig.getBoolean("notifications." + key + ".chat.enabled", false);
    }

    public boolean isNotificationActionBarEnabled(String key) {
        return messagesConfig.getBoolean("notifications." + key + ".action_bar.enabled", false);
    }

    public boolean isNotificationTitleEnabled(String key) {
        return messagesConfig.getBoolean("notifications." + key + ".title.enabled", false);
    }

    private void registerCommands() {
        ClanCommand clanCommand = new ClanCommand(this);
        getCommand("clan").setExecutor(clanCommand);
        getCommand("clan").setTabCompleter(clanCommand);
        getCommand("tl").setExecutor(new TeamLocalizator(this));
        getCommand("clanadmin").setExecutor(new ClanAdminCommand(this));
        getCommand("clanadmin").setTabCompleter(new ClanAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

    }

    private void registerManagers() {
        chestManager = new ClanChestManager(this);
        chatManager = new ClanChatManager(this);
        clanManager = new ClanManager(this);
        newClanManager = new net.diground.exylia.clan.ClanManager(this);
        rankManager = new RankManager(this);
        limitManager = new LimitManager(this);
        mainMenuManager = new MainMenuManager(this);
        new PlaceholderAPI(this).register();
    }

    public LimitManager getLimitManager() {
        return limitManager;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString("messages." + key);
        if (message == null) {
            return "Message not found: " + key;
        }
        return message;
    }

    public String getCustomPlaceholderMessage(String key) {
        String message = messagesConfig.getString("placeholders." + key);
        if (message == null) {
            return "Placeholder not found: " + key;
        }
        return message;
    }
    public ClanChatManager getClanChatManager() {
        return chatManager;
    }
    public ClanManager getClanManager() {
        return clanManager;
    }

    public net.diground.exylia.clan.ClanManager getNewClanManager() {
        return newClanManager;
    }
    public Economy getEconomy() {
        return economy;
    }
    public RankManager getRankManager() {
        return rankManager;
    }

    public String getNotificationChatMessage(String key) {
        return messagesConfig.getString("notifications." + key + ".chat.message", "");
    }

    public String getNotificationActionBarMessage(String key) {
        return messagesConfig.getString("notifications." + key + ".action_bar.message", "");
    }

    public String getNotificationTitleMessage(String key) {
        return messagesConfig.getString("notifications." + key + ".title.message", "");
    }

    public String getNotificationTitleSubtitle(String key) {
        return messagesConfig.getString("notifications." + key + ".title.subtitle", "");
    }
    public Map<ItemStack, String> getMenuCommands() {
        return menuCommands;
    }
    public Map<String, Inventory> getMenuInventories() {
        return menuInventories;
    }

    public MainMenuManager getMenuManager() {
        return mainMenuManager;
    }

    public LeaderboardCache getLeaderboardCache() {
        return leaderboardCache;
    }

    public ClanItemCache getClanItemCache() {
        return clanItemCache;
    }
}
