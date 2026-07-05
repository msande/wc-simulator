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
