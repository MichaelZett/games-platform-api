package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingDecisionTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final Instant DUE = Instant.parse("2026-11-20T18:00:00Z");

    @Test
    void keepsItsValues() {
        PendingDecision decision = new PendingDecision(ALICE, "round-12", DecisionKind.TURN, DUE);

        assertThat(decision.account()).isEqualTo(ALICE);
        assertThat(decision.decisionId()).isEqualTo("round-12");
        assertThat(decision.kind()).isEqualTo(DecisionKind.TURN);
        assertThat(decision.deadline()).isEqualTo(DUE);
        assertThat(decision.dueBy()).contains(DUE);
    }

    @Test
    void mayHaveNoDeadline() {
        PendingDecision decision = PendingDecision.unlimited(ALICE, "trade-7", DecisionKind.ANSWER);

        assertThat(decision.deadline()).isNull();
        assertThat(decision.dueBy()).isEmpty();
    }

    @Test
    void acceptsAnIdOfTheMaximumLength() {
        String id = "x".repeat(PendingDecision.MAX_ID_LENGTH);

        assertThat(new PendingDecision(ALICE, id, DecisionKind.TURN, DUE).decisionId()).hasSize(100);
    }

    @Test
    void rejectsAnIdThatIsTooLong() {
        String id = "x".repeat(PendingDecision.MAX_ID_LENGTH + 1);

        assertThatThrownBy(() -> new PendingDecision(ALICE, id, DecisionKind.TURN, DUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("decisionId has at most 100 characters, got 101");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void rejectsABlankId(String id) {
        assertThatThrownBy(() -> new PendingDecision(ALICE, id, DecisionKind.TURN, DUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("decisionId must not be blank");
    }

    @Test
    void requiresAnAccount() {
        assertThatThrownBy(() -> new PendingDecision(null, "round-12", DecisionKind.TURN, DUE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("account");
    }

    @Test
    void requiresAKind() {
        assertThatThrownBy(() -> new PendingDecision(ALICE, "round-12", null, DUE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("kind");
    }
}
