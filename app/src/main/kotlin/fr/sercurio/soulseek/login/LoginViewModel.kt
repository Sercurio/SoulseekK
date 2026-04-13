package fr.sercurio.soulseek.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sercurio.soulseek.SoulseekClient
import kotlinx.coroutines.launch

class LoginViewModel(val soulseekClient: SoulseekClient) : ViewModel() {
  val loginState = soulseekClient.login

  init {
    viewModelScope.launch { soulseekClient.connect() }
  }

  fun login(username: String, password: String) {
    viewModelScope.launch { soulseekClient.login(username, password) }
  }
}
