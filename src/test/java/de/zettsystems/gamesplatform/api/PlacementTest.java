package de.zettsystems.gamesplatform.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlacementTest {

    private static final AccountId ALICE = AccountId.of(1);

    @Test
    void placeOneIsAWin() {
        assertThat(new Placement(ALICE, 1).isWin()).isTrue();
    }

    @Test
    void anyOtherPlaceIsNoWin() {
        assertThat(new Placement(ALICE, 2).isWin()).isFalse();
    }

    @Test
    void rejectsPlaceZero() {
        assertThatThrownBy(() -> new Placement(ALICE, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("starts at 1");
    }

    @Test
    void requiresAnAccount() {
        assertThatThrownBy(() -> new Placement(null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("account");
    }
}
