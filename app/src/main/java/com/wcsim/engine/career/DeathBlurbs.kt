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
