package com.yourplugin.quests.gui;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = this.itemStack.getItemMeta();
    }

    public static ItemBuilder create(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        if (this.meta != null && name != null) {
            this.meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (this.meta != null && lore != null) {
            this.meta.lore(lore.stream()
                    .map(LegacyComponentSerializer.legacyAmpersand()::deserialize)
                    .collect(Collectors.toList()));
        }
        return this;
    }

    public ItemBuilder flags(List<String> flags) {
        if (this.meta != null && flags != null) {
            for (String flag : flags) {
                try {
                    this.meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return this;
    }

    public ItemBuilder customModelData(Integer data) {
        if (this.meta != null) {
            this.meta.setCustomModelData(data);
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.itemStack.setItemMeta(this.meta);
        }
        return this.itemStack;
    }
}
