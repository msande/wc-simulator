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
