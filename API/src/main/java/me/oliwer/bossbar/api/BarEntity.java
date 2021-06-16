package me.oliwer.bossbar.api;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This abstraction layer holds the functionality behind every bossbar implementation.
 *
 * @param <T> {@link T} the entity type.
 * @param <W> {@link W} the data watcher type.
 */
public abstract class BarEntity<T, W> implements Tickable {
    /** {@link T} the entity involved in this bossbar. **/
    protected final T entity;

    /** {@link BarUpdate} the latest performed update. **/
    protected final BarUpdate latestUpdate;

    /** {@link W} this bossbar's watcher object, if any. **/
    protected W watcher;

    /** {@link Set<Player>} a collection of all players this bossbar is displayed to. **/
    protected final Set<Player> shown = Collections
        .newSetFromMap(new IdentityHashMap<>());

    /**
     * @param initial {@link T} the initial entity.
     */
    public BarEntity(T initial) {
        this.entity = initial;
        this.latestUpdate = new BarUpdate();
    }

    /**
     * Show this bar to a player.
     *
     * @param player {@link Player}
     */
    public void show(Player player) {
        this.shown.add(player);
    }

    /**
     * Hide this bar from a player.
     *
     * @param player {@link Player}
     */
    public void hide(Player player) {
        this.shown.remove(player);
    }

    /**
     * Update this boss bar's state.
     *
     * @param action {@link Consumer<BarUpdate>} the update consumer.
     */
    public abstract void update(Consumer<BarUpdate> action);

    /**
     * Create a new watcher instance.
     *
     * @return {@link W}
     */
    protected abstract W createWatcher();

    /**
     * Get whether or not this bar is shown to a player.
     *
     * @param player {@link Player} the player to check for.
     * @return {@link Boolean}
     */
    public boolean isShown(Player player) {
        return this.shown.contains(player);
    }

    /**
     * Get an immutable copy of the players that this bar is shown to.
     *
     * @return {@link ImmutableSet<Player>}
     */
    public ImmutableSet<Player> getShownTo() {
        return ImmutableSet.copyOf(this.shown);
    }
}