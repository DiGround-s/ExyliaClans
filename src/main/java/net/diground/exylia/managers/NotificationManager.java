package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

public class NotificationManager {
    private final ExyliaClans plugin;

    public NotificationManager(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public void notifyClanMembers(String clanName, String message) {
        ClanUtils.getClanMembersByName(plugin, clanName).forEach(memberUUID -> {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatUtils.translateColors(message));
            }
        });
    }

    private void notifyClanMembers(int clanId, String notificationKey, String... replacements) {
        if (!plugin.isNotificationEnabled(notificationKey)) {
            return;
        }

        String prefix = plugin.getMessagesConfig().getString("notifications.prefix", "");
        String clanName = ClanUtils.getClanNameById(plugin, clanId);
        replacements = appendReplacements(replacements, "%PREFIX%", prefix, "%clan%", clanName);

        String chatMessage = plugin.getNotificationChatMessage(notificationKey);
        if (plugin.isNotificationChatEnabled(notificationKey)) {
            sendChatNotification(clanId, chatMessage, replacements);
        }

        String actionBarMessage = plugin.getNotificationActionBarMessage(notificationKey);
        if (plugin.isNotificationActionBarEnabled(notificationKey)) {
            sendActionBarNotification(clanId, actionBarMessage, replacements);
        }

        String titleMessage = plugin.getNotificationTitleMessage(notificationKey);
        String subtitleMessage = plugin.getNotificationTitleSubtitle(notificationKey);
        if (plugin.isNotificationTitleEnabled(notificationKey)) {
            sendTitleNotification(clanId, titleMessage, subtitleMessage, replacements);
        }

        String soundKey = plugin.getMessagesConfig().getString("notifications." + notificationKey + ".sound");
        if (soundKey != null && !soundKey.isEmpty()) {
            playSoundNotification(clanId, soundKey);
        }
    }

    private void sendChatNotification(int clanId, String message, String[] replacements) {
        Component componentMessage = replacePlaceholders(message, replacements);
        List<String> members = ClanUtils.getClanMembers(plugin, clanId);
        for (String uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(componentMessage);
            }
        }
    }

    private void sendActionBarNotification(int clanId, String message, String[] replacements) {
        Component componentMessage = replacePlaceholders(message, replacements);
        List<String> members = ClanUtils.getClanMembers(plugin, clanId);
        for (String uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendActionBar(componentMessage);
            }
        }
    }

    private void sendTitleNotification(int clanId, String title, String subtitle, String[] replacements) {
        Component componentTitle = replacePlaceholders(title, replacements);
        Component componentSubtitle = replacePlaceholders(subtitle, replacements);

        List<String> members = ClanUtils.getClanMembers(plugin, clanId);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3500),
                Duration.ofMillis(1000)
        );
        Title titlePacket = Title.title(componentTitle, componentSubtitle, times);

        for (String uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.showTitle(titlePacket);
            }
        }
    }

    private Component replacePlaceholders(String message, String[] replacements) {
        // Reemplaza los placeholders en el mensaje
        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = replacements[i];
            String replacement = replacements[i + 1];
            message = message.replace(placeholder, replacement);
        }
        // Traduce colores y devuelve el mensaje como Component
        return ChatUtils.translateColors(message);
    }

    private void playSoundNotification(int clanId, String soundKey) {
        Sound sound = Sound.valueOf(soundKey);
        List<String> members = ClanUtils.getClanMembers(plugin, clanId);
        for (String uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    private String[] appendReplacements(String[] replacements, String... newReplacements) {
        String[] result = new String[replacements.length + newReplacements.length];
        System.arraycopy(replacements, 0, result, 0, replacements.length);
        System.arraycopy(newReplacements, 0, result, replacements.length, newReplacements.length);
        return result;
    }





    public void notifyMemberConnected(int clanId, String playerName) {
        notifyClanMembers(clanId, "member_connected", "%player%", playerName);
    }

    public void notifyMemberDisconnected(int clanId, String playerName) {
        notifyClanMembers(clanId, "member_disconnected", "%player%", playerName);
    }

    public void notifyMemberKicked(int clanId, String playerName) {
        notifyClanMembers(clanId, "member_kicked", "%player%", playerName);
    }

    public void notifyMemberDied(int clanId, String playerName) {
        notifyClanMembers(clanId, "member_died", "%player%", playerName);
    }

    public void notifyBaseCreated(int clanId, String playerName) {
        notifyClanMembers(clanId, "base_created", "%player%", playerName);
    }

    public void notifyBaseDeleted(int clanId, String playerName) {
        notifyClanMembers(clanId, "base_deleted", "%player%", playerName);
    }

    public void notifyTeleportedToBase(int clanId, String playerName) {
        notifyClanMembers(clanId, "teleported_to_base", "%player%", playerName);
    }

    public void notifyClanInvite(int clanId, String inviterName, String inviteeName) {
        notifyClanMembers(clanId, "clan_invite", "%inviter%", inviterName, "%invitee%", inviteeName);
    }

    public void notifyNewMember(int clanId, String playerName) {
        notifyClanMembers(clanId, "new_member", "%player%", playerName);
    }

    public void notifyMemberLeft(int clanId, String playerName) {
        notifyClanMembers(clanId, "member_left", "%player%", playerName);
    }

    public void notifyClanDisbanded(int clanId) {
        notifyClanMembers(clanId, "clan_disbanded");
    }

    public void notifyPvpEnabled(int clanId) {
        notifyClanMembers(clanId, "pvp_enabled");
    }

    public void notifyPvpDisabled(int clanId) {
        notifyClanMembers(clanId, "pvp_disabled");
    }

    public void notifyClanRenamed(int clanId, String newName) {
        notifyClanMembers(clanId, "clan_renamed", "%new_name%", newName);
    }

    // Métodos de notificación para alianzas
    public void notifyAllyRequestSent(int clanId, String targetClan) {
        notifyClanMembers(clanId, "ally_request_sent", "%target_clan%", targetClan);
    }

    public void notifyAllyRequestReceived(int clanId, String requesterClan) {
        notifyClanMembers(clanId, "ally_request_received", "%requester_clan%", requesterClan);
    }

    public void notifyAllyRequestAccepted(int clanId, String allyClan) {
        notifyClanMembers(clanId, "ally_request_accepted", "%ally_clan%", allyClan);
    }

    public void notifyAllyRequestDenied(int clanId, String requesterClan) {
        notifyClanMembers(clanId, "ally_request_denied", "%requester_clan%", requesterClan);
    }

    public void notifyAllianceBroken(int clanId, String targetClan) {
        notifyClanMembers(clanId, "alliance_broken", "%target_clan%", targetClan);
    }

    public void notifyMemberPromoted(int clanId, String promoted, String rank) {
        notifyClanMembers(clanId, "member_promoted", "%player%", promoted, "%rank%", rank);
    }

    public void notifyMemberDemoted(int clanId, String demoted, String rank) {
        notifyClanMembers(clanId, "member_demoted", "%player%", demoted, "%rank%", rank);
    }

    public void notifyNewLeader(int clanId, String newLeader) {
        notifyClanMembers(clanId, "new_leader", "%player%", newLeader);
    }

    public void notifyBankDeposited(int clanId, String playerName, double amount) {
        notifyClanMembers(clanId, "bank_deposited", "%player%", playerName, "%amount%", String.valueOf(amount));
    }

    public void notifyBankWithdrawn(int clanId, String playerName, double amount) {
        notifyClanMembers(clanId, "bank_withdrawn", "%player%", playerName, "%amount%", String.valueOf(amount));
    }

    public void notifyEnemyRemoved(int clanId, String targetClan) {
        notifyClanMembers(clanId, "enemy_removed", "%target%", targetClan);
    }

    public void notifyEnemyAdded(int clanId, String targetClan) {
        notifyClanMembers(clanId, "enemy_added", "%target%", targetClan);
    }

    public void notifyRegroupCreated(int clanId, String playerName) {
        notifyClanMembers(clanId, "regroup_created", "%player%", playerName);
    }

    public void notifyRegroupCancelled(int clanId, String playerName) {
        notifyClanMembers(clanId, "regroup_cancelled", "%player%", playerName);
    }

    public void notifyRegroupExpired(int clanId) {
        notifyClanMembers(clanId, "regroup_expired");
    }

    public void notifyRegroupAccepted(int clanId, String playerName) {
        notifyClanMembers(clanId, "regroup_accepted", "%player%", playerName);
    }

}
