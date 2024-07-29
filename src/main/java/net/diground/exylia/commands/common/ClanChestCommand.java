package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.ClanChestManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ClanChestCommand implements Listener {
    private final ExyliaClans plugin;
    private final ClanChestManager chestManager;

    public ClanChestCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.chestManager = new ClanChestManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin); // Asegúrate de registrar el listener aquí
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            player.sendMessage(plugin.getMessage("not_in_clan"));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_CHEST")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
        chestManager.openClanChest(player, clanId);

        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Clan Chest")) {
            Player player = (Player) event.getPlayer();
            chestManager.closeClanChest(player);
        }
    }
}

