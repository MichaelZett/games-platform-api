# Changelog

Notable changes to games-platform-api. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), versioning follows
[SemVer](https://semver.org/); before 1.0.0 a minor version may break the
contract.

## 0.1.0 - 2026-11-15

### Added

- First cut of the contract between the platform and its games:
  - `AccountId`, the platform account of a person.
  - `MatchSummary` with `MatchStatus`, the number of seats and of computer opponents among them, for the lobby.
  - `MatchDirectory`, implemented by every game: the matches of an account and
    the open matches a viewer could join.
  - `MatchChanged`, published when something the lobby shows changes.
  - `MatchEnded` with a `MatchResult`: either `Ranked` (one `Placement` per
    participant in competition ranking, with an optional `Departure` for
    resigned or eliminated players) or `Aborted`. Computer opponents count as
    players but are not listed, so the places of the accounts may skip theirs.
  - Events may come within the game's transaction or, for a game that keeps its
    state outside the database, after the change is stored; listeners handle
    them after the commit or right away without a transaction.
