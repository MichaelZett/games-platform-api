package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformAccountsTest {

    private static final AccountId ADA = AccountId.of(1);
    private static final AccountId BOB = AccountId.of(2);
    private static final AccountId GONE = AccountId.of(3);

    private final PlatformAccounts accounts = new PlatformAccounts() {
        @Override
        public Optional<AccountId> current() {
            return Optional.of(ADA);
        }

        @Override
        public Optional<String> displayName(AccountId account) {
            return Optional.ofNullable(Map.of(ADA, "Ada", BOB, "Bob").get(account));
        }

        @Override
        public boolean isTester(AccountId account, String gameKey) {
            return false;
        }
    };

    @Test
    void displayNamesKeepsTheOrderAndLeavesOutUnknownAccounts() {
        assertThat(accounts.displayNames(List.of(BOB, GONE, ADA)))
                .containsExactly(Map.entry(BOB, "Bob"), Map.entry(ADA, "Ada"));
    }

    @Test
    void accountRemovedNamesTheAccount() {
        assertThat(new AccountRemoved(ADA).account()).isEqualTo(ADA);
    }

    @Test
    void accountRemovedRequiresAnAccount() {
        assertThatThrownBy(() -> new AccountRemoved(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("account");
    }
}
