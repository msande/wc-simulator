package com.wcsim.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.ui.GameViewModel

@Composable
fun HighScoresScreen(vm: GameViewModel, nav: NavHostController) {
    val entries = vm.scores().entries
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "High Scores",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))
        if (entries.isEmpty()) {
            Text("No careers recorded yet.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(entries) { index, entry ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "${index + 1}. ${entry.coachName} — ${entry.country}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Score ${entry.score} • ${entry.seasons} seasons",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = entry.causeOfDeath,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back")
        }
    }
}
