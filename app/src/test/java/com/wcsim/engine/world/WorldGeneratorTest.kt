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
