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

    /**
     * Whether the account may use a variant of the game's rules, for example to create a match
     * with it, to join or to enter one. The platform keeps a stage (alpha, beta, production) per
     * variant, next to the stage of the game; the account must be allowed into the game and into
     * the stage of the variant. A variant the platform does not know counts as alpha. Ask on every
     * use, because stages and roles change while sessions are running.
     *
     * <p>The default opens every variant: a platform without stages per variant leaves the
     * decision to whether the account may enter the game at all.
     *
     * @param account the account to ask about, not necessarily the one signed in
     * @param gameKey the key of the game, as in its game descriptor
     * @param variant the key of the variant, as the game declares it in its game descriptor
     * @since 0.5.0
     */
    default boolean mayUseVariant(AccountId account, String gameKey, String variant) {
        return true;
    }

    /**
     * The stage of a variant of the game's rules, for the game to mark a variant in alpha or beta
     * where it is chosen or shown. It says nothing about who may use the variant; ask
     * {@link #mayUseVariant} for that. A variant the platform does not know is in alpha.
     *
     * <p>The default answers {@link LifecycleStage#PRODUCTION}, matching {@link #mayUseVariant}: a
     * platform without stages per variant marks none.
     *
     * @param gameKey the key of the game, as in its game descriptor
     * @param variant the key of the variant, as the game declares it in its game descriptor
     * @since 0.5.0
     */
    default LifecycleStage variantStage(String gameKey, String variant) {
        return LifecycleStage.PRODUCTION;
    }
}
