package de.zettsystems.gamesplatform.api;

import java.util.Objects;

/**
 * Where one account finished in a match.
 *
 * @param account   the account
 * @param place     1 for the best; accounts that share a place share the number, and the next
 *                  place skips accordingly (1, 1, 3)
 * @param departure whether the account resigned or was eliminated before the end; it does not
 *                  change the place
 */
public record Placement(AccountId account, int place, Departure departure) {

    public Placement {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(departure, "departure");
        if (place < 1) {
            throw new IllegalArgumentException("a place starts at 1, got " + place);
        }
    }

    /** A placement of an account that stayed until the end. */
    public Placement(AccountId account, int place) {
        this(account, place, Departure.NONE);
    }

    /** Whether this account won, alone or together with others. Decided by the place alone. */
    public boolean isWin() {
        return place == 1;
    }
}
