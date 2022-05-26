package com.zpedroo.kits.objects;

import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Kit {

    private final String name;
    private final File file;
    private final String permission;
    private long cooldown;
    private long itemCooldown;
    private final ItemStack item;
    private ItemStack[] armor;
    private ItemStack[] items;
    private Boolean update;

    public Kit(String name, File file, String permission, long cooldown, long itemCooldown, ItemStack item, ItemStack[] armor, ItemStack[] items) {
        this.name = name;
        this.file = file;
        this.permission = permission;
        this.cooldown = cooldown;
        this.itemCooldown = itemCooldown;
        this.item = item;
        this.armor = armor;
        this.items = items;
        this.update = false;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public String getPermission() {
        return permission;
    }

    public long getCooldown() {
        return cooldown;
    }

    public long getItemCooldown() {
        return itemCooldown;
    }

    public ItemStack getItem(Player player) {
        NBTItem nbt = new NBTItem(item.clone());
        nbt.setString("KitName", name);

        ItemStack item = nbt.getItem();
        if (item.getItemMeta() != null) {
            String displayName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
            ItemMeta meta = item.getItemMeta();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

            if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, new String[]{
                    "{player}",
                    "{date}",
                    "{hour}"
            }, new String[]{
                    player.getName(),
                    dateFormatter.format(System.currentTimeMillis()),
                    timeFormatter.format(System.currentTimeMillis())
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[]{
                            "{player}",
                            "{date}",
                            "{hour}"
                    }, new String[]{
                            player.getName(),
                            dateFormatter.format(System.currentTimeMillis()),
                            timeFormatter.format(System.currentTimeMillis())
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public int getItemsAmount() {
        int ret = 0;
        ret += Arrays.stream(items).filter(items -> items != null && items.getType() != Material.AIR).collect(Collectors.toList()).stream().count();
        ret += Arrays.stream(armor).filter(items -> items != null && items.getType() != Material.AIR).collect(Collectors.toList()).stream().count();

        return ret;
    }

    public void setItems(ItemStack[] items) {
        this.items = items;
        this.update = true;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
        this.update = true;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
        this.update = true;
    }

    public void setItemCooldown(long itemCooldown) {
        this.itemCooldown = itemCooldown;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}