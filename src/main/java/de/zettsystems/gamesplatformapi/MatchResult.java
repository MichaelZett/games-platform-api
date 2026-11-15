package de.zettsystems.gamesplatformapi;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * How a match ended: either with a ranking of all participants, or aborted without one.
 *
 * <p>The interface is sealed: only this library defines results. Further cases may follow in a
 * later minor version (for example for cooperative games). A consumer therefore relies on the
 * methods below where it can, and a {@code switch} over the cases keeps a {@code default} branch,
 * so that it still compiles and does not fail with a {@link MatchException} when a case is added.
 */
public sealed interface MatchResult permits MatchResult.Ranked, MatchResult.Aborted {

    /** The accounts that took part; for a ranked result best first. */
    List<AccountId> participants();

    /** The accounts on place 1; more than one if they tied, none if the match was aborted or a computer opponent won. */
    List<AccountId> winners();

    /** The place of the account, or empty if it did not take part or the match was aborted. */
    OptionalInt placeOf(AccountId account);

    /** Shorthand for {@code new Ranked(placements)}. */
    static Ranked ranked(List<Placement> placements) {
        return new Ranked(placements);
    }

    /** Shorthand for {@code new Aborted(participants)}. */
    static Aborted aborted(List<AccountId> participants) {
        return new Aborted(participants);
    }

    /**
     * The match was played to the end and every participant has a place.
     *
     * <p>The places follow competition ranking: the best place is 1, players that tie share a
     * place, and the next place skips accordingly (1, 1, 3). Computer opponents count as players
     * but are not listed, so the places of the accounts may skip theirs: if a computer opponent
     * wins, the best account is on place 2, and nobody of the listed accounts {@linkplain
     * #winners() won}. The placements are kept sorted by place; accounts that share a place stay
     * in the order they were given.
     *
     * @param placements one placement per participant, at least one
     */
    record Ranked(List<Placement> placements) implements MatchResult {

        public Ranked {
            Objects.requireNonNull(placements, "placements");
            placements = placements.stream().sorted(Comparator.comparingInt(Placement::place)).toList();
            if (placements.isEmpty()) {
                throw new IllegalArgumentException("a ranked result has at least one placement");
            }
            Checks.distinct(placements.stream().map(Placement::account).toList(), "placements' accounts");
            checkRanking(placements);
        }

        // Unlisted computer opponents may take any place in between, so a place can be higher than
        // its rank among the accounts, but never lower: i accounts before it are strictly better.
        private static void checkRanking(List<Placement> sorted) {
            for (int i = 0; i < sorted.size(); i++) {
                int place = sorted.get(i).place();
                boolean tie = i > 0 && place == sorted.get(i - 1).place();
                if (!tie && place < i + 1) {
                    throw new IllegalArgumentException("places must follow competition ranking (1, 1, 3), but place "
                            + place + " stands at rank " + (i + 1) + ": " + sorted);
                }
            }
        }

        @Override
        public List<AccountId> participants() {
            return placements.stream().map(Placement::account).toList();
        }

        @Override
        public List<AccountId> winners() {
            return placements.stream().filter(Placement::isWin).map(Placement::account).toList();
        }

        @Override
        public OptionalInt placeOf(AccountId account) {
            return placements.stream()
                    .filter(placement -> placement.account().equals(account))
                    .mapToInt(Placement::place)
                    .findFirst();
        }
    }

    /**
     * The match ended without a result: cancelled, abandoned or broken off. Nobody won and nobody
     * has a place, but the participants are named so that statistics can count the match.
     *
     * @param participants the accounts that took part when the match was aborted, at least one, no
     *                     duplicates
     */
    record Aborted(List<AccountId> participants) implements MatchResult {

        public Aborted {
            participants = Checks.distinct(participants, "participants");
            if (participants.isEmpty()) {
                throw new IllegalArgumentException("an aborted match names at least one participant");
            }
        }

        @Override
        public List<AccountId> winners() {
            return List.of();
        }

        @Override
        public OptionalInt placeOf(AccountId account) {
            return OptionalInt.empty();
        }
    }
}
