package de.zettsystems.gamesplatformapi;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The settings of the signed-in account in one game, kept by the platform so that they follow the
 * account from device to device. Meant for display and control preferences (sound, filters, the
 * speed of animations), not for match data or rules. The platform implements this interface once;
 * a game started on its own provides its own implementation.
 *
 * <p>The account is always the one signed in for the current request or UI session, never a
 * parameter. Calls outside a request or UI session, such as background threads or computer moves,
 * have no account: reading gives nothing and writing throws. Whether work that another thread hands
 * to a UI session (a push) knows the account is up to the platform; a game does not rely on it. A
 * game therefore reads its settings while it builds a view. The game decides what keys mean, which
 * values are allowed and what applies when a key has no value; it falls back to its default for a
 * value it does not understand. Deleting an account deletes its settings.
 *
 * <p>Keys are at most {@value #MAX_KEY_LENGTH} and values at most {@value #MAX_VALUE_LENGTH}
 * characters long, neither may be blank; an implementation rejects others with an
 * {@link IllegalArgumentException}.
 *
 * @since 0.4.0
 */
public interface PlatformSettings {

    /** The longest key the platform stores. */
    int MAX_KEY_LENGTH = 40;

    /** The longest value the platform stores. */
    int MAX_VALUE_LENGTH = 512;

    /**
     * The value of the key in the game for the signed-in account; empty if it has none or nobody is
     * signed in.
     *
     * @param gameKey the key of the game, as in its game descriptor
     * @param key     the game's name of the setting
     */
    Optional<String> value(String gameKey, String key);

    /**
     * The values of several keys at once; keys without a value are missing from the result, and the
     * result is empty when nobody is signed in. The default asks {@link #value} one by one; a
     * platform should answer with one lookup.
     */
    default Map<String, String> values(String gameKey, Collection<String> keys) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : keys) {
            value(gameKey, key).ifPresent(value -> values.put(key, value));
        }
        return values;
    }

    /**
     * Stores the value of the key for the signed-in account, replacing an earlier one.
     *
     * @throws IllegalStateException    if nobody is signed in
     * @throws IllegalArgumentException if the key or value is blank or too long
     */
    void set(String gameKey, String key, String value);

    /**
     * Removes the value of the key for the signed-in account, so that the game's default applies
     * again. Nothing happens if it has none.
     *
     * @throws IllegalStateException if nobody is signed in
     */
    void reset(String gameKey, String key);
}
