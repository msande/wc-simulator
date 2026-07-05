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
