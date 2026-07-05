package com.wcsim.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wcsim.data.GameRepository
import com.wcsim.engine.career.CareerEngine
import com.wcsim.engine.career.CareerState
import com.wcsim.engine.career.SeasonOutcome
import com.wcsim.engine.model.Squad
import com.wcsim.engine.model.Tactics
import com.wcsim.engine.score.HighScore
import kotlin.random.Random

class GameViewModel(private val repo: GameRepository) : ViewModel() {
    var state by mutableStateOf<CareerState?>(repo.loadCareer())
        private set
    var lastOutcome by mutableStateOf<SeasonOutcome?>(null)
        private set

    private val rng = Random(System.nanoTime())

    fun hasSave() = repo.hasSave()
    fun scores() = repo.loadScores()

    fun startNewCareer(name: String, countryId: Int) {
        val s = CareerEngine.newCareer(name.ifBlank { "Coach" }, countryId, rng)
        state = s; lastOutcome = null; repo.saveCareer(s)
    }

    fun setTactics(t: Tactics) { state = state?.copy(tactics = t)?.also { repo.saveCareer(it) } }
    fun setSquad(s: Squad) { state = state?.copy(squad = s)?.also { repo.saveCareer(it) } }

    fun playSeason() {
        val cur = state ?: return
        val (next, outcome) = CareerEngine.playSeason(cur, rng)
        state = next; lastOutcome = outcome; repo.saveCareer(next)
    }

    fun retireNow() {
        val cur = state ?: return
        state = CareerEngine.endCareer(cur, rng, retired = true).also { repo.saveCareer(it) }
    }

    fun finalizeCareer() {
        val cur = state ?: return
        if (!cur.isOver) return
        repo.addScore(HighScore.Entry(
            coachName = cur.coachName,
            country = cur.country.name,
            seasons = cur.season - 1,
            score = CareerEngine.finalScore(cur),
            causeOfDeath = cur.endBlurb ?: "unknown",
        ))
        repo.clearCareer()
        state = null; lastOutcome = null
    }
}

fun simpleFactory(repo: com.wcsim.data.GameRepository) =
    object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(repo) as T
    }
