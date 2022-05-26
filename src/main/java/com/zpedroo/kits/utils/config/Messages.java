package com.zpedroo.kits.utils.config;

import com.zpedroo.kits.utils.FileUtils;
import net.md_5.bungee.api.ChatColor;

public class Messages {

    public static final String AVAILABLE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.available"));

    public static final String LOCKED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.locked"));

    public static final String COOLDOWN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.cooldown"));

    public static final String PVP_AREA = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.pvp-area"));

    public static final String KIT_ITEM_COOLDOWN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.kit-item-cooldown"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}