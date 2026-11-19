package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.Objects;

/**
 * An account missed a turn: the deadline of a {@linkplain DecisionKind#TURN turn} passed without
 * the account having finished it. The turn ends as it stands: whatever the account ordered until
 * then counts, nothing more. The rule is the same for all games: after the first missed turn in a
 * match the match goes on with the account; the second one in the same match, whether in a row or
 * not, removes the account, and a computer opponent takes over its seat. The platform tells the
 * account, on the platform and by mail. Answers to requests of others never count as missed.
 *
 * <p>The game publishes this event once per missed turn, in addition to the {@link MatchChanged}
 * for the same change, within the transaction that settles the turn or after it is stored if the
 * game keeps its state outside a transaction. A listener handles it after the commit, or right away
 * when no transaction is active.
 *
 * @param gameKey    the key of the game, as in its game descriptor
 * @param matchId    the game's id of the match, unique within the game
 * @param account    the account that missed the turn
 * @param decisionId the id of the missed decision, as in {@link PendingDecision#decisionId()}
 * @param missedAt   when the deadline passed
 * @param missed     how many turns the account has missed in this match so far, this one included
 * @param removed    whether this missed turn removed the account from the match
 * @since 0.4.0
 */
public record TurnMissed(
        String gameKey,
        String matchId,
        AccountId account,
        String decisionId,
        Instant missedAt,
        int missed,
        boolean removed
) {

    public TurnMissed {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(account, "account");
        Checks.text(decisionId, "decisionId");
        Objects.requireNonNull(missedAt, "missedAt");
        if (missed < 1) {
            throw new IllegalArgumentException("a missed turn counts at least 1, got " + missed);
        }
    }
}
