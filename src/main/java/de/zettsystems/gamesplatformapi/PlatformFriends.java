package de.zettsystems.gamesplatformapi;

import java.util.List;

/**
 * The friendships between accounts, kept by the platform. The platform implements this interface
 * once; a game asks it, for example to offer friends when inviting to a match. A game started on
 * its own provides its own implementation.
 *
 * <p>Friendship is mutual: once accepted, each account is a friend of the other. A block by
 * either account ends a friendship and hides the two from each other.
 */
public interface PlatformFriends {

    /** The accounts the account is friends with, in no particular order; never the account itself. */
    List<AccountId> friendsOf(AccountId account);

    /** Whether the two accounts are friends; an account is not its own friend. */
    boolean areFriends(AccountId first, AccountId second);

    /** Whether either account blocked the other. */
    boolean isBlockedBetween(AccountId first, AccountId second);
}
