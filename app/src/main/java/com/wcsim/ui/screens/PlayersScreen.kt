package com.wcsim.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.ui.GameViewModel

private enum class PlayerSort { OVERALL, WC_GOALS }

@Composable
fun PlayersScreen(vm: GameViewModel, nav: NavHostController) {
    val s = vm.state ?: run {
        nav.popBackStack()
        return
    }

    var sort by remember { mutableStateOf(PlayerSort.OVERALL) }

    val players = s.world.playersOf(s.countryId).let { list ->
        when (sort) {
            PlayerSort.OVERALL -> list.sortedByDescending { it.overall }
            PlayerSort.WC_GOALS -> list.sortedByDescending { it.wcStats.goals }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "${s.country.name} Players",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                sort = if (sort == PlayerSort.OVERALL) PlayerSort.WC_GOALS else PlayerSort.OVERALL
            },
        ) {
            Text(
                text = when (sort) {
                    PlayerSort.OVERALL -> "Sort: Overall"
                    PlayerSort.WC_GOALS -> "Sort: WC Goals"
                },
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players, key = { it.id }) { player ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${player.position.name} • Age ${player.age} • OVR ${player.overall}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        text = "Club: ${player.clubStats.appearances} apps, " +
                            "${player.clubStats.goals} goals, ${player.clubStats.assists} assists",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "WC: ${player.wcStats.appearances} apps, " +
                            "${player.wcStats.goals} goals, ${player.wcStats.assists} assists " +
                            "(${player.wcStats.tournaments} tournaments)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back")
        }
    }
}
