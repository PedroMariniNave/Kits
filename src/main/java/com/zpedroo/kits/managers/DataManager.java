package com.zpedroo.kits.managers;

import com.zpedroo.kits.Kits;
import com.zpedroo.kits.managers.cache.DataCache;
import com.zpedroo.kits.mysql.DBConnection;
import com.zpedroo.kits.objects.Category;
import com.zpedroo.kits.objects.CategoryKit;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import com.zpedroo.kits.utils.builder.ItemBuilder;
import com.zpedroo.kits.utils.serialization.BukkitSerialization;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private final DataCache dataCache;

    public DataManager() {
        instance = this;
        this.dataCache = new DataCache();
        this.loadKits();
        this.loadCategories();
    }

    public PlayerData loadPlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().loadData(player);
            dataCache.getPlayerData().put(player, data);
        }

        return data;
    }

    public void savePlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null) return;
        if (!data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().saveData(data);
        data.setUpdate(false);
    }

    public void saveKitData(Kit kit) {
        if (kit == null) return;
        if (!kit.isQueueUpdate()) return;

        File file = kit.getFile();
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        try {
            fileConfig.set("Kit-Settings.permission", kit.getPermission());
            fileConfig.set("Kit-Settings.cooldown", kit.getCooldown());
            fileConfig.set("Kit-Settings.item.cooldown", kit.getItemCooldown());
            fileConfig.set("Kit-Settings.armor", BukkitSerialization.itemStackArrayToBase64(kit.getArmor()));
            fileConfig.set("Kit-Settings.items", BukkitSerialization.itemStackArrayToBase64(kit.getItems()));
            fileConfig.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        kit.setUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getPlayerData().keySet()).forEach(this::savePlayerData);
        new HashSet<>(dataCache.getKits().values()).forEach(this::saveKitData);
    }

    private void loadKits() {
        File folder = new File(Kits.get().getDataFolder(), "/kits");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String permission = file.getString("Kit-Settings.permission");
            long cooldown = file.getLong("Kit-Settings.cooldown", 0);
            long itemCooldown = file.getLong("Kit-Settings.item.cooldown", 0);
            ItemStack item = null;
            if (file.contains("Kit-Settings.item.type")) {
                item = ItemBuilder.build(file, "Kit-Settings.item").build();
            }

            ItemStack[] armor = null;
            ItemStack[] items = null;

            try {
                armor = BukkitSerialization.itemStackArrayFromBase64(file.getString("Kit-Settings.armor"));
                items = BukkitSerialization.itemStackArrayFromBase64(file.getString("Kit-Settings.items"));
            } catch (Exception ex) {
                // ignore
            }

            Kit kit = new Kit(name, fl, permission, cooldown, itemCooldown, item, armor, items);
            dataCache.getKits().put(name, kit);
        }
    }

    private void loadCategories() {
        File folder = new File(Kits.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            List<CategoryKit> categoryKits = new LinkedList<>();

            for (String kitName : file.getConfigurationSection("Inventory.items").getKeys(false)) {
                Kit kit = dataCache.getKits().get(kitName);
                if (kit == null) continue;

                ItemStack availableItem = ItemBuilder.build(file, "Inventory.items." + kitName + ".available").build();
                ItemStack cooldownItem = ItemBuilder.build(file, "Inventory.items." + kitName + ".cooldown").build();
                ItemStack lockedItem = ItemBuilder.build(file, "Inventory.items." + kitName + ".locked").build();

                categoryKits.add(new CategoryKit(kit, availableItem, cooldownItem, lockedItem));
            }

            Category category = new Category(name, file, categoryKits);
            dataCache.getCategories().put(name, category);
        }
    }

    public DataCache getCache() {
        return dataCache;
    }
}