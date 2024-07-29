package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanBankCommand {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanBankCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getClanManager().isInClan(player)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (plugin.getEconomy() == null) {
            player.sendMessage(ChatColor.RED + "Bank functionality is disabled because Vault or an economy plugin is not found.");
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("balance")) {
            balance(player);
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_bank")));
            return true;
        }

        String action = args[1];
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("invalid_amount")));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("invalid_amount")));
            return true;
        }

        if (action.equalsIgnoreCase("deposit")) {
            if (!ClanUtils.hasPermission(plugin, player.getUniqueId().toString(), "DEPOSIT_BANK")) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
                return true;
            }
            deposit(player, amount);
        } else if (action.equalsIgnoreCase("withdraw")) {
            if (!ClanUtils.hasPermission(plugin, player.getUniqueId().toString(), "WITHDRAW_BANK")) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
                return true;
            }
            withdraw(player, amount);
        } else {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_bank")));
        }
        return true;
    }

    private void deposit(Player player, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        if (plugin.getEconomy().withdrawPlayer(offlinePlayer, amount).transactionSuccess()) {
            plugin.getClanManager().addClanBalance(player, amount);
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("successfully_deposited").replace("%amount%", String.valueOf(amount))));
            int clanId = ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString());
            notificationManager.notifyBankDeposited(clanId, player.getName(), amount);

        } else {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("insufficient_player_funds")));
        }
    }

    private void withdraw(Player player, double amount) {
        if (plugin.getClanManager().getClanBalance(player) >= amount) {
            plugin.getClanManager().subtractClanBalance(player, amount);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            plugin.getEconomy().depositPlayer(offlinePlayer, amount);

            String successMessageRaw = plugin.getMessage("successfully_withdrawn")
                    .replace("%amount%", String.valueOf(amount));
            Component successMessage = MiniMessage.miniMessage().deserialize(successMessageRaw);
            player.sendMessage(successMessage);

            int clanId = ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString());
            notificationManager.notifyBankWithdrawn(clanId, player.getName(), amount);

        } else {
            Component insufficientFundsMessage = MiniMessage.miniMessage().deserialize(plugin.getMessage("insufficient_clan_funds"));
            player.sendMessage(insufficientFundsMessage);
        }
    }

    private void balance(Player player) {
        double clanBalance = plugin.getClanManager().getClanBalance(player);
        String balanceMessageRaw = plugin.getMessage("clan_balance")
                .replace("%amount%", String.valueOf(clanBalance));
        Component balanceMessage = MiniMessage.miniMessage().deserialize(balanceMessageRaw);
        player.sendMessage(balanceMessage);
    }

}
