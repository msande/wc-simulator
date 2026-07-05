package com.wcsim.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.engine.career.CareerEngine
import com.wcsim.engine.score.Scoring
import com.wcsim.ui.GameViewModel
import com.wcsim.ui.Routes

@Composable
fun CareerEndScreen(vm: GameViewModel, nav: NavHostController) {
    val s = vm.state ?: run {
        nav.navigate(Routes.MENU)
        return
    }

    val earningsPoints = Scoring.earningsPoints(s.careerEarnings)
    val trophyBonus = s.trophies * 150
    val longevity = (s.season - 1) * 5
    val total = CareerEngine.finalScore(s)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Career Over",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))
        s.endBlurb?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
        }

        Text("Coach: ${s.coachName}", style = MaterialTheme.typography.titleMedium)
        Text("Final nation: ${s.country.name}")
        Text("Seasons coached: ${s.season - 1}")
        Text("Trophies: ${s.trophies}")
        Spacer(Modifier.height(16.dp))

        Text("Score breakdown", style = MaterialTheme.typography.titleMedium)
        Text("World Cup points: ${s.wcPoints}")
        Text("Earnings points: $earningsPoints")
        Text("Trophies bonus: $trophyBonus")
        Text("Longevity: $longevity")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Total score: $total",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                vm.finalizeCareer()
                nav.navigate(Routes.MENU) {
                    popUpTo(Routes.MENU) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save & Return to Menu")
        }
    }
}
