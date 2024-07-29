package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClanRegroupCommand implements CommandExecutor {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;
    private final Map<Integer, RegroupSession> activeRegroupsByClan = new HashMap<>();

    public ClanRegroupCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID.toString())) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID.toString());

        if (args.length <= 1) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_usage")));
            return true;
        }

        String subCommand = args[1].toLowerCase();
        return switch (subCommand) {
            case "send" -> handleSendCommand(player, clanId);
            case "accept" -> handleAcceptCommand(player, clanId);
            case "cancel" -> handleCancelCommand(player, clanId);
            default -> {
                sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_usage")));
                yield true;
            }
        };
    }

    private boolean handleSendCommand(Player player, int clanId) {
        if (!ClanUtils.hasPermission(plugin, player.getUniqueId().toString(), "SEND_REGROUP")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (activeRegroupsByClan.containsKey(clanId)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_already_active")));
            return true;
        }

        Location startLocation = player.getLocation();
        RegroupSession session = new RegroupSession(clanId, startLocation, player);
        activeRegroupsByClan.put(clanId, session);
        session.start();

        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_sent")));
        notificationManager.notifyRegroupCreated(clanId, player.getName());
        return true;
    }

    private boolean handleAcceptCommand(Player player, int clanId) {
        UUID playerUUID = player.getUniqueId();

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID.toString())) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        int playerClanId = ClanUtils.getPlayerClanId(plugin, playerUUID.toString());
        if (playerClanId != clanId) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_same_clan")));
            return true;
        }

        RegroupSession session = activeRegroupsByClan.get(clanId);
        if (session == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_active_regroup")));
            return true;
        }

        session.addParticipant(player);
        return true;
    }


    private boolean handleCancelCommand(Player player, int clanId) {
        RegroupSession session = activeRegroupsByClan.get(clanId);
        if (session == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_active_regroup_to_cancel")));
            return true;
        }

        // Cancelar la sesión inmediatamente
        session.cancel();
        activeRegroupsByClan.remove(clanId); // Asegúrate de eliminar la sesión del mapa de sesiones activas

        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_cancelled")));
        notificationManager.notifyRegroupCancelled(clanId, player.getName());
        return true;
    }


    private class RegroupSession implements Listener {
        private final int clanId;
        private final Location location;
        private final Player initiator;
        private final Set<Player> participants = new HashSet<>();
        private final Set<Player> playersWithBossBar = new HashSet<>();
        private final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();
        private final Map<UUID, Location> lastKnownLocations = new HashMap<>();
        private final BossBar bossBar;
        private boolean active = true;

        public RegroupSession(int clanId, Location location, Player initiator) {
            this.clanId = clanId;
            this.location = location;
            this.initiator = initiator;

            // Create and configure the BossBar
            this.bossBar = Bukkit.createBossBar(
                    "",
                    BarColor.valueOf(plugin.getConfig().getString("regroup.bossbar.color")),
                    BarStyle.valueOf(plugin.getConfig().getString("regroup.bossbar.style"))
            );

            // Register this session as a listener
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        public void start() {
            // Fetch all players in the clan
            List<Player> clanMembers = ClanUtils.getClanMembersPlayers(plugin, clanId);

            // Add the BossBar to all clan members
            for (Player player : clanMembers) {
                if (player.isOnline()) {
                    bossBar.addPlayer(player);
                    playersWithBossBar.add(player);
                }
            }

            // Start a 60-second countdown with BossBar update
            new BukkitRunnable() {
                private int secondsLeft = 60;

                @Override
                public void run() {
                    if (!active) {
                        cancel();
                        return;
                    }

                    if (secondsLeft <= 0) {
                        cancel();
                        activeRegroupsByClan.remove(clanId); // Elimina la sesión del mapa
                        for (Player player : playersWithBossBar) {
                            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_expired")));
                            bossBar.removePlayer(player);
                        }
                        return;
                    }

                    // Update BossBar progress
                    String title = ChatUtils.oldTranslateColors(plugin.getMessage("regroup_time_remaining"));
                    title = title.replace("%time%", String.valueOf(secondsLeft));
                    double progress = (double) secondsLeft / 60.0;
                    bossBar.setTitle(title);
                    bossBar.setProgress(progress);

                    secondsLeft--;
                }
            }.runTaskTimer(plugin, 0, 20); // Run every second (20 ticks)
        }


        public void addParticipant(Player player) {
            UUID playerUUID = player.getUniqueId();

            // Verifica si el jugador ya tiene una tarea de teleportación pendiente
            if (pendingTeleports.containsKey(playerUUID)) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("teleport_in_progress")));
                return;
            }

            if (!active) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_expired")));
                notificationManager.notifyRegroupExpired(clanId);
                return;
            }

            Location startLocation = player.getLocation();

            // Cancela cualquier tarea de teleportación anterior si existe
            if (pendingTeleports.containsKey(playerUUID)) {
                pendingTeleports.get(playerUUID).cancel();
                pendingTeleports.remove(playerUUID);
            }

            BukkitRunnable teleportTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!active) {
                        cancel();
                        return;
                    }

                    Player currentPlayer = Bukkit.getPlayer(playerUUID);
                    if (currentPlayer != null && currentPlayer.getLocation().distanceSquared(startLocation) < 1.0) {
                        currentPlayer.teleport(location);
                        currentPlayer.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_success")));
                        notificationManager.notifyRegroupAccepted(clanId, currentPlayer.getName());
                    }

                    pendingTeleports.remove(playerUUID);
                }
            };

            teleportTask.runTaskLater(plugin, 20L * plugin.getConfig().getInt("regroup.teleport_delay")); // Retraso de 5 segundos (100 ticks)
            pendingTeleports.put(playerUUID, teleportTask);

            // Registra la última ubicación conocida del jugador
            lastKnownLocations.put(playerUUID, startLocation);

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_accepted")));
        }


        public void cancel() {
            active = false;
            for (BukkitRunnable task : pendingTeleports.values()) {
                task.cancel();
            }
            pendingTeleports.clear();
            for (Player player : playersWithBossBar) {
                bossBar.removePlayer(player);
            }
            playersWithBossBar.clear();
            HandlerList.unregisterAll(this); // Unregister this listener
        }



        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (!active) return;
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            Location newLocation = event.getTo();

            // Verifica si las ubicaciones están en el mismo mundo antes de calcular la distancia
            if (lastKnownLocations.containsKey(playerUUID) && lastKnownLocations.get(playerUUID).getWorld().equals(newLocation.getWorld()) &&
                    lastKnownLocations.get(playerUUID).distanceSquared(newLocation) > 1.0) {
                // Cancelar solo la tarea de teleportación para este jugador
                if (pendingTeleports.containsKey(playerUUID)) {
                    pendingTeleports.get(playerUUID).cancel();
                    pendingTeleports.remove(playerUUID);
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("regroup_cancelled_due_to_movement")));
                }
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (!active) return;
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            // Cancela cualquier tarea de teleportación pendiente para el jugador que se desconectó
            if (pendingTeleports.containsKey(playerUUID)) {
                pendingTeleports.get(playerUUID).cancel();
                pendingTeleports.remove(playerUUID);
            }

            lastKnownLocations.remove(playerUUID);
        }
    }
}
