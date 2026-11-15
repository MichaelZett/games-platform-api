package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A match has ended, with a ranking or aborted. The game publishes this event once per match,
 * within the transaction that ends it, or after the end is stored if the game keeps its state
 * outside a transaction. A listener therefore handles it after the commit, or right away when no
 * transaction is active (in Spring a {@code @TransactionalEventListener} with
 * {@code fallbackExecution = true}). The game does not publish a {@link MatchChanged} for the end
 * in addition, and it does not announce a second end if it lets the match go on afterwards.
 *
 * @param gameKey the key of the game, as in its game descriptor
 * @param matchId the game's id of the match, unique within the game
 * @param endedAt when the match ended
 * @param result  the ranking of the participants, or that the match was aborted
 */
public record MatchEnded(String gameKey, String matchId, Instant endedAt, MatchResult result) {

    public MatchEnded {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(endedAt, "endedAt");
        Objects.requireNonNull(result, "result");
    }

    /** Whether the match ended without a result. */
    public boolean isAborted() {
        return result instanceof MatchResult.Aborted;
    }

    /** The accounts that took part; for a ranked result best first. */
    public List<AccountId> participants() {
        return result.participants();
    }

    /** The accounts on place 1; more than one if they tied, none if the match was aborted or a computer opponent won. */
    public List<AccountId> winners() {
        return result.winners();
    }

    /** The place of the account, or empty if it did not take part or the match was aborted. */
    public OptionalInt placeOf(AccountId account) {
        return result.placeOf(account);
    }
}
