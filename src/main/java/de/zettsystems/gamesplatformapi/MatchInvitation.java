package de.zettsystems.gamesplatformapi;

import java.util.Objects;

/**
 * An invitation to take a seat in a match, as the platform lists it for the invited account. The
 * game keeps the invitation and its seat; accepting or declining happens in the game, behind the
 * match's {@link MatchSummary#viewPath() view path}.
 *
 * @param match     the match the account is invited to
 * @param invitedBy the account that sent the invitation
 */
public record MatchInvitation(MatchSummary match, AccountId invitedBy) {

    public MatchInvitation {
        Objects.requireNonNull(match, "match");
        Objects.requireNonNull(invitedBy, "invitedBy");
    }
}
