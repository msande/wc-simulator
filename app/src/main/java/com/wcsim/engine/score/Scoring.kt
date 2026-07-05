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
