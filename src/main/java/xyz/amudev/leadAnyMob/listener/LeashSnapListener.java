package xyz.amudev.leadAnyMob.listener;

import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import xyz.amudev.leadAnyMob.LeadAnyMob;

public final class LeashSnapListener implements Listener {
    private final LeadAnyMob plugin;

    public LeashSnapListener(LeadAnyMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeadSnap(EntityUnleashEvent event) {
        Entity entity = event.getEntity();

        if (!entity.isValid() || entity.isDead()) return;

        // 1. Intercept explicit firework-boost snap conditions
        if (plugin.getLeashManager().isBoostingEntity(entity.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // 2. Standard unleash/snap fallback logic
        if (entity instanceof Leashable leashed) {
            Entity holder = leashed.getLeashHolder();

            // Clean up tracker mappings immediately on any leash snap or release
            if (holder instanceof Player player) {
                plugin.getLeashManager().untrackLeash(player.getUniqueId(), entity.getUniqueId());
            }

            if (holder == null || !holder.isValid() || holder.isDead()) return;
            if (holder instanceof Player player && !player.isOnline()) return;

            EntityUnleashEvent.UnleashReason reason = event.getReason();
            boolean isGlidingPlayer = holder instanceof Player player && player.isGliding();

            if (reason == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
                if (!isGlidingPlayer) {
                    return; // Allow normal manual player releases
                }
            }

            if (reason == EntityUnleashEvent.UnleashReason.DISTANCE ||
                    reason == EntityUnleashEvent.UnleashReason.UNKNOWN ||
                    isGlidingPlayer) {

                if (!plugin.getLeashManager().isWorldValid(entity.getWorld())) {
                    return;
                }

                event.setCancelled(true);

                if (entity instanceof LivingEntity livingLeashed && holder instanceof LivingEntity livingHolder) {
                    if (isGlidingPlayer) {
                        plugin.getLeashManager().teleportLeashedInstant(livingHolder, livingLeashed);
                    } else {
                        plugin.getLeashManager().teleportLeashed(livingHolder, livingLeashed);
                    }
                } else {
                    entity.teleport(holder.getLocation().add(0, 1, 0));
                }
            }
        }
    }
}