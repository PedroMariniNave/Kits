package com.zpedroo.kits.objects;

import com.zpedroo.kits.managers.DataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class Category {

    private final String name;
    private final FileConfiguration file;
    private final List<CategoryKit> categoryKits;

    public Category(String name, FileConfiguration file, List<CategoryKit> categoryKits) {
        this.name = name;
        this.file = file;
        this.categoryKits = categoryKits;
    }

    public String getName() {
        return name;
    }

    public FileConfiguration getFile() {
        return file;
    }

    public List<CategoryKit> getCategoryKits() {
        return categoryKits;
    }

    public int getPlayerKitsAmount(Player player) {
        int ret = 0;
        for (CategoryKit categoryKit : categoryKits) {
            Kit kit = categoryKit.getKit();
            if (kit.getPermission() != null && !player.hasPermission(kit.getPermission())) continue;

            ++ret;
        }

        return ret;
    }

    public int getAvailableKitsAmount(Player player) {
        int ret = 0;

        PlayerData data = DataManager.getInstance().loadPlayerData(player);
        if (data == null) return 0;

        for (CategoryKit categoryKit : categoryKits) {
            Kit kit = categoryKit.getKit();
            if (!data.canCollect(kit)) continue;
            if (data.isInCooldown(kit)) continue;

            ++ret;
        }

        return ret;
    }
}