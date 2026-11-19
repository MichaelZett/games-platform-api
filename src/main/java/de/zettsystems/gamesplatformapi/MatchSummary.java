package de.zettsystems.gamesplatformapi;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What the platform shows about one match of one game, for example in its lobby.
 *
 * @param gameKey          the key of the game, as in its game descriptor (for example {@code starfare})
 * @param matchId          the game's id of the match, unique within the game
 * @param title            the name of the match as the game shows it
 * @param status           open, running or finished
 * @param participants     the accounts taking part, in the order the game lists them, no duplicates
 * @param seats            how many players the match has in total, people and computer opponents;
 *                         for a game that may start with fewer players, the most it allows
 * @param computerSeats    how many of the seats computer opponents take; the rest is for people, at
 *                         least one, and never fewer than take part
 * @param toMove           the participants the game is waiting for; empty unless the match is running.
 *                         Several accounts at once are fine for games with simultaneous turns.
 * @param lastChange       when the match last changed in a way a participant would notice
 * @param viewPath         the path of the match view inside the platform, below {@code /<gameKey>/},
 *                         for example {@code /starfare/game/42}
 * @param tutorial         whether the match is a tutorial; the platform marks it as such and keeps it
 *                         out of competition statistics
 * @param tempo            how fast the match runs, chosen when it was created; {@code null} for a match
 *                         without a tempo, which is every match with only one seat for a person
 * @param pendingDecisions the decisions the running match waits for, from the same source as
 *                         {@code toMove}: every account to move has at least one, and every entry
 *                         belongs to a participant. Empty for a game that does not report them; then
 *                         {@code toMove} alone says who is to move.
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
        String viewPath,
        boolean tutorial,
        @Nullable Tempo tempo,
        List<PendingDecision> pendingDecisions
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
        pendingDecisions = Checks.decisions(pendingDecisions, "pendingDecisions");
        checkSeats(seats, computerSeats, participants.size());
        checkMoves(status, participants, toMove, pendingDecisions);
        if (tempo != null && seats - computerSeats < 2) {
            throw new IllegalArgumentException("only a match with at least two seats for people has a tempo, but it has "
                    + (seats - computerSeats));
        }
    }

    /**
     * A summary without the information added in 0.4.0: no tutorial, no tempo, no pending
     * decisions.
     */
    public MatchSummary(String gameKey, String matchId, String title, MatchStatus status,
                        List<AccountId> participants, int seats, int computerSeats, List<AccountId> toMove,
                        Instant lastChange, String viewPath) {
        this(gameKey, matchId, title, status, participants, seats, computerSeats, toMove, lastChange, viewPath,
                false, null, List.of());
    }

    private static void checkSeats(int seats, int computerSeats, int participants) {
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
        if (participants > seats - computerSeats) {
            throw new IllegalArgumentException(participants + " participants do not fit into "
                    + (seats - computerSeats) + " seats for people");
        }
    }

    private static void checkMoves(MatchStatus status, List<AccountId> participants, List<AccountId> toMove,
                                   List<PendingDecision> decisions) {
        if (status != MatchStatus.RUNNING && !(toMove.isEmpty() && decisions.isEmpty())) {
            throw new IllegalArgumentException("only a running match waits for moves or decisions, but it is " + status);
        }
        if (!participants.containsAll(toMove)) {
            throw new IllegalArgumentException("every account to move must take part: toMove " + toMove
                    + ", participants " + participants);
        }
        List<AccountId> deciding = decisions.stream().map(PendingDecision::account).distinct().toList();
        if (!participants.containsAll(deciding)) {
            throw new IllegalArgumentException("every pending decision must belong to a participant: " + deciding
                    + ", participants " + participants);
        }
        if (!decisions.isEmpty() && !deciding.containsAll(toMove)) {
            throw new IllegalArgumentException("every account to move needs a pending decision: toMove " + toMove
                    + ", accounts with decisions " + deciding);
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

    /**
     * The decisions the match waits for from this account, in the order the game listed them.
     *
     * @since 0.4.0
     */
    public List<PendingDecision> pendingDecisionsOf(AccountId account) {
        return pendingDecisions.stream().filter(decision -> decision.account().equals(account)).toList();
    }

    /**
     * The earliest deadline among the account's pending decisions; empty if none of them has one.
     *
     * @since 0.4.0
     */
    public Optional<Instant> earliestDeadlineOf(AccountId account) {
        return pendingDecisionsOf(account).stream()
                .map(PendingDecision::deadline)
                .filter(Objects::nonNull)
                .min(Instant::compareTo);
    }
}
