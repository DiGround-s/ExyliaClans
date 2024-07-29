package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageListener implements Listener {

    private final ExyliaClans plugin;
    private final Map<String, Integer> hitCounter;
    private final List<PotionEffectType> debuffEffects = Arrays.asList(
            PotionEffectType.HARM,
            PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS,
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.UNLUCK,
            PotionEffectType.LEVITATION
    );

    public DamageListener(ExyliaClans plugin) {
        this.plugin = plugin;
        this.hitCounter = new HashMap<>();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        handleDamage(event);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            Player damager = (Player) event.getPotion().getShooter();
            String damagerUUID = damager.getUniqueId().toString();

            for (LivingEntity damaged : event.getAffectedEntities()) {
                String damagedUUID = damaged.getUniqueId().toString();

                if (ClanUtils.arePlayersInSameClan(plugin, damagerUUID, damagedUUID) || ClanUtils.areClansAllied(plugin, ClanUtils.getPlayerClanId(plugin, damagerUUID), ClanUtils.getPlayerClanId(plugin, damagedUUID))) {
                    for (PotionEffect effect : event.getPotion().getEffects()) {
                        if (debuffEffects.contains(effect.getType())) {
                            event.setIntensity(damaged, 0);
                            sendAlliedClanMessage(damager, (Player) damaged, damagerUUID);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player damager = (Player) event.getEntity().getShooter();
            String damagerUUID = damager.getUniqueId().toString();

            if (event.getHitEntity() instanceof Player) {
                Player damaged = (Player) event.getHitEntity();
                String damagedUUID = damaged.getUniqueId().toString();

                if (ClanUtils.arePlayersInSameClan(plugin, damagerUUID, damagedUUID) || ClanUtils.areClansAllied(plugin, ClanUtils.getPlayerClanId(plugin, damagerUUID), ClanUtils.getPlayerClanId(plugin, damagedUUID))) {
                    event.setCancelled(true);
                    sendAlliedClanMessage(damager, damaged, damagerUUID);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity damager = (LivingEntity) event.getEntity();

            if (damager instanceof Player) {
                Player playerDamager = (Player) damager;
                String damagerUUID = playerDamager.getUniqueId().toString();

                for (Player damaged : playerDamager.getWorld().getPlayers()) {
                    if (damaged.getLocation().distance(event.getLocation()) <= event.getYield()) {
                        String damagedUUID = damaged.getUniqueId().toString();

                        if (ClanUtils.arePlayersInSameClan(plugin, damagerUUID, damagedUUID) || ClanUtils.areClansAllied(plugin, ClanUtils.getPlayerClanId(plugin, damagerUUID), ClanUtils.getPlayerClanId(plugin, damagedUUID))) {
                            damaged.setNoDamageTicks(20); // Prevent damage from explosion
                            sendAlliedClanMessage(playerDamager, damaged, damagerUUID);
                        }
                    }
                }
            }
        }
    }

    private void handleDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof LivingEntity) {
            Player damaged = (Player) event.getEntity();
            LivingEntity damager = (LivingEntity) event.getDamager();

            if (damager instanceof Player) {
                String damagedUUID = damaged.getUniqueId().toString();
                String damagerUUID = damager.getUniqueId().toString();

                if (ClanUtils.arePlayersInSameClan(plugin, damagerUUID, damagedUUID)) {
                    int damagedClanId = ClanUtils.getPlayerClanId(plugin, damagedUUID);
                    int damagerClanId = ClanUtils.getPlayerClanId(plugin, damagerUUID);
                    if (damagerClanId == damagedClanId && !ClanUtils.isPvpEnabled(plugin, damagerClanId)) {
                        event.setCancelled(true);
                        sendPvpDisabledMessage((Player) damager, damaged, damagerUUID);
                    }
                }

                if (ClanUtils.areClansAllied(plugin, ClanUtils.getPlayerClanId(plugin, damagerUUID), ClanUtils.getPlayerClanId(plugin, damagedUUID))) {
                    event.setCancelled(true);
                    sendAlliedClanMessage((Player) damager, damaged, damagerUUID);
                }
            }
        }
    }

    private void sendPvpDisabledMessage(Player damager, Player damaged, String damagerUUID) {
        int count = hitCounter.getOrDefault(damagerUUID, 0) + 1;
        hitCounter.put(damagerUUID, count);

        if (count % 4 == 0) {
            damager.sendMessage(ChatUtils.translateColors(plugin.getMessage("pvp_disabled_in_clan")));
            damaged.sendMessage(ChatUtils.translateColors(plugin.getMessage("pvp_disabled_in_clan_attacked")));
        }
    }

    private void sendAlliedClanMessage(Player damager, Player damaged, String damagerUUID) {
        int count = hitCounter.getOrDefault(damagerUUID, 0) + 1;
        hitCounter.put(damagerUUID, count);

        if (count % 4 == 0) {
            damager.sendMessage(ChatUtils.translateColors(plugin.getMessage("pvp_disabled_among_allies")));
            damaged.sendMessage(ChatUtils.translateColors(plugin.getMessage("pvp_disabled_among_allies_attacked")));
        }
    }
}