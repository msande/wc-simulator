package com.wcsim.engine.world

import org.junit.Assert.assertTrue
import org.junit.Test

class NameDataTest {
    @Test fun has_at_least_32_countries() {
        assertTrue(NameData.COUNTRIES.size >= 32)
    }
    @Test fun name_pools_are_nonempty() {
        assertTrue(NameData.FIRST_NAMES.size >= 20)
        assertTrue(NameData.LAST_NAMES.size >= 20)
        assertTrue(NameData.CLUB_PREFIXES.isNotEmpty())
        assertTrue(NameData.CLUB_SUFFIXES.isNotEmpty())
    }
}
