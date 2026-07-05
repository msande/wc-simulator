package com.wcsim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wcsim.ui.GameViewModel
import com.wcsim.ui.Routes

@Composable
fun MenuScreen(vm: GameViewModel, nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "World Cup Simulator",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { nav.navigate(Routes.NEW_CAREER) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("New Career")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { nav.navigate(Routes.HUD) },
            enabled = vm.hasSave(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { nav.navigate(Routes.HIGH_SCORES) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("High Scores")
        }
    }
}
