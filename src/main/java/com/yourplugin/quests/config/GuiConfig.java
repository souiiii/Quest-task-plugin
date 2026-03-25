package com.yourplugin.quests.config;

import com.yourplugin.quests.QuestsPlugin;
import com.yourplugin.quests.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GuiConfig {

    private final QuestsPlugin plugin;
    private String title;
    private int rows;
    private final Map<String, Integer> navigationSlots;
    private final Map<String, ItemStack> navigationItems;

    public GuiConfig(QuestsPlugin plugin) {
        this.plugin = plugin;
        this.navigationSlots = new HashMap<>();
        this.navigationItems = new HashMap<>();
        loadConfig();
    }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.title = config.getString("title", "Quests");
        this.rows = config.getInt("rows", 6);

        ConfigurationSection navSection = config.getConfigurationSection("navigation");
        if (navSection != null) {
            for (String key : navSection.getKeys(false)) {
                ConfigurationSection itemSection = navSection.getConfigurationSection(key);
                if (itemSection == null) continue;

                this.navigationSlots.put(key, itemSection.getInt("slot", 0));

                ConfigurationSection displaySection = itemSection.getConfigurationSection("item");
                if (displaySection != null) {
                    String matString = displaySection.getString("material", "STONE").toLowerCase();
                    Material material = org.bukkit.Registry.MATERIAL.get(org.bukkit.NamespacedKey.minecraft(matString));
                    if (material == null) material = Material.STONE;

                    ItemBuilder builder = ItemBuilder.create(material);
                    if (displaySection.contains("display-name")) {
                        builder.name(displaySection.getString("display-name"));
                    }
                    if (displaySection.contains("lore")) {
                        builder.lore(displaySection.getStringList("lore"));
                    }

                    this.navigationItems.put(key, builder.build());
                }
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public Map<String, Integer> getNavigationSlots() {
        return navigationSlots;
    }

    public Map<String, ItemStack> getNavigationItems() {
        return navigationItems;
    }
}
