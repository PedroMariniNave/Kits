package com.zpedroo.kits.objects;

import org.bukkit.inventory.ItemStack;

public class CategoryKit {

    private final Kit kit;
    private final ItemStack availableItem;
    private final ItemStack cooldownItem;
    private final ItemStack lockedItem;

    public CategoryKit(Kit kit, ItemStack availableItem, ItemStack cooldownItem, ItemStack lockedItem) {
        this.kit = kit;
        this.availableItem = availableItem;
        this.cooldownItem = cooldownItem;
        this.lockedItem = lockedItem;
    }

    public Kit getKit() {
        return kit;
    }

    public ItemStack getAvailableItem() {
        return availableItem.clone();
    }

    public ItemStack getCooldownItem() {
        return cooldownItem.clone();
    }

    public ItemStack getLockedItem() {
        return lockedItem.clone();
    }
}