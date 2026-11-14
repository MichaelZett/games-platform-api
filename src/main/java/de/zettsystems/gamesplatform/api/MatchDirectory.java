package de.zettsystems.gamesplatform.api;

import java.util.List;

/**
 * The matches of one game, as the platform asks for them. Every game implements this interface
 * once; the platform collects all implementations and asks each of them.
 *
 * <p>Both queries return summaries of this game only ({@link MatchSummary#gameKey()} equals
 * {@link #gameKey()}), most recently changed first. They are read-only and may be called often,
 * for example on every visit to the lobby.
 */
public interface MatchDirectory {

    /** The key of the game this directory belongs to, as in its game descriptor. */
    String gameKey();

    /**
     * The matches the account takes part in that are open or running. Finished matches belong to
     * the game's own archive and are not listed.
     */
    List<MatchSummary> matchesOf(AccountId account);

    /**
     * The open matches the viewer could join: not yet taking part, and allowed to see the match.
     * Matches the viewer already takes part in are listed by {@link #matchesOf(AccountId)}.
     */
    List<MatchSummary> openMatches(AccountId viewer);
}
