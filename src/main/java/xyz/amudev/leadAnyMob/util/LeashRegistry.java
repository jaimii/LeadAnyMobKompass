package xyz.amudev.leadAnyMob.util;

import org.bukkit.entity.EntityType;
import java.util.EnumSet;
import java.util.Set;

public final class LeashRegistry {
    // EnumSet provides highly optimized O(1) bitwise lookup operations
    private final Set<EntityType> leashableInVanilla = EnumSet.noneOf(EntityType.class);

    public LeashRegistry() {
        setupVanillaList();
    }

    private void setupVanillaList() {
        leashableInVanilla.add(EntityType.ALLAY);
        leashableInVanilla.add(EntityType.ARMADILLO);
        leashableInVanilla.add(EntityType.AXOLOTL);
        leashableInVanilla.add(EntityType.BEE);
        leashableInVanilla.add(EntityType.CAMEL);
        leashableInVanilla.add(EntityType.CAT);
        leashableInVanilla.add(EntityType.CHICKEN);
        leashableInVanilla.add(EntityType.COW);
        leashableInVanilla.add(EntityType.DOLPHIN);
        leashableInVanilla.add(EntityType.DONKEY);
        leashableInVanilla.add(EntityType.FOX);
        leashableInVanilla.add(EntityType.FROG);
        leashableInVanilla.add(EntityType.GLOW_SQUID);
        leashableInVanilla.add(EntityType.GOAT);
        leashableInVanilla.add(EntityType.HOGLIN);
        leashableInVanilla.add(EntityType.HORSE);
        leashableInVanilla.add(EntityType.IRON_GOLEM);
        leashableInVanilla.add(EntityType.COPPER_GOLEM);
        leashableInVanilla.add(EntityType.LLAMA);
        leashableInVanilla.add(EntityType.TRADER_LLAMA);
        leashableInVanilla.add(EntityType.MOOSHROOM);
        leashableInVanilla.add(EntityType.MULE);
        leashableInVanilla.add(EntityType.OCELOT);
        leashableInVanilla.add(EntityType.PARROT);
        leashableInVanilla.add(EntityType.PIG);
        leashableInVanilla.add(EntityType.POLAR_BEAR);
        leashableInVanilla.add(EntityType.RABBIT);
        leashableInVanilla.add(EntityType.SHEEP);
        leashableInVanilla.add(EntityType.SKELETON_HORSE);
        leashableInVanilla.add(EntityType.SNIFFER);
        leashableInVanilla.add(EntityType.SNOW_GOLEM);
        leashableInVanilla.add(EntityType.SQUID);
        leashableInVanilla.add(EntityType.STRIDER);
        leashableInVanilla.add(EntityType.WOLF);
        leashableInVanilla.add(EntityType.ZOGLIN);
        leashableInVanilla.add(EntityType.ZOMBIE_HORSE);
        leashableInVanilla.add(EntityType.CAMEL_HUSK);
        leashableInVanilla.add(EntityType.NAUTILUS);
        leashableInVanilla.add(EntityType.ZOMBIE_NAUTILUS);

        // Add all Boat and Chest Boat variants dynamically to avoid missing new wood types
        for (EntityType type : EntityType.values()) {
            if (type.name().contains("BOAT")) {
                leashableInVanilla.add(type);
            }
        }
    }

    /**
     * Determines if a given entity type is leashable in vanilla Minecraft.
     * Uses bitwise comparison for standard enum mapping.
     *
     * @param type The EntityType to verify.
     * @return true if leashable in vanilla, false otherwise.
     */
    public boolean isLeashableInVanilla(EntityType type) {
        return leashableInVanilla.contains(type);
    }
}