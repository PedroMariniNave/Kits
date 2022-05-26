package com.zpedroo.kits.utils.menu;

import com.zpedroo.kits.enums.KitStatus;
import com.zpedroo.kits.managers.DataManager;
import com.zpedroo.kits.objects.Category;
import com.zpedroo.kits.objects.CategoryKit;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import com.zpedroo.kits.utils.FileUtils;
import com.zpedroo.kits.utils.builder.InventoryBuilder;
import com.zpedroo.kits.utils.builder.InventoryUtils;
import com.zpedroo.kits.utils.builder.ItemBuilder;
import com.zpedroo.kits.utils.formatter.TimeFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private final ItemStack nextPageItem;
    private final ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            if (str == null) continue;

            Category categoryToGetInfo = DataManager.getInstance().getCache().getCategories().get(str);

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{category_kits}",
                    "{player_kits}",
                    "{available_kits}"
            }, new String[]{
                    categoryToGetInfo == null ? "-/-" : String.valueOf(categoryToGetInfo.getCategoryKits().size()),
                    categoryToGetInfo == null ? "-/-" : String.valueOf(categoryToGetInfo.getPlayerKitsAmount(player)),
                    categoryToGetInfo == null ? "-/-" : String.valueOf(categoryToGetInfo.getAvailableKitsAmount(player))
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addDefaultItem(item, slot, () -> {
                if (StringUtils.contains(action, ":")) {
                    String[] split = action.split(":");
                    String command = split.length > 1 ? split[1] : null;
                    if (command == null) return;

                    switch (split[0].toUpperCase()) {
                        case "CATEGORY":
                            Category categoryToOpen = DataManager.getInstance().getCache().getCategories().get(split[1]);

                            if (categoryToOpen != null) {
                                openCategoryMenu(player, categoryToOpen);
                                player.playSound(player.getLocation(), Sound.CLICK, 2f, 2f);
                            }
                            break;
                        case "PLAYER":
                            player.chat("/" + command);
                            break;
                        case "CONSOLE":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                    "{player}"
                            }, new String[]{
                                    player.getName()
                            }));
                            break;
                    }
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openCategoryMenu(Player player, Category category) {
        FileConfiguration file = category.getFile();

        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title"));
        int size = file.getInt("Inventory.size");

        int nextPageSlot = file.getInt("Inventory.next-page-slot");
        int previousPageSlot = file.getInt("Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        PlayerData data = DataManager.getInstance().loadPlayerData(player);

        int i = -1;
        String[] slots = file.getString("Inventory.item-slots").replace(" ", "").split(",");
        for (CategoryKit categoryKit : category.getCategoryKits()) {
            Kit kit = categoryKit.getKit();
            ItemStack item = null;

            KitStatus kitStatus = data.getKitStatus(kit);

            switch (kitStatus) {
                case LOCKED:
                    item = categoryKit.getLockedItem();
                    break;
                case AVAILABLE:
                    item = categoryKit.getAvailableItem();
                    break;
                case COOLDOWN:
                    item = categoryKit.getCooldownItem();
                    break;
            }

            if (item == null) continue;
            if (item.getItemMeta() != null) {
                List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
                ItemMeta meta = item.getItemMeta();

                if (lore != null) {
                    List<String> newLore = new ArrayList<>(lore.size());

                    for (String str : lore) {
                        newLore.add(StringUtils.replaceEach(str, new String[] {
                                "{items_amount}",
                                "{status}",
                                "{cooldown}",
                                "{cooldown_remaining}"
                        }, new String[] {
                                String.valueOf(kit.getItemsAmount()),
                                kitStatus.getTranslation(),
                                TimeFormatter.getInstance().format(kit.getCooldown()),
                                data.isInCooldown(kit) ? TimeFormatter.getInstance().format(data.getCooldown(kit) - System.currentTimeMillis())
                                        : "-/-"
                        }));
                    }

                    meta.setLore(newLore);
                }

                item.setItemMeta(meta);
            }

            int slot = Integer.parseInt(slots[++i]);

            inventory.addItem(item, slot, () -> {
                if (!data.canCollect(kit)) return;

                data.collectKit(kit, true, true);
                openCategoryMenu(player, category);
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.2f, 10f);
            }, ActionType.LEFT_CLICK);

            inventory.addAction(slot, () -> {
                openKitPreviewMenu(player, kit, category);
                player.playSound(player.getLocation(), Sound.CLICK, 2f, 2f);
            }, ActionType.RIGHT_CLICK);
        }

        ItemStack backItem = ItemBuilder.build(file, "Back").build();
        int backSlot = file.getInt("Back.slot", 0);

        inventory.addDefaultItem(backItem, backSlot, () -> {
            openMainMenu(player);
            player.playSound(player.getLocation(), Sound.CLICK, 2f, 2f);
        }, ActionType.ALL_CLICKS);

        inventory.open(player);
    }

    public void openKitPreviewMenu(Player player, Kit kit, Category kitCategory) {
        FileUtils.Files file = FileUtils.Files.PREVIEW;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        List<ItemStack> items = new ArrayList<>(Arrays.asList(kit.getArmor()));
        Collections.reverse(items); // fix armor order
        items.addAll(Arrays.asList(kit.getItems()));

        if (items.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Nothing").build();
            int slot = FileUtils.get().getInt(file, "Nothing.slot");

            inventory.addItem(item, slot);
        } else {
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;

            for (ItemStack item : items) {
                if (item == null || item.getType().equals(Material.AIR)) continue;
                if (++i >= slots.length) i = 0;

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot);
            }
        }

        ItemStack backItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Back").build();
        int backSlot = FileUtils.get().getInt(file, "Back.slot");

        inventory.addDefaultItem(backItem, backSlot, () -> {
            openCategoryMenu(player, kitCategory);
            player.playSound(player.getLocation(), Sound.CLICK, 2f, 2f);
        }, ActionType.ALL_CLICKS);

        inventory.open(player);
    }
}