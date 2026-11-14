package de.zettsystems.gamesplatform.api;

/** Where a match stands, as far as the platform is concerned. */
public enum MatchStatus {

    /** Created and waiting for players; it can still be joined. */
    OPEN,

    /** Under way; some participants may be expected to move. */
    RUNNING,

    /** Over; its result has been announced with {@link MatchEnded}. */
    FINISHED
}
