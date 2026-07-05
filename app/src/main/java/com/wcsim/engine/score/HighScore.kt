package com.wcsim.engine.score

import kotlinx.serialization.Serializable

object HighScore {
    @Serializable
    data class Entry(
        val coachName: String,
        val country: String,
        val seasons: Int,
        val score: Int,
        val causeOfDeath: String,
    )

    @Serializable
    data class Table(val entries: List<Entry> = emptyList())

    private const val MAX = 20

    fun add(table: Table, entry: Entry): Table =
        Table((table.entries + entry).sortedByDescending { it.score }.take(MAX))
}
