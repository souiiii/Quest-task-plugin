package com.yourplugin.quests.listener;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;
import com.yourplugin.quests.model.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MovementListener implements Listener {

    private final QuestManager questManager;
    private final Map<UUID, Set<String>> exploredChunksCache;
    private final Map<UUID, Map<String, Integer>> pendingIncrements;

    public MovementListener(QuestsPlugin plugin, QuestManager questManager) {
        this.questManager = questManager;
        this.exploredChunksCache = new ConcurrentHashMap<>();
        this.pendingIncrements = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flushIncrements, 100L, 100L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerQuestData data = questManager.getPlayerData(uuid);

        if (data == null) {
            return;
        }

        boolean chunkChanged = from.getChunk().getX() != to.getChunk().getX() || from.getChunk().getZ() != to.getChunk().getZ();
        String currentChunkKey = to.getWorld().getName() + "_" + to.getChunk().getX() + "_" + to.getChunk().getZ();

        int blocksMoved = Math.max(1, (int) Math.round(from.distance(to)));

        if (!chunkChanged && blocksMoved == 0) {
            return;
        }

        for (Quest quest : questManager.getAllQuests()) {
            if (data.getCompletedQuests().contains(quest.getId())) {
                continue;
            }

            if (quest.getType() == QuestType.RUNNING && blocksMoved > 0) {
                queueIncrement(uuid, quest.getId(), blocksMoved);
            } else if (quest.getType() == QuestType.EXPLORING && chunkChanged) {
                Set<String> explored = exploredChunksCache.computeIfAbsent(uuid, k -> new HashSet<>());
                if (explored.add(currentChunkKey)) {
                    queueIncrement(uuid, quest.getId(), 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        exploredChunksCache.remove(uuid);
        flushPlayer(uuid);
    }

    private void queueIncrement(UUID uuid, String questId, int amount) {
        pendingIncrements.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .merge(questId, amount, Integer::sum);
    }

    private void flushIncrements() {
        for (UUID uuid : pendingIncrements.keySet()) {
            flushPlayer(uuid);
        }
    }

    private void flushPlayer(UUID uuid) {
        Map<String, Integer> increments = pendingIncrements.remove(uuid);
        if (increments != null) {
            for (Map.Entry<String, Integer> entry : increments.entrySet()) {
                questManager.incrementProgress(uuid, entry.getKey(), entry.getValue());
            }
        }
    }
}
