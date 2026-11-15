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
    ELIMINATED
}
