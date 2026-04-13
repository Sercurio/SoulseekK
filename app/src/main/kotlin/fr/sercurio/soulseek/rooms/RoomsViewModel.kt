package fr.sercurio.soulseek.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sercurio.soulseek.SoulseekClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomsViewModel(val soulseekClient: SoulseekClient) : ViewModel() {
  private val _messageInput = MutableStateFlow("")
  private val _currentRoomName = MutableStateFlow<String?>(null)

  val messageInput = _messageInput.asStateFlow()
  val roomsListState = soulseekClient.rooms

  val currentRoomState =
      combine(roomsListState, _currentRoomName) { rooms, name -> rooms.find { it.name == name } }
          .stateIn(viewModelScope, WhileSubscribed(5000), null)

  val currentRoomMessages =
      currentRoomState
          .map { room -> room?.messages ?: emptyList() }
          .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

  fun joinRoom(roomName: String) = viewModelScope.launch {
    _currentRoomName.value = roomName
    soulseekClient.joinRoom(roomName)
  }

  fun onMessageChange(newValue: String) {
    _messageInput.value = newValue
  }

  fun sendRoomMessage() {
    viewModelScope.launch {
      _currentRoomName.value?.let {
        soulseekClient.sendRoomMessage(it, messageInput.value)
        _messageInput.value = ""
      }
    }
  }
}
