package de.zettsystems.gamesplatformapi;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A decision a running match waits for from one account: its turn, or an answer to a request of
 * another participant. Several decisions may be open at once, for one account or for several (an
 * alliance vote asks several accounts at the same time).
 *
 * <p>The platform builds reminders and notices from these entries and keeps one notice per account,
 * match, decision and kind. The id therefore stays the same for as long as the decision is open:
 * it survives restarts, and partial actions within the same turn (a build order, part of an answer)
 * neither create a new decision nor reset its deadline. It is not derived from a version counter,
 * a change time or the account alone; {@code round-12} or {@code year-3-phase-2-seat-1} are good ids.
 *
 * <p>The deadline only ever becomes earlier, never later, for example when a game shortens the time
 * left because everyone else has finished. One exception: while the account's turn waits for
 * answers of others, its time stands still; the game reports the decision without a deadline
 * meanwhile and with the deadline from the remaining time afterwards.
 *
 * @param account    the account the match waits for
 * @param decisionId the game's id of the decision, unique within the match and account, at most
 *                   {@value #MAX_ID_LENGTH} characters
 * @param kind       whether it is the account's turn or an answer
 * @param deadline   when the decision is due at the latest; {@code null} if it has no time limit,
 *                   for example in a match against the computer
 * @since 0.4.0
 */
public record PendingDecision(AccountId account, String decisionId, DecisionKind kind, @Nullable Instant deadline) {

    /** The longest decision id the platform stores. */
    public static final int MAX_ID_LENGTH = 100;

    public PendingDecision {
        Objects.requireNonNull(account, "account");
        Checks.text(decisionId, "decisionId");
        Objects.requireNonNull(kind, "kind");
        if (decisionId.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException("decisionId has at most " + MAX_ID_LENGTH + " characters, got "
                    + decisionId.length());
        }
    }

    /** A decision without a time limit. */
    public static PendingDecision unlimited(AccountId account, String decisionId, DecisionKind kind) {
        return new PendingDecision(account, decisionId, kind, null);
    }

    /** The deadline, or empty if the decision has no time limit. */
    public Optional<Instant> dueBy() {
        return Optional.ofNullable(deadline);
    }
}
