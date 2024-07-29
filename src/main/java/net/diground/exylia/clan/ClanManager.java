package net.diground.exylia.clan;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanManager {

    private final ExyliaClans plugin;

    public ClanManager(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    // Métodos para manejar la lógica de clanes
    public Map<Integer, ClanData> loadClanData(List<Integer> clanIds) {
        Map<Integer, ClanData> clanDataMap = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = clanIds.stream()
                .map(clanId -> CompletableFuture.runAsync(() -> {
                    Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clanId);
                    ClanData data = new ClanData();
                    data.kills = stats.getOrDefault("kills", 0);
                    data.deaths = stats.getOrDefault("deaths", 0);
                    data.onlineMembers = ClanUtils.getOnlineClanMembersCount(plugin, clanId);
                    data.kdr = ClanUtils.getClanKDR(plugin, clanId);
                    data.banner = ClanUtils.getClanBanner(plugin, clanId);
                    data.prefix = ClanUtils.getClanPrefix(plugin, clanId);
                    data.name = ClanUtils.getClanNameById(plugin, clanId);
                    clanDataMap.put(clanId, data);
                }))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return clanDataMap;
    }

    public List<Integer> filterAndSortClans(List<Integer> clanIds, String filter, Map<Integer, ClanData> clanDataMap) {
        long startTime = System.currentTimeMillis();
        List<Integer> sortedClanIds = clanIds.stream()
                .sorted((o1, o2) -> {
                    ClanData data1 = clanDataMap.get(o1);
                    ClanData data2 = clanDataMap.get(o2);
                    switch (filter) {
                        case "kills":
                            return Integer.compare(data2.kills, data1.kills);
                        case "deaths":
                            return Integer.compare(data2.deaths, data1.deaths);
                        case "online":
                            return Integer.compare(data2.onlineMembers, data1.onlineMembers);
                        case "kdr":
                            return Double.compare(data2.kdr, data1.kdr);
                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());
        long endTime = System.currentTimeMillis();
        System.out.println("Tiempo para filtrar y ordenar clanes: " + (endTime - startTime) + "ms");
        return sortedClanIds;
    }
}