# World Cup Simulator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a text-only Android career game where you coach a national team for up to 50 seasons, qualify for and progress through World Cups, manage squads/tactics/contracts, and chase a persistent high score.

**Architecture:** A pure-Kotlin, deterministic, JVM-testable game engine (`engine/`) holds all rules (world generation, match/tournament simulation, career loop, scoring). A thin Jetpack Compose UI with one ViewModel per screen calls the engine and renders state. Persistence is local JSON via kotlinx.serialization.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose (Material 3), Compose Navigation, kotlinx.serialization, JUnit4 for JVM unit tests. Min SDK 24.

**Spec:** `docs/superpowers/specs/2026-07-04-wc-simulator-design.md`

---

## Conventions for every task

- Package root: `com.wcsim`. Engine code under `com.wcsim.engine.*` must have **zero Android imports**.
- Unit tests live in `app/src/test/java/com/wcsim/...` and run with `./gradlew test` (JVM, no device).
- Engine randomness always goes through an injected `kotlin.random.Random` (seeded in tests) — **never** call `Random.Default` or `Math.random()` inside the engine.
- After each task, run the stated command, confirm expected output, then commit with the given message.
- On Windows use `gradlew.bat`; commands below show `./gradlew` — use whichever matches your shell.

---

## Phase 0 — Project scaffolding

### Task 0: Android project skeleton that builds and tests

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts` (root), `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/wcsim/MainActivity.kt`
- Create: `app/src/test/java/com/wcsim/SmokeTest.kt`
- Create: `.gitignore`
- Create: Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`)

- [ ] **Step 1: Create the Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.7` (or copy a wrapper from any recent Android project). Confirm `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` exist.

- [ ] **Step 2: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}
rootProject.name = "WCSimulator"
include(":app")
```

- [ ] **Step 3: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.20"
composeBom = "2024.09.02"
coreKtx = "1.13.1"
lifecycle = "2.8.5"
activityCompose = "1.9.2"
navCompose = "2.8.0"
serialization = "1.7.1"
junit = "4.13.2"

[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 4: Write root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **Step 5: Write `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.wcsim"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.wcsim"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
}
```

- [ ] **Step 6: Write `.gitignore`**

```
*.iml
.gradle/
/local.properties
.idea/
build/
/captures/
.externalNativeBuild/
.cxx/
*.apk
```

- [ ] **Step 7: Write `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="WC Simulator"
        android:theme="@android:style/Theme.Material.Light">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 8: Write minimal `MainActivity.kt`**

```kotlin
package com.wcsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { Text("WC Simulator") }
            }
        }
    }
}
```

- [ ] **Step 9: Write `SmokeTest.kt`**

```kotlin
package com.wcsim

import org.junit.Assert.assertEquals
import org.junit.Test

class SmokeTest {
    @Test fun arithmetic_works() { assertEquals(4, 2 + 2) }
}
```

- [ ] **Step 10: Build and test**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, `SmokeTest` passes.

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "chore: scaffold Android project with Compose and JVM test setup"
```

---

## Phase 1 — Domain models

### Task 1: Core enums and value types

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/model/Enums.kt`
- Test: `app/src/test/java/com/wcsim/engine/model/EnumsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumsTest {
    @Test fun formation_slot_counts_sum_to_ten_outfield() {
        for (f in Formation.entries) {
            assertEquals("${f.name} must have 10 outfield slots",
                10, f.defenders + f.midfielders + f.forwards)
        }
    }

    @Test fun positions_cover_all_lines() {
        assertEquals(4, Position.entries.size) // GK, DEF, MID, FWD
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.EnumsTest"`
Expected: FAIL — unresolved reference `Formation` / `Position`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class Position { GK, DEF, MID, FWD }

/** Outfield line counts (goalkeeper is always 1 and implicit). */
@Serializable
enum class Formation(val defenders: Int, val midfielders: Int, val forwards: Int) {
    F_442(4, 4, 2),
    F_433(4, 3, 3),
    F_352(3, 5, 2),
    F_532(5, 3, 2),
    F_4231(4, 5, 1); // 2 defensive + 3 attacking mids modeled as 5 mids

    val label: String get() = when (this) {
        F_442 -> "4-4-2"; F_433 -> "4-3-3"; F_352 -> "3-5-2"
        F_532 -> "5-3-2"; F_4231 -> "4-2-3-1"
    }
}

@Serializable
enum class TrainingFocus { ATTACK, DEFENSE, FITNESS, BALANCED }

@Serializable
enum class WcStage { GROUP, ROUND16, QUARTER, SEMI, FINAL, CHAMPION }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.EnumsTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/model/Enums.kt app/src/test/java/com/wcsim/engine/model/EnumsTest.kt
git commit -m "feat: add core game enums (position, formation, training, WC stage)"
```

### Task 2: Player, Club, Country, and stat records

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/model/Player.kt`
- Create: `app/src/main/java/com/wcsim/engine/model/Country.kt`
- Test: `app/src/test/java/com/wcsim/engine/model/PlayerTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTest {
    private fun ratings() = Ratings(pace = 70, stamina = 70, finishing = 70,
        passing = 70, defending = 70, goalkeeping = 20)

    @Test fun overall_is_position_weighted() {
        val fwd = Player(1, "A", 25, Position.FWD, clubId = 1, ratings = ratings())
        val def = Player(2, "B", 25, Position.DEF, clubId = 1, ratings = ratings())
        // Same raw ratings but finishing-weighted FWD should differ from defending-weighted DEF only if weights differ.
        assertTrue(fwd.overall in 40..99)
        assertTrue(def.overall in 40..99)
    }

    @Test fun career_stats_accumulate() {
        var p = Player(1, "A", 25, Position.FWD, clubId = 1, ratings = ratings())
        p = p.addClubStats(apps = 30, goals = 12, assists = 5)
        p = p.addWcStats(apps = 7, goals = 4, assists = 2)
        assertEquals(30, p.clubStats.appearances)
        assertEquals(12, p.clubStats.goals)
        assertEquals(4, p.wcStats.goals)
        assertEquals(1, p.wcStats.tournaments) // addWcStats bumps tournaments by 1
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.PlayerTest"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Write minimal implementation** — `Player.kt`

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Ratings(
    val pace: Int, val stamina: Int, val finishing: Int,
    val passing: Int, val defending: Int, val goalkeeping: Int,
)

@Serializable
data class StatLine(
    val appearances: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val tournaments: Int = 0, // only meaningful for WC stats
)

@Serializable
data class Player(
    val id: Int,
    val name: String,
    val age: Int,
    val position: Position,
    val clubId: Int,
    val ratings: Ratings,
    val clubStats: StatLine = StatLine(),
    val wcStats: StatLine = StatLine(),
) {
    /** Position-weighted 40..99 overall. */
    val overall: Int get() {
        val r = ratings
        val raw = when (position) {
            Position.GK -> r.goalkeeping * 0.85 + r.defending * 0.15
            Position.DEF -> r.defending * 0.55 + r.pace * 0.15 + r.stamina * 0.15 + r.passing * 0.15
            Position.MID -> r.passing * 0.45 + r.stamina * 0.25 + r.defending * 0.15 + r.finishing * 0.15
            Position.FWD -> r.finishing * 0.55 + r.pace * 0.25 + r.passing * 0.20
        }
        return raw.toInt().coerceIn(40, 99)
    }

    fun addClubStats(apps: Int, goals: Int, assists: Int) = copy(
        clubStats = clubStats.copy(
            appearances = clubStats.appearances + apps,
            goals = clubStats.goals + goals,
            assists = clubStats.assists + assists,
        )
    )

    fun addWcStats(apps: Int, goals: Int, assists: Int) = copy(
        wcStats = wcStats.copy(
            appearances = wcStats.appearances + apps,
            goals = wcStats.goals + goals,
            assists = wcStats.assists + assists,
            tournaments = wcStats.tournaments + 1,
        )
    )
}
```

- [ ] **Step 4: Write `Country.kt`**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Club(val id: Int, val name: String, val strength: Int)

@Serializable
data class Country(
    val id: Int,
    val name: String,
    /** Base footballing strength 40..90, drives squad quality + difficulty. */
    val baseStrength: Int,
)
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.PlayerTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/model/
git commit -m "feat: add Player/Club/Country domain models with career stat tracking"
```

### Task 3: Squad, tactics, and special ability

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/model/Tactics.kt`
- Create: `app/src/main/java/com/wcsim/engine/model/SpecialAbility.kt`
- Test: `app/src/test/java/com/wcsim/engine/model/TacticsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TacticsTest {
    @Test fun aggression_is_clamped_0_to_100() {
        assertEquals(0, Tactics(aggression = -10).aggression)
        assertEquals(100, Tactics(aggression = 250).aggression)
    }

    @Test fun every_ability_has_a_description() {
        for (a in SpecialAbility.entries) {
            assertTrue(a.displayName.isNotBlank())
            assertTrue(a.description.isNotBlank())
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.TacticsTest"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Write `Tactics.kt`**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

/** aggression 0 = ultra-defensive, 100 = all-out attack. */
@Serializable
data class Tactics(
    val formation: Formation = Formation.F_442,
    val training: TrainingFocus = TrainingFocus.BALANCED,
    private val rawAggression: Int = 50,
) {
    constructor(aggression: Int) : this(rawAggression = aggression)
    val aggression: Int get() = rawAggression.coerceIn(0, 100)
}

/** A chosen starting XI: exactly 11 player ids, first is GK by convention. */
@Serializable
data class Squad(val playerIds: List<Int>) {
    val isValid: Boolean get() = playerIds.size == 11 && playerIds.toSet().size == 11
}
```

- [ ] **Step 4: Write `SpecialAbility.kt`**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class SpecialAbility(val displayName: String, val description: String) {
    ATTACKING_MASTERMIND("Attacking Mastermind", "Your teams score more freely."),
    IRON_WALL("Iron Wall", "Your defense concedes fewer goals."),
    FITNESS_GURU("Fitness Guru", "Better stamina; you dominate late in matches."),
    YOUTH_WHISPERER("Youth Whisperer", "Emerging players develop to a higher ceiling."),
    MOTIVATOR("Motivator", "Your teams overperform in knockout matches."),
    NEGOTIATOR("Negotiator", "You command higher salaries at the table.");
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.model.TacticsTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/model/Tactics.kt app/src/main/java/com/wcsim/engine/model/SpecialAbility.kt app/src/test/java/com/wcsim/engine/model/TacticsTest.kt
git commit -m "feat: add squad, tactics, and special ability models"
```

---

## Phase 2 — World generation

### Task 4: Name pools and country list

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/world/NameData.kt`
- Test: `app/src/test/java/com/wcsim/engine/world/NameDataTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.world

import org.junit.Assert.assertTrue
import org.junit.Test

class NameDataTest {
    @Test fun has_at_least_32_countries() {
        assertTrue(NameData.COUNTRIES.size >= 32)
    }
    @Test fun name_pools_are_nonempty() {
        assertTrue(NameData.FIRST_NAMES.size >= 20)
        assertTrue(NameData.LAST_NAMES.size >= 20)
        assertTrue(NameData.CLUB_PREFIXES.isNotEmpty())
        assertTrue(NameData.CLUB_SUFFIXES.isNotEmpty())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.world.NameDataTest"`
Expected: FAIL — unresolved reference `NameData`.

- [ ] **Step 3: Write minimal implementation**

Create `NameData.kt` with an `object NameData` exposing:
- `COUNTRIES: List<Pair<String, Int>>` — at least 32 real country names paired with a base strength (40..90). Include a spread, e.g. `"Brazil" to 88, "France" to 87, ... "Canada" to 62, ...`.
- `FIRST_NAMES: List<String>` — ≥ 20 generic first names.
- `LAST_NAMES: List<String>` — ≥ 20 generic last names.
- `CLUB_PREFIXES: List<String>` (e.g. "Real", "Athletic", "Inter", "Sporting") and `CLUB_SUFFIXES: List<String>` (e.g. "United", "City", "FC", "Rovers").

```kotlin
package com.wcsim.engine.world

object NameData {
    val COUNTRIES: List<Pair<String, Int>> = listOf(
        "Brazil" to 88, "France" to 87, "Argentina" to 87, "England" to 85,
        "Spain" to 85, "Germany" to 84, "Portugal" to 84, "Netherlands" to 83,
        "Belgium" to 82, "Italy" to 82, "Croatia" to 80, "Uruguay" to 79,
        "Colombia" to 78, "Mexico" to 77, "Switzerland" to 76, "USA" to 75,
        "Senegal" to 75, "Denmark" to 75, "Japan" to 74, "Morocco" to 74,
        "Serbia" to 73, "Poland" to 73, "South Korea" to 72, "Sweden" to 72,
        "Nigeria" to 72, "Ghana" to 71, "Ecuador" to 70, "Australia" to 69,
        "Canada" to 68, "Egypt" to 68, "Cameroon" to 68, "Chile" to 70,
        "Peru" to 69, "Ivory Coast" to 71, "Wales" to 70, "Norway" to 71,
    )
    val FIRST_NAMES = listOf(
        "Luca","Mateo","Kai","Diego","Omar","Noah","Leo","Ivan","Hugo","Marco",
        "Ravi","Yuki","Tariq","Andre","Felix","Emre","Nico","Samir","Dario","Bruno",
        "Ola","Pavel","Sven","Kofi","Juan",
    )
    val LAST_NAMES = listOf(
        "Silva","Kane","Muller","Sato","Diallo","Rossi","Nowak","Haaland","Mbappe","Costa",
        "Okafor","Petrov","Andersen","Kim","Torres","Vidic","Owusu","Reyes","Larsson","Fofana",
        "Yilmaz","Novak","Blanc","Marino","Adeyemi",
    )
    val CLUB_PREFIXES = listOf("Real","Athletic","Inter","Sporting","Racing","Dynamo","Olympic","North","Port","Royal")
    val CLUB_SUFFIXES = listOf("United","City","FC","Rovers","Athletic","Town","Wanderers","Albion")
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.world.NameDataTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/world/NameData.kt app/src/test/java/com/wcsim/engine/world/NameDataTest.kt
git commit -m "feat: add country list and generated-name data pools"
```

### Task 5: World generator (countries, clubs, players)

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/world/WorldGenerator.kt`
- Create: `app/src/main/java/com/wcsim/engine/model/World.kt`
- Test: `app/src/test/java/com/wcsim/engine/world/WorldGeneratorTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.world

import com.wcsim.engine.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WorldGeneratorTest {
    private fun gen() = WorldGenerator.generate(Random(42))

    @Test fun deterministic_for_same_seed() {
        val a = WorldGenerator.generate(Random(7))
        val b = WorldGenerator.generate(Random(7))
        assertEquals(a.players.map { it.name }, b.players.map { it.name })
    }

    @Test fun every_country_has_a_viable_pool() {
        val w = gen()
        for (c in w.countries) {
            val pool = w.playersOf(c.id)
            assertTrue("${c.name} needs >=18 players", pool.size >= 18)
            assertTrue("${c.name} needs a keeper", pool.any { it.position == Position.GK })
        }
    }

    @Test fun stronger_countries_have_stronger_squads() {
        val w = gen()
        val sorted = w.countries.sortedByDescending { it.baseStrength }
        val top = w.playersOf(sorted.first().id).map { it.overall }.average()
        val bottom = w.playersOf(sorted.last().id).map { it.overall }.average()
        assertTrue("top squad avg $top should exceed bottom $bottom", top > bottom)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.world.WorldGeneratorTest"`
Expected: FAIL — unresolved references `WorldGenerator`, `World`.

- [ ] **Step 3: Write `World.kt`**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class World(
    val countries: List<Country>,
    val clubs: List<Club>,
    val players: List<Player>,
) {
    private val byCountry: Map<Int, List<Player>> by lazy {
        // nationality is encoded by the generator via clubId->country mapping is not used;
        // instead players carry their country via a parallel index built at generation.
        players.groupBy { playerCountry[it.id] ?: -1 }
    }
    // Filled by generator through withCountryIndex; defaults empty.
    var playerCountry: Map<Int, Int> = emptyMap()

    fun playersOf(countryId: Int): List<Player> =
        players.filter { playerCountry[it.id] == countryId }

    fun country(id: Int): Country = countries.first { it.id == id }
    fun player(id: Int): Player = players.first { it.id == id }
}
```

Note: to keep `World` serializable and simple, store nationality as a field on `Player`. Adjust: add `val countryId: Int` to `Player` (Task 2) — update the `Player` data class and its test constructor calls to pass `countryId = 0`. Then `playersOf` becomes `players.filter { it.countryId == countryId }` and remove `playerCountry`. **Do this adjustment now**: edit `Player.kt` to add `val countryId: Int = 0` after `clubId`, and simplify `World` to:

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class World(
    val countries: List<Country>,
    val clubs: List<Club>,
    val players: List<Player>,
) {
    fun playersOf(countryId: Int): List<Player> = players.filter { it.countryId == countryId }
    fun country(id: Int): Country = countries.first { it.id == id }
    fun player(id: Int): Player = players.first { it.id == id }
}
```

- [ ] **Step 4: Write `WorldGenerator.kt`**

```kotlin
package com.wcsim.engine.world

import com.wcsim.engine.model.*
import kotlin.random.Random

object WorldGenerator {
    private const val PLAYERS_PER_COUNTRY = 23
    private const val CLUBS = 40

    fun generate(rng: Random): World {
        val countries = NameData.COUNTRIES.mapIndexed { i, (name, str) ->
            Country(id = i, name = name, baseStrength = str)
        }
        val clubs = (0 until CLUBS).map { id ->
            val name = "${NameData.CLUB_PREFIXES.random(rng)} ${NameData.CLUB_SUFFIXES.random(rng)}"
            Club(id = id, name = name, strength = rng.nextInt(50, 90))
        }
        var pid = 0
        val players = mutableListOf<Player>()
        for (c in countries) {
            // Position quota for a 23-man squad: 3 GK, 7 DEF, 8 MID, 5 FWD.
            val quota = listOf(
                Position.GK to 3, Position.DEF to 7, Position.MID to 8, Position.FWD to 5
            )
            for ((pos, count) in quota) {
                repeat(count) {
                    players += makePlayer(pid++, c, pos, clubs, rng)
                }
            }
        }
        return World(countries, clubs, players)
    }

    private fun makePlayer(id: Int, country: Country, pos: Position,
                           clubs: List<Club>, rng: Random): Player {
        val name = "${NameData.FIRST_NAMES.random(rng)} ${NameData.LAST_NAMES.random(rng)}"
        val age = rng.nextInt(18, 34)
        // Center ratings around country strength with position emphasis + noise.
        fun near(center: Int, spread: Int = 8) =
            (center + rng.nextInt(-spread, spread + 1)).coerceIn(30, 99)
        val base = country.baseStrength
        val r = when (pos) {
            Position.GK -> Ratings(near(base-15), near(base), 20, near(base-20), near(base-10), near(base+3))
            Position.DEF -> Ratings(near(base-4), near(base), near(base-25), near(base-8), near(base+4), 15)
            Position.MID -> Ratings(near(base-2), near(base+2), near(base-6), near(base+4), near(base-4), 12)
            Position.FWD -> Ratings(near(base+2), near(base), near(base+5), near(base-4), near(base-25), 10)
        }
        return Player(id, name, age, pos, clubId = clubs.random(rng).id,
            countryId = country.id, ratings = r)
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.world.WorldGeneratorTest"`
Expected: PASS. Also re-run the full model tests to confirm the `Player.countryId` addition didn't break Task 2/3:
Run: `./gradlew :app:testDebugUnitTest`
Expected: all pass. Fix any `Player(...)` test constructors that now need `countryId` (they default to 0, so no change needed).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/world/WorldGenerator.kt app/src/main/java/com/wcsim/engine/model/World.kt app/src/main/java/com/wcsim/engine/model/Player.kt app/src/test/java/com/wcsim/engine/world/WorldGeneratorTest.kt
git commit -m "feat: add deterministic world generator (countries, clubs, players)"
```

---

## Phase 3 — Match & tournament simulation

### Task 6: Team strength calculation

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/match/TeamStrength.kt`
- Test: `app/src/test/java/com/wcsim/engine/match/TeamStrengthTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.*
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamStrengthTest {
    private fun squadOf(overall: Int): List<Player> = (0 until 11).map {
        Player(it, "P$it", 25, if (it == 0) Position.GK else Position.MID, 0, 0,
            Ratings(overall, overall, overall, overall, overall, overall))
    }

    @Test fun higher_overall_gives_higher_attack_and_defense() {
        val weak = TeamStrength.compute(squadOf(60), Tactics(aggression = 50), null)
        val strong = TeamStrength.compute(squadOf(85), Tactics(aggression = 50), null)
        assertTrue(strong.attack > weak.attack)
        assertTrue(strong.defense > weak.defense)
    }

    @Test fun aggression_trades_defense_for_attack() {
        val squad = squadOf(75)
        val def = TeamStrength.compute(squad, Tactics(aggression = 10), null)
        val att = TeamStrength.compute(squad, Tactics(aggression = 90), null)
        assertTrue(att.attack > def.attack)
        assertTrue(att.defense < def.defense)
    }

    @Test fun attacking_mastermind_boosts_attack() {
        val squad = squadOf(75)
        val plain = TeamStrength.compute(squad, Tactics(aggression = 50), null)
        val buffed = TeamStrength.compute(squad, Tactics(aggression = 50), SpecialAbility.ATTACKING_MASTERMIND)
        assertTrue(buffed.attack > plain.attack)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.TeamStrengthTest"`
Expected: FAIL — unresolved reference `TeamStrength`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.*

data class TeamRating(val attack: Double, val defense: Double)

object TeamStrength {
    /** ability is the coach's, applied to THIS team. knockout flag handled by caller for MOTIVATOR. */
    fun compute(
        squad: List<Player>,
        tactics: Tactics,
        ability: SpecialAbility?,
        knockout: Boolean = false,
    ): TeamRating {
        val avg = if (squad.isEmpty()) 50.0 else squad.map { it.overall }.average()
        // aggression 0..100 -> shift of +/- 15% between attack and defense.
        val a = (tactics.aggression - 50) / 50.0 // -1..1
        var attack = avg * (1.0 + 0.15 * a)
        var defense = avg * (1.0 - 0.15 * a)
        when (tactics.training) {
            TrainingFocus.ATTACK -> attack *= 1.04
            TrainingFocus.DEFENSE -> defense *= 1.04
            TrainingFocus.FITNESS -> { attack *= 1.02; defense *= 1.02 }
            TrainingFocus.BALANCED -> {}
        }
        when (ability) {
            SpecialAbility.ATTACKING_MASTERMIND -> attack *= 1.08
            SpecialAbility.IRON_WALL -> defense *= 1.08
            SpecialAbility.FITNESS_GURU -> { attack *= 1.03; defense *= 1.03 }
            SpecialAbility.MOTIVATOR -> if (knockout) { attack *= 1.06; defense *= 1.06 }
            else -> {}
        }
        return TeamRating(attack, defense)
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.TeamStrengthTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/match/TeamStrength.kt app/src/test/java/com/wcsim/engine/match/TeamStrengthTest.kt
git commit -m "feat: add team strength model (tactics, training, ability modifiers)"
```

### Task 7: Match simulator with scoreline + commentary

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/match/MatchSimulator.kt`
- Create: `app/src/main/java/com/wcsim/engine/model/MatchResult.kt`
- Test: `app/src/test/java/com/wcsim/engine/match/MatchSimulatorTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.match

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MatchSimulatorTest {
    @Test fun stronger_team_wins_more_often_over_many_games() {
        val strong = TeamRating(attack = 85.0, defense = 85.0)
        val weak = TeamRating(attack = 60.0, defense = 60.0)
        var strongWins = 0
        val rng = Random(1)
        repeat(1000) {
            val r = MatchSimulator.simulate("Strong", strong, "Weak", weak, rng, allowDraw = true)
            if (r.homeGoals > r.awayGoals) strongWins++
        }
        assertTrue("strong won $strongWins/1000", strongWins > 600)
    }

    @Test fun decisive_mode_never_returns_a_draw() {
        val rng = Random(2)
        repeat(200) {
            val r = MatchSimulator.simulate("A", TeamRating(70.0,70.0), "B", TeamRating(70.0,70.0),
                rng, allowDraw = false)
            assertTrue(r.homeGoals != r.awayGoals || r.winnerName != null)
        }
    }

    @Test fun commentary_lines_match_total_goals() {
        val rng = Random(3)
        val r = MatchSimulator.simulate("A", TeamRating(90.0,60.0), "B", TeamRating(60.0,90.0), rng, true)
        val goalLines = r.commentary.count { it.contains("GOAL") }
        assertTrue(goalLines >= r.homeGoals + r.awayGoals - 0) // at least the goals are narrated
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.MatchSimulatorTest"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Write `MatchResult.kt`**

```kotlin
package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class MatchResult(
    val homeName: String,
    val awayName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val wentToPenalties: Boolean = false,
    val winnerName: String? = null, // null only for a genuine draw (group stage)
    val commentary: List<String> = emptyList(),
) {
    val scoreline: String get() =
        "$homeName $homeGoals–$awayGoals $awayName" + if (wentToPenalties) " (pens)" else ""
}
```

- [ ] **Step 4: Write `MatchSimulator.kt`**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.MatchResult
import kotlin.math.exp
import kotlin.random.Random

object MatchSimulator {
    /** Expected goals from attacker vs defender ratings. */
    private fun xg(attack: Double, oppDefense: Double): Double {
        val diff = attack - oppDefense
        return (1.35 * exp(diff / 40.0)).coerceIn(0.2, 5.0)
    }

    private fun poisson(lambda: Double, rng: Random): Int {
        val l = exp(-lambda)
        var k = 0; var p = 1.0
        do { k++; p *= rng.nextDouble() } while (p > l)
        return k - 1
    }

    fun simulate(
        homeName: String, home: TeamRating,
        awayName: String, away: TeamRating,
        rng: Random,
        allowDraw: Boolean,
    ): MatchResult {
        var hg = poisson(xg(home.attack, away.defense), rng)
        var ag = poisson(xg(away.attack, home.defense), rng)
        val commentary = buildCommentary(homeName, awayName, hg, ag, rng)

        var pens = false
        var winner: String? = when {
            hg > ag -> homeName
            ag > hg -> awayName
            else -> null
        }
        if (winner == null && !allowDraw) {
            pens = true
            // penalty shootout weighted by combined strength
            val homeEdge = home.attack + home.defense
            val awayEdge = away.attack + away.defense
            winner = if (rng.nextDouble() < homeEdge / (homeEdge + awayEdge)) homeName else awayName
        }
        return MatchResult(homeName, awayName, hg, ag, pens, winner, commentary)
    }

    private fun buildCommentary(home: String, away: String, hg: Int, ag: Int, rng: Random): List<String> {
        val lines = mutableListOf<String>()
        data class G(val minute: Int, val team: String)
        val goals = buildList {
            repeat(hg) { add(G(rng.nextInt(1, 91), home)) }
            repeat(ag) { add(G(rng.nextInt(1, 91), away)) }
        }.sortedBy { it.minute }
        lines += "Kickoff: $home vs $away."
        for (g in goals) lines += "${g.minute}' GOAL — ${g.team} find the net!"
        lines += "Full time: $home $hg–$ag $away."
        return lines
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.MatchSimulatorTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/match/MatchSimulator.kt app/src/main/java/com/wcsim/engine/model/MatchResult.kt app/src/test/java/com/wcsim/engine/match/MatchSimulatorTest.kt
git commit -m "feat: add match simulator with Poisson scorelines and commentary"
```

### Task 8: Qualification campaign

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/match/Qualification.kt`
- Test: `app/src/test/java/com/wcsim/engine/match/QualificationTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.match

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QualificationTest {
    @Test fun strong_team_usually_qualifies() {
        var qualified = 0
        val rng = Random(5)
        repeat(200) {
            val res = Qualification.run(TeamRating(88.0, 88.0), opponentAvgStrength = 68.0, rng = rng)
            if (res.qualified) qualified++
        }
        assertTrue("qualified $qualified/200", qualified > 150)
    }

    @Test fun result_reports_record_and_matches() {
        val res = Qualification.run(TeamRating(75.0,75.0), 72.0, Random(6))
        assertTrue(res.matches.isNotEmpty())
        assertTrue(res.wins + res.draws + res.losses == res.matches.size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.QualificationTest"`
Expected: FAIL — unresolved reference `Qualification`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.MatchResult
import kotlin.random.Random

data class QualificationResult(
    val qualified: Boolean,
    val points: Int,
    val wins: Int, val draws: Int, val losses: Int,
    val matches: List<MatchResult>,
)

object Qualification {
    private const val GAMES = 8
    private const val QUALIFY_POINTS = 14 // out of 24 possible

    fun run(team: TeamRating, opponentAvgStrength: Double, rng: Random): QualificationResult {
        val opp = TeamRating(opponentAvgStrength, opponentAvgStrength)
        val matches = mutableListOf<MatchResult>()
        var pts = 0; var w = 0; var d = 0; var l = 0
        repeat(GAMES) { i ->
            val homeGame = i % 2 == 0
            val r = if (homeGame) MatchSimulator.simulate("You", team, "Rival", opp, rng, allowDraw = true)
                    else MatchSimulator.simulate("Rival", opp, "You", team, rng, allowDraw = true)
            matches += r
            val youGoals = if (homeGame) r.homeGoals else r.awayGoals
            val oppGoals = if (homeGame) r.awayGoals else r.homeGoals
            when {
                youGoals > oppGoals -> { pts += 3; w++ }
                youGoals == oppGoals -> { pts += 1; d++ }
                else -> l++
            }
        }
        return QualificationResult(pts >= QUALIFY_POINTS, pts, w, d, l, matches)
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.QualificationTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/match/Qualification.kt app/src/test/java/com/wcsim/engine/match/QualificationTest.kt
git commit -m "feat: add qualification campaign simulation"
```

### Task 9: World Cup tournament (32 teams → champion)

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/match/WorldCup.kt`
- Test: `app/src/test/java/com/wcsim/engine/match/WorldCupTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.WcStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WorldCupTest {
    // 32 teams: index 0 is "You". Provide ratings for all.
    private fun field(youRating: Double): List<WorldCup.Entrant> {
        val rng = Random(11)
        return (0 until 32).map { i ->
            val s = if (i == 0) youRating else rng.nextDouble(60.0, 85.0)
            WorldCup.Entrant("T$i", TeamRating(s, s), isYou = i == 0)
        }
    }

    @Test fun requires_exactly_32_teams() {
        try {
            WorldCup.run(field(80.0).take(30), Random(1), null)
            assertTrue("should have thrown", false)
        } catch (e: IllegalArgumentException) { /* expected */ }
    }

    @Test fun produces_single_champion_and_your_stage() {
        val res = WorldCup.run(field(99.0), Random(1), null)
        assertNotNull(res.champion)
        assertNotNull(res.yourStage)
    }

    @Test fun dominant_you_reaches_at_least_knockouts() {
        // Overwhelming favorite should usually escape the group.
        var deepRuns = 0
        repeat(50) { seed ->
            val res = WorldCup.run(field(99.0), Random(seed.toLong()), null)
            if (res.yourStage != WcStage.GROUP) deepRuns++
        }
        assertTrue("deep runs $deepRuns/50", deepRuns > 35)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.WorldCupTest"`
Expected: FAIL — unresolved reference `WorldCup`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.match

import com.wcsim.engine.model.MatchResult
import com.wcsim.engine.model.WcStage
import kotlin.random.Random

object WorldCup {
    data class Entrant(val name: String, val rating: TeamRating, val isYou: Boolean = false)

    data class Result(
        val champion: String,
        val yourStage: WcStage,          // furthest stage YOU reached (CHAMPION if you won)
        val groupResults: List<MatchResult>,
        val knockoutResults: List<MatchResult>,
    )

    fun run(entrants: List<Entrant>, rng: Random, motivator: com.wcsim.engine.model.SpecialAbility?): Result {
        require(entrants.size == 32) { "World Cup needs exactly 32 teams" }
        val groupMatches = mutableListOf<MatchResult>()
        // 8 groups of 4, round-robin, top 2 advance.
        val groups = entrants.chunked(4)
        val advancers = mutableListOf<Entrant>()
        var yourStage = WcStage.GROUP
        for (group in groups) {
            val pts = HashMap<String, Int>().apply { group.forEach { put(it.name, 0) } }
            for (i in group.indices) for (j in i + 1 until group.size) {
                val r = MatchSimulator.simulate(group[i].name, group[i].rating,
                    group[j].name, group[j].rating, rng, allowDraw = true)
                groupMatches += r
                when {
                    r.homeGoals > r.awayGoals -> pts.merge(group[i].name, 3, Int::plus)
                    r.awayGoals > r.homeGoals -> pts.merge(group[j].name, 3, Int::plus)
                    else -> { pts.merge(group[i].name, 1, Int::plus); pts.merge(group[j].name, 1, Int::plus) }
                }
            }
            val top2 = group.sortedByDescending { pts[it.name] }.take(2)
            advancers += top2
        }
        // Knockouts: Ro16 -> QF -> SF -> Final. allowDraw=false (penalties decide).
        val knockout = mutableListOf<MatchResult>()
        var round = advancers // 16
        val stageOnElimination = listOf(WcStage.ROUND16, WcStage.QUARTER, WcStage.SEMI, WcStage.FINAL)
        var stageIdx = 0
        while (round.size > 1) {
            val next = mutableListOf<Entrant>()
            var i = 0
            while (i < round.size) {
                val a = round[i]; val b = round[i + 1]
                val ability = motivator.takeIf { a.isYou || b.isYou }
                val r = MatchSimulator.simulate(a.name, a.rating, b.name, b.rating, rng, allowDraw = false)
                knockout += r
                val winnerIsA = r.winnerName == a.name
                val winner = if (winnerIsA) a else b
                val loser = if (winnerIsA) b else a
                if (loser.isYou) yourStage = stageOnElimination[stageIdx]
                next += winner
                i += 2
            }
            round = next
            stageIdx++
        }
        val champion = round.first()
        if (champion.isYou) yourStage = WcStage.CHAMPION
        return Result(champion.name, yourStage, groupMatches, knockout)
    }
}
```

Note: the `motivator` param is threaded but knockout strength boosting is applied in the career layer (Task 12) when it builds `Entrant.rating` with `knockout=true`. Here it is accepted for signature stability; simplest correct behavior is to ignore it inside `run`. Keep the param.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.match.WorldCupTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/match/WorldCup.kt app/src/test/java/com/wcsim/engine/match/WorldCupTest.kt
git commit -m "feat: add 32-team World Cup tournament simulation"
```

---

## Phase 4 — Career systems

### Task 10: Scoring configuration and calculator

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/score/Scoring.kt`
- Test: `app/src/test/java/com/wcsim/engine/score/ScoringTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.score

import com.wcsim.engine.model.WcStage
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    @Test fun stage_points_are_monotonic() {
        val stages = listOf(WcStage.GROUP, WcStage.ROUND16, WcStage.QUARTER,
            WcStage.SEMI, WcStage.FINAL, WcStage.CHAMPION)
        val pts = stages.map { Scoring.stagePoints(it) }
        for (i in 1 until pts.size) assertTrue("${stages[i]} must exceed ${stages[i-1]}", pts[i] > pts[i-1])
    }

    @Test fun earnings_contribute_to_score() {
        val low = Scoring.finalScore(wcPoints = 100, careerEarnings = 1_000_000, trophies = 0, seasons = 10)
        val high = Scoring.finalScore(wcPoints = 100, careerEarnings = 50_000_000, trophies = 0, seasons = 10)
        assertTrue(high > low)
    }

    @Test fun trophies_and_longevity_add_bonus() {
        val a = Scoring.finalScore(100, 0, trophies = 0, seasons = 5)
        val b = Scoring.finalScore(100, 0, trophies = 3, seasons = 40)
        assertTrue(b > a)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.score.ScoringTest"`
Expected: FAIL — unresolved reference `Scoring`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.score

import com.wcsim.engine.model.WcStage

object Scoring {
    fun stagePoints(stage: WcStage): Int = when (stage) {
        WcStage.GROUP -> 10
        WcStage.ROUND16 -> 25
        WcStage.QUARTER -> 50
        WcStage.SEMI -> 90
        WcStage.FINAL -> 140
        WcStage.CHAMPION -> 220
    }

    /** Earnings scaled: 1 point per 100k earned. */
    fun earningsPoints(careerEarnings: Long): Int = (careerEarnings / 100_000L).toInt()

    fun finalScore(wcPoints: Int, careerEarnings: Long, trophies: Int, seasons: Int): Int =
        wcPoints + earningsPoints(careerEarnings) + trophies * 150 + seasons * 5
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.score.ScoringTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/score/Scoring.kt app/src/test/java/com/wcsim/engine/score/ScoringTest.kt
git commit -m "feat: add scoring model (stage points, earnings, bonuses)"
```

### Task 11: Contracts, expectations, firing, and salary negotiation

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/career/Contracts.kt`
- Test: `app/src/test/java/com/wcsim/engine/career/ContractsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.SpecialAbility
import com.wcsim.engine.model.WcStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractsTest {
    @Test fun stronger_nations_get_higher_expectations() {
        val weak = Contracts.expectationFor(baseStrength = 62)
        val strong = Contracts.expectationFor(baseStrength = 88)
        assertTrue(Contracts.rank(strong) > Contracts.rank(weak))
    }

    @Test fun failing_expectation_badly_triggers_firing_risk() {
        // Missed by two or more stages => fired.
        assertTrue(Contracts.isFired(expected = WcStage.SEMI, achieved = WcStage.GROUP, reputation = 50))
        assertTrue(!Contracts.isFired(expected = WcStage.QUARTER, achieved = WcStage.QUARTER, reputation = 50))
    }

    @Test fun negotiation_offer_scales_with_reputation_and_ability() {
        val base = Contracts.salaryOffer(baseStrength = 80, reputation = 40, ability = null)
        val rep = Contracts.salaryOffer(baseStrength = 80, reputation = 90, ability = null)
        val neg = Contracts.salaryOffer(baseStrength = 80, reputation = 40, ability = SpecialAbility.NEGOTIATOR)
        assertTrue(rep > base)
        assertTrue(neg > base)
    }

    @Test fun pushing_can_raise_or_lower_final_offer() {
        // deterministic branches
        assertTrue(Contracts.pushOutcome(offer = 1_000_000, reputation = 90, roll = 0.9) > 1_000_000)
        assertEquals(900_000L, Contracts.pushOutcome(offer = 1_000_000, reputation = 10, roll = 0.05))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.career.ContractsTest"`
Expected: FAIL — unresolved reference `Contracts`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.SpecialAbility
import com.wcsim.engine.model.WcStage

object Contracts {
    /** Board's target for the season, based on nation strength. */
    fun expectationFor(baseStrength: Int): WcStage = when {
        baseStrength >= 85 -> WcStage.SEMI
        baseStrength >= 78 -> WcStage.QUARTER
        baseStrength >= 70 -> WcStage.ROUND16
        else -> WcStage.GROUP // just qualify + compete
    }

    fun rank(stage: WcStage): Int = stage.ordinal // GROUP=0 .. CHAMPION=5

    /** Fired if you fall two or more stages short of expectation. */
    fun isFired(expected: WcStage, achieved: WcStage, reputation: Int): Boolean {
        val shortfall = rank(expected) - rank(achieved)
        return shortfall >= 2 || (shortfall == 1 && reputation < 20)
    }

    /** Annual salary offer in currency units. */
    fun salaryOffer(baseStrength: Int, reputation: Int, ability: SpecialAbility?): Long {
        val base = 500_000L + baseStrength * 40_000L
        val repMult = 1.0 + reputation / 100.0
        val abilityMult = if (ability == SpecialAbility.NEGOTIATOR) 1.20 else 1.0
        return (base * repMult * abilityMult).toLong()
    }

    /**
     * When you push for more: high reputation + high roll -> +10%; else -> -10%.
     * roll is a 0..1 random draw supplied by the caller (deterministic in tests).
     */
    fun pushOutcome(offer: Long, reputation: Int, roll: Double): Long {
        val success = roll < (reputation / 100.0)
        return if (success) (offer * 1.10).toLong() else (offer * 0.90).toLong()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.career.ContractsTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/career/Contracts.kt app/src/test/java/com/wcsim/engine/career/ContractsTest.kt
git commit -m "feat: add contracts, expectations, firing, and salary negotiation"
```

### Task 12: Career state, aging/regen, death, and the season engine

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/career/CareerState.kt`
- Create: `app/src/main/java/com/wcsim/engine/career/Aging.kt`
- Create: `app/src/main/java/com/wcsim/engine/career/DeathBlurbs.kt`
- Create: `app/src/main/java/com/wcsim/engine/career/CareerEngine.kt`
- Test: `app/src/test/java/com/wcsim/engine/career/CareerEngineTest.kt`
- Test: `app/src/test/java/com/wcsim/engine/career/AgingTest.kt`

- [ ] **Step 1: Write the failing test** — `AgingTest.kt`

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.*
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AgingTest {
    @Test fun pool_stays_viable_across_50_seasons() {
        var world = com.wcsim.engine.world.WorldGenerator.generate(Random(3))
        val countryId = world.countries.first().id
        val rng = Random(99)
        repeat(50) {
            world = Aging.advanceSeason(world, rng, ability = null)
            val pool = world.playersOf(countryId)
            assertTrue("pool shrank to ${pool.size}", pool.size >= 18)
            assertTrue("no keeper left", pool.any { it.position == Position.GK })
        }
    }

    @Test fun youth_whisperer_raises_regen_quality() {
        val rng1 = Random(4); val rng2 = Random(4)
        var plain = com.wcsim.engine.world.WorldGenerator.generate(Random(1))
        var buffed = com.wcsim.engine.world.WorldGenerator.generate(Random(1))
        repeat(10) {
            plain = Aging.advanceSeason(plain, rng1, ability = null)
            buffed = Aging.advanceSeason(buffed, rng2, ability = SpecialAbility.YOUTH_WHISPERER)
        }
        val cid = plain.countries.first().id
        assertTrue(buffed.playersOf(cid).map { it.overall }.average() >=
                   plain.playersOf(cid).map { it.overall }.average())
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.career.AgingTest"`
Expected: FAIL — unresolved reference `Aging`.

- [ ] **Step 3: Write `Aging.kt`**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.*
import com.wcsim.engine.world.NameData
import kotlin.random.Random

object Aging {
    private const val RETIRE_AGE = 36

    fun advanceSeason(world: World, rng: Random, ability: SpecialAbility?): World {
        var nextId = (world.players.maxOfOrNull { it.id } ?: 0) + 1
        val updated = mutableListOf<Player>()
        // Group by country to backfill retirees per nation/position.
        val retireesByCountryPos = HashMap<Pair<Int, Position>, Int>()

        for (p in world.players) {
            val aged = p.copy(age = p.age + 1)
            if (aged.age > RETIRE_AGE) {
                retireesByCountryPos.merge(aged.countryId to aged.position, 1, Int::plus)
            } else {
                updated += agedRatings(aged)
            }
        }
        // Backfill: create young regens for each retiree.
        for ((key, count) in retireesByCountryPos) {
            val (countryId, pos) = key
            val country = world.country(countryId)
            repeat(count) {
                updated += regen(nextId++, country, pos, world.clubs, rng, ability)
            }
        }
        return world.copy(players = updated)
    }

    private fun agedRatings(p: Player): Player {
        // Improve until ~27, decline after ~30.
        val delta = when {
            p.age <= 27 -> 1
            p.age >= 31 -> -2
            else -> 0
        }
        fun adj(v: Int) = (v + delta).coerceIn(30, 99)
        val r = p.ratings
        return p.copy(ratings = r.copy(
            pace = adj(r.pace), stamina = adj(r.stamina), finishing = adj(r.finishing),
            passing = adj(r.passing), defending = adj(r.defending), goalkeeping = adj(r.goalkeeping)))
    }

    private fun regen(id: Int, country: Country, pos: Position,
                      clubs: List<Club>, rng: Random, ability: SpecialAbility?): Player {
        val bonus = if (ability == SpecialAbility.YOUTH_WHISPERER) 6 else 0
        val base = country.baseStrength + bonus
        fun near(center: Int, spread: Int = 7) = (center + rng.nextInt(-spread, spread + 1)).coerceIn(30, 99)
        val r = when (pos) {
            Position.GK -> Ratings(near(base-15), near(base), 20, near(base-20), near(base-10), near(base))
            Position.DEF -> Ratings(near(base-4), near(base), near(base-25), near(base-8), near(base+2), 15)
            Position.MID -> Ratings(near(base-2), near(base), near(base-6), near(base+2), near(base-4), 12)
            Position.FWD -> Ratings(near(base+2), near(base), near(base+3), near(base-4), near(base-25), 10)
        }
        val name = "${NameData.FIRST_NAMES.random(rng)} ${NameData.LAST_NAMES.random(rng)}"
        return Player(id, name, rng.nextInt(17, 21), pos, clubs.random(rng).id, country.id, r)
    }
}
```

- [ ] **Step 4: Run AgingTest to verify pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.career.AgingTest"`
Expected: PASS.

- [ ] **Step 5: Write `DeathBlurbs.kt`**

```kotlin
package com.wcsim.engine.career

import kotlin.random.Random

object DeathBlurbs {
    private val DEATHS = listOf(
        "was crushed by a giant inflatable World Cup trophy during a victory parade.",
        "vanished into a sinkhole that opened up on the training pitch.",
        "was carried off by an unusually determined flock of seagulls.",
        "perished in a freak Gatorade-cooler avalanche.",
        "was struck by a meteorite the size of a football, mid-interview.",
        "tripped over the tactics whiteboard and out of a tenth-floor window.",
        "was fatally tangled in an over-inflated corner-flag mishap.",
    )
    private val RETIREMENTS = listOf(
        "hung up the clipboard and retired to a quiet vineyard.",
        "walked away from the game to write a best-selling memoir.",
        "retired to breed prize-winning racing pigeons.",
    )
    fun death(rng: Random): String = "You " + DEATHS.random(rng)
    fun retirement(rng: Random): String = "You " + RETIREMENTS.random(rng)
}
```

- [ ] **Step 6: Write `CareerState.kt`**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.*
import kotlinx.serialization.Serializable

@Serializable
data class CareerState(
    val coachName: String,
    val ability: SpecialAbility,
    val world: World,
    val countryId: Int,
    val age: Int = 30,
    val season: Int = 1,           // 1..50
    val salary: Long = 0,
    val careerEarnings: Long = 0,
    val reputation: Int = 30,      // 0..100
    val trophies: Int = 0,
    val wcPoints: Int = 0,
    val tactics: Tactics = Tactics(),
    val squad: Squad? = null,
    val isOver: Boolean = false,
    val endBlurb: String? = null,
) {
    val country: Country get() = world.country(countryId)
    val maxSeasons: Int get() = 50
}
```

- [ ] **Step 7: Write `CareerEngine.kt`**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.match.*
import com.wcsim.engine.model.*
import com.wcsim.engine.score.Scoring
import kotlin.random.Random

/** Outcome of playing one full season. */
data class SeasonOutcome(
    val qualification: QualificationResult,
    val worldCup: WorldCup.Result?,     // null if failed to qualify
    val yourStage: WcStage?,            // null if didn't qualify
    val expectation: WcStage,
    val fired: Boolean,
    val salaryEarned: Long,
    val narrative: List<String>,
)

object CareerEngine {
    /** Build a fresh career: pick country + roll ability + first contract. */
    fun newCareer(coachName: String, countryId: Int, rng: Random): CareerState {
        val world = com.wcsim.engine.world.WorldGenerator.generate(rng)
        val ability = SpecialAbility.entries.random(rng)
        val strength = world.country(countryId).baseStrength
        val salary = Contracts.salaryOffer(strength, reputation = 30, ability = ability)
        return CareerState(coachName, ability, world, countryId, salary = salary)
    }

    /** Effective rating of the coached nation given current squad+tactics. */
    private fun myRating(state: CareerState, knockout: Boolean): TeamRating {
        val pool = state.world.playersOf(state.countryId)
        val chosen = state.squad?.playerIds?.mapNotNull { id -> pool.find { it.id == id } }
            ?.takeIf { it.size == 11 } ?: pool.sortedByDescending { it.overall }.take(11)
        return TeamStrength.compute(chosen, state.tactics, state.ability, knockout)
    }

    /** Play the current season and return the outcome plus the advanced state. */
    fun playSeason(state: CareerState, rng: Random): Pair<CareerState, SeasonOutcome> {
        val strength = state.country.baseStrength
        val expectation = Contracts.expectationFor(strength)
        val narrative = mutableListOf<String>()

        // Qualification
        val myQualRating = myRating(state, knockout = false)
        val qual = Qualification.run(myQualRating, opponentAvgStrength = (strength - 6).toDouble(), rng)
        narrative += if (qual.qualified) "You qualified for the World Cup (${qual.wins}W-${qual.draws}D-${qual.losses}L)."
                     else "You failed to qualify (${qual.wins}W-${qual.draws}D-${qual.losses}L)."

        // World Cup (only if qualified)
        var wc: WorldCup.Result? = null
        var yourStage: WcStage? = null
        var seasonWcPoints = 0
        var trophyGain = 0
        if (qual.qualified) {
            val field = buildWcField(state, rng)
            wc = WorldCup.run(field, rng, state.ability.takeIf { it == SpecialAbility.MOTIVATOR })
            yourStage = wc.yourStage
            seasonWcPoints = Scoring.stagePoints(yourStage)
            if (yourStage == WcStage.CHAMPION) trophyGain = 1
            narrative += "World Cup: you reached ${yourStage}. Champion: ${wc.champion}."
        }

        // Board verdict + firing
        val achieved = yourStage ?: WcStage.GROUP.takeIf { qual.qualified } ?: WcStage.GROUP
        val effectiveAchieved = yourStage ?: (if (qual.qualified) WcStage.GROUP else WcStage.GROUP)
        val fired = !qual.qualified && Contracts.rank(expectation) >= Contracts.rank(WcStage.ROUND16) ||
                    Contracts.isFired(expectation, effectiveAchieved, state.reputation)
        narrative += if (fired) "The board has SACKED you." else "The board keeps faith in you."

        // Reputation + earnings
        val repDelta = (Contracts.rank(effectiveAchieved) - Contracts.rank(expectation)) * 8 +
                       if (qual.qualified) 2 else -6
        val newReputation = (state.reputation + repDelta).coerceIn(0, 100)
        val salaryEarned = state.salary

        // Age players and world one season.
        val agedWorld = Aging.advanceSeason(state.world, rng, state.ability)

        var next = state.copy(
            world = agedWorld,
            age = state.age + 1,
            season = state.season + 1,
            careerEarnings = state.careerEarnings + salaryEarned,
            reputation = newReputation,
            trophies = state.trophies + trophyGain,
            wcPoints = state.wcPoints + seasonWcPoints,
        )

        // End-of-career check: 50 seasons reached.
        if (next.season > next.maxSeasons) {
            next = endCareer(next, rng, retired = false)
        } else if (fired) {
            // Move to a (often weaker) new nation; salary re-negotiated at lower base.
            next = reassignAfterFiring(next, rng)
        }

        return next to SeasonOutcome(qual, wc, yourStage, expectation, fired, salaryEarned, narrative)
    }

    private fun buildWcField(state: CareerState, rng: Random): List<WorldCup.Entrant> {
        // You + 31 other nations by strength (fallback random if fewer countries exist).
        val others = state.world.countries.filter { it.id != state.countryId }
            .sortedByDescending { it.baseStrength }.take(31)
        val youKnockRating = myRating(state, knockout = true)
        val you = WorldCup.Entrant(state.country.name, youKnockRating, isYou = true)
        val rest = others.map { c ->
            val pool = state.world.playersOf(c.id).sortedByDescending { it.overall }.take(11)
            WorldCup.Entrant(c.name, TeamStrength.compute(pool,
                Tactics(aggression = 50), ability = null))
        }
        val field = (listOf(you) + rest).toMutableList()
        // Pad if the world has <32 countries (shouldn't happen: 36 provided).
        while (field.size < 32) field += WorldCup.Entrant("Filler${field.size}", TeamRating(60.0, 60.0))
        field.shuffle(rng)
        return field.take(32).let { if (it.any { e -> e.isYou }) it else listOf(you) + it.drop(1) }
    }

    private fun reassignAfterFiring(state: CareerState, rng: Random): CareerState {
        // Pick a random nation weaker than or equal to current reputation-adjusted level.
        val candidates = state.world.countries.filter { it.id != state.countryId }
        val newCountry = candidates.minByOrNull { kotlin.math.abs(it.baseStrength - (state.reputation + 55)) }
            ?: return endCareer(state, rng, retired = false) // no job -> career ends
        val salary = Contracts.salaryOffer(newCountry.baseStrength, state.reputation, state.ability)
        return state.copy(countryId = newCountry.id, salary = salary, squad = null)
    }

    fun endCareer(state: CareerState, rng: Random, retired: Boolean): CareerState {
        val blurb = if (retired) DeathBlurbs.retirement(rng) else DeathBlurbs.death(rng)
        return state.copy(isOver = true, endBlurb = blurb)
    }

    fun finalScore(state: CareerState): Int =
        Scoring.finalScore(state.wcPoints, state.careerEarnings, state.trophies, state.season - 1)
}
```

- [ ] **Step 8: Write `CareerEngineTest.kt`**

```kotlin
package com.wcsim.engine.career

import com.wcsim.engine.model.SpecialAbility
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CareerEngineTest {
    @Test fun new_career_starts_at_30_with_ability_and_salary() {
        val s = CareerEngine.newCareer("Coach", countryId = 0, rng = Random(1))
        assertTrue(s.age == 30)
        assertTrue(s.salary > 0)
        assertTrue(SpecialAbility.entries.contains(s.ability))
    }

    @Test fun playing_a_full_50_season_career_terminates_and_scores() {
        var s = CareerEngine.newCareer("Coach", countryId = 0, rng = Random(2))
        val rng = Random(2)
        var guard = 0
        while (!s.isOver && guard < 200) {
            val (next, _) = CareerEngine.playSeason(s, rng)
            s = next; guard++
        }
        assertTrue("career should end", s.isOver)
        assertTrue("should have a blurb", s.endBlurb != null)
        assertTrue("earnings accumulated", s.careerEarnings > 0)
        assertTrue("score computable", CareerEngine.finalScore(s) >= 0)
    }

    @Test fun earnings_accumulate_each_season() {
        var s = CareerEngine.newCareer("Coach", countryId = 0, rng = Random(3))
        val (afterOne, outcome) = CareerEngine.playSeason(s, Random(3))
        assertTrue(afterOne.careerEarnings >= outcome.salaryEarned)
        assertTrue(outcome.salaryEarned > 0)
    }
}
```

- [ ] **Step 9: Run the career tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.engine.career.*"`
Expected: PASS.

- [ ] **Step 10: Run the whole engine suite**

Run: `./gradlew :app:testDebugUnitTest`
Expected: all tests PASS.

- [ ] **Step 11: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/career/ app/src/test/java/com/wcsim/engine/career/
git commit -m "feat: add career engine (season loop, aging, firing, death, scoring)"
```

---

## Phase 5 — Persistence

### Task 13: Save/load + high-score table (pure serialization)

**Files:**
- Create: `app/src/main/java/com/wcsim/engine/score/HighScore.kt`
- Create: `app/src/main/java/com/wcsim/data/SaveCodec.kt`
- Test: `app/src/test/java/com/wcsim/data/SaveCodecTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.wcsim.data

import com.wcsim.engine.career.CareerEngine
import com.wcsim.engine.score.HighScore
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class SaveCodecTest {
    @Test fun career_round_trips_through_json() {
        val s = CareerEngine.newCareer("Coach", 0, Random(1))
        val json = SaveCodec.encodeCareer(s)
        val back = SaveCodec.decodeCareer(json)
        assertEquals(s.coachName, back.coachName)
        assertEquals(s.world.players.size, back.world.players.size)
        assertEquals(s.ability, back.ability)
    }

    @Test fun high_score_table_sorts_desc_and_caps() {
        var table = HighScore.Table()
        table = HighScore.add(table, HighScore.Entry("A", "Brazil", 10, 1234, "meteor"))
        table = HighScore.add(table, HighScore.Entry("B", "Japan", 20, 9999, "seagulls"))
        assertEquals("B", table.entries.first().coachName)
        val json = SaveCodec.encodeScores(table)
        assertEquals(table, SaveCodec.decodeScores(json))
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.data.SaveCodecTest"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Write `HighScore.kt`**

```kotlin
package com.wcsim.engine.score

import kotlinx.serialization.Serializable

object HighScore {
    @Serializable
    data class Entry(
        val coachName: String,
        val country: String,
        val seasons: Int,
        val score: Int,
        val causeOfDeath: String,
    )

    @Serializable
    data class Table(val entries: List<Entry> = emptyList())

    private const val MAX = 20

    fun add(table: Table, entry: Entry): Table =
        Table((table.entries + entry).sortedByDescending { it.score }.take(MAX))
}
```

- [ ] **Step 4: Write `SaveCodec.kt`**

```kotlin
package com.wcsim.data

import com.wcsim.engine.career.CareerState
import com.wcsim.engine.score.HighScore
import kotlinx.serialization.json.Json

object SaveCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encodeCareer(state: CareerState): String = json.encodeToString(CareerState.serializer(), state)
    fun decodeCareer(text: String): CareerState = json.decodeFromString(CareerState.serializer(), text)

    fun encodeScores(table: HighScore.Table): String =
        json.encodeToString(HighScore.Table.serializer(), table)
    fun decodeScores(text: String): HighScore.Table =
        json.decodeFromString(HighScore.Table.serializer(), text)
}
```

- [ ] **Step 5: Run to verify pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.wcsim.data.SaveCodecTest"`
Expected: PASS. If serialization fails, ensure every model in the graph (`CareerState`, `World`, `Player`, `Country`, `Club`, `Ratings`, `StatLine`, `Tactics`, `Squad`, all enums) is annotated `@Serializable`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/engine/score/HighScore.kt app/src/main/java/com/wcsim/data/SaveCodec.kt app/src/test/java/com/wcsim/data/SaveCodecTest.kt
git commit -m "feat: add JSON save/load codec and high-score table"
```

### Task 14: Android persistence repository (file-backed)

**Files:**
- Create: `app/src/main/java/com/wcsim/data/GameRepository.kt`

- [ ] **Step 1: Write `GameRepository.kt`** (no unit test — thin Android file IO; covered by manual smoke test in Task 20)

```kotlin
package com.wcsim.data

import android.content.Context
import com.wcsim.engine.career.CareerState
import com.wcsim.engine.score.HighScore
import java.io.File

class GameRepository(private val context: Context) {
    private val saveFile: File get() = File(context.filesDir, "career.json")
    private val scoreFile: File get() = File(context.filesDir, "highscores.json")

    fun hasSave(): Boolean = saveFile.exists()

    fun saveCareer(state: CareerState) = saveFile.writeText(SaveCodec.encodeCareer(state))
    fun loadCareer(): CareerState? =
        if (saveFile.exists()) runCatching { SaveCodec.decodeCareer(saveFile.readText()) }.getOrNull() else null
    fun clearCareer() { if (saveFile.exists()) saveFile.delete() }

    fun loadScores(): HighScore.Table =
        if (scoreFile.exists()) runCatching { SaveCodec.decodeScores(scoreFile.readText()) }
            .getOrDefault(HighScore.Table()) else HighScore.Table()
    fun addScore(entry: HighScore.Entry) {
        val updated = HighScore.add(loadScores(), entry)
        scoreFile.writeText(SaveCodec.encodeScores(updated))
    }
}
```

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/wcsim/data/GameRepository.kt
git commit -m "feat: add file-backed game repository for save + high scores"
```

---

## Phase 6 — UI (Jetpack Compose)

> UI tasks are integration-style: build screen + ViewModel, compile, and verify by building. Final manual smoke test is Task 20. Keep all game rules in the engine — ViewModels only orchestrate.

### Task 15: App navigation graph and GameViewModel

**Files:**
- Create: `app/src/main/java/com/wcsim/ui/GameViewModel.kt`
- Create: `app/src/main/java/com/wcsim/ui/Nav.kt`
- Modify: `app/src/main/java/com/wcsim/MainActivity.kt`

- [ ] **Step 1: Write `GameViewModel.kt`**

Holds the active `CareerState`, the last `SeasonOutcome`, and the `GameRepository`. Exposes intent methods: `startNewCareer(name, countryId)`, `setTactics(...)`, `setSquad(...)`, `playSeason()`, `endCareerNow(retired)`, `saveScoreAndReset()`. Use `mutableStateOf`. Seed a `Random` from `System.nanoTime()` for live play; keep the engine calls pure.

```kotlin
package com.wcsim.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wcsim.data.GameRepository
import com.wcsim.engine.career.CareerEngine
import com.wcsim.engine.career.CareerState
import com.wcsim.engine.career.SeasonOutcome
import com.wcsim.engine.model.Squad
import com.wcsim.engine.model.Tactics
import com.wcsim.engine.score.HighScore
import kotlin.random.Random

class GameViewModel(private val repo: GameRepository) : ViewModel() {
    var state by mutableStateOf<CareerState?>(repo.loadCareer())
        private set
    var lastOutcome by mutableStateOf<SeasonOutcome?>(null)
        private set

    private val rng = Random(System.nanoTime())

    fun hasSave() = repo.hasSave()
    fun scores() = repo.loadScores()

    fun startNewCareer(name: String, countryId: Int) {
        val s = CareerEngine.newCareer(name.ifBlank { "Coach" }, countryId, rng)
        state = s; lastOutcome = null; repo.saveCareer(s)
    }

    fun setTactics(t: Tactics) { state = state?.copy(tactics = t)?.also { repo.saveCareer(it) } }
    fun setSquad(s: Squad) { state = state?.copy(squad = s)?.also { repo.saveCareer(it) } }

    fun playSeason() {
        val cur = state ?: return
        val (next, outcome) = CareerEngine.playSeason(cur, rng)
        state = next; lastOutcome = outcome; repo.saveCareer(next)
    }

    fun retireNow() {
        val cur = state ?: return
        state = CareerEngine.endCareer(cur, rng, retired = true).also { repo.saveCareer(it) }
    }

    fun finalizeCareer() {
        val cur = state ?: return
        if (!cur.isOver) return
        repo.addScore(HighScore.Entry(
            coachName = cur.coachName,
            country = cur.country.name,
            seasons = cur.season - 1,
            score = CareerEngine.finalScore(cur),
            causeOfDeath = cur.endBlurb ?: "unknown",
        ))
        repo.clearCareer()
        state = null; lastOutcome = null
    }
}
```

- [ ] **Step 2: Write `Nav.kt`** — a sealed list of routes: `Menu`, `NewCareer`, `Hud`, `Squad`, `Players`, `Season`, `CareerEnd`, `HighScores`. Provide a `NavHost` wiring each to its screen composable (screens created in later tasks; stub them as empty `@Composable fun` for now so it compiles, then fill in).

- [ ] **Step 3: Modify `MainActivity.kt`** to construct `GameRepository`, build the `GameViewModel` (simple factory), and render the `NavHost`.

```kotlin
package com.wcsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.wcsim.data.GameRepository
import com.wcsim.ui.AppNavHost
import com.wcsim.ui.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = GameRepository(applicationContext)
        setContent {
            MaterialTheme {
                Surface {
                    val vm: GameViewModel = viewModel(factory = simpleFactory(repo))
                    val nav = rememberNavController()
                    AppNavHost(nav, vm)
                }
            }
        }
    }
}
```

Add a `simpleFactory(repo)` helper in `ui/GameViewModel.kt`:

```kotlin
fun simpleFactory(repo: com.wcsim.data.GameRepository) =
    object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(repo) as T
    }
```

- [ ] **Step 4: Build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (with empty screen stubs).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/ui/ app/src/main/java/com/wcsim/MainActivity.kt
git commit -m "feat: add navigation graph and GameViewModel"
```

### Task 16: Main menu + high scores screens

**Files:**
- Create: `app/src/main/java/com/wcsim/ui/screens/MenuScreen.kt`
- Create: `app/src/main/java/com/wcsim/ui/screens/HighScoresScreen.kt`

- [ ] **Step 1: Write `MenuScreen.kt`** — buttons: New Career, Continue (enabled if `vm.hasSave()`), High Scores, and title text. Wire navigation callbacks.
- [ ] **Step 2: Write `HighScoresScreen.kt`** — a `LazyColumn` of `vm.scores().entries` showing rank, coach, country, seasons, score, cause of death; back button.
- [ ] **Step 3: Wire both into `Nav.kt`.**
- [ ] **Step 4: Build** — Run: `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.
- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/ui/screens/MenuScreen.kt app/src/main/java/com/wcsim/ui/screens/HighScoresScreen.kt app/src/main/java/com/wcsim/ui/Nav.kt
git commit -m "feat: add main menu and high scores screens"
```

### Task 17: New career screen (pick country + reveal ability)

**Files:**
- Create: `app/src/main/java/com/wcsim/ui/screens/NewCareerScreen.kt`

- [ ] **Step 1: Write the screen** — a name text field, a scrollable list of countries (from `vm`'s in-progress world or a fresh `WorldGenerator` preview — simplest: let the user type a name + pick country by name from `NameData.COUNTRIES`), then a "Begin Career" button that calls `vm.startNewCareer(name, countryId)` and navigates to the HUD, showing the rolled special ability (name + description) on arrival.
- [ ] **Step 2: Build** → BUILD SUCCESSFUL.
- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/wcsim/ui/screens/NewCareerScreen.kt
git commit -m "feat: add new career setup screen (country pick + ability reveal)"
```

### Task 18: Career HUD + squad/tactics + player stats screens

**Files:**
- Create: `app/src/main/java/com/wcsim/ui/screens/HudScreen.kt`
- Create: `app/src/main/java/com/wcsim/ui/screens/SquadScreen.kt`
- Create: `app/src/main/java/com/wcsim/ui/screens/PlayersScreen.kt`

- [ ] **Step 1: Write `HudScreen.kt`** — shows season/age, nation, ability, reputation, current salary, career earnings, board expectation (`Contracts.expectationFor`), and buttons: Squad & Tactics, Players, Play Season, Retire. If `state.isOver`, route to CareerEnd.
- [ ] **Step 2: Write `SquadScreen.kt`** — formation dropdown (`Formation.entries`), training dropdown (`TrainingFocus.entries`), aggression `Slider` (0..100), and an XI picker: list the nation's players grouped by position with checkboxes; enforce exactly 11 selected before enabling Save (calls `vm.setSquad` + `vm.setTactics`). If none chosen, engine auto-picks best XI (already handled).
- [ ] **Step 3: Write `PlayersScreen.kt`** — sortable `LazyColumn` of the nation's players showing name, position, age, overall, and both **club stats** and **WC stats** (apps/goals/assists). Sort toggle by overall/goals.
- [ ] **Step 4: Wire into `Nav.kt`.**
- [ ] **Step 5: Build** → BUILD SUCCESSFUL.
- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/wcsim/ui/screens/HudScreen.kt app/src/main/java/com/wcsim/ui/screens/SquadScreen.kt app/src/main/java/com/wcsim/ui/screens/PlayersScreen.kt app/src/main/java/com/wcsim/ui/Nav.kt
git commit -m "feat: add HUD, squad/tactics, and player stats screens"
```

### Task 19: Season results + career end screens

**Files:**
- Create: `app/src/main/java/com/wcsim/ui/screens/SeasonScreen.kt`
- Create: `app/src/main/java/com/wcsim/ui/screens/CareerEndScreen.kt`

- [ ] **Step 1: Write `SeasonScreen.kt`** — after `vm.playSeason()`, render `vm.lastOutcome`: qualification record, WC stage reached + champion, a scrollable list of the match commentary lines, board verdict, and whether you were fired (+ new nation). "Continue" returns to HUD (or to CareerEnd if `state.isOver`).
- [ ] **Step 2: Write `CareerEndScreen.kt`** — show `state.endBlurb` (freak-accident death or retirement), the final score breakdown (`wcPoints`, earnings→points, trophies, seasons), total via `CareerEngine.finalScore`, then a "Save & Return to Menu" button calling `vm.finalizeCareer()`.
- [ ] **Step 3: Wire into `Nav.kt`; make Play Season navigate to SeasonScreen.**
- [ ] **Step 4: Build** → BUILD SUCCESSFUL.
- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wcsim/ui/screens/SeasonScreen.kt app/src/main/java/com/wcsim/ui/screens/CareerEndScreen.kt app/src/main/java/com/wcsim/ui/Nav.kt
git commit -m "feat: add season results and career end screens"
```

---

## Phase 7 — Integration & polish

### Task 20: Full-suite test run, install, and manual smoke test

- [ ] **Step 1: Run the entire unit-test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests green.

- [ ] **Step 2: Assemble the debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL; APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 3: Install on a device/emulator** (requires the user's SDK/adb)

Run: `./gradlew installDebug` (or `adb install -r app/build/outputs/apk/debug/app-debug.apk`)
Expected: app installs and launches.

- [ ] **Step 4: Manual smoke checklist** (do on device)
  - New Career → pick a country → ability is revealed.
  - HUD shows season 1, age 30, expectation, salary.
  - Squad & Tactics → set formation, training, aggression, pick an XI → Save.
  - Players screen shows club + WC stats.
  - Play Season → see qualification result, WC commentary, board verdict.
  - Continue several seasons; confirm earnings grow, players age, occasional firing moves you to a new nation.
  - Reach season 50 (or Retire) → death/retirement blurb + final score.
  - Save & Return → score appears in High Scores, persists after app restart.

- [ ] **Step 5: Commit any fixes found during smoke test**

```bash
git add -A
git commit -m "fix: address issues found during manual smoke test"
```

- [ ] **Step 6: Tag a build**

```bash
git tag v1.0
```

---

## Self-review notes (author)

- **Spec coverage:** country pick (T17), player stats club+WC (T2, T18), firing (T11–12), salary negotiation + earnings→score (T10–12), random ability (T3, T12), 50-season cap + freak-accident death (T12), qualify→WC progression (T8–9), scoring + persistent high scores (T10, T13–14), all-text commentary (T7). Architecture (pure engine + thin UI + local JSON) realized across phases.
- **Determinism:** every engine entry point takes an injected `Random`; tests seed it.
- **Type consistency:** `Player.countryId` added in T5 and used consistently thereafter; `TeamRating`, `WcStage`, `SpecialAbility`, `Tactics`, `Squad`, `CareerState`, `SeasonOutcome` names match across tasks.
- **Known tuning knobs** (safe to adjust without structural change): `QUALIFY_POINTS`, stage points in `Scoring`, firing thresholds in `Contracts`, ability multipliers in `TeamStrength`, `RETIRE_AGE`.
