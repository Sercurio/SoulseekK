package fr.sercurio.soulseek.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import fr.sercurio.soulseek.domain.model.Room

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    modifier: Modifier = Modifier,
    currentRoom: Room?,
    rooms: List<Room>,
    joinRoom: (Room) -> Unit = {},
) {
  var expanded by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf(currentRoom?.name ?: "") }

  val filteredRooms =
      remember(searchQuery, rooms) {
        if (searchQuery.isBlank()) rooms
        else rooms.filter { it.name.contains(searchQuery, ignoreCase = true) }
      }

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = modifier.fillMaxWidth(),
  ) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = {
          searchQuery = it
          expanded = true
        },
        modifier = Modifier.fillMaxWidth().menuAnchor(),
        label = { Text("Rooms") },
        placeholder = { Text("Rechercher une room...") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        singleLine = true,
    )

    ExposedDropdownMenu(
        expanded = expanded && filteredRooms.isNotEmpty(),
        onDismissRequest = { expanded = false },
    ) {
      filteredRooms.forEach { room ->
        DropdownMenuItem(
            text = {
              Text(
                  text = room.name,
                  fontWeight = if (room == currentRoom) FontWeight.Bold else FontWeight.Normal,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
            },
            trailingIcon = {
              Text(
                  text = "${room.userCount} 👤",
              )
            },
            onClick = {
              searchQuery = room.name
              expanded = false
              joinRoom(room)
            },
        )
      }
    }
  }
}
