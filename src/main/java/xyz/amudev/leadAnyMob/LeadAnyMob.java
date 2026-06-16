package xyz.amudev.leadAnyMob;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.amudev.leadAnyMob.listener.LeashInteractListener;
import xyz.amudev.leadAnyMob.listener.LeashSnapListener;
import xyz.amudev.leadAnyMob.listener.PlayerTeleportListener;
import xyz.amudev.leadAnyMob.listener.EntityPortalListener;
import xyz.amudev.leadAnyMob.listener.PlayerElytraBoostListener;
import xyz.amudev.leadAnyMob.manager.LeashManager;
import xyz.amudev.leadAnyMob.util.LeashRegistry;

public final class LeadAnyMob extends JavaPlugin {
    private LeashRegistry leashRegistry;
    private LeashManager leashManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.leashRegistry = new LeashRegistry();
        this.leashManager = new LeashManager(this);

        // Register all listeners
        getServer().getPluginManager().registerEvents(new LeashInteractListener(this, leashRegistry), this);
        getServer().getPluginManager().registerEvents(new LeashSnapListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPortalListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerElytraBoostListener(this), this);

        getComponentLogger().info("LeadAnyMob has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getComponentLogger().info("LeadAnyMob has been disabled.");
    }

    public LeashRegistry getLeashRegistry() {
        return leashRegistry;
    }

    public LeashManager getLeashManager() {
        return leashManager;
    }
}