package com.yourplugin.quests.manager;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.config.QuestLoader;
import com.yourplugin.quests.database.PlayerDataRepository;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    public void incrementProgress(UUID uuid, String questId, int amount) {
        PlayerQuestData data = getPlayerData(uuid);
        Quest quest = getQuest(questId);

        if (data == null || quest == null || data.getCompletedQuests().contains(questId)) {
            return;
        }

        int currentProgress = data.getQuestProgress().getOrDefault(questId, 0);
        int newProgress = currentProgress + amount;

        if (newProgress >= quest.getRequiredAmount()) {
            data.getCompletedQuests().add(questId);
            data.getQuestProgress().remove(questId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    if (quest.getRewardCommand() != null && !quest.getRewardCommand().isEmpty()) {
                        String cmd = quest.getRewardCommand().replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                    if (quest.getRewardItems() != null) {
                        for (ItemStack item : quest.getRewardItems()) {
                            player.getInventory().addItem(item);
                        }
                    }
                }
            });
        } else {
            data.getQuestProgress().put(questId, newProgress);
        }

        this.repository.saveData(data);
    }
}
