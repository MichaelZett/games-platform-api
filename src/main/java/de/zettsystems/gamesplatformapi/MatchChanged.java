package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.Objects;

/**
 * Something about a match changed that its {@link MatchSummary} shows: a player joined or left,
 * the match started, the turn passed, the title changed. The game publishes this event within
 * the transaction that makes the change; a listener that refreshes a view listens after the
 * commit and asks the game's {@link MatchDirectory} for the current state.
 *
 * <p>The event deliberately carries no summary: what a viewer sees depends on the viewer (which
 * open matches they may join), so the platform asks again for each viewer anyway, and a summary
 * taken inside the transaction could be stale by the time a listener reads it. The end of a match
 * is announced by {@link MatchEnded} instead.
 *
 * @param gameKey   the key of the game, as in its game descriptor
 * @param matchId   the game's id of the match, unique within the game
 * @param changedAt when the match changed
 */
public record MatchChanged(String gameKey, String matchId, Instant changedAt) {

    public MatchChanged {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(changedAt, "changedAt");
    }
}
