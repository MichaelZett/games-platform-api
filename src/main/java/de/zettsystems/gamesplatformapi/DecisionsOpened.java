package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A running match now waits for decisions: a new turn, a new round for several accounts at once, or
 * answers to a request. The platform creates a notice for each decision and plans the reminder
 * before its deadline. The event carries everything the notice needs, because the decision may
 * already be made by the time a listener could ask the game.
 *
 * <p>The game publishes this event, in addition to the {@link MatchChanged} for the same change,
 * when decisions open, and again for a decision whose deadline became earlier or that got its
 * deadline back after waiting for answers of others. The platform keeps one notice per account,
 * match, decision id and kind, so publishing a decision again does not notify twice; it only plans
 * the reminder anew. The same publication rules apply as for {@link MatchChanged}: within the
 * transaction or after the change is stored, handled after the commit or right away when no
 * transaction is active.
 *
 * @param gameKey   the key of the game, as in its game descriptor
 * @param matchId   the game's id of the match, unique within the game
 * @param openedAt  when the decisions opened or changed
 * @param decisions the decisions, at least one, at most one per account and decision id
 * @since 0.4.0
 */
public record DecisionsOpened(String gameKey, String matchId, Instant openedAt, List<PendingDecision> decisions) {

    public DecisionsOpened {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(openedAt, "openedAt");
        decisions = Checks.decisions(decisions, "decisions");
        if (decisions.isEmpty()) {
            throw new IllegalArgumentException("at least one decision opens");
        }
    }
}
