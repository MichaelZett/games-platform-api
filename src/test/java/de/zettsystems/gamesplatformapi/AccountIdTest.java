package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountIdTest {

    @Test
    void keepsAPositiveId() {
        assertThat(AccountId.of(42)).isEqualTo(new AccountId(42L)).extracting(AccountId::value).isEqualTo(42L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, Long.MIN_VALUE})
    void rejectsIdsThatAreNotPositive(long value) {
        assertThatThrownBy(() -> AccountId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
