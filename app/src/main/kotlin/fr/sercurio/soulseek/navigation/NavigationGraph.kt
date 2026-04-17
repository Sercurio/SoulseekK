package fr.sercurio.soulseek.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.sercurio.soulseek.MainScreen
import fr.sercurio.soulseek.presentation.login.LoginScreen
import kotlinx.serialization.Serializable

@Serializable object Login

@Serializable object Main

@Composable
fun NavigationGraph() {
  val navController: NavHostController = rememberNavController()

  NavHost(navController, startDestination = Login) {
    composable<Login> { _ -> LoginScreen({ navController.navigate(Main) }) }
    composable<Main> { _ -> MainScreen() }
  }
}
