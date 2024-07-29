package net.diground.exylia.cache;

import me.clip.placeholderapi.PlaceholderAPI;
import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ClanItemCache {
    private final ExyliaClans plugin;
    private Map<Integer, ItemStack> cachedClanItems;
    private Map<String, List<Integer>> cachedFilteredClans;
    private long lastUpdateTime;

    public ClanItemCache(ExyliaClans plugin) {
        this.plugin = plugin;
        this.cachedClanItems = new HashMap<>();
        this.cachedFilteredClans = new HashMap<>();
        this.lastUpdateTime = 0;
        startCacheUpdateTask();
    }

    private void startCacheUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateCache();
            }
        }.runTaskTimer(plugin, 0, 20L * 60 * plugin.getConfig().getInt("leaderboard.refresh-time"));
    }

    private void updateCache() {
        long start = System.currentTimeMillis();
        List<Integer> clanIds = ClanUtils.getAllClansIds(plugin);
        cachedClanItems.clear();
        cachedFilteredClans.clear();

        // Dividir la actualización en partes
        new BukkitRunnable() {
            int index = 0;
            final int batchSize = 10; // Número de clanes a procesar por tick

            @Override
            public void run() {
                int endIndex = Math.min(index + batchSize, clanIds.size());
                for (int i = index; i < endIndex; i++) {
                    Integer clanId = clanIds.get(i);
                    ItemStack clanItem = createClanItem(clanId);
                    cachedClanItems.put(clanId, clanItem);
                }
                index += batchSize;
                if (index >= clanIds.size()) {
                    this.cancel();
                    cacheFilteredClans(clanIds);
                    lastUpdateTime = System.currentTimeMillis();
                    long end = System.currentTimeMillis();
                    Bukkit.getLogger().info("Caché de ítems de clanes y filtros actualizado en " + (end - start) + "ms");
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Ejecutar cada tick
    }

    private void cacheFilteredClans(List<Integer> clanIds) {
        cachedFilteredClans.put("kills", filterAndSortClans(clanIds, "kills"));
        cachedFilteredClans.put("deaths", filterAndSortClans(clanIds, "deaths"));
        cachedFilteredClans.put("online", filterAndSortClans(clanIds, "online"));
        cachedFilteredClans.put("kdr", filterAndSortClans(clanIds, "kdr"));
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

    private ItemStack createClanItem(int clanId) {
        FileConfiguration config = plugin.getMenuConfig("leaderboard.yml");
        String clansName = config.getString("clans.name");
        List<String> clansLore = config.getStringList("clans.lore");

        OfflinePlayer leader = ClanUtils.getClanLeader(plugin, clanId);
        String leaderName = leader == null ? "" : leader.getName();

        String clanName = ChatUtils.oldTranslateColors(clansName
                .replace("%name%", ClanUtils.getClanNameById(plugin, clanId))
                .replace("%prefix%", Objects.requireNonNull(ClanUtils.getClanPrefix(plugin, clanId))));
        String finalLeaderName = leaderName;
        String clanPrefix = ClanUtils.getClanPrefix(plugin, clanId);
        int onlineMembersCount = ClanUtils.getOnlineClanMembersCount(plugin, clanId);
        int memberCount = ClanUtils.getMemberCount(plugin, clanId);
        Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clanId);

        int clanKills = stats.getOrDefault("kills", 0);
        int clanDeaths = stats.getOrDefault("deaths", 0);
        double clanKDR = ClanUtils.getClanKDR(plugin, clanId);

        List<String> memberLore = clansLore.stream()
                .map(l -> l.replace("%name%", ClanUtils.getClanNameById(plugin, clanId)))
                .map(l -> l.replace("%online%", String.valueOf(onlineMembersCount)))
                .map(l -> l.replace("%total%", String.valueOf(memberCount)))
                .map(l -> l.replace("%kills%", String.valueOf(clanKills)))
                .map(l -> l.replace("%deaths%", String.valueOf(clanDeaths)))
                .map(l -> l.replace("%kdr%", String.valueOf(clanKDR)))
                .map(l -> l.replace("%leader%", finalLeaderName))
                .map(l -> l.replace("%prefix%", Objects.requireNonNull(clanPrefix)))
                .collect(Collectors.toList());

        ItemStack clanBanner = ClanUtils.getClanBanner(plugin, clanId);
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

        return clanBanner;
    }

    public ItemStack getCachedClanItem(int clanId) {
        if (System.currentTimeMillis() - lastUpdateTime > 20 * 60 * 5 * 1000) {
            updateCache();
        }
        return cachedClanItems.get(clanId);
    }

    public List<Integer> getCachedFilteredClans(String filter) {
        if (System.currentTimeMillis() - lastUpdateTime > 20 * 60 * 5 * 1000) {
            updateCache();
        }
        return cachedFilteredClans.get(filter);
    }
}