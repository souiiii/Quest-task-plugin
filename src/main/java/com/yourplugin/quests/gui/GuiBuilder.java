package com.yourplugin.quests.gui;

import com.yourplugin.quests.config.GuiConfig;
import com.yourplugin.quests.manager.ProgressTracker;
import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import com.yourplugin.quests.model.Quest;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiBuilder {

    private final Player player;
    private final List<Quest> quests;
    private final QuestManager questManager;
    private final GuiConfig config;
    private final int page;
    private final ProgressTracker progressTracker;

    public GuiBuilder(Player player, List<Quest> quests, QuestManager questManager, GuiConfig config, int page) {
        this.player = player;
        this.quests = quests;
        this.questManager = questManager;
        this.config = config;
        this.page = page;
        this.progressTracker = new ProgressTracker();
    }

    public Inventory build() {
        int size = config.getRows() * 9;
        String titleStr = config.getTitle().replace("%page%", String.valueOf(page + 1));
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            titleStr = PlaceholderAPI.setPlaceholders(player, titleStr);
        }

        Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr);
        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<Integer> navSlots = new ArrayList<>(config.getNavigationSlots().values());
        int maxItemsPerPage = size - navSlots.size();
        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, quests.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            while (navSlots.contains(slot) && slot < size) {
                slot++;
            }
            if (slot >= size) break;

            Quest quest = quests.get(i);
            inventory.setItem(slot, buildQuestItem(quest));
            slot++;
        }

        if (page > 0 && config.getNavigationSlots().containsKey("previous")) {
            inventory.setItem(config.getNavigationSlots().get("previous"), config.getNavigationItems().get("previous"));
        }

        if (endIndex < quests.size() && config.getNavigationSlots().containsKey("next")) {
            inventory.setItem(config.getNavigationSlots().get("next"), config.getNavigationItems().get("next"));
        }

        if (config.getNavigationSlots().containsKey("close")) {
            inventory.setItem(config.getNavigationSlots().get("close"), config.getNavigationItems().get("close"));
        }

        return inventory;
    }

    private ItemStack buildQuestItem(Quest quest) {
        if (quest.getGuiItem() == null) return null;

        ItemStack item = quest.getGuiItem().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PlayerQuestData data = questManager.getPlayerData(player.getUniqueId());
        int progress = (data != null) ? data.getQuestProgress().getOrDefault(quest.getId(), 0) : 0;
        if (data != null && data.getCompletedQuests().contains(quest.getId())) {
            progress = quest.getRequiredAmount();
        }

        String percentage = progressTracker.computePercentage(progress, quest.getRequiredAmount());
        String status = progressTracker.computeStatus(player.getUniqueId(), quest.getId(), questManager);

        boolean hasPapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        if (meta.hasDisplayName()) {
            String name = LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName());
            name = replacePlaceholders(name, progress, quest.getRequiredAmount(), percentage, status);
            if (hasPapi) name = PlaceholderAPI.setPlaceholders(player, name);
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        }

        if (meta.hasLore()) {
            List<Component> newLore = new ArrayList<>();
            for (Component lineComp : meta.lore()) {
                String line = LegacyComponentSerializer.legacyAmpersand().serialize(lineComp);
                line = replacePlaceholders(line, progress, quest.getRequiredAmount(), percentage, status);
                if (hasPapi) line = PlaceholderAPI.setPlaceholders(player, line);
                newLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(newLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(String text, int progress, int required, String percentage, String status) {
        if (text == null) return null;
        return text.replace("%progress%", String.valueOf(progress))
                .replace("%required%", String.valueOf(required))
                .replace("%percentage%", percentage)
                .replace("%status%", status);
    }
}
