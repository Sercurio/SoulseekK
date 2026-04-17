package fr.sercurio.soulseek.presentation.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.sercurio.soulseek.presentation.rooms.components.ChatMessageList
import fr.sercurio.soulseek.presentation.rooms.components.Dropdown
import org.koin.androidx.compose.koinViewModel

@Composable
fun RoomsScreen(roomsViewModel: RoomsViewModel = koinViewModel()) {
  val messageInput by roomsViewModel.messageInput.collectAsStateWithLifecycle()

  val rooms by roomsViewModel.roomsListState.collectAsStateWithLifecycle()
  val currentRoom by roomsViewModel.currentRoomState.collectAsStateWithLifecycle()
  val messages by roomsViewModel.currentRoomMessages.collectAsStateWithLifecycle()

  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  Column {
    Dropdown(Modifier.padding(20.dp), currentRoom, rooms) {
      roomsViewModel.joinRoom(it.name)
      focusManager.clearFocus()
    }

    ChatMessageList(messages, "gladOS", Modifier.weight(1f))

    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
          modifier = Modifier.weight(5 / 6f),
          singleLine = true,
          value = messageInput,
          onValueChange = { roomsViewModel.onMessageChange(it) },
          label = { Text("Type something") },
          shape = RoundedCornerShape(percent = 20),
      )
      IconButton(
          modifier = Modifier.align(alignment = Alignment.CenterVertically),
          onClick = {
            roomsViewModel.sendRoomMessage()
            keyboardController?.hide()
          },
      ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "",
            tint = Color.Black,
        )
      }
    }
  }
}

@Preview
@Composable
fun RoomsScreenPreview() {
  RoomsScreen()
}
