/**
 * The contract between the games platform and its games.
 *
 * <p>A game implements {@link de.zettsystems.gamesplatformapi.MatchDirectory} so that the
 * platform can list the matches of an account and the open matches of the game, each as a
 * {@link de.zettsystems.gamesplatformapi.MatchSummary}, the finished matches of an account as
 * {@link de.zettsystems.gamesplatformapi.FinishedMatch}, and ask who may use a match's chat. A view
 * that shows one match implements {@link de.zettsystems.gamesplatformapi.MatchView}.
 *
 * <p>When something the lobby shows changes, the game publishes a
 * {@link de.zettsystems.gamesplatformapi.MatchChanged}; when a match ends, a
 * {@link de.zettsystems.gamesplatformapi.MatchEnded} with its
 * {@link de.zettsystems.gamesplatformapi.MatchResult}. Invitations, starts, open decisions and
 * missed turns have events of their own that carry what the platform needs for notices.
 *
 * <p>The platform implements {@link de.zettsystems.gamesplatformapi.PlatformAccounts},
 * {@link de.zettsystems.gamesplatformapi.PlatformFriends},
 * {@link de.zettsystems.gamesplatformapi.PlatformPresence} and
 * {@link de.zettsystems.gamesplatformapi.PlatformSettings} for the games.
 *
 * <p>All records are immutable and check their invariants on construction. They do not depend on
 * any framework; in a Spring application the events travel as ordinary application events.
 */
@NullMarked
package de.zettsystems.gamesplatformapi;

import org.jspecify.annotations.NullMarked;
