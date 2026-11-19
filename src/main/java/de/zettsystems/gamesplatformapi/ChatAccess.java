package de.zettsystems.gamesplatformapi;

/**
 * What an account may do in the chat of a match. The platform keeps the chat and shows it; the
 * game decides who may read and write, and the platform asks it on every read and every write.
 * Knowing the match id, being a friend of a participant or a tester of the game is not enough.
 *
 * <p>The usual rules: participants still in the game read and write, in the waiting room and while
 * the match runs; participants who resigned, were eliminated or removed for missing turns only
 * read; after the end everyone who took part only reads; spectators have no chat.
 *
 * @since 0.4.0
 */
public enum ChatAccess {

    /** Neither read nor write. */
    NONE,

    /** Read, but not write. */
    READ,

    /** Read and write. */
    WRITE;

    /** Whether the account may read the chat. */
    public boolean canRead() {
        return this != NONE;
    }

    /** Whether the account may write to the chat. */
    public boolean canWrite() {
        return this == WRITE;
    }
}
