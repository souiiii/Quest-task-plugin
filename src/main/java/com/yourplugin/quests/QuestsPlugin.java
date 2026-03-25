package com.yourplugin.quests;

import com.yourplugin.quests.command.QuestAdminCommand;
import com.yourplugin.quests.command.QuestsCommand;
import com.yourplugin.quests.config.GuiConfig;
import com.yourplugin.quests.config.QuestLoader;
import com.yourplugin.quests.database.MongoManager;
import com.yourplugin.quests.database.PlayerDataRepository;
import com.yourplugin.quests.listener.KillingListener;
import com.yourplugin.quests.listener.MiningListener;
import com.yourplugin.quests.listener.MovementListener;
import com.yourplugin.quests.manager.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestsPlugin extends JavaPlugin implements Listener {

    private static QuestsPlugin instance;

    private MongoManager mongoManager;
    private QuestManager questManager;

    public static QuestsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.mongoManager = new MongoManager(this);
        this.mongoManager.connect();

        PlayerDataRepository repository = new PlayerDataRepository(this.mongoManager);
        QuestLoader questLoader = new QuestLoader(this);
        GuiConfig guiConfig = new GuiConfig(this);

        this.questManager = new QuestManager(this, repository, questLoader);
        this.questManager.loadQuests();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new MiningListener(this.questManager), this);
        Bukkit.getPluginManager().registerEvents(new KillingListener(this.questManager), this);
        Bukkit.getPluginManager().registerEvents(new MovementListener(this, this.questManager), this);

        getCommand("quests").setExecutor(new QuestsCommand(this, this.questManager, guiConfig));
        getCommand("questadmin").setExecutor(new QuestAdminCommand(this, this.questManager, repository, guiConfig, questLoader));

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.questManager.loadPlayerData(player.getUniqueId());
        }

        getLogger().info("QuestsPlugin enabled. Core mechanics and listeners registered.");
    }

    @Override
    public void onDisable() {
        if (this.questManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.questManager.unloadPlayerData(player.getUniqueId());
            }
        }

        if (this.mongoManager != null) {
            this.mongoManager.disconnect();
        }
        
        getLogger().info("QuestsPlugin disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.questManager != null) {
            this.questManager.loadPlayerData(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.questManager != null) {
            this.questManager.unloadPlayerData(event.getPlayer().getUniqueId());
        }
    }
}
