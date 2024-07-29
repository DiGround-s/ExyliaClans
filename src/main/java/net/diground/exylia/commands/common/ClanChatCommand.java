package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ClanChatCommand {

    private final ExyliaClans plugin;

    public ClanChatCommand(ExyliaClans plugin) {
        this.plugin = plugin;
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

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_CHAT")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("chat_usage")));
            return true;
        }

        // Process commands
        String command = args[0].toLowerCase();
        if (command.equals("allychat")) {
            if (args.length < 2) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("allychat_usage")));
                return true;
            }
            sendManualAllyMessage(ClanUtils.getPlayerClan(plugin, playerUUID), player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            return true;
        }

        if (args.length == 1) {
            // Toggle chat mode
            String chatMode = plugin.getClanChatManager().toggleChatMode(player.getUniqueId());
            switch (chatMode) {
                case "CLAN":
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("chat_mode_clan")));
                    break;
                case "ALLY":
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("chat_mode_ally")));
                    break;
                case "GLOBAL":
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("chat_mode_global")));
                    break;
            }
            return true;
        }

        // Always send to clan chat
        if (args.length >= 2) {
            sendManualClanMessage(ClanUtils.getPlayerClan(plugin, playerUUID), player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            return true;
        }

        return false;
    }

    private void sendManualClanMessage(String clanName, Player sender, String message) {
        String rawMessage = plugin.getMessage("chat_format.clan")
                .replace("%player%", sender.getName())
                .replace("%message%", message)
                .replace("%clan%", clanName);

        Component formattedMessage = MiniMessage.miniMessage().deserialize(rawMessage);

        for (Player player : sender.getWorld().getPlayers()) {
            if (plugin.getClanManager().isPlayerInClan(player.getUniqueId(), clanName)) {
                player.sendMessage(formattedMessage);
            }
        }
    }



    private void sendManualAllyMessage(String clanName, Player sender, String message) {
        String rawMessage = plugin.getMessage("chat_format.ally")
                .replace("%player%", sender.getName())
                .replace("%message%", message)
                .replace("%clan%", clanName);

        Component formattedMessage = MiniMessage.miniMessage().deserialize(rawMessage);

        for (Player player : sender.getWorld().getPlayers()) {
            String playerClan = plugin.getClanManager().getClanNameByPlayer(player.getUniqueId());

            if (playerClan == null) {
                plugin.getLogger().info("Skipping player " + player.getName() + " as they are not in any clan.");
                continue; // Skip players who are not in any clan
            }

            int clanPlayerId = ClanUtils.getClanIdByName(plugin, playerClan);
            int targetClanId = ClanUtils.getClanIdByName(plugin, clanName);
            if (playerClan.equals(clanName) || ClanUtils.areClansAllied(plugin, clanPlayerId, targetClanId)) {
                player.sendMessage(formattedMessage);
            } else {
                plugin.getLogger().info("Player " + player.getName() + " is not in the clan or allied clan.");
            }
        }
    }



}
