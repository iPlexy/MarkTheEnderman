package de.iplexy.marktheenderman;

import de.iplexy.marktheenderman.listeners.NPCRightClick;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MarkTheEnderman extends JavaPlugin {

    public static final String MARK_NAME = "<gradient:#E2B0FF:#9F44D3>Mark The Enderman";
    @Getter(AccessLevel.PUBLIC)
    private static MarkTheEnderman plugin;

    @Getter(AccessLevel.PUBLIC)
    private static List<OfflinePlayer> dailyBoniPlayers = new ArrayList<>();

    @Getter(AccessLevel.PUBLIC)
    private static List<OfflinePlayer> monthlyBoniPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        registerListeners();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new NPCRightClick(), this);
    }
}
