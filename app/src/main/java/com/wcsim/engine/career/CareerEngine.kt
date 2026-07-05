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
