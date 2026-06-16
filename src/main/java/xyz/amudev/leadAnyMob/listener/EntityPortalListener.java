package xyz.amudev.leadAnyMob.listener;

import xyz.amudev.leadAnyMob.LeadAnyMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public final class EntityPortalListener implements Listener {
    private final LeadAnyMob plugin;

    public EntityPortalListener(LeadAnyMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.isLeashed() && livingEntity.getLeashHolder() instanceof Player player) {
                if (plugin.getLeashManager().isWorldValid(livingEntity.getWorld()) &&
                        player.hasPermission("leadanymob.use")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}