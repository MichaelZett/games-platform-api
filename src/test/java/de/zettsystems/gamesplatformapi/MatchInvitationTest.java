package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MatchInvitationTest {

    private static final AccountId HOST = AccountId.of(1);
    private static final MatchSummary MATCH = new MatchSummary("starfare", "7", "Nebula", MatchStatus.OPEN,
            List.of(HOST), 3, 0, List.of(), Instant.parse("2026-11-16T10:00:00Z"), "/starfare/match/7");

    @Test
    void namesTheMatchAndWhoInvited() {
        MatchInvitation invitation = new MatchInvitation(MATCH, HOST);

        assertThat(invitation.match()).isEqualTo(MATCH);
        assertThat(invitation.invitedBy()).isEqualTo(HOST);
    }

    @Test
    void requiresTheMatch() {
        assertThatNullPointerException().isThrownBy(() -> new MatchInvitation(null, HOST)).withMessage("match");
    }

    @Test
    void requiresWhoInvited() {
        assertThatNullPointerException().isThrownBy(() -> new MatchInvitation(MATCH, null)).withMessage("invitedBy");
    }
}
