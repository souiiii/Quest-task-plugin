package com.yourplugin.quests.manager;

import com.yourplugin.quests.model.PlayerQuestData;

import java.util.UUID;

public class ProgressTracker {

    public String computePercentage(int current, int required) {
        if (required <= 0) {
            return "[■■■■■]";
        }
        
        int percent = (int) (((double) current / required) * 5);
        if (percent > 5) {
            percent = 5;
        } else if (percent < 0) {
            percent = 0;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 5; i++) {
            if (i < percent) {
                sb.append("■");
            } else {
                sb.append("□");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String computeStatus(UUID uuid, String questId, QuestManager questManager) {
        PlayerQuestData data = questManager.getPlayerData(uuid);
        if (data == null) {
            return "Not Started";
        }

        if (data.getCompletedQuests().contains(questId)) {
            return "Completed";
        }

        int progress = data.getQuestProgress().getOrDefault(questId, 0);
        if (progress > 0) {
            return "In Progress";
        }

        return "Not Started";
    }
}
