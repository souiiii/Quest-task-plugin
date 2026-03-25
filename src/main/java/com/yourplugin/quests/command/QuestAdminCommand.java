package com.yourplugin.quests.command;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.config.GuiConfig;
import com.yourplugin.quests.config.QuestLoader;
import com.yourplugin.quests.database.PlayerDataRepository;
import com.yourplugin.quests.manager.QuestManager;
import com.yourplugin.quests.model.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestAdminCommand implements CommandExecutor {

    private final QuestsPlugin plugin;
    private final QuestManager questManager;
    private final PlayerDataRepository repository;
    private final GuiConfig guiConfig;
    private final QuestLoader questLoader;

    public QuestAdminCommand(QuestsPlugin plugin, QuestManager questManager, PlayerDataRepository repository, GuiConfig guiConfig, QuestLoader questLoader) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.repository = repository;
        this.guiConfig = guiConfig;
        this.questLoader = questLoader;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("quests.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /questadmin <setprogress|reset|reload>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            guiConfig.loadConfig();
            questManager.loadQuests();
            sender.sendMessage("Quests plugin reloaded successfully.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /questadmin <setprogress|reset> <player> <id> [value]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found or offline.");
            return true;
        }

        String questId = args[2];
        if (questManager.getQuest(questId) == null) {
            sender.sendMessage("Quest ID not found.");
            return true;
        }

        PlayerQuestData data = questManager.getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage("Player data not loaded yet.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setprogress")) {
            if (args.length < 4) {
                sender.sendMessage("Usage: /questadmin setprogress <player> <id> <value>");
                return true;
            }

            try {
                int value = Integer.parseInt(args[3]);
                
                data.getQuestProgress().put(questId, value);
                if (value < questManager.getQuest(questId).getRequiredAmount()) {
                    data.getCompletedQuests().remove(questId);
                }
                
                repository.saveData(data);
                sender.sendMessage("Progress updated for " + target.getName() + " on quest " + questId + ".");
            } catch (NumberFormatException e) {
                sender.sendMessage("Value must be a number.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            data.getQuestProgress().remove(questId);
            data.getCompletedQuests().remove(questId);
            repository.saveData(data);
            sender.sendMessage("Quest " + questId + " reset for " + target.getName() + ".");
            return true;
        }

        sender.sendMessage("Unknown subcommand.");
        return true;
    }
}
