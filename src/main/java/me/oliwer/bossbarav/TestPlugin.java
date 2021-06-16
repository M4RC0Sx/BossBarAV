package me.oliwer.bossbarav;

import me.oliwer.bossbar.api.BarController;
import me.oliwer.bossbar.api.Tickable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class TestPlugin extends JavaPlugin implements Listener {
    private final BarController<Chunk> chunkController = new BarController<>(new HashMap<>(), true);

    @Override
    public void onEnable() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(chunkController, this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
            chunkController.getCache().values().forEach(Tickable::tick),
            0, 1
        );
    }

    @EventHandler
    private void on(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Chunk chunk = player.getLocation().getChunk();
        chunkController.lookupOrInsert(chunk, BossBarWorker.create(chunk.getWorld(), 100f, "Test"));
        chunkController.display(player, chunk);
    }
}