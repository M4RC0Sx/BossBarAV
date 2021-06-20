package me.oliwer.bossbarav.impl;

import me.oliwer.bossbar.api.BarEntity;
import me.oliwer.bossbar.api.BarUpdate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public final class BossBarNewer extends BarEntity<BossBar, Object> {
    public BossBarNewer(float healthPercentage, String text) {
        super(Bukkit.createBossBar(text, BarColor.PURPLE, BarStyle.SOLID));
        this.entity.setProgress(healthPercentage * 0.01);
    }

    @Override
    public void show(Player player) {
        // make sure player is not already seeing this bar
        if (this.isShown(player)) {
            return;
        }

        // add player
        this.entity.addPlayer(player);

        // handle show functionality at last
        super.show(player);
    }

    @Override
    public void hide(Player player) {
        // make sure the player is not already hidden
        if (!this.isShown(player)) {
            return;
        }

        // remove player
        this.entity.removePlayer(player);

        // handle hide functionality at last
        super.hide(player);
    }

    @Override
    public void update(Consumer<BarUpdate> action) {
        // create new update and consume the modification(s)
        action.accept(latestUpdate);

        // update
        this.entity.setTitle(latestUpdate.text);
        this.entity.setProgress(latestUpdate.healthPercentage * 0.01);
    }

    @Override
    protected Object createWatcher() {
        return null;
    }

    @Override
    public void tick() {}

    public static BarEntity<?, ?> create(World world, float healthPercentage, String text) {
        return new BossBarNewer(healthPercentage, text);
    }
}