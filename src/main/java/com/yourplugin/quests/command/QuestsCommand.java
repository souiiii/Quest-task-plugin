package com.yourplugin.quests.command;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.config.GuiConfig;
import com.yourplugin.quests.gui.GuiSession;
import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.Quest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestsCommand implements CommandExecutor {

    private final QuestsPlugin plugin;
    private final QuestManager questManager;
    private final GuiConfig guiConfig;

    public QuestsCommand(QuestsPlugin plugin, QuestManager questManager, GuiConfig guiConfig) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.guiConfig = guiConfig;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        List<Quest> questsList = new ArrayList<>(questManager.getAllQuests());
        new GuiSession(plugin, player, questsList, questManager, guiConfig, 0);

        return true;
    }
}
