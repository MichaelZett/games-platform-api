package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatAccessTest {

    @Test
    void noneAllowsNothing() {
        assertThat(ChatAccess.NONE.canRead()).isFalse();
        assertThat(ChatAccess.NONE.canWrite()).isFalse();
    }

    @Test
    void readAllowsOnlyReading() {
        assertThat(ChatAccess.READ.canRead()).isTrue();
        assertThat(ChatAccess.READ.canWrite()).isFalse();
    }

    @Test
    void writeAllowsBoth() {
        assertThat(ChatAccess.WRITE.canRead()).isTrue();
        assertThat(ChatAccess.WRITE.canWrite()).isTrue();
    }
}
