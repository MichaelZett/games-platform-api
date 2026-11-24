# games-platform-api

[![Maven Central](https://img.shields.io/maven-central/v/de.zettsystems/games-platform-api)](https://central.sonatype.com/artifact/de.zettsystems/games-platform-api)
[![Build](https://github.com/MichaelZett/games-platform-api/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/MichaelZett/games-platform-api/actions/workflows/build.yml?query=branch%3Amain)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=MichaelZett_games-platform-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=MichaelZett_games-platform-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=MichaelZett_games-platform-api&metric=coverage)](https://sonarcloud.io/component_measures?id=MichaelZett_games-platform-api&metric=coverage)

The contract between a games platform and its games. The platform shows the
matches of all games in one lobby and keeps statistics across all games; the
games supply match summaries for it and announce changes and the end of a
match. This library defines the shared types.

- Java 25, records and interfaces only.
- No dependency on Spring, Vaadin or any other framework. The only dependency
  is [JSpecify](https://jspecify.dev/) for the null annotations (the package is
  `@NullMarked`).
- All records are immutable and check their invariants on construction: a
  missing value throws a `NullPointerException` naming the field, an invalid
  one an `IllegalArgumentException`.

Status: `0.4.0` on Maven Central. Before 1.0.0 a minor version may
break the contract; the changelog says what changed.

## Types

All in the package `de.zettsystems.gamesplatformapi`.

| Type | Purpose |
|---|---|
| `AccountId` | The platform account of a person (a positive `long` id). |
| `MatchStatus` | `OPEN` (waiting for players), `RUNNING`, `FINISHED`. |
| `MatchSummary` | What the lobby shows about a match: game key, match id, title, status, participants, seats and how many of them computer opponents take, who is to move, last change, path of the match view; since 0.4.0 also whether it is a tutorial, its `Tempo` and its pending decisions; since 0.5.0 the variant of the rules it is played with. |
| `Tempo` | How fast a match with several people runs: `LIVE`, `BRISK`, `DAILY`, `RELAXED` (since 0.4.0). Chosen when the match is created; the game sets and enforces the time per turn. |
| `PendingDecision` | A decision a running match waits for from one account: a stable id, `DecisionKind` `TURN` or `ANSWER`, an optional deadline that only ever becomes earlier (since 0.4.0). |
| `MatchDirectory` | Implemented by every game: the matches of an account, the open matches a viewer could join, the account's open invitations (since 0.2.0), its finished matches page by page and what an account may do in a match's chat (since 0.4.0). The later ones are default methods. |
| `MatchInvitation` | An invitation to a match for one account: the match and who invited. Accepting and declining happen in the game. |
| `FinishedMatch` | A finished match in an account's archive: title, end, result, tutorial, path of the review (since 0.4.0). |
| `ChatAccess` | `NONE`, `READ` or `WRITE`: what an account may do in a match's chat; the game decides, the platform keeps and shows the chat (since 0.4.0). |
| `MatchView` | Implemented by a game's view that shows one match: which match it shows, or none (since 0.4.0). |
| `PlatformAccounts` | Implemented by the platform (since 0.3.0): who is signed in, display names, testers of a game; since 0.5.0 whether an account may use a variant of the rules, which has a stage of its own. |
| `PlatformFriends` | Implemented by the platform (since 0.2.0): an account's friends and whether two accounts are friends or blocked. |
| `PlatformPresence` | Implemented by the platform (since 0.2.0): who is online and who may see whom, always for a viewer. |
| `PlatformSettings` | Implemented by the platform (since 0.4.0): the signed-in account's settings per game, across devices. |
| `MatchChanged` | Event: something the lobby shows has changed (join, leave, start, turn, title). Carries no summary; the platform asks the `MatchDirectory` again. |
| `MatchStarted`, `InvitationSent`, `DecisionsOpened`, `TurnMissed` | Events for notices and reminders (since 0.4.0), published in addition to `MatchChanged`. They carry what a notice needs, because the match may have moved on by the time the platform could ask. |
| `MatchEnded` | Event: a match has ended, with a `MatchResult`; since 0.4.0 marked as tutorial where it was one, since 0.5.0 with the variant of the rules. |
| `AccountRemoved` | Event from the platform (since 0.3.0): an account was deleted; games remove its data in the same transaction. |
| `MatchResult` | Sealed: `Ranked` (a `Placement` per participant) or `Aborted` (the participants, no places). |
| `Placement` | The place of an account; 1 is best, ties share a place (1, 1, 3), computer opponents count but are not listed. A `Departure` marks players who resigned, were eliminated or, since 0.4.0, removed for missing turns (`TIMED_OUT`, with the place held at the removal, never a win); it does not change the place. |

Rules the records check:

- A game key consists of lower-case letters, digits and hyphens and starts with
  a letter (`starfare`, `frontier-lords`).
- The path of the match view lies within the game's area: `/<game-key>/…`,
  without scheme, host or dot segments.
- A match sets its number of players (`seats`) and how many of them are computer
  opponents (`computerSeats`); people take the rest (`humanSeats()`, at least
  one). Participants never outnumber the seats for people, and `freeSeats()`
  counts only those. `openMatches` lists only joinable matches: open, with a
  free seat.
- Only participants can be to move, and only in a running match. When a game
  reports pending decisions, every account to move has at least one; a decision
  belongs to a participant and appears once per account and id.
- A ranked result follows competition ranking (1, 1, 3) and names every account
  exactly once. Computer opponents count as players but are not listed, so the
  places of the accounts may skip theirs (an account on place 2 lost to a
  computer opponent); an aborted match names at least one participant. An
  account removed for missing turns keeps the place it held then; that place is
  not checked against the others, and it is never a win.

`MatchResult` may gain further cases in a later minor version. Prefer its
methods (`participants()`, `winners()`, `placeOf(..)`), and give a `switch` over
it a `default` branch.

## Usage

Dependency from Maven Central:

```groovy
dependencies {
    implementation 'de.zettsystems:games-platform-api:0.4.0'
}
```

A game implements the directory:

```java
public class StarfareMatches implements MatchDirectory {

    @Override
    public String gameKey() {
        return "starfare";
    }

    @Override
    public List<MatchSummary> matchesOf(AccountId account) {
        // open and running matches the account takes part in, most recently changed first
    }

    @Override
    public List<MatchSummary> openMatches(AccountId viewer) {
        // joinable matches the viewer is not part of yet
    }
}
```

and announces changes and the end of a match, in a Spring application for
example as application events:

```java
events.publishEvent(new MatchChanged("starfare", matchId, Instant.now()));

events.publishEvent(new MatchEnded("starfare", matchId, Instant.now(), MatchResult.ranked(List.of(
        new Placement(AccountId.of(17), 1),
        new Placement(AccountId.of(23), 2, Departure.RESIGNED)))));

events.publishEvent(new MatchEnded("starfare", matchId, Instant.now(),
        MatchResult.aborted(List.of(AccountId.of(17), AccountId.of(23)))));
```

The platform collects every `MatchDirectory` and listens to `MatchChanged` and
`MatchEnded` after the commit of the game's transaction, or right away if the
game published the event outside a transaction (a game that keeps its state
outside the database transaction publishes once the change is stored). In
Spring that is a `@TransactionalEventListener` with `fallbackExecution = true`.
A game announces the end of a match once, even if it lets the match go on.

### Rules for games

- One `MatchDirectory` per game. The platform calls it often (every lobby
  visit, after every `MatchChanged`), so answer from stored data.
- Publish `MatchChanged` for every change the lobby shows, but not for the
  end; the end is `MatchEnded` alone. An aborted match names at least one
  participant.
- Publish `MatchStarted`, `InvitationSent`, `DecisionsOpened` and `TurnMissed`
  in addition to `MatchChanged`. Publish `DecisionsOpened` again when a
  deadline becomes earlier; the platform keeps one notice per account, match,
  decision id and kind.
- Decision ids stay the same while a decision is open and survive restarts;
  partial actions within a turn neither create a new decision nor reset its
  deadline. Report `toMove` and the pending decisions from the same source.
- Only a missed `TURN` counts. The second missed turn in a match removes the
  account (`TurnMissed.removed`); a computer opponent takes over, and the end
  reports the account with `Departure.TIMED_OUT` and the place it held then.
- The chat belongs to the platform; the game answers `chatAccess` from the
  current state on every call. A view that shows a match implements
  `MatchView` and returns no match when the match is invalid or not
  accessible.
- Read and write settings only through `PlatformSettings`; the account always
  comes from the session.
- Report the end of tutorials (`MatchEnded.tutorial`) only to a platform built
  against 0.4.0 or later; an older one would count them in its statistics.
- A game with variants of its rules declares them with their first stage in its
  game descriptor, asks `mayUseVariant` before a variant is chosen, joined or
  entered, and names the variant in `MatchSummary` and `MatchEnded`. The same
  key in all three places.

## Building

```bash
./gradlew build                  # compile, tests, static analysis
./gradlew publishToMavenLocal    # to try it out in an application
```

Instead of `publishToMavenLocal`, an application can also use
`includeBuild('../games-platform-api')` in its `settings.gradle`.

## Licence

[Apache License 2.0](LICENSE)
