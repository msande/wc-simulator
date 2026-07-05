# World Cup Simulator — Design Spec

**Date:** 2026-07-04
**Status:** Approved for planning
**Platform:** Native Android (Kotlin + Jetpack Compose), phone-first, offline, no accounts.

## 1. Concept

A text-only career-management game. You are a football head coach. You start at
age 30 and can coach for up to 50 seasons; at the end (or on early retirement)
your career closes with a randomized dramatic freak-accident death and a final
career score. Scores are saved to a persistent high-score table.

No graphical match simulation — everything is text: menus, stat screens, and
narrative match commentary lines.

## 2. Goals & non-goals

**Goals**
- A complete, replayable single-player career loop that runs fully offline.
- All player-requested systems: pick starting country, browse player stats
  (club + WC), getting fired, salary negotiation (earnings count toward score),
  and a random special ability at game start.
- A pure-Kotlin, unit-testable game engine independent of Android.

**Non-goals (YAGNI)**
- No graphics, animation, or real-time match view.
- No multiplayer, no network, no cloud save, no accounts.
- No real player names/likenesses (players are generated/fictional).
- No in-app purchases or ads.

## 3. Architecture

- **Native Android**, Kotlin, **Jetpack Compose** UI, single `Activity`, screen
  navigation via Compose Navigation.
- **Game engine = pure Kotlin, no Android imports.** Lives in its own package
  (`engine/`). Deterministic given a seed + inputs, so it is fully unit-testable
  on the JVM without a device or emulator. All rules live here: world
  generation, match simulation, career progression, scoring.
- **UI layer** is thin: Compose screens + a `ViewModel` per major screen that
  calls the engine and exposes state. UI holds no game rules.
- **Persistence:** local only.
  - Current career save (one active slot) serialized to JSON via
    `kotlinx.serialization`, stored in app files dir (or DataStore).
  - High-score table persisted the same way.
- **Build:** Gradle wrapper committed so the project builds from Android Studio
  regardless of a globally installed Gradle. Min SDK 24, target current stable.

### Module/package sketch
```
app/
  ui/            Compose screens + ViewModels (Android)
  engine/        pure-Kotlin rules (JVM-testable)
    world/       country/club/player generation
    career/      season loop, contracts, firing, aging, death
    match/       match + tournament simulation, commentary
    score/       scoring + high-score model
  data/          persistence (save/load JSON), repositories
  test/          JVM unit tests for engine
```

## 4. Game world (generated per new career)

- **Countries:** a fixed list of ~48 **real country names**, each with a base
  strength rating (used to seed squad quality and difficulty). Country names are
  the only "real" data; everything else is generated.
- **Clubs:** generated fictional clubs (name + strength), grouped into a handful
  of fictional leagues, so players have a club identity.
- **Players:** generated. Each player has:
  - Identity: generated name, age, nationality, position (GK/DEF/MID/FWD),
    club affiliation.
  - Ratings: `overall` plus sub-stats — `pace`, `stamina`, `finishing`,
    `passing`, `defending`, `goalkeeping`.
  - **Career stats, tracked over time and viewable:**
    - **Club stats:** appearances, goals, assists (accumulated per season).
    - **WC stats:** World Cup appearances, goals, assists, tournaments played.
  - Aging: players age each season, ratings drift up then decline; players
    retire and are replaced by generated regens.
- **Your nation's player pool** is what you pick squads from. Pools refresh via
  aging + regen so a 50-season career stays fresh.

## 5. Career loop

Start: age 30, choose a country, receive one random special ability, sign a
first contract (starting salary). Then repeat each season until 50 seasons pass,
you retire, or you run out of jobs:

1. **Pre-season setup**
   - Select squad (starting XI + bench) from your nation's pool.
   - Choose **formation** (e.g. 4-4-2, 4-3-3, 3-5-2, 5-3-2, 4-2-3-1).
   - Choose **training focus** (e.g. Attack / Defense / Fitness / Balanced) —
     applies small stat modifiers / development effects for the season.
   - Choose **tactics**: an **aggression ↔ defense** slider (affects goals
     scored vs conceded variance).
2. **Qualification** — a short simulated campaign vs regional opponents. Pass a
   threshold to reach the World Cup; fail = no WC this cycle (hurts the board
   verdict).
3. **World Cup** (only if qualified) — classic **32-team** format:
   group stage (8 groups of 4, top 2 advance) → Round of 16 → Quarterfinal →
   Semifinal → Final. You proceed round by round with text results.
4. **Season review**
   - Board compares results to the season **expectation**.
   - **Firing:** badly missing expectation risks the sack. If fired, you
     job-hunt: offered one or more openings (often weaker nations); accepting
     starts a new contract. No offers for several seasons can end the career.
   - **Contract / salary negotiation** on renewal or new job (see §6).
5. Advance one season (age +1). At 50 seasons or on retirement → **death &
   scoring** (§7).

## 6. The requested systems

### 6.1 Special ability (random at start)
Exactly one, assigned uniformly at random at new-career:
- **Attacking Mastermind** — offensive output bonus.
- **Iron Wall** — defensive solidity bonus.
- **Fitness Guru** — stamina/late-game bonus.
- **Youth Whisperer** — better regen player quality.
- **Motivator** — small all-round bonus in knockout matches.
- **Negotiator** — extra salary leverage.

Each is a passive numeric modifier consumed by the engine. Shown on the career
HUD. (Set is easy to extend.)

### 6.2 Getting fired
Each contract sets a season **expectation** derived from nation strength (e.g.
"reach the QF", "qualify"). Missing it accrues board dissatisfaction; a bad miss
(especially failing to qualify) can trigger dismissal. Firing sends you to the
job market.

### 6.3 Salary negotiation
On each new/renewed contract you negotiate: the board offers a figure based on
your reputation (built from trophies + WC runs + longevity); you can accept,
push for more (risk a lower final offer), or walk. The **Negotiator** ability
and a strong track record improve leverage. **All salary earned each season is
banked into `careerEarnings`.**

### 6.4 Player stat browser
A screen to browse your nation's players (and optionally scout others), sortable,
showing ratings + **club stats** and **WC stats** side by side.

## 7. Death, scoring & high scores

- **End trigger:** 50 seasons elapsed, voluntary retirement, or an extended
  spell with no job offers.
- **Death blurb:** randomized dramatic freak-accident text (only if the career
  ends by reaching the 50-season cap or naturally — retirement gets a retirement
  blurb; both lead to scoring).
- **Final career score:**
  ```
  score = wcProgressPoints        // summed over every WC entered, by furthest round reached
        + earningsPoints          // careerEarnings scaled into points
        + bonuses                 // trophies won, longevity (seasons survived), qualification streaks
  ```
  Stage points (per WC): Group exit < Ro16 < QF < SF < Runner-up < Champion,
  monotonically increasing. Exact constants are tunable and centralized in one
  `ScoringConfig`.
- **High-score table:** persistent, sortable, stores name, country(ies) coached,
  seasons coached, trophies, final score, and cause of death. Shown on the main
  menu and after each career ends.

## 8. Match & tournament simulation (text)

- **Match model:** each team gets an effective strength from squad overall +
  formation fit + training + tactics slider + special ability + home/context,
  then a scoreline is generated with weighted randomness (Poisson-style goal
  draw). Tactics slider trades expected-goals-for vs expected-goals-against and
  variance.
- **Commentary:** each simulated match emits a short list of **narrative event
  lines** ("73' — your striker latches onto a through ball and finishes!") so a
  result reads like a match without graphics. Events also update player club/WC
  stats (goals, assists, appearances).
- **Determinism:** the engine takes a seed so matches are reproducible and
  testable.

## 9. Screens (UI)

1. **Main menu** — New Career, Continue (if save exists), High Scores, About.
2. **New career setup** — choose country → reveal random ability → first contract.
3. **Career HUD** — age/season, nation, ability, expectation, earnings, menu.
4. **Squad & tactics** — pick XI/bench, formation, training, aggression slider.
5. **Player stats browser** — sortable list, club + WC stats.
6. **Match / tournament view** — text results + commentary, advance button.
7. **Season review** — board verdict, firing, contract/salary negotiation.
8. **Job market** — offers after firing.
9. **Career end** — death/retirement blurb + final score breakdown.
10. **High scores** — persistent table.

## 10. Testing strategy

- **Engine unit tests (JVM, no device):** world generation validity, match
  sim distributions (seeded determinism + sane averages), tournament bracket
  correctness (right number of matches, single champion), aging/regen keeps
  pools viable across 50 seasons, scoring monotonicity, firing/expectation
  logic, salary accumulation.
- **ViewModel tests** where logic warrants.
- Manual smoke test of a full career on device/emulator before calling done.

## 11. Risks / open items

- **Toolchain unverified from this environment** (no Java/SDK detected). Build
  will rely on the user's Android Studio (bundled JDK + SDK manager). The
  committed Gradle wrapper mitigates Gradle version issues. Command-line builds
  may need `local.properties` / `ANDROID_HOME` pointing at the SDK.
- Balancing (difficulty, scoring constants, firing thresholds) will need tuning
  passes; constants are centralized to make this cheap.
- 50-season longevity depends on solid aging/regen — covered by tests.
