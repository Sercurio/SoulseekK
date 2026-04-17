package fr.sercurio.soulseek

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.sercurio.soulseek.navigation.Screens
import fr.sercurio.soulseek.presentation.core.bottom_navigation_bar.BottomNavigationBar
import fr.sercurio.soulseek.presentation.rooms.RoomsScreen
import fr.sercurio.soulseek.presentation.search.SearchScreen
import fr.sercurio.soulseek.presentation.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
  val navController = rememberNavController()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("SoulseekK") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        )
      },
      bottomBar = { BottomNavigationBar(navController) },
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      NavHost(navController = navController, startDestination = Screens.Rooms.route) {
        composable(Screens.Rooms.route) { RoomsScreen() }
        composable(Screens.Search.route) { SearchScreen() }
        composable(Screens.Settings.route) { SettingsScreen() }
      }
    }
  }
}

@Preview
@Composable
fun MainScreenPreview() {
  MainScreen()
}
