package de.zettsystems.gamesplatformapi;

import java.util.List;

/**
 * The matches of one game, as the platform asks for them. Every game implements this interface
 * once; the platform collects all implementations and asks each of them.
 *
 * <p>Both queries return summaries of this game only ({@link MatchSummary#gameKey()} equals
 * {@link #gameKey()}), most recently changed first. They are read-only and may be called often,
 * for example on every visit to the lobby and after every {@link MatchChanged}.
 */
public interface MatchDirectory {

    /** The key of the game this directory belongs to, as in its game descriptor. */
    String gameKey();

    /**
     * The matches the account takes part in that are open or running. Finished matches are listed by
     * {@link #finishedMatchesOf(AccountId, int, int)}.
     */
    List<MatchSummary> matchesOf(AccountId account);

    /**
     * The matches the viewer could join: every one of them is {@linkplain MatchSummary#isJoinable()
     * joinable} (open, with a free seat), the viewer does not take part yet and may see it. Matches
     * the viewer already takes part in are listed by {@link #matchesOf(AccountId)}.
     */
    List<MatchSummary> openMatches(AccountId viewer);

    /**
     * The open invitations of the account: matches someone invited it to that it has neither
     * accepted nor declined yet, most recently changed first. A game without invitations keeps the
     * default, which lists none.
     *
     * @since 0.2.0
     */
    default List<MatchInvitation> invitationsOf(AccountId account) {
        return List.of();
    }

    /**
     * One page of the finished matches the account took part in, including those it left early
     * (resigned, eliminated, removed for missing turns) and tutorials; matches cancelled before they
     * started are not among them. Sorted by {@link FinishedMatch#endedAt()}, latest first, and by
     * match id where two ended at the same time, so that pages neither overlap nor skip a match as
     * long as the list does not change in between; a match that ends or disappears meanwhile shifts
     * the later pages. A match whose state the game has deleted is no longer listed. A game without
     * finished matches to show keeps the default, which lists none.
     *
     * @param account the account whose matches are listed
     * @param offset  how many matches to skip, at least 0
     * @param limit   how many matches to list at most, at least 1
     * @since 0.4.0
     */
    default List<FinishedMatch> finishedMatchesOf(AccountId account, int offset, int limit) {
        return List.of();
    }

    /**
     * What the account may do in the chat of the match, decided anew on every call from the state of
     * the match and the account's part in it (see {@link ChatAccess} for the usual rules). An unknown
     * match gives {@link ChatAccess#NONE}. A game that does not offer a chat keeps the default, which
     * allows nothing.
     *
     * @param matchId the game's id of the match
     * @param account the account that wants to read or write
     * @since 0.4.0
     */
    default ChatAccess chatAccess(String matchId, AccountId account) {
        return ChatAccess.NONE;
    }
}
