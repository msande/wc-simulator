package com.wcsim.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.ui.GameViewModel
import com.wcsim.ui.Routes

@Composable
fun SeasonScreen(vm: GameViewModel, nav: NavHostController) {
    val outcome = vm.lastOutcome
    val careerOver = vm.state?.isOver == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Season Results",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))

        if (outcome == null) {
            Text("No season has been played yet.")
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { nav.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Back") }
            return
        }

        val q = outcome.qualification
        val lines = buildList {
            add(
                "Qualification: ${q.wins}W-${q.draws}D-${q.losses}L — " +
                    if (q.qualified) "QUALIFIED" else "DID NOT QUALIFY",
            )
            add("Board expectation: ${outcome.expectation.name}")
            outcome.yourStage?.let { add("World Cup stage reached: ${it.name}") }
            outcome.worldCup?.let { add("World Cup champion: ${it.champion}") }
            add(if (outcome.fired) "Board verdict: SACKED" else "Board verdict: retained")
            add("Salary earned: ${outcome.salaryEarned}")
            add("")
            addAll(outcome.narrative)
            outcome.worldCup?.let { wc ->
                if (wc.knockoutResults.isNotEmpty()) {
                    add("")
                    add("— Knockout results —")
                    wc.knockoutResults.forEach { add(it.scoreline) }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(lines) { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (careerOver) {
                    nav.navigate(Routes.CAREER_END)
                } else {
                    nav.navigate(Routes.HUD) {
                        popUpTo(Routes.HUD) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue")
        }
    }
}
