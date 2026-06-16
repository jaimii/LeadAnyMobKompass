package xyz.amudev.leadAnyMob.listener;

import xyz.amudev.leadAnyMob.LeadAnyMob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PlayerTeleportListener implements Listener {
    private final LeadAnyMob plugin;

    public PlayerTeleportListener(LeadAnyMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Fast-fail 1: O(1) tracker check. Stops teleport tracking operations instantly if player has no custom-leashed mobs.
        if (!plugin.getLeashManager().hasActiveLeashes(player.getUniqueId())) {
            return;
        }

        if (event.getFrom().getWorld() == null || event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }

        if (!plugin.getLeashManager().isWorldValid(event.getFrom().getWorld()) ||
                !plugin.getLeashManager().isWorldValid(event.getTo().getWorld())) {
            return;
        }

        if (player.hasPermission("leadanymob.use")) {
            plugin.getLeashManager().teleportLeashed(player);
        }
    }
}