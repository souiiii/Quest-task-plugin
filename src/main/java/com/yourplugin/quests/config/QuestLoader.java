package com.yourplugin.quests.config;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.gui.ItemBuilder;
import com.yourplugin.quests.model.Quest;
import com.yourplugin.quests.model.QuestType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestLoader {

    private final QuestsPlugin plugin;

    public QuestLoader(QuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<String, Quest> loadQuests() {
        Map<String, Quest> quests = new HashMap<>();
        File file = new File(plugin.getDataFolder(), "quests.yml");

        if (!file.exists()) {
            return quests;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection questsSection = config.getConfigurationSection("quests");

        if (questsSection == null) {
            return quests;
        }

        for (String id : questsSection.getKeys(false)) {
            ConfigurationSection section = questsSection.getConfigurationSection(id);
            if (section == null) continue;

            QuestType type = QuestType.valueOf(section.getString("type", "MINING").toUpperCase());
            String target = section.getString("target");
            int requiredAmount = section.getInt("amount", 1);
            String rewardCommand = section.getString("reward.command");
            List<ItemStack> rewardItems = new ArrayList<>();

            ConfigurationSection guiItemSection = section.getConfigurationSection("gui-item");
            ItemStack guiItem = null;

            if (guiItemSection != null) {
                String matString = guiItemSection.getString("material", "STONE").toLowerCase();
                Material material = org.bukkit.Registry.MATERIAL.get(org.bukkit.NamespacedKey.minecraft(matString));
                if (material == null) material = Material.STONE;

                ItemBuilder builder = ItemBuilder.create(material);

                if (guiItemSection.contains("amount")) {
                    builder.amount(guiItemSection.getInt("amount"));
                }
                if (guiItemSection.contains("display-name")) {
                    builder.name(guiItemSection.getString("display-name"));
                }
                if (guiItemSection.contains("lore")) {
                    builder.lore(guiItemSection.getStringList("lore"));
                }
                if (guiItemSection.contains("item-flags")) {
                    builder.flags(guiItemSection.getStringList("item-flags"));
                }
                if (guiItemSection.contains("custom-model-data")) {
                    builder.customModelData(guiItemSection.getInt("custom-model-data"));
                }

                guiItem = builder.build();
            }

            quests.put(id, new Quest(id, type, target, requiredAmount, rewardCommand, rewardItems, guiItem));
        }

        return quests;
    }
}
