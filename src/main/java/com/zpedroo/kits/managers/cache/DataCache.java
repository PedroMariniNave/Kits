package com.zpedroo.kits.managers.cache;

import com.zpedroo.kits.objects.Category;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import org.bukkit.entity.Player;

import java.util.*;

public class DataCache {

    private final Map<String, Kit> kits;
    private final Map<String, Category> categories;
    private final Map<Player, PlayerData> playerData;

    public DataCache() {
        this.kits = new HashMap<>(32);
        this.categories = new HashMap<>(2);
        this.playerData = new HashMap<>(64);
    }

    public Map<String, Kit> getKits() {
        return kits;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public Map<Player, PlayerData> getPlayerData() {
        return playerData;
    }
}