package de.zettsystems.gamesplatformapi;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** What 0.4.0 added to {@link MatchSummary}: tutorial, tempo and pending decisions. */
class MatchSummaryDecisionsTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final Instant NOW = Instant.parse("2026-11-19T10:00:00Z");
    private static final Instant SOON = Instant.parse("2026-11-19T11:00:00Z");
    private static final Instant LATER = Instant.parse("2026-11-20T10:00:00Z");

    @Test
    void theOldConstructorLeavesTheNewValuesEmpty() {
        MatchSummary summary = new MatchSummary("starfare", "42", "Andromeda", MatchStatus.RUNNING,
                List.of(ALICE, BOB), 2, 0, List.of(BOB), NOW, "/starfare/game/42");

        assertThat(summary.tutorial()).isFalse();
        assertThat(summary.tempo()).isNull();
        assertThat(summary.pendingDecisions()).isEmpty();
        assertThat(summary.earliestDeadlineOf(BOB)).isEmpty();
    }

    @Test
    void keepsTutorialTempoAndDecisions() {
        PendingDecision turn = new PendingDecision(BOB, "round-3", DecisionKind.TURN, LATER);
        MatchSummary summary = summary(MatchStatus.RUNNING, true, Tempo.DAILY, List.of(BOB), List.of(turn));

        assertThat(summary.tutorial()).isTrue();
        assertThat(summary.tempo()).isEqualTo(Tempo.DAILY);
        assertThat(summary.pendingDecisions()).containsExactly(turn);
    }

    @Test
    void listsTheDecisionsOfOneAccountAndItsEarliestDeadline() {
        PendingDecision bobsTurn = new PendingDecision(BOB, "year-1-phase-2", DecisionKind.TURN, LATER);
        PendingDecision bobsVote = new PendingDecision(BOB, "alliance-4", DecisionKind.ANSWER, SOON);
        PendingDecision carolsVote = new PendingDecision(CAROL, "alliance-4", DecisionKind.ANSWER, SOON);
        MatchSummary summary = summary(MatchStatus.RUNNING, false, Tempo.BRISK, List.of(BOB, CAROL),
                List.of(bobsTurn, carolsVote, bobsVote));

        assertThat(summary.pendingDecisionsOf(BOB)).containsExactly(bobsTurn, bobsVote);
        assertThat(summary.pendingDecisionsOf(ALICE)).isEmpty();
        assertThat(summary.earliestDeadlineOf(BOB)).contains(SOON);
    }

    @Test
    void decisionsWithoutDeadlineGiveNoEarliestDeadline() {
        MatchSummary summary = summary(MatchStatus.RUNNING, false, null, List.of(BOB),
                List.of(PendingDecision.unlimited(BOB, "round-3", DecisionKind.TURN)));

        assertThat(summary.earliestDeadlineOf(BOB)).isEmpty();
    }

    @Test
    void allowsAnAnswerFromAnAccountThatIsNotToMove() {
        PendingDecision vote = PendingDecision.unlimited(ALICE, "pact-2", DecisionKind.ANSWER);
        PendingDecision turn = new PendingDecision(BOB, "round-3", DecisionKind.TURN, LATER);

        MatchSummary summary = summary(MatchStatus.RUNNING, false, null, List.of(BOB), List.of(turn, vote));

        assertThat(summary.pendingDecisionsOf(ALICE)).containsExactly(vote);
    }

    @Test
    void freezesTheDecisions() {
        List<PendingDecision> decisions = new ArrayList<>(List.of(new PendingDecision(BOB, "round-3",
                DecisionKind.TURN, LATER)));
        MatchSummary summary = summary(MatchStatus.RUNNING, false, null, List.of(BOB), decisions);
        decisions.clear();

        assertThat(summary.pendingDecisions()).hasSize(1);
    }

    @Test
    void rejectsAnAccountToMoveWithoutADecision() {
        List<PendingDecision> onlyBob = List.of(new PendingDecision(BOB, "round-3", DecisionKind.TURN, LATER));
        List<AccountId> both = List.of(ALICE, BOB);

        assertThatThrownBy(() -> summary(MatchStatus.RUNNING, false, null, both, onlyBob))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("every account to move needs a pending decision");
    }

    @Test
    void rejectsADecisionOfAnAccountThatDoesNotTakePart() {
        List<PendingDecision> stranger = List.of(new PendingDecision(AccountId.of(4), "round-3", DecisionKind.TURN, LATER));
        List<AccountId> nobody = List.of();

        assertThatThrownBy(() -> summary(MatchStatus.RUNNING, false, null, nobody, stranger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("every pending decision must belong to a participant");
    }

    @Test
    void rejectsTheSameDecisionTwice() {
        List<PendingDecision> twice = List.of(new PendingDecision(BOB, "round-3", DecisionKind.TURN, LATER),
                new PendingDecision(BOB, "round-3", DecisionKind.ANSWER, SOON));
        List<AccountId> bob = List.of(BOB);

        assertThatThrownBy(() -> summary(MatchStatus.RUNNING, false, null, bob, twice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendingDecisions (account and decisionId) must not contain duplicates");
    }

    @ParameterizedTest
    @EnumSource(value = MatchStatus.class, names = {"OPEN", "FINISHED"})
    void onlyARunningMatchWaitsForDecisions(MatchStatus status) {
        List<PendingDecision> decision = List.of(PendingDecision.unlimited(BOB, "pact-2", DecisionKind.ANSWER));
        List<AccountId> nobody = List.of();

        assertThatThrownBy(() -> summary(status, false, null, nobody, decision))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("only a running match waits for moves or decisions, but it is " + status);
    }

    @Test
    void keepsTheTempoWhenOnlyOnePersonRemains() {
        MatchSummary summary = new MatchSummary("frontier-lords", "17", "Taken over", MatchStatus.RUNNING,
                List.of(ALICE), 4, 3, List.of(), NOW, "/frontier-lords/game/17", false, Tempo.DAILY, List.of());

        assertThat(summary.humanSeats()).isEqualTo(1);
        assertThat(summary.tempo()).isEqualTo(Tempo.DAILY);
    }

    @Test
    void aMatchWithTwoSeatsForPeopleMayHaveATempo() {
        MatchSummary summary = new MatchSummary("starfare", "42", "Duel", MatchStatus.OPEN, List.of(ALICE), 3, 1,
                List.of(), NOW, "/starfare/game/42", false, Tempo.LIVE, List.of());

        assertThat(summary.tempo()).isEqualTo(Tempo.LIVE);
    }

    @Test
    void requiresTheListOfDecisions() {
        List<AccountId> nobody = List.of();

        assertThatThrownBy(() -> summary(MatchStatus.RUNNING, false, null, nobody, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("pendingDecisions");
    }

    private static MatchSummary summary(MatchStatus status, boolean tutorial, @Nullable Tempo tempo,
                                        List<AccountId> toMove, List<PendingDecision> decisions) {
        return new MatchSummary("starfare", "42", "Andromeda", status, List.of(ALICE, BOB, CAROL), 4, 0, toMove,
                NOW, "/starfare/game/42", tutorial, tempo, decisions);
    }
}
