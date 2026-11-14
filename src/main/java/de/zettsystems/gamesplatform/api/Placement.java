package de.zettsystems.gamesplatform.api;

import java.util.Objects;

/**
 * Where one account finished in a match.
 *
 * @param account the account
 * @param place   1 for the best; accounts that share a place share the number, and the next place
 *                skips accordingly (1, 1, 3)
 */
public record Placement(AccountId account, int place) {

    public Placement {
        Objects.requireNonNull(account, "account");
        if (place < 1) {
            throw new IllegalArgumentException("a place starts at 1, got " + place);
        }
    }

    /** Whether this account won, alone or together with others. */
    public boolean isWin() {
        return place == 1;
    }
}
