package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinishedMatchTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final Instant ENDED = Instant.parse("2026-11-19T18:00:00Z");
    private static final Placement BOB_TIMED_OUT = new Placement(BOB, 3, Departure.TIMED_OUT);
    private static final MatchResult RANKED = MatchResult.ranked(List.of(new Placement(ALICE, 1), BOB_TIMED_OUT));

    @Test
    void keepsItsValues() {
        FinishedMatch match = new FinishedMatch("frontier-lords", "17", "First steps", ENDED, RANKED, true,
                "/frontier-lords/game/17");

        assertThat(match.gameKey()).isEqualTo("frontier-lords");
        assertThat(match.matchId()).isEqualTo("17");
        assertThat(match.title()).isEqualTo("First steps");
        assertThat(match.endedAt()).isEqualTo(ENDED);
        assertThat(match.result()).isEqualTo(RANKED);
        assertThat(match.tutorial()).isTrue();
        assertThat(match.viewPath()).isEqualTo("/frontier-lords/game/17");
    }

    @Test
    void answersTheOwnResultWithItsDeparture() {
        FinishedMatch match = finished(RANKED);

        assertThat(match.isAborted()).isFalse();
        assertThat(match.placementOf(BOB)).contains(BOB_TIMED_OUT);
        assertThat(match.placementOf(CAROL)).isEmpty();
    }

    @Test
    void anAbortedMatchHasNoPlacements() {
        FinishedMatch match = finished(MatchResult.aborted(List.of(ALICE, BOB)));

        assertThat(match.isAborted()).isTrue();
        assertThat(match.placementOf(ALICE)).isEmpty();
    }

    @Test
    void rejectsAPathOutsideTheGame() {
        assertThatThrownBy(() -> new FinishedMatch("starfare", "42", "Andromeda", ENDED, RANKED, false,
                "/frontier-lords/game/42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewPath");
    }

    @Test
    void rejectsABlankTitle() {
        assertThatThrownBy(() -> new FinishedMatch("starfare", "42", " ", ENDED, RANKED, false,
                "/starfare/game/42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    void requiresTheEndTime() {
        assertThatThrownBy(() -> new FinishedMatch("starfare", "42", "Andromeda", null, RANKED, false,
                "/starfare/game/42"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("endedAt");
    }

    @Test
    void requiresTheResult() {
        assertThatThrownBy(() -> new FinishedMatch("starfare", "42", "Andromeda", ENDED, null, false,
                "/starfare/game/42"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("result");
    }

    private static FinishedMatch finished(MatchResult result) {
        return new FinishedMatch("starfare", "42", "Andromeda", ENDED, result, false, "/starfare/game/42");
    }
}
