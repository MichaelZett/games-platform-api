package de.zettsystems.gamesplatformapi;

import java.util.Objects;

/**
 * An account was deleted on the platform. The platform publishes this event synchronously within
 * the transaction that deletes the account. Every game removes or releases what belongs to the
 * account in that same transaction (in Spring an {@code @EventListener} with
 * {@code @Transactional(propagation = MANDATORY)}); if a game fails, the whole deletion is rolled
 * back, so no game keeps data of an account the platform no longer knows. Work outside the
 * database, such as clearing a cache, waits for the commit.
 *
 * @param account the deleted account
 */
public record AccountRemoved(AccountId account) {

    public AccountRemoved {
        Objects.requireNonNull(account, "account");
    }
}
