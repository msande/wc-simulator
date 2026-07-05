package com.wcsim.engine.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumsTest {
    @Test fun formation_slot_counts_sum_to_ten_outfield() {
        for (f in Formation.entries) {
            assertEquals("${f.name} must have 10 outfield slots",
                10, f.defenders + f.midfielders + f.forwards)
        }
    }

    @Test fun positions_cover_all_lines() {
        assertEquals(4, Position.entries.size) // GK, DEF, MID, FWD
    }
}
