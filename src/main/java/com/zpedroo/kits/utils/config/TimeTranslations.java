package com.zpedroo.kits.utils.config;

import com.zpedroo.kits.utils.FileUtils;
import net.md_5.bungee.api.ChatColor;

public class TimeTranslations {

    public static final String SECOND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.second"));

    public static final String SECONDS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.seconds"));

    public static final String MINUTE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minute"));

    public static final String MINUTES = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minutes"));

    public static final String HOUR = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hour"));

    public static final String HOURS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hours"));

    public static final String DAY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.day"));

    public static final String DAYS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.days"));

    public static final String NOW = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.now"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}