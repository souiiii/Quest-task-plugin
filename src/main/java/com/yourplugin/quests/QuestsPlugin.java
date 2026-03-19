package com.yourplugin.quests;

import org.bukkit.plugin.java.JavaPlugin;

public class QuestsPlugin extends JavaPlugin {

    private static QuestsPlugin instance;

    public static QuestsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("QuestsPlugin enabled. Database and quest loading coming soon.");
    }

    @Override
    public void onDisable() {
        getLogger().info("QuestsPlugin disabled.");
    }
}
