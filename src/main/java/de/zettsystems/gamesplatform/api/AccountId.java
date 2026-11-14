package de.zettsystems.gamesplatform.api;

/**
 * The account of a person on the platform: the user id of the identity building block
 * (zs-identity) the platform signs people in with. Games store memberships by this id; a
 * game's own player or seat ids never cross the contract.
 *
 * @param value the user id, always positive
 */
public record AccountId(long value) {

    public AccountId {
        if (value <= 0) {
            throw new IllegalArgumentException("an account id is positive, got " + value);
        }
    }

    /** Shorthand for {@code new AccountId(value)}. */
    public static AccountId of(long value) {
        return new AccountId(value);
    }
}
