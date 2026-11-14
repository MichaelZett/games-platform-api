package de.zettsystems.gamesplatform.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchSummaryTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId CAROL = AccountId.of(3);
    private static final Instant NOW = Instant.parse("2026-11-14T10:15:30Z");

    @Test
    void keepsAllValuesOfARunningMatch() {
        MatchSummary summary = new MatchSummary("starfare", "42", "Andromeda", MatchStatus.RUNNING,
                List.of(ALICE, BOB), List.of(BOB), NOW, "/starfare/game/42");

        assertThat(summary.gameKey()).isEqualTo("starfare");
        assertThat(summary.matchId()).isEqualTo("42");
        assertThat(summary.title()).isEqualTo("Andromeda");
        assertThat(summary.status()).isEqualTo(MatchStatus.RUNNING);
        assertThat(summary.participants()).containsExactly(ALICE, BOB);
        assertThat(summary.toMove()).containsExactly(BOB);
        assertThat(summary.lastChange()).isEqualTo(NOW);
        assertThat(summary.viewPath()).isEqualTo("/starfare/game/42");
    }

    @Test
    void answersWhoTakesPartAndWhoIsToMove() {
        MatchSummary summary = running(List.of(ALICE, BOB), List.of(BOB));

        assertThat(summary.takesPart(ALICE)).isTrue();
        assertThat(summary.takesPart(CAROL)).isFalse();
        assertThat(summary.isToMove(BOB)).isTrue();
        assertThat(summary.isToMove(ALICE)).isFalse();
    }

    @Test
    void allowsSeveralAccountsToMoveAtOnce() {
        MatchSummary summary = running(List.of(ALICE, BOB, CAROL), List.of(ALICE, CAROL));

        assertThat(summary.toMove()).containsExactly(ALICE, CAROL);
    }

    @Test
    void allowsAnOpenMatchWithoutParticipants() {
        MatchSummary summary = withStatus(MatchStatus.OPEN, List.of(), List.of());

        assertThat(summary.participants()).isEmpty();
    }

    @Test
    void freezesTheLists() {
        List<AccountId> participants = new ArrayList<>(List.of(ALICE, BOB));
        List<AccountId> toMove = new ArrayList<>(List.of(BOB));
        MatchSummary summary = running(participants, toMove);
        participants.add(CAROL);
        toMove.clear();

        assertThat(summary.participants()).containsExactly(ALICE, BOB);
        assertThat(summary.toMove()).containsExactly(BOB);
        List<AccountId> frozen = summary.participants();
        assertThatThrownBy(() -> frozen.add(CAROL)).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @EnumSource(value = MatchStatus.class, names = {"OPEN", "FINISHED"})
    void onlyARunningMatchWaitsForMoves(MatchStatus status) {
        List<AccountId> participants = List.of(ALICE);
        List<AccountId> toMove = List.of(ALICE);

        assertThatThrownBy(() -> withStatus(status, participants, toMove))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only a running match");
    }

    @Test
    void rejectsAnAccountToMoveThatDoesNotTakePart() {
        List<AccountId> participants = List.of(ALICE);
        List<AccountId> toMove = List.of(BOB);

        assertThatThrownBy(() -> running(participants, toMove))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must take part");
    }

    @Test
    void rejectsDuplicateParticipants() {
        List<AccountId> participants = List.of(ALICE, ALICE);
        List<AccountId> toMove = List.of();

        assertThatThrownBy(() -> running(participants, toMove))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("participants must not contain duplicates");
    }

    @Test
    void rejectsDuplicateAccountsToMove() {
        List<AccountId> participants = List.of(ALICE);
        List<AccountId> toMove = List.of(ALICE, ALICE);

        assertThatThrownBy(() -> running(participants, toMove))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toMove must not contain duplicates");
    }

    @Test
    void rejectsANullParticipant() {
        List<AccountId> participants = Arrays.asList(ALICE, null);
        List<AccountId> toMove = List.of();

        assertThatThrownBy(() -> running(participants, toMove)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Starfare", "1game", "-x", "star_fare", "star fare"})
    void rejectsAMalformedGameKey(String gameKey) {
        List<AccountId> none = List.of();
        String viewPath = "/" + gameKey + "/game/42";

        assertThatThrownBy(() -> new MatchSummary(gameKey, "42", "Andromeda", MatchStatus.OPEN, none, none, NOW,
                viewPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameKey");
    }

    @Test
    void acceptsAGameKeyWithDigitsAndHyphens() {
        MatchSummary summary = new MatchSummary("frontier-lords2", "7", "Plains", MatchStatus.OPEN, List.of(),
                List.of(), NOW, "/frontier-lords2/match/7");

        assertThat(summary.gameKey()).isEqualTo("frontier-lords2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void rejectsABlankMatchId(String matchId) {
        List<AccountId> none = List.of();

        assertThatThrownBy(() -> new MatchSummary("starfare", matchId, "Andromeda", MatchStatus.OPEN, none, none,
                NOW, "/starfare/game/42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("matchId must not be blank");
    }

    @Test
    void rejectsABlankTitle() {
        List<AccountId> none = List.of();

        assertThatThrownBy(() -> new MatchSummary("starfare", "42", " ", MatchStatus.OPEN, none, none, NOW,
                "/starfare/game/42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    void namesTheMissingValue() {
        List<AccountId> none = List.of();

        assertThatThrownBy(() -> new MatchSummary("starfare", "42", "Andromeda", MatchStatus.OPEN, none, none, null,
                "/starfare/game/42"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("lastChange");
    }

    @Test
    void requiresAStatus() {
        List<AccountId> none = List.of();

        assertThatThrownBy(() -> new MatchSummary("starfare", "42", "Andromeda", null, none, none, NOW,
                "/starfare/game/42"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/starfare/game/42?tab=orders", "/starfare/game/42#map", "/starfare/x"})
    void acceptsPathsBelowTheGame(String viewPath) {
        assertThat(withPath(viewPath).viewPath()).isEqualTo(viewPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "starfare/game/42",
            "/starfare/",
            "/starfare",
            "/other/game/42",
            "/starfareX/game/42",
            "https://example.org/starfare/game/42",
            "//example.org/starfare/game/42",
            "/starfare//game",
            "/starfare/../other/game",
            "/starfare/./game",
            "/starfare/game 42",
            "/starfare/game\\42"
    })
    void rejectsPathsOutsideTheGame(String viewPath) {
        assertThatThrownBy(() -> withPath(viewPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewPath");
    }

    private static MatchSummary running(List<AccountId> participants, List<AccountId> toMove) {
        return withStatus(MatchStatus.RUNNING, participants, toMove);
    }

    private static MatchSummary withStatus(MatchStatus status, List<AccountId> participants, List<AccountId> toMove) {
        return new MatchSummary("starfare", "42", "Andromeda", status, participants, toMove, NOW, "/starfare/game/42");
    }

    private static MatchSummary withPath(String viewPath) {
        return new MatchSummary("starfare", "42", "Andromeda", MatchStatus.OPEN, List.of(), List.of(), NOW, viewPath);
    }
}
