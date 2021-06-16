package me.oliwer.bossbar.api;

import com.google.common.collect.ImmutableMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * This class is a basic controller for bossbars.
 *
 * @param <K> {@link K} the type of key to be used for the cache.
 */
public class BarController<K> implements Listener {
    /** {@link Map} this controller's cache of bossbars. **/
    protected final Map<K, BarEntity<?, ?>> cache;

    /**
     * {@link Boolean} whether or not an entry should be removed from the
     * cache when there are NO players left to display bossbar to.
     */
    protected final boolean removeEntryIfNoPlayers;

    /**
     * @param initial                {@link Map} the initial map for our cache.
     * @param removeEntryIfNoPlayers {@link Boolean} whether or not an entry should be removed from the
     *                               cache when there are NO players left to display bossbar to.
     */
    public BarController(Map<K, BarEntity<?, ?>> initial, boolean removeEntryIfNoPlayers) {
        this.cache = new ConcurrentHashMap<>(initial);
        this.removeEntryIfNoPlayers = removeEntryIfNoPlayers;
    }

    /**
     * This event handles the disconnection of players.
     *
     * @param event {@link PlayerQuitEvent}
     */
    @EventHandler private void on(PlayerQuitEvent event) {
        Helper.oldAsyncRemovalThen(this, event.getPlayer(), $ -> {});
    }

    /**
     * Display a specific bossbar to a player if present.
     *
     * @param player {@link Player} the player this bossbar should be displayed for.
     * @param key    {@link K} the key of the corresponding bossbar.
     */
    public void display(Player player, K key) {
        Helper.oldAsyncRemovalThen(this, player, $ -> lookupByKey(key)
            .ifPresent(bar -> bar.show(player)));
    }

    /**
     * Expose a specific bossbar from a player by it's key.
     *
     * @param player {@link Player} the player to expose bossbar from.
     * @param key    {@link K} the key of the corresponding bossbar.
     */
    public void expose(Player player, K key) {
        this.lookupByKey(key).ifPresent(it -> {
            it.hide(player);

            if (it.shown.size() > 0 || !removeEntryIfNoPlayers) {
                return;
            }

            cache.values().remove(it);
        });
    }

    /**
     * Look for a bossbar associated with passed down key, if absent, insert.
     *
     * @param key     {@link K} the key to look for / associate with.
     * @param bossbar {@link BarEntity} the bossbar to be the value inserted if there was none found.
     * @return {@link BarEntity}
     */
    public BarEntity<?, ?> lookupOrInsert(K key, BarEntity<?, ?> bossbar) {
        final BarEntity<?, ?> bar = this.cache.putIfAbsent(key, bossbar);
        return bar == null ? bossbar : bar;
    }

    /**
     * Lookup a bossbar by it's key.
     *
     * @param key {@link K} the key to search for.
     * @return {@link Optional<BarEntity>}
     */
    public Optional<BarEntity<?, ?>> lookupByKey(K key) {
        return Optional.ofNullable(this.cache.get(key));
    }

    /**
     * Lookup a bossbar by passed down predicate.
     *
     * @param predicate {@link BiPredicate} the predicate to test on all entries.
     * @return {@link Optional<java.util.Map.Entry>}
     */
    public Optional<Map.Entry<K, BarEntity<?, ?>>> lookup(BiPredicate<K, BarEntity<?, ?>> predicate) {
        final Set<Map.Entry<K, BarEntity<?, ?>>> entries = this.cache.entrySet();

        for (final Map.Entry<K, BarEntity<?, ?>> entry : entries) {
            final K key = entry.getKey();
            final BarEntity<?, ?> value = entry.getValue();

            if (!predicate.test(key, value)) {
                continue;
            }

            return Optional.of(entry);
        }

        return Optional.empty();
    }

    /**
     * Perform a lookup for a specific bossbar asynchronously by passed down predicate.
     *
     * @param predicate {@link BiPredicate} the predicate to test all entries with.
     * @return {@link CompletableFuture<Optional>}
     */
    public CompletableFuture<Optional<Map.Entry<K, BarEntity<?, ?>>>> lookupAsync(BiPredicate<K, BarEntity<?, ?>> predicate) {
        return supplyAsync(() -> this.lookup(predicate));
    }

    /**
     * Get an immutable copy of this controller's current cache.
     *
     * @return {@link ImmutableMap}
     */
    public final ImmutableMap<K, BarEntity<?, ?>> getCache() {
        return ImmutableMap.copyOf(this.cache);
    }
}