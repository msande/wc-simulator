package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class SpecialAbility(val displayName: String, val description: String) {
    ATTACKING_MASTERMIND("Attacking Mastermind", "Your teams score more freely."),
    IRON_WALL("Iron Wall", "Your defense concedes fewer goals."),
    FITNESS_GURU("Fitness Guru", "Better stamina; you dominate late in matches."),
    YOUTH_WHISPERER("Youth Whisperer", "Emerging players develop to a higher ceiling."),
    MOTIVATOR("Motivator", "Your teams overperform in knockout matches."),
    NEGOTIATOR("Negotiator", "You command higher salaries at the table.");
}
