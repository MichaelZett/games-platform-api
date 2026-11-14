package de.zettsystems.gamesplatform.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchEndedTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final AccountId DAVE = AccountId.of(4);
    private static final Instant ENDED = Instant.parse("2026-11-14T18:00:00Z");

    @Test
    void sortsThePlacementsByPlace() {
        MatchEnded ended = ended(List.of(new Placement(CAROL, 3), new Placement(ALICE, 1), new Placement(BOB, 2)));

        assertThat(ended.placements()).extracting(Placement::place).containsExactly(1, 2, 3);
        assertThat(ended.participants()).containsExactly(ALICE, BOB, CAROL);
        assertThat(ended.gameKey()).isEqualTo("starfare");
        assertThat(ended.matchId()).isEqualTo("42");
        assertThat(ended.endedAt()).isEqualTo(ENDED);
    }

    @Test
    void keepsTheGivenOrderWithinATie() {
        MatchEnded ended = ended(List.of(new Placement(CAROL, 3), new Placement(BOB, 1), new Placement(ALICE, 1)));

        assertThat(ended.participants()).containsExactly(BOB, ALICE, CAROL);
    }

    @Test
    void namesTheWinners() {
        MatchEnded ended = ended(List.of(new Placement(ALICE, 1), new Placement(BOB, 1), new Placement(CAROL, 3)));

        assertThat(ended.winners()).containsExactly(ALICE, BOB);
    }

    @Test
    void acceptsASinglePlacement() {
        MatchEnded ended = ended(List.of(new Placement(ALICE, 1)));

        assertThat(ended.winners()).containsExactly(ALICE);
    }

    @Test
    void acceptsATieFurtherDown() {
        MatchEnded ended = ended(List.of(new Placement(ALICE, 1), new Placement(BOB, 2), new Placement(CAROL, 2),
                new Placement(DAVE, 4)));

        assertThat(ended.placeOf(DAVE)).hasValue(4);
    }

    @Test
    void answersThePlaceOfAnAccount() {
        MatchEnded ended = ended(List.of(new Placement(ALICE, 1), new Placement(BOB, 2)));

        assertThat(ended.placeOf(BOB)).hasValue(2);
        assertThat(ended.placeOf(CAROL)).isEmpty();
    }

    @Test
    void freezesThePlacements() {
        List<Placement> placements = new ArrayList<>(List.of(new Placement(ALICE, 1)));
        MatchEnded ended = ended(placements);
        placements.add(new Placement(BOB, 2));

        assertThat(ended.placements()).hasSize(1);
    }

    @Test
    void rejectsAnEndWithoutPlacements() {
        List<Placement> none = List.of();

        assertThatThrownBy(() -> ended(none))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one placement");
    }

    @Test
    void rejectsAnAccountPlacedTwice() {
        List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(ALICE, 2));

        assertThatThrownBy(() -> ended(placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicates");
    }

    @Test
    void rejectsAResultWithoutPlaceOne() {
        List<Placement> placements = List.of(new Placement(ALICE, 2), new Placement(BOB, 3));

        assertThatThrownBy(() -> ended(placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("competition ranking");
    }

    @Test
    void rejectsDenseRankingAfterATie() {
        List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(BOB, 1), new Placement(CAROL, 2));

        assertThatThrownBy(() -> ended(placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("competition ranking");
    }

    @Test
    void rejectsAGapInThePlaces() {
        List<Placement> placements = List.of(new Placement(ALICE, 1), new Placement(BOB, 3));

        assertThatThrownBy(() -> ended(placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("competition ranking");
    }

    @Test
    void rejectsAMalformedGameKey() {
        List<Placement> placements = List.of(new Placement(ALICE, 1));

        assertThatThrownBy(() -> new MatchEnded("Star Fare", "42", ENDED, placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameKey");
    }

    @Test
    void rejectsABlankMatchId() {
        List<Placement> placements = List.of(new Placement(ALICE, 1));

        assertThatThrownBy(() -> new MatchEnded("starfare", "", ENDED, placements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("matchId must not be blank");
    }

    @Test
    void requiresTheEndTime() {
        List<Placement> placements = List.of(new Placement(ALICE, 1));

        assertThatThrownBy(() -> new MatchEnded("starfare", "42", null, placements))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("endedAt");
    }

    @Test
    void requiresThePlacements() {
        assertThatThrownBy(() -> new MatchEnded("starfare", "42", ENDED, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("placements");
    }

    private static MatchEnded ended(List<Placement> placements) {
        return new MatchEnded("starfare", "42", ENDED, placements);
    }
}
