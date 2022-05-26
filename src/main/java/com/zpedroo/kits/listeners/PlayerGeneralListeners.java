package com.zpedroo.kits.listeners;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.zpedroo.kits.managers.DataManager;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import com.zpedroo.kits.utils.config.Messages;
import com.zpedroo.kits.utils.cooldown.Cooldown;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        DataManager.getInstance().savePlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack item = event.getItem().clone();
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("KitName")) return;

        event.setCancelled(true);

        Kit kit = DataManager.getInstance().getCache().getKits().get(nbt.getString("KitName"));
        if (kit == null) return;

        Player player = event.getPlayer();
        if (Cooldown.isInCooldown(player, kit)) {
            player.sendMessage(StringUtils.replaceEach(Messages.KIT_ITEM_COOLDOWN, new String[]{
                    "{cooldown}"
            }, new String[]{
                    Cooldown.getTimeLeft(player, kit)
            }));
            return;
        }

        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testState(player.getLocation(), player, DefaultFlag.PVP)) {
            player.sendMessage(Messages.PVP_AREA);
            return;
        }

        PlayerData data = DataManager.getInstance().loadPlayerData(player);
        if (data == null) return;

        if (kit.getItemCooldown() > 0) Cooldown.addCooldown(player, kit, TimeUnit.SECONDS.toMillis(kit.getItemCooldown()));

        item.setAmount(1);
        player.getInventory().removeItem(item);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 10f);

        data.collectKit(kit, false, true);
    }
}