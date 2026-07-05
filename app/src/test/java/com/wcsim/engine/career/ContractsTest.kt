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
