package com.yourplugin.quests.listener;

import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;
import com.yourplugin.quests.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final QuestManager questManager;

    public MiningListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerQuestData data = questManager.getPlayerData(player.getUniqueId());

        if (data == null) {
            return;
        }

        for (Quest quest : questManager.getAllQuests()) {
            if (quest.getType() == QuestType.MINING && !data.getCompletedQuests().contains(quest.getId())) {
                if (event.getBlock().getType().getKey().getKey().equalsIgnoreCase(quest.getTarget())) {
                    questManager.incrementProgress(player.getUniqueId(), quest.getId(), 1);
                }
            }
        }
    }
}
