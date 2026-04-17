package fr.sercurio.soulseek.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SoulseekTheme(content: @Composable () -> Unit) {
  val colorScheme =
      lightColorScheme(
          primary = Color(0xFF0000CC),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFDDE1FF),
          onPrimaryContainer = Color(0xFF00006E),
          secondary = Color(0xFF3A5BA0),
          background = Color(0xFFF8F8FF),
          surface = Color(0xFFF8F8FF),
      )

  MaterialTheme(
      colorScheme = colorScheme,
      content = content,
  )
}
