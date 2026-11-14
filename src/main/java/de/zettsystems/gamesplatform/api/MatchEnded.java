package de.zettsystems.gamesplatform.api;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A match has ended. The game publishes this event once per match, within the transaction that
 * ends it; a listener that needs the committed state listens after the commit.
 *
 * <p>The result ranks every participant by competition ranking: the best place is 1, accounts
 * that tie share a place, and the next place skips accordingly (1, 1, 3). The placements are kept
 * sorted by place; accounts that share a place stay in the order they were given.
 *
 * @param gameKey    the key of the game, as in its game descriptor
 * @param matchId    the game's id of the match, unique within the game
 * @param endedAt    when the match ended
 * @param placements one placement per participant, at least one
 */
public record MatchEnded(String gameKey, String matchId, Instant endedAt, List<Placement> placements) {

    public MatchEnded {
        Checks.gameKey(gameKey);
        Checks.text(matchId, "matchId");
        Objects.requireNonNull(endedAt, "endedAt");
        Objects.requireNonNull(placements, "placements");
        placements = placements.stream().sorted(Comparator.comparingInt(Placement::place)).toList();
        if (placements.isEmpty()) {
            throw new IllegalArgumentException("a match ends with at least one placement");
        }
        Checks.distinct(placements.stream().map(Placement::account).toList(), "placements' accounts");
        checkRanking(placements);
    }

    private static void checkRanking(List<Placement> sorted) {
        for (int i = 0; i < sorted.size(); i++) {
            int place = sorted.get(i).place();
            boolean tie = i > 0 && place == sorted.get(i - 1).place();
            if (!tie && place != i + 1) {
                throw new IllegalArgumentException("places must follow competition ranking (1, 1, 3), but place "
                        + place + " stands at rank " + (i + 1) + ": " + sorted);
            }
        }
    }

    /** The accounts that took part, best first. */
    public List<AccountId> participants() {
        return placements.stream().map(Placement::account).toList();
    }

    /** The accounts on place 1; more than one if they tied. */
    public List<AccountId> winners() {
        return placements.stream().filter(Placement::isWin).map(Placement::account).toList();
    }

    /** The place of the account, or empty if it did not take part. */
    public OptionalInt placeOf(AccountId account) {
        return placements.stream()
                .filter(placement -> placement.account().equals(account))
                .mapToInt(Placement::place)
                .findFirst();
    }
}
