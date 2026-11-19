package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The events added in 0.4.0 for notices and reminders. */
class MatchEventsTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final Instant NOW = Instant.parse("2026-11-19T10:00:00Z");
    private static final Instant DUE = Instant.parse("2026-11-20T10:00:00Z");

    @Test
    void turnMissedKeepsItsValues() {
        TurnMissed missed = new TurnMissed("starfare", "42", BOB, "round-7", NOW, 2, true);

        assertThat(missed.gameKey()).isEqualTo("starfare");
        assertThat(missed.matchId()).isEqualTo("42");
        assertThat(missed.account()).isEqualTo(BOB);
        assertThat(missed.decisionId()).isEqualTo("round-7");
        assertThat(missed.missedAt()).isEqualTo(NOW);
        assertThat(missed.missed()).isEqualTo(2);
        assertThat(missed.removed()).isTrue();
    }

    @Test
    void turnMissedCountsAtLeastOne() {
        assertThatThrownBy(() -> new TurnMissed("starfare", "42", BOB, "round-7", NOW, 0, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a missed turn counts at least 1, got 0");
    }

    @Test
    void turnMissedNamesTheDecision() {
        assertThatThrownBy(() -> new TurnMissed("starfare", "42", BOB, " ", NOW, 1, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("decisionId must not be blank");
    }

    @Test
    void turnMissedRequiresAccountAndTime() {
        assertThatThrownBy(() -> new TurnMissed("starfare", "42", null, "round-7", NOW, 1, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("account");
        assertThatThrownBy(() -> new TurnMissed("starfare", "42", BOB, "round-7", null, 1, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("missedAt");
    }

    @Test
    void matchStartedKeepsTheParticipants() {
        List<AccountId> participants = new ArrayList<>(List.of(ALICE, BOB));
        MatchStarted started = new MatchStarted("starfare", "42", NOW, participants);
        participants.clear();

        assertThat(started.gameKey()).isEqualTo("starfare");
        assertThat(started.matchId()).isEqualTo("42");
        assertThat(started.startedAt()).isEqualTo(NOW);
        assertThat(started.participants()).containsExactly(ALICE, BOB);
    }

    @Test
    void matchStartedHasAParticipant() {
        List<AccountId> nobody = List.of();

        assertThatThrownBy(() -> new MatchStarted("starfare", "42", NOW, nobody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a started match has at least one participant");
    }

    @Test
    void matchStartedRequiresTheTime() {
        List<AccountId> alice = List.of(ALICE);

        assertThatThrownBy(() -> new MatchStarted("starfare", "42", null, alice))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("startedAt");
    }

    @Test
    void invitationSentKeepsItsValues() {
        InvitationSent sent = new InvitationSent("frontier-lords", "17", BOB, ALICE, NOW);

        assertThat(sent.gameKey()).isEqualTo("frontier-lords");
        assertThat(sent.matchId()).isEqualTo("17");
        assertThat(sent.invited()).isEqualTo(BOB);
        assertThat(sent.invitedBy()).isEqualTo(ALICE);
        assertThat(sent.sentAt()).isEqualTo(NOW);
    }

    @Test
    void anAccountCannotInviteItself() {
        assertThatThrownBy(() -> new InvitationSent("starfare", "42", ALICE, ALICE, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot invite itself");
    }

    @Test
    void invitationSentRequiresItsValues() {
        assertThatThrownBy(() -> new InvitationSent("starfare", "42", null, ALICE, NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("invited");
        assertThatThrownBy(() -> new InvitationSent("starfare", "42", BOB, null, NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("invitedBy");
        assertThatThrownBy(() -> new InvitationSent("starfare", "42", BOB, ALICE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("sentAt");
    }

    @Test
    void decisionsOpenedKeepsTheDecisions() {
        PendingDecision alice = new PendingDecision(ALICE, "round-8", DecisionKind.TURN, DUE);
        PendingDecision bob = new PendingDecision(BOB, "round-8", DecisionKind.TURN, DUE);
        DecisionsOpened opened = new DecisionsOpened("starfare", "42", NOW, List.of(alice, bob));

        assertThat(opened.gameKey()).isEqualTo("starfare");
        assertThat(opened.matchId()).isEqualTo("42");
        assertThat(opened.openedAt()).isEqualTo(NOW);
        assertThat(opened.decisions()).containsExactly(alice, bob);
    }

    @Test
    void decisionsOpenedHasADecision() {
        List<PendingDecision> none = List.of();

        assertThatThrownBy(() -> new DecisionsOpened("starfare", "42", NOW, none))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("at least one decision opens");
    }

    @Test
    void decisionsOpenedRejectsTheSameDecisionTwice() {
        List<PendingDecision> twice = List.of(new PendingDecision(BOB, "round-8", DecisionKind.TURN, DUE),
                new PendingDecision(BOB, "round-8", DecisionKind.TURN, NOW));

        assertThatThrownBy(() -> new DecisionsOpened("starfare", "42", NOW, twice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not contain duplicates");
    }

    @Test
    void decisionsOpenedRequiresTheTime() {
        List<PendingDecision> one = List.of(new PendingDecision(BOB, "round-8", DecisionKind.TURN, DUE));

        assertThatThrownBy(() -> new DecisionsOpened("starfare", "42", null, one))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("openedAt");
    }
}
