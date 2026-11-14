/**
 * The contract between the games platform and its games.
 *
 * <p>A game implements {@link de.zettsystems.gamesplatform.api.MatchDirectory} so that the
 * platform can list the matches of an account and the open matches of the game, each as a
 * {@link de.zettsystems.gamesplatform.api.MatchSummary}. When a match ends, the game publishes
 * a {@link de.zettsystems.gamesplatform.api.MatchEnded} with the placement of every account.
 *
 * <p>All types are immutable records that check their invariants on construction. They do not
 * depend on any framework; in a Spring application the events travel as ordinary application
 * events.
 */
@NullMarked
package de.zettsystems.gamesplatform.api;

import org.jspecify.annotations.NullMarked;
