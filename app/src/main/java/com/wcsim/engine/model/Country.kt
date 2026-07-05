package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Club(val id: Int, val name: String, val strength: Int)

@Serializable
data class Country(
    val id: Int,
    val name: String,
    /** Base footballing strength 40..90, drives squad quality + difficulty. */
    val baseStrength: Int,
)
