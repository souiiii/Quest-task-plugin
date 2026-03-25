package com.yourplugin.quests.gui;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.config.GuiConfig;
import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.Quest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class GuiSession implements Listener {

    private final QuestsPlugin plugin;
    private final Player player;
    private final List<Quest> quests;
    private final QuestManager questManager;
    private final GuiConfig config;
    private int page;
    private Inventory inventory;

    public GuiSession(QuestsPlugin plugin, Player player, List<Quest> quests, QuestManager questManager, GuiConfig config, int page) {
        this.plugin = plugin;
        this.player = player;
        this.quests = quests;
        this.questManager = questManager;
        this.config = config;
        this.page = page;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        open();
    }

    public void open() {
        GuiBuilder builder = new GuiBuilder(player, quests, questManager, config, page);
        this.inventory = builder.build();
        player.openInventory(this.inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() == null || !event.getInventory().equals(this.inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(this.inventory)) return;

        int slot = event.getSlot();

        if (config.getNavigationSlots().getOrDefault("previous", -1) == slot && page > 0) {
            page--;
            open();
            return;
        }

        List<Integer> navSlots = new ArrayList<>(config.getNavigationSlots().values());
        int size = config.getRows() * 9;
        int maxItemsPerPage = size - navSlots.size();

        if (config.getNavigationSlots().getOrDefault("next", -1) == slot && (page + 1) * maxItemsPerPage < quests.size()) {
            page++;
            open();
            return;
        }

        if (config.getNavigationSlots().getOrDefault("close", -1) == slot) {
            player.closeInventory();
            return;
        }

        if (!navSlots.contains(slot)) {
            int startIndex = page * maxItemsPerPage;
            int questIndex = startIndex;

            for (int i = 0; i <= slot; i++) {
                if (navSlots.contains(i)) continue;
                if (i == slot) {
                    if (questIndex < quests.size()) {
                        Quest quest = quests.get(questIndex);
                        player.sendMessage(Component.text("Now tracking: " + quest.getId()));
                    }
                    break;
                }
                questIndex++;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(this.inventory) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
}
