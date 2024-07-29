package net.diground.exylia.commands.main;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.commands.admin.AdminReloadCommand;
import net.diground.exylia.managers.MigrationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClanAdminCommand implements CommandExecutor, TabCompleter {
    private final ExyliaClans plugin;
    private final MigrationManager migrationManager;
    private final AdminReloadCommand adminReloadCommand;

    public ClanAdminCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        migrationManager = new MigrationManager(plugin);
        adminReloadCommand = new AdminReloadCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage("Usage: /clanadmin <args>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                adminReloadCommand.execute(sender, args);
                return true;
            case "migrate":
                String pluginName = args[1];
                switch (pluginName) {
                    case "advancedclans":
                        migrationManager.migrateData(plugin);
                        player.sendMessage("Migration from AdvancedClans started.");
                        break;
                    //case "uclans":
                      //  UClansMigration.migrate(plugin);
                       // player.sendMessage("Migration from UClans started.");
                       // break;
                    default:
                        player.sendMessage("Unknown plugin.");
                        break;
                }
                return true;
            case "join":
                return true;
            case "kick":
                return true;
            case "promote":
                return true;
            case "demote":
                return true;
            case "ally":
                return true;
            case "enemy":
                return true;
            case "delete":
                return true;
            case "chest":
                return true;
            case "base":
                return true;
            case "setbase":
                return true;
            case "delbase":
                return true;
            case "setleader":
                return true;
            case "pvp":
                return true;
            case "globalpvp":
                return true;
            case "rename":
                return true;
            default:
                sender.sendMessage("Usage: /clanadmin <args>");
                return true;

        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("migrate");
            completions.add("join");
            completions.add("kick");
            completions.add("promote");
            completions.add("demote");
            completions.add("ally");
            completions.add("enemy");
            completions.add("delete");
            completions.add("chest");
            completions.add("base");
            completions.add("setbase");
            completions.add("delbase");
            completions.add("setleader");
            completions.add("pvp");
            completions.add("globalpvp");
            completions.add("rename");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("migrate")) {
                completions.add("advancedclans");
                //completions.add("uclans");
            }
        }
        return completions;
    }
}
