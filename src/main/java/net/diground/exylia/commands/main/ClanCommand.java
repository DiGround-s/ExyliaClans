package net.diground.exylia.commands.main;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.commands.allies.ClanAllyAcceptCommand;
import net.diground.exylia.commands.allies.ClanAllyBreakCommand;
import net.diground.exylia.commands.allies.ClanAllyDenyCommand;
import net.diground.exylia.commands.allies.ClanAllyRequestCommand;
import net.diground.exylia.commands.base.ClanBaseCommand;
import net.diground.exylia.commands.base.ClanDelbaseCommand;
import net.diground.exylia.commands.base.ClanSetbaseCommand;
import net.diground.exylia.commands.extras.ClanAlliesCommand;
import net.diground.exylia.commands.extras.ClanEnemiesCommand;
import net.diground.exylia.commands.extras.ClanTopCommand;
import net.diground.exylia.commands.members.*;
import net.diground.exylia.commands.common.*;
import net.diground.exylia.commands.vip.ClanPrefixCommand;
import net.diground.exylia.commands.vip.ClanRenameCommand;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final ExyliaClans plugin;
    private final ClanCreateCommand createCommand;
    private final ClanDisbandCommand disbandCommand;
    private final ClanInfoCommand infoCommand;
    private final ClanInviteCommand inviteCommand;
    private final ClanJoinCommand joinCommand;
    private final ClanLeaveCommand leaveCommand;
    private final ClanKickCommand kickCommand;
    private final ClanSetbaseCommand setBaseCommand;
    private final ClanBaseCommand baseCommand;
    private final ClanDelbaseCommand delBaseCommand;
    private final ClanRenameCommand renameCommand;
    private final ClanListCommand listCommand;
    private final ClanPvpCommand pvpCommand;
    private final ClanChestCommand chestCommand;
    private final ClanChatCommand chatCommand;
    private final ClanAllyRequestCommand allyRequestCommand;
    private final ClanAllyAcceptCommand allyAcceptCommand;
    private final ClanAllyDenyCommand allyDenyCommand;
    private final ClanAllyBreakCommand allyBreakCommand;
    private final ClanPromoteCommand promoteCommand;
    private final ClanDemoteCommand demoteCommand;
    private final ClanSetLeaderCommand setLeaderCommand;
    private final ClanBankCommand clanBankCommand;
    private final ClanEnemyCommand clanEnemyCommand;
    private final ClanRegroupCommand regroupCommand;
    private final ClanMainCommand mainCommand;
    private final ClanMembersCommand membersCommand;
    private final ClanAlliesCommand alliesCommand;
    private final ClanEnemiesCommand enemiesCommand;
    private final ClanTopCommand topCommand;
    private final ClanBannerCommand clanBannerCommand;
    private final ClanPrefixCommand prefixCommand;

    public ClanCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.createCommand = new ClanCreateCommand(plugin);
        this.disbandCommand = new ClanDisbandCommand(plugin);
        this.infoCommand = new ClanInfoCommand(plugin);
        this.inviteCommand = new ClanInviteCommand(plugin);
        this.joinCommand = new ClanJoinCommand(plugin);
        this.leaveCommand = new ClanLeaveCommand(plugin);
        this.kickCommand = new ClanKickCommand(plugin);
        this.setBaseCommand = new ClanSetbaseCommand(plugin);
        this.baseCommand = new ClanBaseCommand(plugin);
        this.delBaseCommand = new ClanDelbaseCommand(plugin);
        this.renameCommand = new ClanRenameCommand(plugin);
        this.listCommand = new ClanListCommand(plugin);
        this.pvpCommand = new ClanPvpCommand(plugin);
        this.chestCommand = new ClanChestCommand(plugin);
        this.chatCommand = new ClanChatCommand(plugin);
        this.allyRequestCommand = new ClanAllyRequestCommand(plugin);
        this.allyAcceptCommand = new ClanAllyAcceptCommand(plugin);
        this.allyDenyCommand = new ClanAllyDenyCommand(plugin);
        this.allyBreakCommand = new ClanAllyBreakCommand(plugin);
        this.promoteCommand = new ClanPromoteCommand(plugin);
        this.demoteCommand = new ClanDemoteCommand(plugin);
        this.setLeaderCommand = new ClanSetLeaderCommand(plugin);
        this.clanBankCommand = new ClanBankCommand(plugin);
        this.clanEnemyCommand = new ClanEnemyCommand(plugin);
        this.regroupCommand = new ClanRegroupCommand(plugin);
        this.mainCommand = new ClanMainCommand(plugin);
        this.membersCommand = new ClanMembersCommand(plugin);
        this.alliesCommand = new ClanAlliesCommand(plugin);
        this.enemiesCommand = new ClanEnemiesCommand(plugin);
        this.topCommand = new ClanTopCommand(plugin);
        this.clanBannerCommand = new ClanBannerCommand(plugin);
        this.prefixCommand = new ClanPrefixCommand(plugin);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            mainCommand.execute(sender, args);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "prefix":
                return prefixCommand.execute(sender, args);
            case "banner":
                return clanBannerCommand.execute(sender, args);
            case "leaderboard", "top":
                return topCommand.execute(sender, args);
            case "allies":
                return alliesCommand.execute(sender, args);
            case "enemies":
                return enemiesCommand.execute(sender, args);
            case "members":
                return membersCommand.execute(sender, args);
            case "create":
                return createCommand.execute(sender, args);
            case "disband":
                return disbandCommand.execute(sender, args);
            case "info":
                return infoCommand.execute(sender, args);
            case "invite":
                return inviteCommand.execute(sender, args);
            case "join":
                return joinCommand.execute(sender, args);
            case "leave":
                return leaveCommand.execute(sender, args);
            case "kick":
                return kickCommand.execute(sender, args);
            case "setbase":
                return setBaseCommand.execute(sender, args);
            case "base":
                return baseCommand.execute(sender, args);
            case "delbase":
                return delBaseCommand.execute(sender, args);
            case "rename":
                return renameCommand.execute(sender, args);
            case "list":
                return listCommand.execute(sender, args);
            case "pvp":
                return pvpCommand.execute(sender, args);
            case "chest":
                return chestCommand.execute(sender, args);
            case "chat":
                return chatCommand.execute(sender, args);
            case "allychat":
                return chatCommand.execute(sender, args);
            case "promote":
                return promoteCommand.execute(sender, args);
            case "demote":
                return demoteCommand.execute(sender, args);
            case "setleader":
                return setLeaderCommand.execute(sender, args);
            case "bank":
                return clanBankCommand.execute(sender, args);
            case "enemy":
                return clanEnemyCommand.execute(sender, args);
            case "regroup":
                return regroupCommand.onCommand(sender, command, label, args);
            case "ally":
                if (args.length < 2) {
                    Player player = (Player) sender;
                    plugin.getMenuManager().openAlliesMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()));
                    return true;
                }

                String allySubCommand = args[1].toLowerCase();
                switch (allySubCommand) {
                    case "request":
                        return allyRequestCommand.execute(sender, args);
                    case "accept":
                        return allyAcceptCommand.execute(sender, args);
                    case "deny":
                        return allyDenyCommand.execute(sender, args);
                    case "break":
                        return allyBreakCommand.execute(sender, args);
                    default:
                        sender.sendMessage("Unknown command. Usage: /clan ally <request|accept|deny|break> <clan>");
                        return true;
                }
            default:
                sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("unknown_command")));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("members", "create", "disband", "info", "invite", "join", "leave", "kick", "setbase", "base", "delbase", "rename", "list", "pvp", "chest", "chat", "ally", "promote", "demote", "setleader", "bank", "enemy", "allychat", "regroup", "leaderboard", "top", "banner");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            completions.add("<NOMBRE>");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            completions.addAll(ClanUtils.getClanNames(plugin));
        } else if (args[0].equalsIgnoreCase("invite")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        } else if (args[0].equalsIgnoreCase("join")) {
            try {
                Connection connection = plugin.getConnection();
                String playerUUID = ((Player) sender).getUniqueId().toString();
                String query = "SELECT clans.name FROM invites JOIN clans ON invites.clan_id = clans.id WHERE invites.player_uuid = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, playerUUID);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    completions.add(rs.getString("name"));
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not retrieve clan invites: " + e.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            if (clanId != -1) {
                completions.addAll(ClanUtils.getClanMembers(plugin, clanId));
            }
        }else if (args[0].equalsIgnoreCase("promote")) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            if (clanId != -1) {
                completions.addAll(ClanUtils.getClanMembers(plugin, clanId));
            }
        }else if (args[0].equalsIgnoreCase("demote")) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            if (clanId != -1) {
                completions.addAll(ClanUtils.getClanMembers(plugin, clanId));
            }
        }else if (args[0].equalsIgnoreCase("setleader")) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            if (clanId != -1) {
                completions.addAll(ClanUtils.getClanMembers(plugin, clanId));
            }
        } else if (args[0].equalsIgnoreCase("rename")) {
            completions.add("<NUEVO_NOMBRE>");
        } else if (args[0].equalsIgnoreCase("list")) {
            completions.add("<PÁGINA>");
        } else if (args[0].equalsIgnoreCase("enemy")) {
            completions.addAll(ClanUtils.getClanNames(plugin));
        } else if (args[0].equalsIgnoreCase("ally")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList("request", "accept", "deny", "break"));
            } else if (args.length == 3) {
                completions.addAll(ClanUtils.getClanNames(plugin));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("bank")) {
            if ("deposit".startsWith(args[1].toLowerCase())) {
                completions.add("deposit");
            }
            if ("withdraw".startsWith(args[1].toLowerCase())) {
                completions.add("withdraw");
            }
            if ("balance".startsWith(args[1].toLowerCase())) {
                completions.add("balance");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("regroup")) {
            completions.addAll(Arrays.asList("send", "accept", "cancel"));
        }

        return completions;
    }

}