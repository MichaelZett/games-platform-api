package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A match that has ended, as the platform lists it among the finished matches of an account. The
 * game keeps the match; the entry exists as long as the match can still be viewed behind its
 * {@link #viewPath() view path}, and disappears together with it.
 *
 * @param gameKey  the key of the game, as in its game descriptor
 * @param matchId  the game's id of the match, unique within the game
 * @param title    the name of the match as the game shows it
 * @param endedAt  when the match ended; stored by the game once and never changed afterwards
 * @param result   the ranking of the participants, or that the match was aborted
 * @param tutorial whether the match was a tutorial
 * @param viewPath the path of the match's review inside the platform, below {@code /<gameKey>/}
 * @since 0.4.0
 */
public record FinishedMatch(
        String gameKey,
        String matchId,
        String title,
        Instant endedAt,
        MatchResult result,
        boolean tutorial,
        String viewPath
) {

    public FinishedMatch {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Checks.text(title, "title");
        Objects.requireNonNull(endedAt, "endedAt");
        Objects.requireNonNull(result, "result");
        Checks.viewPath(viewPath, gameKey);
    }

    /** Whether the match ended without a result. */
    public boolean isAborted() {
        return result instanceof MatchResult.Aborted;
    }

    /** Where the account finished, or empty if it did not take part or the match was aborted. */
    public Optional<Placement> placementOf(AccountId account) {
        return result.placementOf(account);
    }
}
