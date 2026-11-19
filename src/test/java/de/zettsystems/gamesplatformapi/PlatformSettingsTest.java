package de.zettsystems.gamesplatformapi;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Shows the contract from the side of a game started on its own: settings kept in memory. */
class PlatformSettingsTest {

    private final PlatformSettings settings = new InMemorySettings();

    @Test
    void readsWhatWasSet() {
        settings.set("starfare", "SOUND", "off");

        assertThat(settings.value("starfare", "SOUND")).contains("off");
        assertThat(settings.value("frontier-lords", "SOUND")).isEmpty();
    }

    @Test
    void readsSeveralKeysInTheirOrderAndLeavesOutThoseWithoutValue() {
        settings.set("starfare", "SPEED", "fast");
        settings.set("starfare", "SOUND", "off");

        assertThat(settings.values("starfare", List.of("SOUND", "SIDEBAR", "SPEED")))
                .containsExactly(Map.entry("SOUND", "off"), Map.entry("SPEED", "fast"));
    }

    @Test
    void resetLetsTheDefaultApplyAgain() {
        settings.set("starfare", "SOUND", "off");
        settings.reset("starfare", "SOUND");

        assertThat(settings.value("starfare", "SOUND")).isEmpty();
    }

    private static final class InMemorySettings implements PlatformSettings {

        private final Map<String, String> values = new HashMap<>();

        @Override
        public Optional<String> value(String gameKey, String key) {
            return Optional.ofNullable(values.get(gameKey + "/" + key));
        }

        @Override
        public void set(String gameKey, String key, String value) {
            values.put(gameKey + "/" + key, value);
        }

        @Override
        public void reset(String gameKey, String key) {
            values.remove(gameKey + "/" + key);
        }
    }
}
