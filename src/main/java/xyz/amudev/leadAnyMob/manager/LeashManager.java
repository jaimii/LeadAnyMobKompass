package xyz.amudev.leadAnyMob.manager;

import xyz.amudev.leadAnyMob.LeadAnyMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LeashManager {
    private final LeadAnyMob plugin;
    private final Set<UUID> boostingLeashedEntities = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Set<UUID>> activeLeashes = new ConcurrentHashMap<>();

    public LeashManager(LeadAnyMob plugin) {
        this.plugin = plugin;
    }

    /**
     * Tracks a custom leashed entity under a specific player.
     */
    public void trackLeash(UUID playerUuid, UUID entityUuid) {
        activeLeashes.computeIfAbsent(playerUuid, k -> ConcurrentHashMap.newKeySet()).add(entityUuid);
    }

    /**
     * Untracks a specific leashed entity under a player.
     */
    public void untrackLeash(UUID playerUuid, UUID entityUuid) {
        Set<UUID> leashes = activeLeashes.get(playerUuid);
        if (leashes != null) {
            leashes.remove(entityUuid);
            if (leashes.isEmpty()) {
                activeLeashes.remove(playerUuid);
            }
        }
    }

    /**
     * Checks if the player is currently leading any active custom-leashed mobs.
     * This provides an O(1) check to bypass expensive world entity searches.
     */
    public boolean hasActiveLeashes(UUID playerUuid) {
        return activeLeashes.containsKey(playerUuid);
    }

    /**
     * Flags an entity as participating in an Elytra firework boost.
     * Clears on the next tick automatically.
     */
    public void registerBoostingEntity(UUID entityId) {
        boostingLeashedEntities.add(entityId);
        plugin.getServer().getScheduler().runTask(plugin, () -> boostingLeashedEntities.remove(entityId));
    }

    /**
     * Checks if the entity is currently flagged as boosting.
     */
    public boolean isBoostingEntity(UUID entityId) {
        return boostingLeashedEntities.contains(entityId);
    }

    /**
     * Teleports all entities leashed by a specific holder to that holder's location.
     */
    public void teleportLeashed(LivingEntity holder) {
        getLeashedEntities(holder).forEach(leashed -> teleportLeashed(holder, leashed));
    }

    /**
     * Safely teleports a leashed entity to its holder, managing passengers and chunk loading.
     */
    public void teleportLeashed(LivingEntity holder, LivingEntity leashed) {
        List<Entity> passengers = new ArrayList<>(leashed.getPassengers());
        Chunk chunk = leashed.getLocation().getChunk();

        chunk.addPluginChunkTicket(plugin);
        leashed.setLeashHolder(null);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            leashed.eject();
            leashed.teleport(holder);
            leashed.setLeashHolder(holder);

            for (Entity passenger : passengers) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    passenger.teleport(leashed, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    leashed.addPassenger(passenger);
                }, 1L);
            }

            chunk.removePluginChunkTicket(plugin);
        }, 3L);
    }

    /**
     * Instantly teleports a leashed entity and its passengers to the holder.
     */
    public void teleportLeashedInstant(LivingEntity holder, LivingEntity leashed) {
        List<Entity> passengers = new ArrayList<>(leashed.getPassengers());
        leashed.eject();
        leashed.teleport(holder.getLocation().add(0, 1, 0));

        for (Entity passenger : passengers) {
            passenger.teleport(leashed, PlayerTeleportEvent.TeleportCause.UNKNOWN);
            leashed.addPassenger(passenger);
        }
    }

    /**
     * Checks if the configuration allows this world.
     */
    public boolean isWorldValid(World world) {
        List<String> worlds = plugin.getConfig().getStringList("worlds");
        if (worlds.isEmpty()) {
            return true;
        }
        return worlds.contains("ALL") || worlds.contains(world.getName());
    }

    /**
     * Identifies nearby entities currently leashed by the specified holder.
     */
    private List<LivingEntity> getLeashedEntities(Entity holder) {
        List<LivingEntity> leashedEntities = new ArrayList<>();
        for (Entity nearby : holder.getWorld().getNearbyEntities(holder.getLocation(), 15, 15, 15)) {
            if (nearby instanceof LivingEntity living && living.isLeashed()) {
                Entity leashHolder = living.getLeashHolder();
                if (leashHolder != null && leashHolder.getUniqueId().equals(holder.getUniqueId())) {
                    leashedEntities.add(living);
                }
            }
        }
        return leashedEntities;
    }
}