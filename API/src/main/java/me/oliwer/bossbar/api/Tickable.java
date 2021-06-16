package me.oliwer.bossbar.api;

/**
 * This interface is used to introduce ticking functionality.
 */
public interface Tickable {
    /**
     * Function to be called every tick.
     */
    void tick();
}