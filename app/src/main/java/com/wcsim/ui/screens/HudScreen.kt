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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.engine.career.Contracts
import com.wcsim.ui.GameViewModel
import com.wcsim.ui.Routes

@Composable
fun HudScreen(vm: GameViewModel, nav: NavHostController) {
    val s = vm.state ?: run {
        nav.navigate(Routes.MENU)
        return
    }
    if (s.isOver) {
        LaunchedEffect(Unit) { nav.navigate(Routes.CAREER_END) }
        return
    }

    val expectation = Contracts.expectationFor(s.country.baseStrength)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = s.coachName,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Nation: ${s.country.name} (strength ${s.country.baseStrength})",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        Text("Season ${s.season} / ${s.maxSeasons}")
        Text("Age ${s.age}")
        Text("Reputation ${s.reputation}")
        Text("Salary ${s.salary}")
        Text("Career earnings ${s.careerEarnings}")
        Text("Trophies ${s.trophies}")
        Text("Board expectation: ${expectation.name}")
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Special ability: ${s.ability.displayName}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = s.ability.description,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { nav.navigate(Routes.SQUAD) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Squad & Tactics") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { nav.navigate(Routes.PLAYERS) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Players") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                vm.playSeason()
                nav.navigate(Routes.SEASON)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Play Season") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                vm.retireNow()
                nav.navigate(Routes.CAREER_END)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Retire") }
    }
}
