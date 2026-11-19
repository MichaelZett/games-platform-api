package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.Objects;

/**
 * An account was invited to a match. The platform tells the invited account, on the platform and,
 * if the account wishes, by mail. The event carries everything the notice needs, because the
 * invitation may already be accepted or withdrawn by the time a listener could ask the game; the
 * open invitations themselves stay with {@link MatchDirectory#invitationsOf(AccountId)}.
 *
 * <p>The game publishes this event once per invitation, in addition to the {@link MatchChanged} for
 * the same change, within the transaction that stores the invitation or after it is stored if the
 * game keeps its state outside a transaction. A listener handles it after the commit, or right away
 * when no transaction is active.
 *
 * @param gameKey   the key of the game, as in its game descriptor
 * @param matchId   the game's id of the match, unique within the game
 * @param invited   the invited account
 * @param invitedBy the account that sent the invitation, not the invited one
 * @param sentAt    when the invitation was sent
 * @since 0.4.0
 */
public record InvitationSent(String gameKey, String matchId, AccountId invited, AccountId invitedBy, Instant sentAt) {

    public InvitationSent {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(invited, "invited");
        Objects.requireNonNull(invitedBy, "invitedBy");
        Objects.requireNonNull(sentAt, "sentAt");
        if (invited.equals(invitedBy)) {
            throw new IllegalArgumentException("an account cannot invite itself: " + invited);
        }
    }
}
