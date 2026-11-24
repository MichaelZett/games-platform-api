package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchVariantTest {

    private static final AccountId ALICE = AccountId.of(1);
    private static final Instant NOW = Instant.parse("2026-11-24T10:15:30Z");
    private static final MatchResult RANKED = MatchResult.ranked(List.of(new Placement(ALICE, 1)));

    @Test
    void aSummaryKeepsItsVariant() {
        MatchSummary summary = summary("spaceward");

        assertThat(summary.variant()).isEqualTo("spaceward");
    }

    @Test
    void earlierSummaryConstructorsLeaveTheVariantEmpty() {
        MatchSummary withTempo = new MatchSummary("starfare", "42", "Andromeda", MatchStatus.OPEN, List.of(ALICE), 4,
                0, List.of(), NOW, "/starfare/game/42", true, Tempo.DAILY, List.of());
        MatchSummary plain = new MatchSummary("starfare", "42", "Andromeda", MatchStatus.OPEN, List.of(ALICE), 4, 0,
                List.of(), NOW, "/starfare/game/42");

        assertThat(withTempo.variant()).isNull();
        assertThat(withTempo.tutorial()).isTrue();
        assertThat(withTempo.tempo()).isEqualTo(Tempo.DAILY);
        assertThat(plain.variant()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Spaceward", "1classic", "sector forces", "-classic"})
    void aSummaryRejectsAMalformedVariant(String variant) {
        assertThatThrownBy(() -> summary(variant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("variant must consist of lower-case letters, digits and hyphens, starting with a letter: '"
                        + variant + "'");
    }

    @Test
    void aSummaryAcceptsAVariantWithDigitsAndHyphens() {
        assertThat(summary("sector-forces2").variant()).isEqualTo("sector-forces2");
    }

    @Test
    void anEndKeepsItsVariant() {
        MatchEnded ended = new MatchEnded("starfare", "42", NOW, RANKED, false, "classic");

        assertThat(ended.variant()).isEqualTo("classic");
    }

    @Test
    void earlierEndConstructorsLeaveTheVariantEmpty() {
        MatchEnded tutorial = new MatchEnded("starfare", "42", NOW, RANKED, true);

        assertThat(tutorial.variant()).isNull();
        assertThat(tutorial.tutorial()).isTrue();
        assertThat(new MatchEnded("starfare", "42", NOW, RANKED).variant()).isNull();
    }

    @Test
    void anEndRejectsAMalformedVariant() {
        assertThatThrownBy(() -> new MatchEnded("starfare", "42", NOW, RANKED, false, "Classic"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("variant must consist of lower-case letters, digits and hyphens, starting with a letter:"
                        + " 'Classic'");
    }

    @Test
    void aPlatformWithoutStagesPerVariantOpensEveryVariant() {
        PlatformAccounts accounts = new PlatformAccounts() {
            @Override
            public Optional<AccountId> current() {
                return Optional.empty();
            }

            @Override
            public Optional<String> displayName(AccountId account) {
                return Optional.empty();
            }

            @Override
            public boolean isTester(AccountId account, String gameKey) {
                return false;
            }
        };

        assertThat(accounts.mayUseVariant(ALICE, "starfare", "spaceward")).isTrue();
        assertThat(accounts.variantStage("starfare", "spaceward")).isEqualTo(LifecycleStage.PRODUCTION);
    }

    private static MatchSummary summary(String variant) {
        return new MatchSummary("starfare", "42", "Andromeda", MatchStatus.OPEN, List.of(ALICE), 4, 0, List.of(), NOW,
                "/starfare/game/42", false, null, List.of(), variant);
    }
}
