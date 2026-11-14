package de.zettsystems.gamesplatform.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shows the contract from the side of a game: a small in-memory directory that answers both
 * queries from the same list of summaries.
 */
class MatchDirectoryTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final Instant EARLIER = Instant.parse("2026-11-13T10:00:00Z");
    private static final Instant LATER = Instant.parse("2026-11-14T10:00:00Z");

    private final MatchDirectory directory = new InMemoryDirectory(List.of(
            summary("1", MatchStatus.RUNNING, List.of(ALICE, BOB), List.of(ALICE), LATER),
            summary("2", MatchStatus.OPEN, List.of(BOB), List.of(), EARLIER),
            summary("3", MatchStatus.FINISHED, List.of(ALICE, BOB), List.of(), LATER),
            summary("4", MatchStatus.OPEN, List.of(ALICE), List.of(), EARLIER)));

    @Test
    void listsTheOpenAndRunningMatchesOfAnAccount() {
        assertThat(directory.matchesOf(ALICE)).extracting(MatchSummary::matchId).containsExactly("1", "4");
    }

    @Test
    void listsTheOpenMatchesAViewerCouldJoin() {
        assertThat(directory.openMatches(ALICE)).extracting(MatchSummary::matchId).containsExactly("2");
    }

    @Test
    void answersForItsOwnGameOnly() {
        assertThat(directory.matchesOf(BOB)).extracting(MatchSummary::gameKey).containsOnly(directory.gameKey());
    }

    private static MatchSummary summary(String id, MatchStatus status, List<AccountId> participants,
                                        List<AccountId> toMove, Instant lastChange) {
        return new MatchSummary("starfare", id, "Match " + id, status, participants, toMove, lastChange,
                "/starfare/game/" + id);
    }

    private record InMemoryDirectory(List<MatchSummary> matches) implements MatchDirectory {

        @Override
        public String gameKey() {
            return "starfare";
        }

        @Override
        public List<MatchSummary> matchesOf(AccountId account) {
            return matches.stream()
                    .filter(match -> match.status() != MatchStatus.FINISHED && match.takesPart(account))
                    .sorted((a, b) -> b.lastChange().compareTo(a.lastChange()))
                    .toList();
        }

        @Override
        public List<MatchSummary> openMatches(AccountId viewer) {
            return matches.stream()
                    .filter(match -> match.status() == MatchStatus.OPEN && !match.takesPart(viewer))
                    .toList();
        }
    }
}
