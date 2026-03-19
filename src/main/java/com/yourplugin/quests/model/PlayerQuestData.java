package com.yourplugin.quests.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerQuestData {
    private final UUID uuid;
    private final Map<String, Integer> questProgress;
    private final Set<String> completedQuests;

    public PlayerQuestData(UUID uuid) {
        this.uuid = uuid;
        this.questProgress = new HashMap<>();
        this.completedQuests = new HashSet<>();
    }

    public PlayerQuestData(UUID uuid, Map<String, Integer> questProgress, Set<String> completedQuests) {
        this.uuid = uuid;
        this.questProgress = questProgress;
        this.completedQuests = completedQuests;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, Integer> getQuestProgress() {
        return questProgress;
    }

    public Set<String> getCompletedQuests() {
        return completedQuests;
    }
}
