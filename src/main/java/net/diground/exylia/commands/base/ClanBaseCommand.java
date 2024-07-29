package net.diground.exylia.commands.base;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.listeners.TeleportListener;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanBaseCommand {
    private final ExyliaClans plugin;
    private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final NotificationManager notificationManager;


    public ClanBaseCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);

    }


    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_BASE")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT base_world, base_x, base_y, base_z, base_yaw, base_pitch FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String worldName = rs.getString("base_world");
                if (worldName == null) {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_not_set")));
                    return true;
                }

                double x = rs.getDouble("base_x");
                double y = rs.getDouble("base_y");
                double z = rs.getDouble("base_z");
                float yaw = rs.getFloat("base_yaw");
                float pitch = rs.getFloat("base_pitch");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("world_not_found")));
                    return true;
                }

                Location baseLocation = new Location(world, x, y, z, yaw, pitch);
                startTeleport(player, baseLocation, clanId);
            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_not_set")));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not teleport to clan base: " + e.getMessage());
        }

        return true;
    }

    private void startTeleport(Player player, Location location, int clanId) {
        UUID playerUUID = player.getUniqueId();

        if (teleportTasks.containsKey(playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("teleport_in_progress")));
            return;
        }

        Location startLocation = player.getLocation();
        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("teleport_start")));

        TeleportListener listener = new TeleportListener(plugin, player, startLocation, this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                teleportTasks.remove(playerUUID);
                player.teleport(location);
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("teleport_success")));
                notificationManager.notifyTeleportedToBase(clanId, player.getName());
                listener.unregister();
            }
        };

        teleportTasks.put(playerUUID, task);
        task.runTaskLater(plugin, 20L * plugin.getConfig().getInt("base.teleport_delay"));
    }

    public void cancelTeleport(UUID playerUUID) {
        BukkitRunnable task = teleportTasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("teleport_cancelled")));
            }
        }
    }
}