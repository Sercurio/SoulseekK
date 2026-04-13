package fr.sercurio.soulseek.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.sercurio.soulseek.domain.model.RoomMessage
import kotlin.math.abs

private val bubbleColors =
    listOf(
        Color(0xFF5B8CFF),
        Color(0xFFFF6B9D),
        Color(0xFF43D9AD),
        Color(0xFFFFB347),
        Color(0xFFB57BFF),
        Color(0xFF4FC3F7),
        Color(0xFFFF8A65),
    )

private fun colorForUser(username: String): Color =
    bubbleColors[abs(username.hashCode()) % bubbleColors.size]

@Composable
fun ChatMessageList(
    messages: List<RoomMessage>,
    currentUsername: String,
    modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
  }

  LazyColumn(
      state = listState,
      modifier = modifier.padding(horizontal = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
  ) {
    items(messages, key = { "${it.username}-${it.message}-${it.hashCode()}" }) { msg ->
      ChatBubble(message = msg, isOwn = msg.username == currentUsername)
    }
  }
}

@Composable
private fun ChatBubble(message: RoomMessage, isOwn: Boolean) {
  val alignment = if (isOwn) Alignment.End else Alignment.Start
  val bubbleColor =
      if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isOwn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
  val userColor = if (isOwn) Color.White.copy(alpha = 0.75f) else colorForUser(message.username)

  val bubbleShape =
      if (isOwn)
          RoundedCornerShape(
              topStart = 18.dp,
              topEnd = 4.dp,
              bottomStart = 18.dp,
              bottomEnd = 18.dp,
          )
      else
          RoundedCornerShape(
              topStart = 4.dp,
              topEnd = 18.dp,
              bottomStart = 18.dp,
              bottomEnd = 18.dp,
          )

  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = alignment,
  ) {
    if (!isOwn) {
      Text(
          text = message.username,
          color = userColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
      )
    }

    Box(
        modifier =
            Modifier.widthIn(min = 48.dp, max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
      Text(
          text = message.message,
          color = textColor,
          fontSize = 14.sp,
          lineHeight = 20.sp,
      )
    }
  }
}
