package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchEndedTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final Instant ENDED = Instant.parse("2026-11-14T18:00:00Z");
    private static final MatchResult RANKED = MatchResult.ranked(List.of(new Placement(BOB, 2),
            new Placement(ALICE, 1)));

    @Test
    void keepsItsValues() {
        MatchEnded ended = new MatchEnded("starfare", "42", ENDED, RANKED);

        assertThat(ended.gameKey()).isEqualTo("starfare");
        assertThat(ended.matchId()).isEqualTo("42");
        assertThat(ended.endedAt()).isEqualTo(ENDED);
        assertThat(ended.result()).isEqualTo(RANKED);
    }

    @Test
    void answersFromARankedResult() {
        MatchEnded ended = new MatchEnded("starfare", "42", ENDED, RANKED);

        assertThat(ended.isAborted()).isFalse();
        assertThat(ended.participants()).containsExactly(ALICE, BOB);
        assertThat(ended.winners()).containsExactly(ALICE);
        assertThat(ended.placeOf(BOB)).hasValue(2);
        assertThat(ended.placeOf(CAROL)).isEmpty();
    }

    @Test
    void answersFromAnAbortedMatch() {
        MatchEnded ended = new MatchEnded("starfare", "42", ENDED, MatchResult.aborted(List.of(ALICE, BOB)));

        assertThat(ended.isAborted()).isTrue();
        assertThat(ended.participants()).containsExactly(ALICE, BOB);
        assertThat(ended.winners()).isEmpty();
        assertThat(ended.placeOf(ALICE)).isEmpty();
    }

    @Test
    void rejectsAMalformedGameKey() {
        assertThatThrownBy(() -> new MatchEnded("Star Fare", "42", ENDED, RANKED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameKey");
    }

    @Test
    void rejectsABlankMatchId() {
        assertThatThrownBy(() -> new MatchEnded("starfare", "", ENDED, RANKED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("matchId must not be blank");
    }

    @Test
    void requiresTheEndTime() {
        assertThatThrownBy(() -> new MatchEnded("starfare", "42", null, RANKED))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("endedAt");
    }

    @Test
    void requiresTheResult() {
        assertThatThrownBy(() -> new MatchEnded("starfare", "42", ENDED, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("result");
    }
}
