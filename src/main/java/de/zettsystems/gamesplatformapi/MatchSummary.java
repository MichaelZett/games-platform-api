package de.zettsystems.gamesplatformapi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * What the platform shows about one match of one game, for example in its lobby.
 *
 * @param gameKey      the key of the game, as in its game descriptor (for example {@code starfare})
 * @param matchId      the game's id of the match, unique within the game
 * @param title        the name of the match as the game shows it
 * @param status       open, running or finished
 * @param participants the accounts taking part, in the order the game lists them, no duplicates
 * @param seats        how many players the match has in total, people and computer opponents;
 *                     for a game that may start with fewer players, the most it allows
 * @param computerSeats how many of the seats computer opponents take; the rest is for people, at
 *                     least one, and never fewer than take part
 * @param toMove       the participants the game is waiting for; empty unless the match is running.
 *                     Several accounts at once are fine for games with simultaneous turns.
 * @param lastChange   when the match last changed in a way a participant would notice
 * @param viewPath     the path of the match view inside the platform, below {@code /<gameKey>/},
 *                     for example {@code /starfare/game/42}
 */
public record MatchSummary(
        String gameKey,
        String matchId,
        String title,
        MatchStatus status,
        List<AccountId> participants,
        int seats,
        int computerSeats,
        List<AccountId> toMove,
        Instant lastChange,
        String viewPath
) {

    public MatchSummary {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Checks.text(title, "title");
        Objects.requireNonNull(status, "status");
        participants = Checks.distinct(participants, "participants");
        toMove = Checks.distinct(toMove, "toMove");
        Objects.requireNonNull(lastChange, "lastChange");
        Checks.viewPath(viewPath, gameKey);
        if (seats < 1) {
            throw new IllegalArgumentException("a match has at least one seat, got " + seats);
        }
        if (computerSeats < 0) {
            throw new IllegalArgumentException("computer seats cannot be negative, got " + computerSeats);
        }
        if (computerSeats >= seats) {
            throw new IllegalArgumentException("a match keeps at least one seat for a person, but computer opponents take "
                    + computerSeats + " of " + seats + " seats");
        }
        if (participants.size() > seats - computerSeats) {
            throw new IllegalArgumentException(participants.size() + " participants do not fit into "
                    + (seats - computerSeats) + " seats for people");
        }
        if (status != MatchStatus.RUNNING && !toMove.isEmpty()) {
            throw new IllegalArgumentException("only a running match waits for moves, but it is " + status);
        }
        if (!participants.containsAll(toMove)) {
            throw new IllegalArgumentException("every account to move must take part: toMove " + toMove
                    + ", participants " + participants);
        }
    }

    /** Whether the account takes part in this match. */
    public boolean takesPart(AccountId account) {
        return participants.contains(account);
    }

    /** Whether the game is waiting for this account to move. */
    public boolean isToMove(AccountId account) {
        return toMove.contains(account);
    }

    /** How many seats are for people: all seats minus those of computer opponents. */
    public int humanSeats() {
        return seats - computerSeats;
    }

    /** How many seats for people are still free; computer opponents never leave a seat free. */
    public int freeSeats() {
        return humanSeats() - participants.size();
    }

    /** Whether someone can still join: the match is open and has a free seat. */
    public boolean isJoinable() {
        return status == MatchStatus.OPEN && freeSeats() > 0;
    }
}
