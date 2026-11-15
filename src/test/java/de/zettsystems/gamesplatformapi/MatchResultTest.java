package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchResultTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final AccountId DAVE = AccountId.of(4);

    @Nested
    class Ranked {

        @Test
        void sortsThePlacementsByPlace() {
            MatchResult.Ranked ranked = MatchResult.ranked(List.of(new Placement(CAROL, 3), new Placement(ALICE, 1),
                    new Placement(BOB, 2)));

            assertThat(ranked.placements()).extracting(Placement::place).containsExactly(1, 2, 3);
            assertThat(ranked.participants()).containsExactly(ALICE, BOB, CAROL);
        }

        @Test
        void keepsTheGivenOrderWithinATie() {
            MatchResult result = MatchResult.ranked(List.of(new Placement(CAROL, 3), new Placement(BOB, 1),
                    new Placement(ALICE, 1)));

            assertThat(result.participants()).containsExactly(BOB, ALICE, CAROL);
        }

        @Test
        void namesTheWinners() {
            MatchResult result = MatchResult.ranked(List.of(new Placement(ALICE, 1), new Placement(BOB, 1),
                    new Placement(CAROL, 3)));

            assertThat(result.winners()).containsExactly(ALICE, BOB);
        }

        @Test
        void acceptsASinglePlacement() {
            MatchResult result = MatchResult.ranked(List.of(new Placement(ALICE, 1)));

            assertThat(result.winners()).containsExactly(ALICE);
        }

        @Test
        void acceptsATieFurtherDown() {
            MatchResult result = MatchResult.ranked(List.of(new Placement(ALICE, 1), new Placement(BOB, 2),
                    new Placement(CAROL, 2), new Placement(DAVE, 4)));

            assertThat(result.placeOf(DAVE)).hasValue(4);
        }

        @Test
        void answersThePlaceOfAnAccount() {
            MatchResult result = MatchResult.ranked(List.of(new Placement(ALICE, 1), new Placement(BOB, 2)));

            assertThat(result.placeOf(BOB)).hasValue(2);
            assertThat(result.placeOf(CAROL)).isEmpty();
        }

        @Test
        void keepsTheDepartureNextToThePlace() {
            MatchResult.Ranked ranked = MatchResult.ranked(List.of(new Placement(ALICE, 1),
                    new Placement(BOB, 2, Departure.RESIGNED), new Placement(CAROL, 3, Departure.ELIMINATED)));

            assertThat(ranked.placements()).extracting(Placement::departure)
                    .containsExactly(Departure.NONE, Departure.RESIGNED, Departure.ELIMINATED);
        }

        @Test
        void freezesThePlacements() {
            List<Placement> placements = new ArrayList<>(List.of(new Placement(ALICE, 1)));
            MatchResult.Ranked ranked = MatchResult.ranked(placements);
            placements.add(new Placement(BOB, 2));

            assertThat(ranked.placements()).hasSize(1);
        }

        @Test
        void rejectsAResultWithoutPlacements() {
            List<Placement> none = List.of();

            assertThatThrownBy(() -> MatchResult.ranked(none))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one placement");
        }

        @Test
        void requiresThePlacements() {
            assertThatThrownBy(() -> MatchResult.ranked(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("placements");
        }

        @Test
        void rejectsAnAccountPlacedTwice() {
            List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(ALICE, 2));

            assertThatThrownBy(() -> MatchResult.ranked(placements))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duplicates");
        }

        @Test
        void rejectsAResultWithoutPlaceOne() {
            List<Placement> placements = List.of(new Placement(ALICE, 2), new Placement(BOB, 3));

            assertThatThrownBy(() -> MatchResult.ranked(placements))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("competition ranking");
        }

        @Test
        void rejectsDenseRankingAfterATie() {
            List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(BOB, 1),
                    new Placement(CAROL, 2));

            assertThatThrownBy(() -> MatchResult.ranked(placements))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("competition ranking");
        }

        @Test
        void rejectsAGapInThePlaces() {
            List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(BOB, 3));

            assertThatThrownBy(() -> MatchResult.ranked(placements))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("competition ranking");
        }
    }

    @Nested
    class Aborted {

        @Test
        void namesTheParticipantsButNoWinnerAndNoPlace() {
            MatchResult result = MatchResult.aborted(List.of(BOB, ALICE));

            assertThat(result.participants()).containsExactly(BOB, ALICE);
            assertThat(result.winners()).isEmpty();
            assertThat(result.placeOf(ALICE)).isEmpty();
        }

        @Test
        void freezesTheParticipants() {
            List<AccountId> participants = new ArrayList<>(List.of(ALICE));
            MatchResult result = MatchResult.aborted(participants);
            participants.add(BOB);

            assertThat(result.participants()).containsExactly(ALICE);
        }

        @Test
        void rejectsAnAbortWithoutParticipants() {
            List<AccountId> none = List.of();

            assertThatThrownBy(() -> MatchResult.aborted(none))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one participant");
        }

        @Test
        void rejectsDuplicateParticipants() {
            List<AccountId> participants = List.of(ALICE, ALICE);

            assertThatThrownBy(() -> MatchResult.aborted(participants))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duplicates");
        }

        @Test
        void rejectsANullParticipant() {
            List<AccountId> participants = Arrays.asList(ALICE, null);

            assertThatThrownBy(() -> MatchResult.aborted(participants)).isInstanceOf(NullPointerException.class);
        }
    }

    /** The way the type's documentation asks consumers to switch: with a default branch. */
    @Test
    void aSwitchWithADefaultBranchCoversTheCases() {
        assertThat(describe(MatchResult.ranked(List.of(new Placement(ALICE, 1))))).isEqualTo("ranked 1");
        assertThat(describe(MatchResult.aborted(List.of(ALICE, BOB)))).isEqualTo("aborted 2");
    }

    private static String describe(MatchResult result) {
        return switch (result) {
            case MatchResult.Ranked(List<Placement> placements) -> "ranked " + placements.size();
            case MatchResult.Aborted(List<AccountId> participants) -> "aborted " + participants.size();
            default -> "unknown";
        };
    }
}
