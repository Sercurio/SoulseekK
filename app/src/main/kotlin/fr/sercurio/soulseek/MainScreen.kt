package fr.sercurio.soulseek

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.sercurio.soulseek.bottom_navigation_bar.BottomNavigationBar
import fr.sercurio.soulseek.rooms.RoomsScreen
import fr.sercurio.soulseek.search.SearchScreen
import fr.sercurio.soulseek.settings.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun MainScreen(soulseekClient: SoulseekClient = koinInject()) {
    val navController = rememberNavController()

    Scaffold(bottomBar = { BottomNavigationBar(soulseekClient, navController) }) { padding ->
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
    MainScreen(SoulseekClient())
}
