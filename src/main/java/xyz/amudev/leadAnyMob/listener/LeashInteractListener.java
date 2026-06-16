package xyz.amudev.leadAnyMob.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.amudev.leadAnyMob.LeadAnyMob;
import xyz.amudev.leadAnyMob.util.LeashRegistry;

public final class LeashInteractListener implements Listener {
    private final LeadAnyMob plugin;
    private final LeashRegistry leashRegistry;

    public LeashInteractListener(LeadAnyMob plugin, LeashRegistry leashRegistry) {
        this.plugin = plugin;
        this.leashRegistry = leashRegistry;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Fast-fail 1: Hand check (extremely cheap)
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Fast-fail 2: Entity type assertion
        if (!(event.getRightClicked() instanceof LivingEntity entity)) return;

        Player player = event.getPlayer();

        // Fast-fail 3: Held item check (avoids permission framework overhead)
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.LEAD) return;

        // Fast-fail 4: Vanilla and leashed state check (EnumSet lookup)
        if (leashRegistry.isLeashableInVanilla(entity.getType()) || entity.isLeashed()) return;

        // Fast-fail 5: Heavy permission lookups
        if (!player.hasPermission("leadanymob.use")) return;

        if (player.getGameMode() != GameMode.CREATIVE) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }

        // Track custom leashed entity in O(1) map
        plugin.getLeashManager().trackLeash(player.getUniqueId(), entity.getUniqueId());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            entity.setLeashHolder(player);
        }, 1L);
    }

    @EventHandler
    public void onFenceAttach(PlayerInteractEvent event) {
        // Fast-fail 1: Action check
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        Player player = event.getPlayer();

        // Fast-fail 2: Tracker check (O(1) memory lookup).
        // If the player does not have custom-leashed mobs, skip search sweeps entirely.
        if (!plugin.getLeashManager().hasActiveLeashes(player.getUniqueId())) return;

        // Fast-fail 3: Material check
        if (!Tag.FENCES.isTagged(event.getClickedBlock().getType())) return;

        var clickedLoc = event.getClickedBlock().getLocation();

        // Performs entity scan only if the player has active custom leashes
        for (Entity nearby : player.getNearbyEntities(10, 10, 10)) {
            if (nearby instanceof LivingEntity living && living.isLeashed() && player.equals(living.getLeashHolder())) {
                if (!leashRegistry.isLeashableInVanilla(living.getType())) {
                    LeashHitch hitch = nearby.getWorld().spawn(clickedLoc, LeashHitch.class);
                    living.setLeashHolder(hitch);

                    // Stop tracking leash on fence tie
                    plugin.getLeashManager().untrackLeash(player.getUniqueId(), living.getUniqueId());
                    event.setCancelled(true);
                }
            }
        }
    }
}