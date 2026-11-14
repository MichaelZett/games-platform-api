package de.zettsystems.gamesplatform.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** The invariants several records share, in one place so that they cannot drift apart. */
final class Checks {

    /** Lower-case letters, digits and hyphens, starting with a letter, as in the game descriptor. */
    private static final Pattern GAME_KEY = Pattern.compile("[a-z][a-z0-9-]*");

    private Checks() {
    }

    static String gameKey(String gameKey) {
        Objects.requireNonNull(gameKey, "gameKey");
        if (!GAME_KEY.matcher(gameKey).matches()) {
            throw new IllegalArgumentException(
                    "gameKey must consist of lower-case letters, digits and hyphens, starting with a letter: '"
                            + gameKey + "'");
        }
        return gameKey;
    }

    static String text(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Copies the list unmodifiably and rejects null elements and duplicates. */
    static <T> List<T> distinct(List<T> values, String name) {
        Objects.requireNonNull(values, name);
        List<T> copy = List.copyOf(values);
        if (new HashSet<>(copy).size() != copy.size()) {
            throw new IllegalArgumentException(name + " must not contain duplicates: " + copy);
        }
        return copy;
    }

    /**
     * A path inside the platform that leads into the game's own area: it starts with
     * {@code /<gameKey>/}, carries no scheme or host and no {@code .}, {@code ..} or empty
     * segments. That keeps a game from linking into another game or off the platform.
     */
    static String viewPath(String viewPath, String gameKey) {
        Objects.requireNonNull(viewPath, "viewPath");
        String prefix = "/" + gameKey + "/";
        URI uri = parse(viewPath);
        // Without a scheme the URI is hierarchical, so it always has a (possibly empty) path.
        String path = uri.getRawPath();
        if (uri.getScheme() != null || uri.getRawAuthority() != null
                || !path.startsWith(prefix) || path.length() == prefix.length()
                || path.contains("//") || !uri.normalize().getRawPath().equals(path)) {
            throw new IllegalArgumentException(
                    "viewPath must be a path below " + prefix + " without dot segments: '" + viewPath + "'");
        }
        return viewPath;
    }

    private static URI parse(String viewPath) {
        try {
            return new URI(viewPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("viewPath is not a valid path: '" + viewPath + "'", e);
        }
    }
}
