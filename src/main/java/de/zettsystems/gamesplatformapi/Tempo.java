package de.zettsystems.gamesplatformapi;

/**
 * How fast a match with several people runs: the platform names the levels the same for all games,
 * each game sets the time a turn may take for each level it offers and enforces it. The tempo is
 * chosen when the match is created and does not change while it runs; neither does the time per
 * turn the game sets for it, so nobody changes the deadlines of a running match with a tempo. A
 * match with only one person has no tempo.
 *
 * <p>The usual time per turn is given for each level; a game may deviate where a turn needs more or
 * less work. Further levels may follow in a later minor version, so a {@code switch} over the
 * levels keeps a {@code default} branch.
 *
 * @since 0.4.0
 */
public enum Tempo {

    /** Everyone is there at the same time; about two minutes per turn. */
    LIVE,

    /** Several turns a day, played on the side; about an hour per turn. */
    BRISK,

    /** One turn a day; about 24 hours per turn. */
    DAILY,

    /** Little time, long matches; about 72 hours per turn. */
    RELAXED
}
