package net.diground.exylia.menus.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.LimitManager;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.menus.model.InventorySection;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.diground.exylia.ExyliaClans.refreshTasks;

public class MainMenuManager {

    private final ExyliaClans plugin;
    private ArrayList<InventoryPlayer> players;
    private Map<String, Map<ClickType, List<String>>> sectionCommandsMap;
    public static int allowedBannerSlot;
    private static String timeFormat;

    public MainMenuManager(ExyliaClans plugin) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.sectionCommandsMap = new HashMap<>();
    }

    public InventoryPlayer getInventoryPlayer(Player player) {
        for (InventoryPlayer inventoryPlayer : players) {
            if (inventoryPlayer.getPlayer() == player) {
                return inventoryPlayer;
            }
        }
        return null;
    }

    public void removePlayer(Player player) {
        players.removeIf(inventoryPlayer -> inventoryPlayer.getPlayer() == player);
    }

    public void openMainMenu(InventoryPlayer inventoryPlayer, int clanId) {
        inventoryPlayer.setSection(InventorySection.MAIN);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("main.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadItems(inv, player, config, inventoryPlayer);

        player.openInventory(inv);
        players.add(inventoryPlayer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                refreshItems(inv, player, config);
            }
        }, 0L, 20L);
        refreshTasks.put(player.getUniqueId(), task);
    }

    public void openMembersMenu(InventoryPlayer inventoryPlayer, int clanId, int page) {
        timeFormat = plugin.getCustomPlaceholderMessage("timeformat");
        SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());
        inventoryPlayer.setSection(InventorySection.MEMBERS);
        inventoryPlayer.setClanId(clanId);
        inventoryPlayer.setPage(page);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("members.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        addNavigationItems(inv, player, clanId, config, inventoryPlayer.getSection());

        loadItems(inv, player, config, inventoryPlayer);

        refreshMemberItems(inv, inventoryPlayer, config, dateFormat, player);

        player.openInventory(inv);
        players.add(inventoryPlayer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                refreshItems(inv, player, config);
                refreshMemberItems(inv, inventoryPlayer, config, dateFormat, player);
            }
        }, 0L, 20L);

        refreshTasks.put(player.getUniqueId(), task);
    }


    public void openAlliesMenu(InventoryPlayer inventoryPlayer, int clanId) {
        inventoryPlayer.setSection(InventorySection.ALLIES);
        inventoryPlayer.setClanId(clanId);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("allies.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadItems(inv, player, config, inventoryPlayer);

        addBackItem(inv, player, config, inventoryPlayer.getSection());

        refreshAlliesItems(inv, inventoryPlayer, config, player);

        player.openInventory(inv);
        players.add(inventoryPlayer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                refreshItems(inv, player, config);
                refreshAlliesItems(inv, inventoryPlayer, config, player);
            }
        }, 0L, 20L);

        refreshTasks.put(player.getUniqueId(), task);
    }

    public void openEnemiesMenu(InventoryPlayer inventoryPlayer, int clanId) {
        inventoryPlayer.setSection(InventorySection.ENEMIES);
        inventoryPlayer.setClanId(clanId);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("enemies.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadItems(inv, player, config, inventoryPlayer);

        refreshEnemiesItems(inv, inventoryPlayer, config, player);
        addBackItem(inv, player, config, inventoryPlayer.getSection());

        player.openInventory(inv);
        players.add(inventoryPlayer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                refreshItems(inv, player, config);
                refreshEnemiesItems(inv, inventoryPlayer, config, player);
            }
        }, 0L, 20L);

        refreshTasks.put(player.getUniqueId(), task);
    }

    public void openInfoMenu(InventoryPlayer inventoryPlayer, int clanId) {
        inventoryPlayer.setSection(InventorySection.INFO);
        inventoryPlayer.setClanId(clanId);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("info.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadInfoItems(inv, player, config, clanId, inventoryPlayer);

        player.openInventory(inv);
        players.add(inventoryPlayer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                refreshItems(inv, player, config);
            }
        }, 0L, 20L);

        refreshTasks.put(player.getUniqueId(), task);
    }

    public void openLeaderboardMenu(InventoryPlayer inventoryPlayer, int page, String filter) {
        inventoryPlayer.setSection(InventorySection.LEADERBOARD);
        inventoryPlayer.setPage(page);
        inventoryPlayer.setFilter(filter);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("leaderboard.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title);
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadFilterItems(inv, inventoryPlayer, config, player, filter);

        addNavigationItems(inv, player, -1, config, inventoryPlayer.getSection());

        loadItems(inv, player, config, inventoryPlayer);

        loadLeaderboardItems(inv, inventoryPlayer, config, player, filter);


        player.openInventory(inv);
        players.add(inventoryPlayer);
    }

    public void openMainNoClanMenu(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setSection(InventorySection.MAIN_NOCLAN);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("main_noclan.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        title = PlaceholderAPI.setPlaceholders(player, title);
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadItems(inv, player, config, inventoryPlayer);

        player.openInventory(inv);
        players.add(inventoryPlayer);
    }

    public void openBannerMenu(InventoryPlayer inventoryPlayer, int clanId) {
        inventoryPlayer.setSection(InventorySection.BANNER);
        inventoryPlayer.setClanId(clanId);
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration config = plugin.getMenuConfig("banner.yml");
        String title = config.getString("title");
        int size = config.getInt("size");

        allowedBannerSlot = config.getInt("upload_banner.slot");
        int showBannerSlot = config.getInt("show_banner.slot");

        title = PlaceholderAPI.setPlaceholders(player, title).replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId)));
        Inventory inv = Bukkit.createInventory(null, size, ChatUtils.oldTranslateColors(title));

        loadItems(inv, player, config, inventoryPlayer);

        ItemStack banner = ClanUtils.getClanBanner(plugin, clanId);
        if (banner == null) {
            banner = new ItemStack(Material.valueOf(plugin.getConfig().getString("leaderboard.no_banner_material")));
            ItemMeta meta = banner.getItemMeta();

            String name = config.getString("show_banner.no_banner.name");
            List<String> lore = config.getStringList("show_banner.no_banner.lore");

            name = PlaceholderAPI.setPlaceholders(player, name);
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }


            meta.setDisplayName(ChatUtils.oldTranslateColors(name));
            meta.setLore(coloredLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);

            banner.setItemMeta(meta);

            inv.setItem(showBannerSlot, banner);
        } else {
            inv.setItem(allowedBannerSlot, banner);

            BannerMeta meta = (BannerMeta) banner.getItemMeta();

            String name = config.getString("show_banner.banner.name");
            List<String> lore = config.getStringList("show_banner.banner.lore");

            name = PlaceholderAPI.setPlaceholders(player, name);
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }


            meta.setDisplayName(ChatUtils.oldTranslateColors(name));
            meta.setLore(coloredLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            banner.setItemMeta(meta);

            inv.setItem(showBannerSlot, banner);
        }


        player.openInventory(inv);
        players.add(inventoryPlayer);
    }

    private void loadItems(Inventory inv, Player player, FileConfiguration config, InventoryPlayer inventoryPlayer) {
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            Material material = Material.valueOf(config.getString(path + ".material"));
            String slotPath = path + ".slot";
            String slotsPath = path + ".slots";

            List<Integer> slots = new ArrayList<>();

            if (config.contains(slotPath)) {
                slots.add(config.getInt(slotPath));
            } else if (config.contains(slotsPath)) {
                slots.addAll(config.getIntegerList(slotsPath));
            } else {
                continue;
            }

            String name = config.getString(path + ".name");
            List<String> lore = config.getStringList(path + ".lore");

            name = PlaceholderAPI.setPlaceholders(player, name);
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtils.oldTranslateColors(name));
            meta.setLore(coloredLore);
            if (config.contains(path + ".glow")) {
                boolean glow = config.getBoolean(path + ".glow");
                if (glow) {
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            item.setItemMeta(meta);

            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            if (config.contains(path + ".left_click_commands")) {
                itemCommands.put(ClickType.LEFT, config.getStringList(path + ".left_click_commands"));
            }
            if (config.contains(path + ".right_click_commands")) {
                itemCommands.put(ClickType.RIGHT, config.getStringList(path + ".right_click_commands"));
            }
            if (config.contains(path + ".middle_click_commands")) {
                itemCommands.put(ClickType.MIDDLE, config.getStringList(path + ".middle_click_commands"));
            }
            if (config.contains(path + ".click_commands")) {
                itemCommands.put(ClickType.UNKNOWN, config.getStringList(path + ".click_commands"));
            }

            for (int slot : slots) {
                String key2 = inventoryPlayer.getSection().name() + "_" + slot;
                sectionCommandsMap.put(key2, itemCommands);
                inv.setItem(slot, item);
            }
        }
    }


    private void loadFilterItems(Inventory inv, InventoryPlayer inventoryPlayer, FileConfiguration config, Player player, String filter) {
        for (String key : config.getConfigurationSection("filter").getKeys(false)) {
            String path = "filter." + key;
            Material material = Material.valueOf(config.getString(path + ".material"));
            String slotPath = path + ".slot";
            String slotsPath = path + ".slots";

            List<Integer> slots = new ArrayList<>();

            if (config.contains(slotPath)) {
                slots.add(config.getInt(slotPath));
            } else if (config.contains(slotsPath)) {
                slots.addAll(config.getIntegerList(slotsPath));
            } else {
                continue;
            }

            String name = config.getString(path + ".name");
            List<String> lore = config.getStringList(path + ".lore");

            String selectPlaceholder = key.equals(filter) ? plugin.getCustomPlaceholderMessage("select.selected") : plugin.getCustomPlaceholderMessage("select.deselected");

            name = PlaceholderAPI.setPlaceholders(player, name).replace("%select%", selectPlaceholder);
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                line = PlaceholderAPI.setPlaceholders(player, line).replace("%select%", selectPlaceholder);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtils.oldTranslateColors(name));
            meta.setLore(coloredLore);

            if (key.equals(filter)) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);

            for (int slot : slots) {
                inv.setItem(slot, item);
            }
        }
    }

    private void loadLeaderboardItems(Inventory inv, InventoryPlayer inventoryPlayer, FileConfiguration config, Player player, String filter) {
        long startTime = System.currentTimeMillis();

        long start = System.currentTimeMillis();
        List<Integer> clanIds = ClanUtils.getAllClansIds(plugin);
        long end = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo para obtener IDs de clanes: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        List<Integer> clansSlots = config.getIntegerList("clans.slots");
        String clansName = config.getString("clans.name");
        List<String> clansLore = config.getStringList("clans.lore");
        end = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo para obtener configuraciones: " + (end - start) + "ms");

        clanIds = filterAndSortClans(clanIds, filter);

        int clansPerPage = clansSlots.size();
        int totalMembers = clanIds.size();
        int startIndex = inventoryPlayer.getPage() * clansPerPage;
        int endIndex = Math.min(startIndex + clansPerPage, totalMembers);

        long start2 = System.currentTimeMillis();
        for (int i = startIndex; i < endIndex; i++) {
            long startClan = System.currentTimeMillis();

            Integer clan = clanIds.get(i);
            int slot = clansSlots.get(i - startIndex);

            OfflinePlayer leader = ClanUtils.getClanLeader(plugin, clan);
            String leaderName = leader == null ? "" : leader.getName();

            String clanName = ChatUtils.oldTranslateColors(clansName.replace("%name%", ClanUtils.getClanNameById(plugin, clan)).replace("%position%", String.valueOf(i + 1)).replace("%prefix%", Objects.requireNonNull(ClanUtils.getClanPrefix(plugin, clan))));
            String finalLeaderName = leaderName;
            String clanPrefix = ClanUtils.getClanPrefix(plugin, clan);
            int onlineMembersCount = ClanUtils.getOnlineClanMembersCount(plugin, clan);
            int memberCount = ClanUtils.getMemberCount(plugin, clan);
            Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clan);

            int clanKills = stats.getOrDefault("kills", 0);
            int clanDeaths = stats.getOrDefault("deaths", 0);
            double clanKDR = ClanUtils.getClanKDR(plugin, clan);
            int finalI = i;
            List<String> memberLore = clansLore.stream()
                    .map(l -> l.replace("%name%", ClanUtils.getClanNameById(plugin, clan)))
                    .map(l -> l.replace("%online%", String.valueOf(onlineMembersCount)))
                    .map(l -> l.replace("%total%", String.valueOf(memberCount)))
                    .map(l -> l.replace("%kills%", String.valueOf(clanKills)))
                    .map(l -> l.replace("%deaths%", String.valueOf(clanDeaths)))
                    .map(l -> l.replace("%kdr%", String.valueOf(clanKDR)))
                    .map(l -> l.replace("%position%", String.valueOf(finalI + 1)))
                    .map(l -> l.replace("%leader%", finalLeaderName))
                    .map(l -> l.replace("%prefix%", Objects.requireNonNull(clanPrefix)))
                    .collect(Collectors.toList());

            ItemStack clanBanner = ClanUtils.getClanBanner(plugin, clan);
            if (clanBanner == null) {
                clanBanner = new ItemStack(Material.valueOf(plugin.getConfig().getString("leaderboard.no_banner_material")));
            }
            ItemMeta meta = clanBanner.getItemMeta();
            if (config.contains("clans.glow")) {
                boolean glow = config.getBoolean("clans.glow");
                if (glow) {
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            meta.setDisplayName(clanName);
            List<String> coloredLore = new ArrayList<>();
            for (String line : memberLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }
            meta.setLore(coloredLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            clanBanner.setItemMeta(meta);

            inv.setItem(slot, clanBanner);


            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            if (config.contains("clans.left_click_commands")) {
                List<String> commands = config.getStringList("clans.left_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", ClanUtils.getClanNameById(plugin, clan))).collect(Collectors.toList());
                itemCommands.put(ClickType.LEFT, commands);
            }
            if (config.contains("clans.right_click_commands")) {
                List<String> commands = config.getStringList("clans.right_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", ClanUtils.getClanNameById(plugin, clan))).collect(Collectors.toList());
                itemCommands.put(ClickType.RIGHT, commands);
            }
            if (config.contains("clans.middle_click_commands")) {
                List<String> commands = config.getStringList("clans.middle_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", ClanUtils.getClanNameById(plugin, clan))).collect(Collectors.toList());
                itemCommands.put(ClickType.MIDDLE, commands);
            }
            if (config.contains("clans.click_commands")) {
                List<String> commands = config.getStringList("clans.click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", ClanUtils.getClanNameById(plugin, clan))).collect(Collectors.toList());
                itemCommands.put(ClickType.UNKNOWN, commands);
            }

            String key = InventorySection.LEADERBOARD.name() + "_" + slot;
            sectionCommandsMap.put(key, itemCommands);

            long endClan = System.currentTimeMillis();
            Bukkit.getLogger().info("Tiempo total para procesar clan ID " + clan + ": " + (endClan - startClan) + "ms");
        }
        long end2 = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo total para cargar los clanes: " + (end2 - start2) + "ms");

        long end3 = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo total para cargar los elementos del leaderboard: " + (end3 - startTime) + "ms");
    }


    private void loadInfoItems(Inventory inv, Player player, FileConfiguration config, int clanId, InventoryPlayer inventoryPlayer) {
        long startTime, endTime;

        // Measure time for enemy and allies names
        startTime = System.nanoTime();
        String enemyNames = ClanUtils.getClanEnemies(plugin, clanId).toString();
        String alliesNames = ClanUtils.getClanAllies(plugin, clanId).toString();
        if (enemyNames.equals("[]")) {
            enemyNames = plugin.getCustomPlaceholderMessage("no_enemies");
        }
        if (alliesNames.equals("[]")) {
            alliesNames = plugin.getCustomPlaceholderMessage("no_allies");
        }
        endTime = System.nanoTime();
        System.out.println("Time for enemy and allies names: " + (endTime - startTime) / 1_000_000 + " ms");

        // Measure time for banner item
        startTime = System.nanoTime();
        if (config.contains("banner")) {
            ItemStack clanBanner = ClanUtils.getClanBanner(plugin, clanId);
            Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clanId);

            List<String> loreBanner = config.getStringList("banner.lore");
            List<String> coloredLoreBanner = new ArrayList<>();
            String finalEnemyNames = enemyNames;
            String finalAlliesNames = alliesNames;

            int onlineMembersCount = ClanUtils.getOnlineClanMembersCount(plugin, clanId);
            int totalMembersCount = ClanUtils.getMemberCount(plugin, clanId);
            int kills = stats.getOrDefault("kills", 0);
            String leaderName = Objects.requireNonNull(ClanUtils.getClanLeader(plugin, clanId)).getName();
            int deaths = stats.getOrDefault("deaths", 0);
            double kdr = ClanUtils.getClanKDR(plugin, clanId);
            String clanName = Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId));
            String clanPrefix = Objects.requireNonNull(ClanUtils.getClanPrefix(plugin, clanId));
            int allyCount = ClanUtils.getAllianceCount(plugin, clanId);
            int enemyCount = ClanUtils.getEnemyCount(plugin, clanId);
            int allyMax = plugin.getLimitManager().getMaxAlliances();
            int enemyMax = plugin.getLimitManager().getMaxEnemies();

// Aplica los valores cacheados en lugar de llamar a los métodos repetidamente
            List<String> infoLore = loreBanner.stream()
                    .map(l -> l.replace("%online%", String.valueOf(onlineMembersCount)))
                    .map(l -> l.replace("%total%", String.valueOf(totalMembersCount)))
                    .map(l -> l.replace("%kills%", String.valueOf(kills)))
                    .map(l -> l.replace("%leader%", leaderName))
                    .map(l -> l.replace("%deaths%", String.valueOf(deaths)))
                    .map(l -> l.replace("%kdr%", String.valueOf(kdr)))
                    .map(l -> l.replace("%name%", clanName))
                    .map(l -> l.replace("%prefix%", clanPrefix))
                    .map(l -> l.replace("%ally_count%", String.valueOf(allyCount)))
                    .map(l -> l.replace("%enemy_count%", String.valueOf(enemyCount)))
                    .map(l -> l.replace("%ally_max%", String.valueOf(allyMax)))
                    .map(l -> l.replace("%enemy_max%", String.valueOf(enemyMax)))
                    .map(l -> l.replace("%ally_names%", finalAlliesNames))
                    .map(l -> l.replace("%enemy_names%", finalEnemyNames))
                    .collect(Collectors.toList());

            for (String line : infoLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLoreBanner.add(ChatUtils.oldTranslateColors(line));
            }

            if (clanBanner == null) {
                clanBanner = new ItemStack(Material.valueOf(plugin.getConfig().getString("leaderboard.no_banner_material")));
            } else {
                clanBanner = new ItemStack(ClanUtils.getClanBanner(plugin, clanId));
            }

            ItemMeta meta = clanBanner.getItemMeta();
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(player, ChatUtils.oldTranslateColors(config.getString("banner.name"))));
            meta.setLore(coloredLoreBanner);
            if (config.contains("banner.glow") && config.getBoolean("banner.glow")) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            clanBanner.setItemMeta(meta);
            inv.setItem(config.getInt("banner.slot"), clanBanner);
        }
        endTime = System.nanoTime();
        System.out.println("Time for banner item: " + (endTime - startTime) / 1_000_000 + " ms");

        // Measure time for items
        startTime = System.nanoTime();
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            Material material = Material.valueOf(config.getString(path + ".material"));
            String slotPath = path + ".slot";
            String slotsPath = path + ".slots";

            List<Integer> slots = new ArrayList<>();
            if (config.contains(slotPath)) {
                slots.add(config.getInt(slotPath));
            } else if (config.contains(slotsPath)) {
                slots.addAll(config.getIntegerList(slotsPath));
            } else {
                continue;
            }

            String name = PlaceholderAPI.setPlaceholders(player, config.getString(path + ".name"));
            List<String> lore = config.getStringList(path + ".lore");
            List<String> coloredLore = new ArrayList<>();
            Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clanId);

            String finalEnemyNames1 = enemyNames;
            String finalEnemyNames2 = enemyNames;
            int onlineMembersCount = ClanUtils.getOnlineClanMembersCount(plugin, clanId);
            int totalMembersCount = ClanUtils.getMemberCount(plugin, clanId);
            int kills = stats.getOrDefault("kills", 0);
            String leaderName = Objects.requireNonNull(ClanUtils.getClanLeader(plugin, clanId)).getName();
            int deaths = stats.getOrDefault("deaths", 0);
            double kdr = ClanUtils.getClanKDR(plugin, clanId);
            String clanName = Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId));
            String clanPrefix = Objects.requireNonNull(ClanUtils.getClanPrefix(plugin, clanId));
            int allyCount = ClanUtils.getAllianceCount(plugin, clanId);
            int enemyCount = ClanUtils.getEnemyCount(plugin, clanId);
            int allyMax = plugin.getLimitManager().getMaxAlliances();
            int enemyMax = plugin.getLimitManager().getMaxEnemies();

// Aplica los valores cacheados en lugar de llamar a los métodos repetidamente
            List<String> infoLore = lore.stream()
                    .map(l -> l.replace("%online%", String.valueOf(onlineMembersCount)))
                    .map(l -> l.replace("%total%", String.valueOf(totalMembersCount)))
                    .map(l -> l.replace("%kills%", String.valueOf(kills)))
                    .map(l -> l.replace("%leader%", leaderName))
                    .map(l -> l.replace("%deaths%", String.valueOf(deaths)))
                    .map(l -> l.replace("%kdr%", String.valueOf(kdr)))
                    .map(l -> l.replace("%name%", clanName))
                    .map(l -> l.replace("%prefix%", clanPrefix))
                    .map(l -> l.replace("%ally_count%", String.valueOf(allyCount)))
                    .map(l -> l.replace("%enemy_count%", String.valueOf(enemyCount)))
                    .map(l -> l.replace("%ally_max%", String.valueOf(allyMax)))
                    .map(l -> l.replace("%enemy_max%", String.valueOf(enemyMax)))
                    .map(l -> l.replace("%ally_names%", finalEnemyNames1))
                    .map(l -> l.replace("%enemy_names%", finalEnemyNames2))
                    .collect(Collectors.toList());

            for (String line : infoLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtils.oldTranslateColors(name));
            meta.setLore(coloredLore);
            if (config.contains(path + ".glow") && config.getBoolean(path + ".glow")) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);

            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            if (config.contains(path + ".left_click_commands")) {
                itemCommands.put(ClickType.LEFT, config.getStringList(path + ".left_click_commands").stream()
                        .map(cmd -> cmd.replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId))))
                        .collect(Collectors.toList()));
            }
            if (config.contains(path + ".right_click_commands")) {
                itemCommands.put(ClickType.RIGHT, config.getStringList(path + ".right_click_commands").stream()
                        .map(cmd -> cmd.replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId))))
                        .collect(Collectors.toList()));
            }
            if (config.contains(path + ".middle_click_commands")) {
                itemCommands.put(ClickType.MIDDLE, config.getStringList(path + ".middle_click_commands").stream()
                        .map(cmd -> cmd.replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId))))
                        .collect(Collectors.toList()));
            }
            if (config.contains(path + ".click_commands")) {
                itemCommands.put(ClickType.LEFT, config.getStringList(path + ".click_commands").stream()
                        .map(cmd -> cmd.replace("%name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, clanId))))
                        .collect(Collectors.toList()));
            }

            for (int slot : slots) {
                String key2 = inventoryPlayer.getSection().name() + "_" + slot;
                sectionCommandsMap.put(key2, itemCommands);
                inv.setItem(slot, item);
            }
        }
        endTime = System.nanoTime();
        System.out.println("Time for items: " + (endTime - startTime) / 1_000_000 + " ms");
    }




    private void refreshItems(Inventory inv, Player player, FileConfiguration config) {
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            if (config.contains(path + ".refresh") && config.getBoolean(path + ".refresh")) {
                int slot = config.getInt(path + ".slot");
                ItemStack item = inv.getItem(slot);

                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (config.contains(path + ".glow")) {
                        boolean glow = config.getBoolean(path + ".glow");
                        if (glow) {
                            meta.addEnchant(Enchantment.LURE, 1, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                    }
                    String name = config.getString(path + ".name");
                    List<String> lore = config.getStringList(path + ".lore");

                    name = PlaceholderAPI.setPlaceholders(player, name);
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        line = PlaceholderAPI.setPlaceholders(player, line);
                        coloredLore.add(ChatUtils.oldTranslateColors(line));
                    }

                    meta.setDisplayName(ChatUtils.oldTranslateColors(name));
                    meta.setLore(coloredLore);
                    item.setItemMeta(meta);
                    inv.setItem(slot, item);
                }
            }
        }
    }

    private void refreshMemberItems(Inventory inv, InventoryPlayer inventoryPlayer, FileConfiguration config, SimpleDateFormat dateFormat, Player player) {
        long startTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Inicio de refreshMemberItems");

        List<OfflinePlayer> members = ClanUtils.getClanOfflinePlayerMembers(plugin, inventoryPlayer.getClanId());
        List<Integer> membersSlots = config.getIntegerList("members.slots");
        String membersName = config.getString("members.name");
        List<String> membersLore = config.getStringList("members.lore");

        // Optimización: Pre-calcular los rangos y el estado online
        Map<UUID, Integer> playerRanks = new HashMap<>();
        Map<UUID, Boolean> playerOnlineStatus = new HashMap<>();
        for (OfflinePlayer member : members) {
            UUID memberId = member.getUniqueId();
            playerRanks.put(memberId, ClanUtils.getPlayerRankId(plugin, memberId.toString()));
            playerOnlineStatus.put(memberId, member.isOnline());
        }

        long sortStartTime = System.currentTimeMillis();
        members.sort((o1, o2) -> {
            UUID o1Id = o1.getUniqueId();
            UUID o2Id = o2.getUniqueId();

            boolean o1Online = playerOnlineStatus.get(o1Id);
            boolean o2Online = playerOnlineStatus.get(o2Id);
            if (o1Online && !o2Online) {
                return -1;
            }
            if (!o1Online && o2Online) {
                return 1;
            }

            int rankComparison = Integer.compare(playerRanks.get(o1Id), playerRanks.get(o2Id));
            if (rankComparison != 0) {
                return rankComparison;
            }

            return Objects.requireNonNull(o1.getName()).compareTo(Objects.requireNonNull(o2.getName()));
        });
        long sortEndTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo para ordenar miembros: " + (sortEndTime - sortStartTime) + "ms");

        int membersPerPage = membersSlots.size();
        int totalMembers = members.size();
        int startIndex = inventoryPlayer.getPage() * membersPerPage;
        int endIndex = Math.min(startIndex + membersPerPage, totalMembers);

        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer member = members.get(i);
            int slot = membersSlots.get(i - startIndex);

            assert membersName != null;
            String memberName = ChatUtils.oldTranslateColors(membersName.replace("%name%", Objects.requireNonNull(member.getName())));

            String name = member.getName();
            String rank = ChatUtils.oldTranslateColors(ClanUtils.getRankDisplayNameById(plugin, ClanUtils.getPlayerRankId(plugin, member.getUniqueId().toString())));
            String timeJoined;
            String lastSeen;
            String onlineStatus;
            try {
                Date joinDate = ClanUtils.getPlayerTimeStampByName(plugin, name);
                timeJoined = ChatUtils.oldTranslateColors(dateFormat.format(joinDate));
            } catch (Exception e) {
                e.printStackTrace();
                timeJoined = "Format error";
            }
            try {
                Long seenLong = member.getLastSeen();
                Date lastSeenDate = new Date(seenLong);
                lastSeen = ChatUtils.oldTranslateColors(dateFormat.format(lastSeenDate));
            } catch (Exception e) {
                e.printStackTrace();
                lastSeen = "Format error";
            }
            onlineStatus = ChatUtils.oldTranslateColors(member.isOnline() ? plugin.getCustomPlaceholderMessage("online.online") : plugin.getCustomPlaceholderMessage("online.offline"));

            String finalTimeJoined = timeJoined;
            String finalLastSeen = lastSeen;
            List<String> memberLore = membersLore.stream()
                    .map(l -> l.replace("%name%", name))
                    .map(l -> l.replace("%rank%", rank))
                    .map(l -> l.replace("%time_joined%", finalTimeJoined))
                    .map(l -> l.replace("%last_seen%", finalLastSeen))
                    .map(l -> l.replace("%online%", onlineStatus))
                    .collect(Collectors.toList());


            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (config.contains("members.glow")) {
                boolean glow = config.getBoolean("members.glow");
                if (glow) {
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            meta.setOwningPlayer(member);
            meta.setDisplayName(memberName);
            List<String> coloredLore = new ArrayList<>();
            for (String line : memberLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);

            inv.setItem(slot, item);

            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            if (config.contains("members.left_click_commands")) {
                List<String> commands = config.getStringList("members.left_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", member.getName())).collect(Collectors.toList());
                itemCommands.put(ClickType.LEFT, commands);
            }
            if (config.contains("members.right_click_commands")) {
                List<String> commands = config.getStringList("members.right_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", member.getName())).collect(Collectors.toList());
                itemCommands.put(ClickType.RIGHT, commands);
            }
            if (config.contains("members.middle_click_commands")) {
                List<String> commands = config.getStringList("members.middle_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", member.getName())).collect(Collectors.toList());
                itemCommands.put(ClickType.MIDDLE, commands);
            }
            if (config.contains("members.click_commands")) {
                List<String> commands = config.getStringList("members.click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%name%", member.getName())).collect(Collectors.toList());
                itemCommands.put(ClickType.UNKNOWN, commands);
            }

            String key = InventorySection.MEMBERS.name() + "_" + slot;
            sectionCommandsMap.put(key, itemCommands);
        }
        long itemsEndTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo total para preparar ítems: " + (itemsEndTime - startTime) + "ms");
    }

    public void refreshAlliesItems(Inventory inv, InventoryPlayer inventoryPlayer, FileConfiguration config, Player player) {
        List<Integer> allies = ClanUtils.getClanAlliesIds(plugin, inventoryPlayer.getClanId());
        List<Integer> alliesSlots = config.getIntegerList("allies.slots");
        String alliesName = config.getString("allies.name");
        List<String> alliesLore = config.getStringList("allies.lore");
        Material material = Material.valueOf(config.getString("allies.material"));

        for (int i = 0; i < allies.size(); i++) {
            int slot = alliesSlots.get(i);
            int allyId = allies.get(i);
            ItemStack item = new ItemStack(material);
            item.setAmount(1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtils.oldTranslateColors(alliesName.replace("%ally_name%", ClanUtils.getClanName(plugin, allyId))));
            if (config.contains("allies.glow")) {
                boolean glow = config.getBoolean("allies.glow");
                if (glow) {
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            List<String> coloredLore = new ArrayList<>();
            Map<String, Integer> stats = ClanUtils.getClanStats(plugin, allyId);
            List<String> allieLore = alliesLore.stream()
                    .map(l -> l.replace("%ally_online%", ClanUtils.getOnlineClanMembersCount(plugin, allyId) + ""))
                    .map(l -> l.replace("%ally_total%", ClanUtils.getMemberCount(plugin, allyId) + ""))
                    .map(l -> l.replace("%ally_kills%",  stats.getOrDefault("kills", 0) + ""))
                    .map(l -> l.replace("%ally_deaths%", stats.getOrDefault("deaths", 0) + ""))
                    .map(l -> l.replace("%ally_kdr", ClanUtils.getClanKDR(plugin, allyId) + ""))
                    .collect(Collectors.toList());
            for (String line : allieLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }

            meta.setLore(coloredLore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            if (config.contains("allies.left_click_commands")) {
                List<String> commands = config.getStringList("allies.left_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%ally_name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, allyId)))).collect(Collectors.toList());
                itemCommands.put(ClickType.LEFT, commands);
            }
            if (config.contains("allies.right_click_commands")) {
                List<String> commands = config.getStringList("allies.right_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%ally_name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, allyId)))).collect(Collectors.toList());
                itemCommands.put(ClickType.RIGHT, commands);
            }
            if (config.contains("allies.middle_click_commands")) {
                List<String> commands = config.getStringList("allies.middle_click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%ally_name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, allyId)))).collect(Collectors.toList());
                itemCommands.put(ClickType.MIDDLE, commands);
            }
            if (config.contains("allies.click_commands")) {
                List<String> commands = config.getStringList("allies.click_commands");
                commands = commands.stream().map(cmd -> cmd.replace("%ally_name%", Objects.requireNonNull(ClanUtils.getClanName(plugin, allyId)))).collect(Collectors.toList());
                itemCommands.put(ClickType.UNKNOWN, commands);
            }

            String key = InventorySection.ALLIES.name() + "_" + slot;
            sectionCommandsMap.put(key, itemCommands);
        }
    }

    public void refreshEnemiesItems(Inventory inv, InventoryPlayer inventoryPlayer, FileConfiguration config, Player player) {
        long startTime = System.nanoTime();

        // Cachear los valores que se obtienen repetidamente
        List<Integer> enemies = ClanUtils.getClanEnemiesIds(plugin, inventoryPlayer.getClanId());
        List<Integer> enemiesSlots = config.getIntegerList("enemies.slots");
        String enemiesNameTemplate = config.getString("enemies.name");
        List<String> enemiesLoreTemplate = config.getStringList("enemies.lore");
        Material material = Material.valueOf(config.getString("enemies.material"));

        long fetchTime = System.nanoTime();
        System.out.println("Time for fetching values: " + (fetchTime - startTime) / 1_000_000 + " ms");

        for (int i = 0; i < enemies.size(); i++) {
            int slot = enemiesSlots.get(i);
            int enemyId = enemies.get(i);

            // Crear el ItemStack y ItemMeta
            ItemStack item = new ItemStack(material);
            item.setAmount(1);
            ItemMeta meta = item.getItemMeta();

            // Reemplazar el nombre del item
            String enemyName = ClanUtils.getClanName(plugin, enemyId);
            String displayName = ChatUtils.oldTranslateColors(enemiesNameTemplate.replace("%enemy_name%", enemyName));
            meta.setDisplayName(displayName);

            // Aplicar el brillo si está configurado
            if (config.contains("enemies.glow")) {
                boolean glow = config.getBoolean("enemies.glow");
                if (glow) {
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            // Cachear las estadísticas del enemigo
            Map<String, Integer> stats = ClanUtils.getClanStats(plugin, enemyId);
            int enemyOnlineCount = ClanUtils.getOnlineClanMembersCount(plugin, enemyId);
            int enemyTotalCount = ClanUtils.getMemberCount(plugin, enemyId);
            int enemyKills = stats.getOrDefault("kills", 0);
            int enemyDeaths = stats.getOrDefault("deaths", 0);
            double enemyKDR = ClanUtils.getClanKDR(plugin, enemyId);

            long statsTime = System.nanoTime();
            System.out.println("Time for fetching stats: " + (statsTime - fetchTime) / 1_000_000 + " ms");

            // Reemplazar el lore del item
            List<String> coloredLore = enemiesLoreTemplate.stream()
                    .map(l -> l.replace("%enemy_online%", String.valueOf(enemyOnlineCount)))
                    .map(l -> l.replace("%enemy_total%", String.valueOf(enemyTotalCount)))
                    .map(l -> l.replace("%enemy_kills%", String.valueOf(enemyKills)))
                    .map(l -> l.replace("%enemy_deaths%", String.valueOf(enemyDeaths)))
                    .map(l -> l.replace("%enemy_kdr%", String.valueOf(enemyKDR)))
                    .collect(Collectors.toList());

            long loreTime = System.nanoTime();
            System.out.println("Time for processing lore: " + (loreTime - statsTime) / 1_000_000 + " ms");

            // Aplicar los colores y placeholders al lore
            List<String> finalColoredLore = new ArrayList<>();
            for (String line : coloredLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                finalColoredLore.add(ChatUtils.oldTranslateColors(line));
            }
            meta.setLore(finalColoredLore);
            item.setItemMeta(meta);

            // Agregar el item al inventario
            inv.setItem(slot, item);

            // Configurar los comandos del item
            Map<ClickType, List<String>> itemCommands = new HashMap<>();
            addCommandsToMap(config, "enemies.left_click_commands", ClickType.LEFT, enemyName, itemCommands);
            addCommandsToMap(config, "enemies.right_click_commands", ClickType.RIGHT, enemyName, itemCommands);
            addCommandsToMap(config, "enemies.middle_click_commands", ClickType.MIDDLE, enemyName, itemCommands);
            addCommandsToMap(config, "enemies.click_commands", ClickType.UNKNOWN, enemyName, itemCommands);

            String key = InventorySection.ENEMIES.name() + "_" + slot;
            sectionCommandsMap.put(key, itemCommands);
        }

        long endTime = System.nanoTime();
        System.out.println("Total time for refreshEnemiesItems: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private void addCommandsToMap(FileConfiguration config, String path, ClickType clickType, String enemyName, Map<ClickType, List<String>> itemCommands) {
        if (config.contains(path)) {
            List<String> commands = config.getStringList(path);
            commands = commands.stream().map(cmd -> cmd.replace("%enemy_name%", enemyName)).collect(Collectors.toList());
            itemCommands.put(clickType, commands);
        }
    }






    public void inventoryClick(InventoryPlayer inventoryPlayer, int slot, ClickType clickType) {
        Player player = inventoryPlayer.getPlayer();
        InventorySection section = inventoryPlayer.getSection();

        String key = section.name() + "_" + slot;
        Map<ClickType, List<String>> itemCommands = sectionCommandsMap.getOrDefault(key, new HashMap<>());

        // Crear una nueva lista para almacenar los comandos a ejecutar
        List<String> commands = new ArrayList<>();

        // Agregar los comandos correspondientes al tipo de click
        if (itemCommands.containsKey(clickType)) {
            commands.addAll(itemCommands.get(clickType));
        }

        // Agregar los comandos para cualquier tipo de click
        if (itemCommands.containsKey(ClickType.UNKNOWN)) {
            commands.addAll(itemCommands.get(ClickType.UNKNOWN));
        }

        // Ejecutar cada comando en la lista
        for (String command : commands) {
            player.performCommand(command);
            Bukkit.getLogger().info("Executing command: " + command);
        }

        if (section == InventorySection.MEMBERS) {
            FileConfiguration config = plugin.getMenuConfig("members.yml");
            int nextPageSlot = config.getInt("next_page.slot");
            int previousPageSlot = config.getInt("previous_page.slot");
            int backSlot = config.getInt("back.slot");

            if (slot == nextPageSlot) {
                if ((inventoryPlayer.getPage() + 1) * config.getIntegerList("members.slots").size() < ClanUtils.getClanOfflinePlayerMembers(plugin, inventoryPlayer.getClanId()).size()) {
                    openMembersMenu(inventoryPlayer, inventoryPlayer.getClanId(), inventoryPlayer.getPage() + 1);
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_next_pages")));
                }
            } else if (slot == previousPageSlot) {
                if (inventoryPlayer.getPage() > 0) {
                    openMembersMenu(inventoryPlayer, inventoryPlayer.getClanId(), inventoryPlayer.getPage() - 1);
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_previous_page")));
                }
            } else if (slot == backSlot) {
                if (ClanUtils.isPlayerInTheClan(plugin, inventoryPlayer.getClanId(), player.getUniqueId())) {
                    openMainMenu(inventoryPlayer, inventoryPlayer.getClanId());
                } else {

                    openInfoMenu(inventoryPlayer, inventoryPlayer.getClanId());
                }
            }
        }

        if (section == InventorySection.ALLIES) {
            FileConfiguration config = plugin.getMenuConfig("allies.yml");
            int backSlot = config.getInt("back.slot");

            if (slot == backSlot) {
                if (ClanUtils.isPlayerInTheClan(plugin, inventoryPlayer.getClanId(), player.getUniqueId())) {
                    openMainMenu(inventoryPlayer, inventoryPlayer.getClanId());
                } else {
                    openInfoMenu(inventoryPlayer, inventoryPlayer.getClanId());
                }
            }
        }

        if (section == InventorySection.ENEMIES) {
            FileConfiguration config = plugin.getMenuConfig("enemies.yml");
            int backSlot = config.getInt("back.slot");

            if (slot == backSlot) {
                if (ClanUtils.isPlayerInTheClan(plugin, inventoryPlayer.getClanId(), player.getUniqueId())) {
                    openMainMenu(inventoryPlayer, inventoryPlayer.getClanId());
                } else {
                    openInfoMenu(inventoryPlayer, inventoryPlayer.getClanId());
                }
            }
        }

        if (section == InventorySection.LEADERBOARD) {
            FileConfiguration config = plugin.getMenuConfig("leaderboard.yml");
            int nextPageSlot = config.getInt("next_page.slot");
            int previousPageSlot = config.getInt("previous_page.slot");
            int closeSlot = config.getInt("close.slot");

            if (slot == nextPageSlot) {
                if ((inventoryPlayer.getPage() + 1) * config.getIntegerList("clans.slots").size() < Objects.requireNonNull(ClanUtils.getAllClansIds(plugin)).size()) {
                    openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage() + 1, inventoryPlayer.getFilter());
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_next_pages")));
                }
            } else if (slot == previousPageSlot) {
                if (inventoryPlayer.getPage() > 0) {
                    openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage() - 1, inventoryPlayer.getFilter());
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_previous_page")));
                }
            } else if (slot == closeSlot) {
                if (ClanUtils.isPlayerInClan(plugin, player.getUniqueId().toString())) {
                    openMainMenu(inventoryPlayer, ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()));
                } else {
                    player.closeInventory();
                }
            }

            int killsFilterSlot = config.getInt("filter.kills.slot");
            int deathsFilterSlot = config.getInt("filter.deaths.slot");
            int kdrFilterSlot = config.getInt("filter.kdr.slot");
            int onlineFilterSlot = config.getInt("filter.online.slot");

            if (slot == killsFilterSlot) {
                openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage(), "kills");
            } else if (slot == deathsFilterSlot) {
                openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage(), "deaths");
            } else if (slot == kdrFilterSlot) {
                openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage(), "kdr");
            } else if (slot == onlineFilterSlot) {
                openLeaderboardMenu(inventoryPlayer, inventoryPlayer.getPage(), "online");
            }


        }
    }



    private void combineCommandMaps(Map<ClickType, List<String>> baseMap, Map<ClickType, List<String>> mapToAdd) {
        if (mapToAdd != null) {
            for (Map.Entry<ClickType, List<String>> entry : mapToAdd.entrySet()) {
                baseMap.merge(entry.getKey(), entry.getValue(), (oldList, newList) -> {
                    oldList.addAll(newList);
                    return oldList;
                });
            }
        }
    }


    public void addBackItem(Inventory inv, Player player, FileConfiguration config, InventorySection section) {
        String backName = null;
        Material backMaterial = null;
        List<String> backLore = null;
        int backSlot = -1;

        if (config.isSet("back")) {
            backName = config.getString("back.name");
            backMaterial = Material.valueOf(config.getString("back.material"));
            backLore = config.getStringList("back.lore");
            backSlot = config.getInt("back.slot");
        }

        List<String> coloredLore = new ArrayList<>();

        ItemStack back = new ItemStack(backMaterial);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatUtils.oldTranslateColors(backName));
        coloredLore.clear();
        for (String line : backLore) {
            line = PlaceholderAPI.setPlaceholders(player, line);
            coloredLore.add(ChatUtils.oldTranslateColors(line));
        }
        backMeta.setLore(coloredLore);
        back.setItemMeta(backMeta);
        inv.setItem(backSlot, back);


    }


    public void addNavigationItems(Inventory inv, Player player, int clanId, FileConfiguration config, InventorySection section) {

        String nextPageName = null;
        Material nextPageMaterial = null;
        List<String> nextPageLore = null;
        int nextPageSlot = -1;

        if (config.isSet("next_page")) {
            nextPageName = config.getString("next_page.name");
            nextPageMaterial = Material.valueOf(config.getString("next_page.material"));
            nextPageLore = config.getStringList("next_page.lore");
            nextPageSlot = config.getInt("next_page.slot");
        }


        String previousPageName = null;
        Material previousPageMaterial = null;
        List<String> previousPageLore = null;
        int previousPageSlot = -1;

        if (config.isSet("previous_page")) {
            previousPageName = config.getString("previous_page.name");
            previousPageMaterial = Material.valueOf(config.getString("previous_page.material"));
            previousPageLore = config.getStringList("previous_page.lore");
            previousPageSlot = config.getInt("previous_page.slot");
        }

        String closeName = null;
        Material closeMaterial = null;
        List<String> closeLore = null;
        int closeSlot = -1;

        if (config.isSet("close")) {
            closeName = config.getString("close.name");
            closeMaterial = Material.valueOf(config.getString("close.material"));
            closeLore = config.getStringList("close.lore");
            closeSlot = config.getInt("close.slot");
        }

        String backName = null;
        Material backMaterial = null;
        List<String> backLore = null;
        int backSlot = -1;

        if (config.isSet("back")) {
            backName = config.getString("back.name");
            backMaterial = Material.valueOf(config.getString("back.material"));
            backLore = config.getStringList("back.lore");
           backSlot = config.getInt("back.slot");
        }

        List<String> coloredLore = new ArrayList<>();

        if (nextPageName != null && nextPageMaterial != null && nextPageLore != null && nextPageSlot != -1) {
            ItemStack nextPage = new ItemStack(nextPageMaterial);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(ChatUtils.oldTranslateColors(nextPageName));
            for (String line : nextPageLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }
            nextPageMeta.setLore(coloredLore);
            nextPage.setItemMeta(nextPageMeta);
            inv.setItem(nextPageSlot, nextPage);
        }

        if (previousPageName != null && previousPageMaterial != null && previousPageLore != null && previousPageSlot != -1) {
            ItemStack previousPage = new ItemStack(previousPageMaterial);
            ItemMeta previousPageMeta = previousPage.getItemMeta();
            previousPageMeta.setDisplayName(ChatUtils.oldTranslateColors(previousPageName));
            coloredLore.clear();
            for (String line : previousPageLore) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                coloredLore.add(ChatUtils.oldTranslateColors(line));
            }
            previousPageMeta.setLore(coloredLore);
            previousPage.setItemMeta(previousPageMeta);
            inv.setItem(previousPageSlot, previousPage);
        }
        if (section == InventorySection.MEMBERS){
            if (backName != null && backMaterial != null && backLore != null && backSlot != -1) {
                ItemStack back = new ItemStack(backMaterial);
                ItemMeta backMeta = back.getItemMeta();
                backMeta.setDisplayName(ChatUtils.oldTranslateColors(backName));
                coloredLore.clear();
                for (String line : backLore) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                    coloredLore.add(ChatUtils.oldTranslateColors(line));
                }
                backMeta.setLore(coloredLore);
                back.setItemMeta(backMeta);
                inv.setItem(backSlot, back);

            }
        }
        if (section == InventorySection.LEADERBOARD){
            if (ClanUtils.isPlayerInClan(plugin, player.getUniqueId().toString())) {
                if (backName != null && backMaterial != null && backLore != null && backSlot != -1) {
                    ItemStack back = new ItemStack(backMaterial);
                    ItemMeta backMeta = back.getItemMeta();
                    backMeta.setDisplayName(ChatUtils.oldTranslateColors(backName));
                    coloredLore.clear();
                    for (String line : backLore) {
                        line = PlaceholderAPI.setPlaceholders(player, line);
                        coloredLore.add(ChatUtils.oldTranslateColors(line));
                    }
                    backMeta.setLore(coloredLore);
                    back.setItemMeta(backMeta);
                    inv.setItem(backSlot, back);

                }
            } else {
                if (closeName != null && closeMaterial != null && closeLore != null && closeSlot != -1) {
                    ItemStack close = new ItemStack(closeMaterial);
                    ItemMeta closeMeta = close.getItemMeta();
                    closeMeta.setDisplayName(ChatUtils.oldTranslateColors(closeName));
                    coloredLore.clear();
                    for (String line : closeLore) {
                        line = PlaceholderAPI.setPlaceholders(player, line);
                        coloredLore.add(ChatUtils.oldTranslateColors(line));
                    }
                    closeMeta.setLore(coloredLore);
                    close.setItemMeta(closeMeta);
                    inv.setItem(closeSlot, close);
                }

            }
        }
    }
    private List<Integer> filterAndSortClans(List<Integer> clanIds, String filter) {
        long startTime = System.currentTimeMillis();

        // Mapas para almacenar los datos necesarios dependiendo del filtro
        Map<Integer, Integer> killsMap = null;
        Map<Integer, Integer> deathsMap = null;
        Map<Integer, Integer> onlineMembersMap = null;
        Map<Integer, Double> kdrMap = null;

        // Cachear los datos necesarios para la ordenación basándose en el filtro
        long cacheStartTime = System.currentTimeMillis();

        switch (filter) {
            case "kills":
                killsMap = new HashMap<>();
                for (Integer clanId : clanIds) {
                    killsMap.put(clanId, ClanUtils.getClanKills(plugin, clanId));
                }
                break;
            case "deaths":
                deathsMap = new HashMap<>();
                for (Integer clanId : clanIds) {
                    deathsMap.put(clanId, ClanUtils.getClanDeaths(plugin, clanId));
                }
                break;
            case "online":
                onlineMembersMap = new HashMap<>();
                for (Integer clanId : clanIds) {
                    onlineMembersMap.put(clanId, ClanUtils.getOnlineClanMembersCount(plugin, clanId));
                }
                break;
            case "kdr":
                kdrMap = new HashMap<>();
                for (Integer clanId : clanIds) {
                    kdrMap.put(clanId, ClanUtils.getClanKDR(plugin, clanId));
                }
                break;
            default:
                throw new IllegalArgumentException("Filtro desconocido: " + filter);
        }

        long cacheEndTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo para cachear datos: " + (cacheEndTime - cacheStartTime) + "ms");

        // Ordenar usando los datos cacheados en paralelo
        long sortStartTime = System.currentTimeMillis();

        Map<Integer, Integer> finalKillsMap = killsMap;
        Map<Integer, Integer> finalDeathsMap = deathsMap;
        Map<Integer, Integer> finalOnlineMembersMap = onlineMembersMap;
        Map<Integer, Double> finalKdrMap = kdrMap;
        List<Integer> sortedClanIds = clanIds.parallelStream()
                .sorted((o1, o2) -> {
                    switch (filter) {
                        case "kills":
                            return Integer.compare(finalKillsMap.get(o2), finalKillsMap.get(o1));
                        case "deaths":
                            return Integer.compare(finalDeathsMap.get(o2), finalDeathsMap.get(o1));
                        case "online":
                            return Integer.compare(finalOnlineMembersMap.get(o2), finalOnlineMembersMap.get(o1));
                        case "kdr":
                            return Double.compare(finalKdrMap.get(o2), finalKdrMap.get(o1));
                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());

        long sortEndTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo para ordenar clanes: " + (sortEndTime - sortStartTime) + "ms");

        long endTime = System.currentTimeMillis();
        Bukkit.getLogger().info("Tiempo total para filtrar y ordenar clanes: " + (endTime - startTime) + "ms");

        return sortedClanIds;
    }




}
