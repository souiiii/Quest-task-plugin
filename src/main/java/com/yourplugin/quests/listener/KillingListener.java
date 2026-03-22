package com.yourplugin.quests.listener;

import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;
import com.yourplugin.quests.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillingListener implements Listener {

    private final QuestManager questManager;

    public KillingListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        
        if (killer == null) {
            return;
        }

        PlayerQuestData data = questManager.getPlayerData(killer.getUniqueId());

        if (data == null) {
            return;
        }

        for (Quest quest : questManager.getAllQuests()) {
            if (quest.getType() == QuestType.KILLING && !data.getCompletedQuests().contains(quest.getId())) {
                if (event.getEntity().getType().getKey().getKey().equalsIgnoreCase(quest.getTarget())) {
                    questManager.incrementProgress(killer.getUniqueId(), quest.getId(), 1);
                }
            }
        }
    }
}
