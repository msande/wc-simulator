package com.wcsim.data

import com.wcsim.engine.career.CareerState
import com.wcsim.engine.score.HighScore
import kotlinx.serialization.json.Json

object SaveCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encodeCareer(state: CareerState): String = json.encodeToString(CareerState.serializer(), state)
    fun decodeCareer(text: String): CareerState = json.decodeFromString(CareerState.serializer(), text)

    fun encodeScores(table: HighScore.Table): String =
        json.encodeToString(HighScore.Table.serializer(), table)
    fun decodeScores(text: String): HighScore.Table =
        json.decodeFromString(HighScore.Table.serializer(), text)
}
