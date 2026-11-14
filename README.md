# games-platform-api

[![Build](https://github.com/MichaelZett/games-platform-api/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/MichaelZett/games-platform-api/actions/workflows/build.yml?query=branch%3Amain)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=MichaelZett_games-platform-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=MichaelZett_games-platform-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=MichaelZett_games-platform-api&metric=coverage)](https://sonarcloud.io/component_measures?id=MichaelZett_games-platform-api&metric=coverage)

Der Vertrag zwischen einer Spieleplattform und ihren Spielen. Die Plattform
zeigt in ihrer Lobby die Partien aller Spiele und führt eine Statistik über alle
Spiele hinweg; die Spiele liefern ihr dafür Partieübersichten und melden das
Ende einer Partie. Diese Bibliothek legt die gemeinsamen Typen fest.

- Java 25, nur Datensätze und Schnittstellen.
- Keine Abhängigkeit von Spring, Vaadin oder einem anderen Framework; einzige
  Abhängigkeit ist [JSpecify](https://jspecify.dev/) für die
  Null-Annotationen (das Paket ist `@NullMarked`).
- Alle Datensätze sind unveränderlich und prüfen ihre Invarianten beim Anlegen:
  `null` führt zu einer `NullPointerException` mit dem Feldnamen, ein
  ungültiger Wert zu einer `IllegalArgumentException`.

Stand: `0.1.0-SNAPSHOT`, noch nicht veröffentlicht. Die Typen können sich bis
zur ersten Version noch ändern.

## Typen

Alle im Paket `de.zettsystems.gamesplatform.api`.

| Typ | Zweck |
|---|---|
| `AccountId` | Konto einer Person auf der Plattform (positive `long`-ID). |
| `MatchStatus` | `OPEN` (wartet auf Mitspieler), `RUNNING`, `FINISHED`. |
| `MatchSummary` | Partieübersicht: Spielkennung, Partie-ID, Titel, Status, Teilnehmer, wer am Zug ist, letzte Änderung, Pfad zur Partieansicht. |
| `MatchDirectory` | Schnittstelle, die jedes Spiel umsetzt: Partien eines Kontos und offene Partien zum Beitreten. |
| `MatchEnded` | Ereignis Partieende mit der Platzierung jedes Kontos. |
| `Placement` | Platz eines Kontos; 1 ist der beste, Gleichstand teilt sich den Platz (1, 1, 3). |

Wichtige Regeln, die die Datensätze prüfen:

- Die Spielkennung besteht aus Kleinbuchstaben, Ziffern und Bindestrich und
  beginnt mit einem Buchstaben (`starfare`, `frontier-lords`).
- Der Pfad zur Partieansicht liegt im Bereich des Spiels:
  `/<spielkennung>/…`, ohne Schema, Host und Punkt-Segmente.
- Am Zug sein können nur Teilnehmer, und nur in einer laufenden Partie.
- Die Platzierungen eines Partieendes folgen der Wettkampf-Rangfolge
  (1, 1, 3); jedes Konto steht genau einmal darin.

## Nutzung

Abhängigkeit (sobald veröffentlicht; bis dahin per `publishToMavenLocal` oder
`includeBuild`):

```groovy
dependencies {
    implementation 'de.zettsystems:games-platform-api:0.1.0'
}
```

Ein Spiel setzt die Schnittstelle um:

```java
public class StarfareMatches implements MatchDirectory {

    @Override
    public String gameKey() {
        return "starfare";
    }

    @Override
    public List<MatchSummary> matchesOf(AccountId account) {
        // offene und laufende Partien, an denen das Konto teilnimmt, zuletzt geänderte zuerst
    }

    @Override
    public List<MatchSummary> openMatches(AccountId viewer) {
        // offene Partien, denen das Konto beitreten kann
    }
}
```

und meldet das Ende einer Partie, in einer Spring-Anwendung zum Beispiel als
Anwendungsereignis:

```java
events.publishEvent(new MatchEnded("starfare", matchId, Instant.now(), List.of(
        new Placement(AccountId.of(17), 1),
        new Placement(AccountId.of(23), 2))));
```

Die Plattform sammelt alle `MatchDirectory`-Umsetzungen ein und hört auf
`MatchEnded`.

## Bauen

```bash
./gradlew build                  # Kompilieren, Tests, statische Analyse
./gradlew publishToMavenLocal    # für die lokale Erprobung in einer Anwendung
```

Statt `publishToMavenLocal` geht auch ein `includeBuild('../games-platform-api')`
in der `settings.gradle` der Anwendung.

## Lizenz

[Apache License 2.0](LICENSE)
