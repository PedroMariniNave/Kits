package com.zpedroo.kits.commands;

import com.zpedroo.kits.managers.DataManager;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import com.zpedroo.kits.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class KitsCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length > 0) {
            Player target = null;
            Kit kit = null;
            switch (args[0].toUpperCase()) {
                case "GIVE":
                    if (!sender.hasPermission("kits.admin")) break;
                    if (args.length < 3) break;

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) break;

                    kit = DataManager.getInstance().getCache().getKits().get(args[2]);
                    if (kit == null) break;

                    PlayerData data = DataManager.getInstance().loadPlayerData(target);
                    if (data == null) return true;
                    
                    data.collectKit(kit, false, false);
                    return true;
                case "ITEM":
                    if (!sender.hasPermission("kits.admin")) break;
                    if (args.length < 4) break;

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) break;

                    kit = DataManager.getInstance().getCache().getKits().get(args[2]);
                    if (kit == null) break;

                    int amount = 0;
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (Exception ex) {
                        // ignore
                    }

                    if (amount <= 0) break;

                    ItemStack item = kit.getItem(target);
                    item.setAmount(amount);

                    if (target.getInventory().firstEmpty() != -1) {
                        target.getInventory().addItem(item);
                        return true;
                    }

                    target.getWorld().dropItemNaturally(target.getLocation(), item);
                    return true;
                case "ITEMS":
                    if (player == null) break;
                    if (!player.hasPermission("kits.admin")) break;
                    if (args.length < 2) break;

                    kit = DataManager.getInstance().getCache().getKits().get(args[1]);
                    if (kit == null) break;

                    kit.setItems(player.getInventory().getContents());
                    kit.setArmor(player.getInventory().getArmorContents());

                    player.getInventory().clear();
                    player.getInventory().setArmorContents(new ItemStack[4]);
                    return true;
                case "COOLDOWN":
                    if (player == null) break;
                    if (!player.hasPermission("kits.admin")) break;
                    if (args.length < 3) break;

                    kit = DataManager.getInstance().getCache().getKits().get(args[1]);
                    if (kit == null) break;

                    long cooldown = 0;

                    try {
                        cooldown = TimeUnit.MINUTES.toMillis(Long.parseLong(args[2]));
                    } catch (Exception ex) {
                        // ignore
                    }

                    if (cooldown <= 0) break;

                    kit.setCooldown(cooldown);
                    return true;
                case "ITEMCOOLDOWN":
                    if (player == null) break;
                    if (!player.hasPermission("kits.admin")) break;
                    if (args.length < 3) break;

                    kit = DataManager.getInstance().getCache().getKits().get(args[1]);
                    if (kit == null) break;

                    cooldown = 0;

                    try {
                        cooldown = TimeUnit.SECONDS.toMillis(Long.parseLong(args[2]));
                    } catch (Exception ex) {
                        // ignore
                    }

                    if (cooldown <= 0) break;

                    kit.setItemCooldown(cooldown);
                    return true;
            }
        }

        if (player == null) return true;

        Menus.getInstance().openMainMenu(player);
        return false;
    }
}