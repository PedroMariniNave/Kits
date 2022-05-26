package com.zpedroo.kits.objects;

import com.zpedroo.kits.enums.KitStatus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final Map<Kit, Long> cooldowns;
    private Boolean update;

    public PlayerData(UUID uuid, Map<Kit, Long> cooldowns) {
        this.uuid = uuid;
        this.cooldowns = cooldowns;
        this.update = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<Kit, Long> getCooldowns() {
        return cooldowns;
    }

    public boolean isQueueUpdate() {
        return update;
    }

    public boolean isInCooldown(Kit kit) {
        if (!cooldowns.containsKey(kit)) return false;

        long expiration = cooldowns.get(kit);
        if (System.currentTimeMillis() >= expiration) cooldowns.remove(kit);

        return System.currentTimeMillis() < expiration;
    }

    public KitStatus getKitStatus(Kit kit) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        if (kit.getPermission() != null && !player.hasPermission(kit.getPermission())) return KitStatus.LOCKED;

        return isInCooldown(kit) ? KitStatus.COOLDOWN : KitStatus.AVAILABLE;
    }

    public boolean canCollect(Kit kit) {
        if (isInCooldown(kit)) return false;
        if (kit.getPermission() == null) return true;

        return Bukkit.getPlayer(uuid).hasPermission(kit.getPermission());
    }

    public long getCooldown(Kit kit) {
        return cooldowns.getOrDefault(kit, 0L);
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public void collectKit(Kit kit, boolean affectCooldown, boolean dropItems) {
        if (affectCooldown) {
            this.cooldowns.put(kit, System.currentTimeMillis() + kit.getCooldown());
            this.update = true;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        ItemStack[] kitItems = kit.getItems();
        ItemStack[] kitArmor = kit.getArmor();
        List<ItemStack> itemsToDrop = new LinkedList<>();

        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            if (i >= kitItems.length) break;

            ItemStack itemToGive = kitItems[i];
            if (itemToGive == null || itemToGive.getType().equals(Material.AIR)) continue;

            ItemStack playerItem = player.getInventory().getItem(i);
            if (playerItem == null || playerItem.getType().equals(Material.AIR)) {
                player.getInventory().setItem(i, itemToGive);
                continue;
            } else {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(itemToGive);
                    continue;
                }
            }

            itemsToDrop.add(itemToGive);
        }

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (int i = 0; i < armorContents.length; ++i) {
            if (i >= kitArmor.length) break;

            ItemStack itemToGive = kitArmor[i];
            if (itemToGive == null || itemToGive.getType().equals(Material.AIR)) continue;

            ItemStack playerItem = armorContents[i];
            if (playerItem == null || playerItem.getType().equals(Material.AIR)) {
                armorContents[i] = itemToGive;
                continue;
            }

            itemsToDrop.add(itemToGive);
        }

        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();

        if (itemsToDrop.isEmpty() || !dropItems) return;

        for (ItemStack itemToDrop : itemsToDrop) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(itemToDrop);
                continue;
            }

            player.getWorld().dropItemNaturally(player.getLocation(), itemToDrop);
        }
    }
}