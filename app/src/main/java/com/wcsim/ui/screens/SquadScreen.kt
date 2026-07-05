package com.wcsim.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.engine.model.Formation
import com.wcsim.engine.model.Squad
import com.wcsim.engine.model.Tactics
import com.wcsim.engine.model.TrainingFocus
import com.wcsim.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(vm: GameViewModel, nav: NavHostController) {
    val s = vm.state ?: run {
        nav.popBackStack()
        return
    }

    var formation by remember { mutableStateOf(s.tactics.formation) }
    var training by remember { mutableStateOf(s.tactics.training) }
    var aggression by remember { mutableFloatStateOf(s.tactics.aggression.toFloat()) }
    val selectedIds = remember {
        mutableStateListOf<Int>().apply { s.squad?.playerIds?.let { addAll(it) } }
    }

    val players = s.world.playersOf(s.countryId).sortedWith(
        compareBy({ it.position.ordinal }, { -it.overall }),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Squad & Tactics",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))

        Text("Formation", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Formation.entries.forEach { f ->
                FilterChip(
                    selected = formation == f,
                    onClick = { formation = f },
                    label = { Text(f.label) },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Text("Training", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TrainingFocus.entries.forEach { t ->
                FilterChip(
                    selected = training == t,
                    onClick = { training = t },
                    label = { Text(t.name) },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Text("Aggression: ${aggression.toInt()}", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = aggression,
            onValueChange = { aggression = it },
            valueRange = 0f..100f,
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Starting XI (${selectedIds.size}/11 selected)",
            style = MaterialTheme.typography.titleSmall,
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players, key = { it.id }) { player ->
                val checked = selectedIds.contains(player.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (!selectedIds.contains(player.id)) selectedIds.add(player.id)
                            } else {
                                selectedIds.remove(player.id)
                            }
                        },
                    )
                    Text(
                        text = player.position.name,
                        modifier = Modifier.width(48.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = player.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "OVR ${player.overall}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                vm.setTactics(
                    Tactics(
                        formation = formation,
                        training = training,
                        rawAggression = aggression.toInt(),
                    ),
                )
                vm.setSquad(Squad(selectedIds.toList()))
                nav.popBackStack()
            },
            enabled = selectedIds.size == 11,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }
    }
}
