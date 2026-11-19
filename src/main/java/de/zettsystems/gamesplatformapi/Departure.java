package de.zettsystems.gamesplatformapi;

/**
 * Whether and how a participant left a match before its end. The participant still has a place;
 * the departure only tells statistics why.
 */
public enum Departure {

    /** Stayed until the end of the match. */
    NONE,

    /** Gave up of their own accord. */
    RESIGNED,

    /** Was knocked out by the course of the game. */
    ELIMINATED,

    /**
     * Was removed for missing turns; a computer opponent took over the seat. The place is the one
     * the account held when it was removed, stored by the game at that moment: if the computer
     * opponent goes on to win with the same seat, that is not a win of the removed account. The
     * removed account never counts as a winner, even on place 1, and its place is not checked
     * against the ranking of the others.
     *
     * @since 0.4.0
     */
    TIMED_OUT
}
