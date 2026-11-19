package de.zettsystems.gamesplatformapi;

/**
 * What kind of decision a match waits for. Only a {@link #TURN} counts as missed when its deadline
 * passes; an {@link #ANSWER} that does not come in is settled by the game (a trade declined, a vote
 * counted as abstention) without counting against the account.
 *
 * <p>Further kinds may follow in a later minor version, so a {@code switch} over the kinds keeps a
 * {@code default} branch.
 *
 * @since 0.4.0
 */
public enum DecisionKind {

    /**
     * The account's own turn, including choices that block the match until made (for example
     * picking a starting territory). A turn whose deadline passes counts as missed.
     */
    TURN,

    /** An answer to a request of another participant, such as a trade or an alliance vote. */
    ANSWER
}
