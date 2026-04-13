package fr.sercurio.soulseek

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()

    setContent { NavigationGraph() }
  }
}
