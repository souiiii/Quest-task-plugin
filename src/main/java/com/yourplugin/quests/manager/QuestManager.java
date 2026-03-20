package com.yourplugin.quests.manager;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.config.QuestLoader;
import com.yourplugin.quests.database.PlayerDataRepository;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final QuestsPlugin plugin;
    private final PlayerDataRepository repository;
    private final QuestLoader loader;
    private final Map<String, Quest> quests;
    private final Map<UUID, PlayerQuestData> playerDataCache;

    public QuestManager(QuestsPlugin plugin, PlayerDataRepository repository, QuestLoader loader) {
        this.plugin = plugin;
        this.repository = repository;
        this.loader = loader;
        this.quests = new HashMap<>();
        this.playerDataCache = new ConcurrentHashMap<>();
    }

    public void loadQuests() {
        this.quests.clear();
        this.quests.putAll(this.loader.loadQuests());
        plugin.getLogger().info("Loaded " + this.quests.size() + " quests.");
    }

    public void loadPlayerData(UUID uuid) {
        this.repository.loadData(uuid).thenAccept(data -> {
            this.playerDataCache.put(uuid, data);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void unloadPlayerData(UUID uuid) {
        PlayerQuestData data = this.playerDataCache.remove(uuid);
        if (data != null) {
            this.repository.saveData(data);
        }
    }

    public Quest getQuest(String id) {
        return this.quests.get(id);
    }

    public Collection<Quest> getAllQuests() {
        return this.quests.values();
    }

    public PlayerQuestData getPlayerData(UUID uuid) {
        return this.playerDataCache.get(uuid);
    }

    // TODO: incrementProgress logic — coming next
}
