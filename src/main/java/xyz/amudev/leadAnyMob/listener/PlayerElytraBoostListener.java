package xyz.amudev.leadAnyMob.listener;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import xyz.amudev.leadAnyMob.LeadAnyMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlayerElytraBoostListener implements Listener {
    private final LeadAnyMob plugin;

    public PlayerElytraBoostListener(LeadAnyMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();

        // Fast-fail 1: O(1) map check. If the player isn't leading custom-leashed mobs, skip sweeps entirely.
        // This eliminates performance loss on every firework boost used across the server.
        if (!plugin.getLeashManager().hasActiveLeashes(player.getUniqueId())) {
            return;
        }

        if (!plugin.getLeashManager().isWorldValid(player.getWorld())) {
            return;
        }

        if (!player.hasPermission("leadanymob.use")) {
            return;
        }

        for (Entity nearby : player.getWorld().getNearbyEntities(player.getLocation(), 15, 15, 15)) {
            if (nearby instanceof LivingEntity living && living.isLeashed()) {
                Entity holder = living.getLeashHolder();
                if (holder != null && holder.getUniqueId().equals(player.getUniqueId())) {
                    plugin.getLeashManager().registerBoostingEntity(living.getUniqueId());
                    plugin.getLeashManager().teleportLeashedInstant(player, living);
                }
            }
        }
    }
}