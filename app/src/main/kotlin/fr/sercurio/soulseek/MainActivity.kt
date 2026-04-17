package fr.sercurio.soulseek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.sercurio.soulseek.navigation.NavigationGraph
import fr.sercurio.soulseek.theme.SoulseekTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()

    setContent { SoulseekTheme { NavigationGraph() } }
  }
}
