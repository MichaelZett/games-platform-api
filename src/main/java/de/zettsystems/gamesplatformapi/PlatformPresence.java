package de.zettsystems.gamesplatformapi;

import java.util.List;

/**
 * Who is online on the platform and who may see whom. The platform implements this interface once
 * and counts an account as online while it has a page of the platform or of any game open; a game
 * asks it, for example to offer people to invite. A game started on its own provides its own
 * implementation.
 *
 * <p>Every answer is given for a viewer: each account chooses whom it shows itself to (everyone,
 * friends only or nobody), both accounts must allow it, and a block hides them from each other.
 * An account always sees itself.
 */
public interface PlatformPresence {

    /** Whether the viewer may see the other account at all, online or not. */
    boolean canSee(AccountId viewer, AccountId other);

    /** Whether the other account is online and the viewer may see it. */
    boolean isOnlineFor(AccountId viewer, AccountId other);

    /** The online accounts the viewer may see, in no particular order, without the viewer. */
    List<AccountId> onlineFor(AccountId viewer);
}
