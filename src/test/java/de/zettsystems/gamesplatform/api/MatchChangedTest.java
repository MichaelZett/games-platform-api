package de.zettsystems.gamesplatform.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchChangedTest {

    private static final Instant CHANGED = Instant.parse("2026-11-14T12:00:00Z");

    @Test
    void keepsItsValues() {
        MatchChanged changed = new MatchChanged("starfare", "42", CHANGED);

        assertThat(changed.gameKey()).isEqualTo("starfare");
        assertThat(changed.matchId()).isEqualTo("42");
        assertThat(changed.changedAt()).isEqualTo(CHANGED);
    }

    @Test
    void rejectsAMalformedGameKey() {
        assertThatThrownBy(() -> new MatchChanged("star_fare", "42", CHANGED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameKey");
    }

    @Test
    void rejectsABlankMatchId() {
        assertThatThrownBy(() -> new MatchChanged("starfare", " ", CHANGED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("matchId must not be blank");
    }

    @Test
    void requiresTheTimeOfTheChange() {
        assertThatThrownBy(() -> new MatchChanged("starfare", "42", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("changedAt");
    }
}
