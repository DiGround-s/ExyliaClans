package net.diground.exylia.commands.extras;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.ClanChatManager;
import net.diground.exylia.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamLocalizator implements CommandExecutor {

    private final ExyliaClans plugin;

    public TeamLocalizator(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        // Procesar el comando sin argumentos
        Player p = (Player) sender;
        Location loc = p.getLocation();
        ClanChatManager clanChatManager = plugin.getClanChatManager();

        if (plugin.getConfig().getString("team_localizator.to_members").equals("true")) {
            // Obtener el formato del mensaje desde la configuración
            String format = plugin.getConfig().getString("team_localizator.format_members");

            // Reemplazar los placeholders
            String message = format
                    .replace("%player%", p.getName())
                    .replace("%x%", String.valueOf(Math.round(loc.getX())))
                    .replace("%y%", String.valueOf(Math.round(loc.getY())))
                    .replace("%z%", String.valueOf(Math.round(loc.getZ())));
            Component formattedMessage = MiniMessage.miniMessage().deserialize(message);
            clanChatManager.sendClanMessage(p, formattedMessage);
        }

        if (plugin.getConfig().getString("team_localizator.to_allies").equals("true")) {
            // Obtener el formato del mensaje desde la configuración
            String format = plugin.getConfig().getString("team_localizator.format_allies");

            // Reemplazar los placeholders
            String message = format
                    .replace("%player%", p.getName())
                    .replace("%x%", String.valueOf(Math.round(loc.getX())))
                    .replace("%y%", String.valueOf(Math.round(loc.getY())))
                    .replace("%z%", String.valueOf(Math.round(loc.getZ())));
            Component formattedMessage = MiniMessage.miniMessage().deserialize(message);
            clanChatManager.sendAllyMessage(p, formattedMessage);
        }

        return true;
    }


}
