package com.wcsim.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wcsim.ui.screens.CareerEndScreen
import com.wcsim.ui.screens.HighScoresScreen
import com.wcsim.ui.screens.HudScreen
import com.wcsim.ui.screens.MenuScreen
import com.wcsim.ui.screens.NewCareerScreen
import com.wcsim.ui.screens.PlayersScreen
import com.wcsim.ui.screens.SeasonScreen
import com.wcsim.ui.screens.SquadScreen

object Routes {
    const val MENU = "menu"
    const val NEW_CAREER = "new"
    const val HUD = "hud"
    const val SQUAD = "squad"
    const val PLAYERS = "players"
    const val SEASON = "season"
    const val CAREER_END = "end"
    const val HIGH_SCORES = "scores"
}

@Composable
fun AppNavHost(nav: NavHostController, vm: GameViewModel) {
    NavHost(navController = nav, startDestination = Routes.MENU) {
        composable(Routes.MENU) { MenuScreen(vm, nav) }
        composable(Routes.NEW_CAREER) { NewCareerScreen(vm, nav) }
        composable(Routes.HUD) { HudScreen(vm, nav) }
        composable(Routes.SQUAD) { SquadScreen(vm, nav) }
        composable(Routes.PLAYERS) { PlayersScreen(vm, nav) }
        composable(Routes.SEASON) { SeasonScreen(vm, nav) }
        composable(Routes.CAREER_END) { CareerEndScreen(vm, nav) }
        composable(Routes.HIGH_SCORES) { HighScoresScreen(vm, nav) }
    }
}
