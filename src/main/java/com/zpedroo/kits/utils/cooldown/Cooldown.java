package com.zpedroo.kits.utils.cooldown;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.zpedroo.kits.utils.formatter.TimeFormatter;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Cooldown {

    private static final Table<UUID, Object, Long> cooldowns = HashBasedTable.create();

    public static void addCooldown(Player player, Object object, long cooldownInMillis) {
        if (cooldownInMillis <= 0) return;

        cooldowns.put(player.getUniqueId(), object, System.currentTimeMillis() + cooldownInMillis);
    }

    public static boolean isInCooldown(Player player, Object object) {
        Long cooldown = cooldowns.row(player.getUniqueId()).getOrDefault(object, null);
        if (cooldown != null && System.currentTimeMillis() >= cooldown) {
            cooldowns.row(player.getUniqueId()).remove(object);
            return false;
        }

        return cooldown != null;
    }

    public static long getCooldown(Player player, Object object) {
        if (!isInCooldown(player, object)) return 0L;

        return cooldowns.row(player.getUniqueId()).get(object);
    }

    public static String getTimeLeft(Player player, Object object) {
        return TimeFormatter.getInstance().format(getCooldown(player, object) - System.currentTimeMillis());
    }
}