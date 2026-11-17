package de.zettsystems.gamesplatformapi;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The accounts of the platform as a game sees them. The platform implements this interface once
 * and is the only place that knows how people sign in; a game asks it who is signed in, what a
 * person is called and who may see the game's parts in testing. A game started on its own provides
 * its own implementation, for example a single default account without sign-in.
 *
 * <p>A deleted account is announced by {@link AccountRemoved}; afterwards the platform no longer
 * knows it.
 */
public interface PlatformAccounts {

    /**
     * The account signed in for the current request or UI session; empty when nobody is signed in.
     * Asked again on every call, never cached by the platform across sign-in or sign-out.
     */
    Optional<AccountId> current();

    /** The public display name of the account; empty if the platform knows no such account. */
    Optional<String> displayName(AccountId account);

    /**
     * The public display names of several accounts at once, for lists. Accounts the platform does
     * not know are missing from the result. The default asks {@link #displayName} one by one; a
     * platform should answer with one lookup.
     */
    default Map<AccountId, String> displayNames(Collection<AccountId> accounts) {
        Map<AccountId, String> names = new LinkedHashMap<>();
        for (AccountId account : accounts) {
            displayName(account).ifPresent(name -> names.put(account, name));
        }
        return names;
    }

    /**
     * Whether the account may use the parts of the game that are still in testing: an enabled
     * account that is a system administrator or a tester of exactly this game. A tester of another
     * game is not; unknown and disabled accounts are not. Roles are read fresh on every call, so a
     * change applies to sessions that are already running.
     *
     * @param account the account to ask about, not necessarily the one signed in
     * @param gameKey the key of the game, as in its game descriptor
     */
    boolean isTester(AccountId account, String gameKey);
}
