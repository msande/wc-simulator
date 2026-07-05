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
