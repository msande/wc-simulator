package com.wcsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.wcsim.data.GameRepository
import com.wcsim.ui.AppNavHost
import com.wcsim.ui.GameViewModel
import com.wcsim.ui.simpleFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = GameRepository(applicationContext)
        setContent {
            MaterialTheme {
                Surface {
                    val vm: GameViewModel = viewModel(factory = simpleFactory(repo))
                    val nav = rememberNavController()
                    AppNavHost(nav, vm)
                }
            }
        }
    }
}
