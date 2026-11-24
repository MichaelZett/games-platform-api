package de.zettsystems.gamesplatformapi;

/**
 * How far a game or a variant of its rules has come, and with it who may use it. The platform keeps
 * the stage and decides who belongs to which circle; a game only shows the stage, for example
 * where a variant is chosen. Further stages may follow in a later minor version, so a
 * {@code switch} over the stages keeps a {@code default} branch.
 *
 * @since 0.5.0
 */
public enum LifecycleStage {

    /** In development: only the game's team and testers invited one by one. */
    ALPHA,

    /** Being tried by experienced players before everyone gets it. */
    BETA,

    /** Open to every signed-in account. */
    PRODUCTION
}
