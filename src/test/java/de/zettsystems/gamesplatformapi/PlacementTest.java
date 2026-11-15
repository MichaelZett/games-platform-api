package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
    void staysUntilTheEndUnlessToldOtherwise() {
        assertThat(new Placement(ALICE, 2)).isEqualTo(new Placement(ALICE, 2, Departure.NONE));
    }

    @ParameterizedTest
    @EnumSource(value = Departure.class, names = {"RESIGNED", "ELIMINATED"})
    void theDepartureDoesNotDecideTheWin(Departure departure) {
        Placement placement = new Placement(ALICE, 1, departure);

        assertThat(placement.isWin()).isTrue();
        assertThat(placement.departure()).isEqualTo(departure);
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

    @Test
    void requiresADeparture() {
        assertThatThrownBy(() -> new Placement(ALICE, 1, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("departure");
    }
}
