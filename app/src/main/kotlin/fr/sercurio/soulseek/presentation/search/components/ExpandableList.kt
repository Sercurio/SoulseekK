package fr.sercurio.soulseek.presentation.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.sercurio.soulseek.data.model.SoulFile

data class SectionData(val headerText: String, val items: List<SoulFile>)

@Composable
fun SectionItem(
    username: String,
    soulFile: SoulFile,
    downloadCallback: (String, SoulFile) -> Unit,
) {
  Column(Modifier.padding(horizontal = 20.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(text = soulFile.filename)
      Spacer(modifier = Modifier.weight(1f))
      IconButton(onClick = { downloadCallback(username, soulFile) }) {
        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "download")
      }
    }
    HorizontalDivider(modifier = Modifier.padding(top = 10.dp), thickness = 1.dp)
  }
}

@Composable
fun SectionHeader(text: String, isExpanded: Boolean, onHeaderClicked: () -> Unit) {
  Row(
      modifier =
          Modifier.clickable { onHeaderClicked() }
              .background(Color.LightGray)
              .padding(vertical = 8.dp, horizontal = 16.dp)
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1.0f),
    )
    if (isExpanded) {
      Icon(Icons.Filled.ChevronRight, contentDescription = "chevron_right")
    } else {
      Icon(Icons.Filled.ChevronLeft, contentDescription = "chevron_left")
    }
  }
}

@Composable
fun ExpandableList(
    modifier: Modifier = Modifier,
    sections: List<SectionData>,
    downloadCallback: (String, SoulFile) -> Unit,
) {
  // On garde ton système de Map avec le username comme clé
  val isExpandedMap = rememberSavableSnapshotStateMap { mutableStateMapOf<String, Boolean>() }

  LazyColumn(
      modifier = modifier,
  ) {
    sections.forEach { sectionData ->
      val username = sectionData.headerText
      val isExpanded = isExpandedMap[username] == true

      section(
          sectionData = sectionData,
          isExpanded = isExpanded,
          onHeaderClick = { isExpandedMap[username] = !isExpanded },
          downloadCallback = downloadCallback,
      )
    }
  }
}

fun LazyListScope.section(
    sectionData: SectionData,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    downloadCallback: (String, SoulFile) -> Unit,
) {
  item(key = "header_${sectionData.headerText}") {
    SectionHeader(
        text = "${sectionData.headerText} (${sectionData.items.size} fichiers)",
        isExpanded = isExpanded,
        onHeaderClicked = onHeaderClick,
    )
  }

  if (isExpanded) {
    items(
        items = sectionData.items,
        key = { file -> "${sectionData.headerText}_${file.filename}_${file.size}" },
    ) { soulFile ->
      SectionItem(sectionData.headerText, soulFile, downloadCallback)
    }
  }
}

fun <K, V> snapshotStateMapSaver() =
    Saver<SnapshotStateMap<K, V>, Any>(
        save = { state -> state.toList() },
        restore = { value ->
          @Suppress("UNCHECKED_CAST")
          (value as? List<Pair<K, V>>)?.toMutableStateMap() ?: mutableStateMapOf<K, V>()
        },
    )

@Composable
fun <K, V> rememberSavableSnapshotStateMap(
    init: () -> SnapshotStateMap<K, V>
): SnapshotStateMap<K, V> = rememberSaveable(saver = snapshotStateMapSaver(), init = init)
