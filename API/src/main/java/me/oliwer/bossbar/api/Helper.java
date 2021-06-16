package me.oliwer.bossbar.api;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * This class contains utility functionality for bossbars.
 */
class Helper {
    /**
     * Remove a player from their displayed bossbar asynchronously with an operation performed afterwards.
     *
     * @param controller {@link BarController<K>} the controller involved in this removal.
     * @param player     {@link Player} the player to expose of bossbar.
     * @param then       {@link Consumer<Player>} operation to perform after removal completion.
     * @param <K>        {@link K} the controller key type.
     */
    static <K> void oldAsyncRemovalThen(BarController<K> controller, Player player, Consumer<Player> then) {
        controller.lookupAsync(($, bar) -> bar.isShown(player)).thenAccept(it -> {
            it.ifPresent(entry -> {
                final BarEntity<?, ?> bar = entry.getValue();
                bar.hide(player);

                if (bar.shown.size() == 0 && controller.removeEntryIfNoPlayers) {
                    controller.cache.remove(entry.getKey());
                }
            });
            then.accept(player);
        });
    }

    /** No need for instantiation. **/
    private Helper() {
        throw new IllegalStateException("Helper is not to be instantiated.");
    }
}