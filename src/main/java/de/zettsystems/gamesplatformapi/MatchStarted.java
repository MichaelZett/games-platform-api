package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A match has started: it left the waiting room and is running now. The platform tells the
 * participants. The event carries everything the notice needs, because by the time a listener could
 * ask the game, the match may have moved on.
 *
 * <p>The game publishes this event once per match, in addition to the {@link MatchChanged} for the
 * same change, within the transaction that starts the match or after the start is stored if the
 * game keeps its state outside a transaction. A listener handles it after the commit, or right away
 * when no transaction is active.
 *
 * @param gameKey      the key of the game, as in its game descriptor
 * @param matchId      the game's id of the match, unique within the game
 * @param startedAt    when the match started
 * @param participants the accounts taking part, at least one, no duplicates
 * @since 0.4.0
 */
public record MatchStarted(String gameKey, String matchId, Instant startedAt, List<AccountId> participants) {

    public MatchStarted {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(startedAt, "startedAt");
        participants = Checks.distinct(participants, "participants");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("a started match has at least one participant");
        }
    }
}
