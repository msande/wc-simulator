package com.wcsim.engine.career

import com.wcsim.engine.model.*
import kotlinx.serialization.Serializable

@Serializable
data class CareerState(
    val coachName: String,
    val ability: SpecialAbility,
    val world: World,
    val countryId: Int,
    val age: Int = 30,
    val season: Int = 1,           // 1..50
    val salary: Long = 0,
    val careerEarnings: Long = 0,
    val reputation: Int = 30,      // 0..100
    val trophies: Int = 0,
    val wcPoints: Int = 0,
    val tactics: Tactics = Tactics(),
    val squad: Squad? = null,
    val isOver: Boolean = false,
    val endBlurb: String? = null,
) {
    val country: Country get() = world.country(countryId)
    val maxSeasons: Int get() = 50
}
