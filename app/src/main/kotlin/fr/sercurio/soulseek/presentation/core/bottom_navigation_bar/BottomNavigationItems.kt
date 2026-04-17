package fr.sercurio.soulseek.presentation.core.bottom_navigation_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import fr.sercurio.soulseek.navigation.Screens

data class BottomNavigationItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.NotInterested,
    val route: String = "",
) {
  fun bottomNavigationItems(): List<BottomNavigationItem> {
    return listOf(
        BottomNavigationItem(
            label = "Rooms",
            icon = Icons.Filled.ChatBubble,
            route = Screens.Rooms.route,
        ),
        BottomNavigationItem(
            label = "Search",
            icon = Icons.Filled.Search,
            route = Screens.Search.route,
        ),
        BottomNavigationItem(
            label = "Settings",
            icon = Icons.Filled.Settings,
            route = Screens.Settings.route,
        ),
    )
  }
}
