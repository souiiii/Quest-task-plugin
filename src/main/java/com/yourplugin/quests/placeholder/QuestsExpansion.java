package com.yourplugin.quests.placeholder;

import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class QuestsExpansion extends PlaceholderExpansion {

    private final QuestManager questManager;

    public QuestsExpansion(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "YourName";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PlayerQuestData data = questManager.getPlayerData(player.getUniqueId());
        if (data == null) {
            return "0";
        }

        if (params.startsWith("progress_")) {
            String questId = params.substring(9);
            
            if (data.getCompletedQuests().contains(questId)) {
                if (questManager.getQuest(questId) != null) {
                    return String.valueOf(questManager.getQuest(questId).getRequiredAmount());
                }
            }
            return String.valueOf(data.getQuestProgress().getOrDefault(questId, 0));
        }

        if (params.equals("completed_total")) {
            return String.valueOf(data.getCompletedQuests().size());
        }

        return null;
    }
}
