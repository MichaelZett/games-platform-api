/**
 * The contract between the games platform and its games.
 *
 * <p>A game implements {@link de.zettsystems.gamesplatformapi.MatchDirectory} so that the
 * platform can list the matches of an account and the open matches of the game, each as a
 * {@link de.zettsystems.gamesplatformapi.MatchSummary}. When something the lobby shows changes,
 * the game publishes a {@link de.zettsystems.gamesplatformapi.MatchChanged}; when a match ends,
 * a {@link de.zettsystems.gamesplatformapi.MatchEnded} with its
 * {@link de.zettsystems.gamesplatformapi.MatchResult}.
 *
 * <p>All types are immutable records that check their invariants on construction. They do not
 * depend on any framework; in a Spring application the events travel as ordinary application
 * events.
 */
@NullMarked
package de.zettsystems.gamesplatformapi;

import org.jspecify.annotations.NullMarked;
