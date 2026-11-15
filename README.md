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

Status: `0.1.0` on Maven Central. Before 1.0.0 a minor version may break the
contract; the changelog says what changed.

## Types

All in the package `de.zettsystems.gamesplatformapi`.

| Type | Purpose |
|---|---|
| `AccountId` | The platform account of a person (a positive `long` id). |
| `MatchStatus` | `OPEN` (waiting for players), `RUNNING`, `FINISHED`. |
| `MatchSummary` | What the lobby shows about a match: game key, match id, title, status, participants, seats and how many of them computer opponents take, who is to move, last change, path of the match view. |
| `MatchDirectory` | Implemented by every game: the matches of an account and the open matches a viewer could join. |
| `MatchChanged` | Event: something the lobby shows has changed (join, leave, start, turn, title). Carries no summary; the platform asks the `MatchDirectory` again. |
| `MatchEnded` | Event: a match has ended, with a `MatchResult`. |
| `MatchResult` | Sealed: `Ranked` (a `Placement` per participant) or `Aborted` (the participants, no places). |
| `Placement` | The place of an account; 1 is best, ties share a place (1, 1, 3), computer opponents count but are not listed. A `Departure` marks players who resigned or were eliminated; it does not change the place. |

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
- Only participants can be to move, and only in a running match.
- A ranked result follows competition ranking (1, 1, 3) and names every account
  exactly once. Computer opponents count as players but are not listed, so the
  places of the accounts may skip theirs (an account on place 2 lost to a
  computer opponent); an aborted match names at least one participant.

`MatchResult` may gain further cases in a later minor version. Prefer its
methods (`participants()`, `winners()`, `placeOf(..)`), and give a `switch` over
it a `default` branch.

## Usage

Dependency from Maven Central:

```groovy
dependencies {
    implementation 'de.zettsystems:games-platform-api:0.1.0'
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

## Building

```bash
./gradlew build                  # compile, tests, static analysis
./gradlew publishToMavenLocal    # to try it out in an application
```

Instead of `publishToMavenLocal`, an application can also use
`includeBuild('../games-platform-api')` in its `settings.gradle`.

## Licence

[Apache License 2.0](LICENSE)
