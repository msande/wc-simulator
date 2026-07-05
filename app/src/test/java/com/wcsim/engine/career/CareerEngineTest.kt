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
