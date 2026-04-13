package fr.sercurio.soulseek.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.sercurio.soulseek.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(onNavigateToMain: () -> Unit, loginViewModel: LoginViewModel = koinViewModel()) {
  var username by rememberSaveable { mutableStateOf("gladOS") }
  var password by rememberSaveable { mutableStateOf("gladOS") }

  val focusManager = LocalFocusManager.current
  val context = LocalContext.current

  val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

  Column(
      modifier = Modifier.fillMaxSize().padding(26.dp),
      verticalArrangement = Arrangement.SpaceAround,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Column {
      Image(
          painter = painterResource(id = R.drawable.soulseek_logo),
          contentDescription = "soulseek_logo",
      )
      Text(text = "SoulseekK", style = MaterialTheme.typography.headlineSmall)
    }
    Row {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            shape = RoundedCornerShape(percent = 20),
        )

        Spacer(Modifier.height(20.dp))

        var showPassword by remember { mutableStateOf(value = false) }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { newText -> password = newText },
            label = { Text(text = "Password") },
            placeholder = { Text(text = "Type password here") },
            shape = RoundedCornerShape(percent = 20),
            visualTransformation =
                if (showPassword) {

                  VisualTransformation.None
                } else {

                  PasswordVisualTransformation()
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
              if (showPassword) {
                IconButton(onClick = { showPassword = false }) {
                  Icon(
                      imageVector = Icons.Filled.Visibility,
                      contentDescription = "hide_password",
                  )
                }
              } else {
                IconButton(onClick = { showPassword = true }) {
                  Icon(
                      imageVector = Icons.Filled.VisibilityOff,
                      contentDescription = "hide_password",
                  )
                }
              }
            },
        )
      }
    }
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(1f)) {
      Button(
          modifier = Modifier.fillMaxWidth(0.5f).height(60.dp),
          onClick = { loginViewModel.login(username, password) },
          content = { Text("Log in", style = MaterialTheme.typography.headlineSmall) },
      )
    }
    LaunchedEffect(loginState) {
      loginState?.let {
        if (it.success) {
          Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
          onNavigateToMain()
        } else {
          Toast.makeText(
                  context,
                  "Login failed: ${it.message}",
                  Toast.LENGTH_SHORT,
              )
              .show()
        }
      }
    }
  }
}
