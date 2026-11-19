package de.zettsystems.gamesplatformapi;

import java.util.Optional;

/**
 * A view of a game that shows one match, such as the waiting room or the match itself. The view
 * implements this interface so that the platform can show things that belong to the match around
 * it, for example the match's chat.
 *
 * <p>A match is identified by the game and the match id together. The game is the one the view
 * belongs to: a view below {@code /<gameKey>/} shows matches of that game, and the platform takes
 * the game key from there, never from the view. The view tells only the match id.
 *
 * <p>The platform asks after every navigation, once the view has read its parameters and checked
 * access. Rights are not derived from the answer: the platform still asks the game's
 * {@link MatchDirectory#chatAccess(String, AccountId)} before it shows or accepts anything.
 *
 * @since 0.4.0
 */
public interface MatchView {

    /**
     * The id of the match the view shows; empty if it shows none, for example because the match does
     * not exist (any more) or the signed-in account may not see it. The platform then shows nothing
     * of any match, not even of the one shown before.
     */
    Optional<String> shownMatchId();
}
