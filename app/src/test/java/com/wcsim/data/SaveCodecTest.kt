package com.wcsim.data

import com.wcsim.engine.career.CareerEngine
import com.wcsim.engine.score.HighScore
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class SaveCodecTest {
    @Test fun career_round_trips_through_json() {
        val s = CareerEngine.newCareer("Coach", 0, Random(1))
        val json = SaveCodec.encodeCareer(s)
        val back = SaveCodec.decodeCareer(json)
        assertEquals(s.coachName, back.coachName)
        assertEquals(s.world.players.size, back.world.players.size)
        assertEquals(s.ability, back.ability)
    }

    @Test fun high_score_table_sorts_desc_and_caps() {
        var table = HighScore.Table()
        table = HighScore.add(table, HighScore.Entry("A", "Brazil", 10, 1234, "meteor"))
        table = HighScore.add(table, HighScore.Entry("B", "Japan", 20, 9999, "seagulls"))
        assertEquals("B", table.entries.first().coachName)
        val json = SaveCodec.encodeScores(table)
        assertEquals(table, SaveCodec.decodeScores(json))
    }
}
