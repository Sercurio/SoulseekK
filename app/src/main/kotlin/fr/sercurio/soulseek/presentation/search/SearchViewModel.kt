package fr.sercurio.soulseek.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sercurio.soulseek.SoulseekClient
import fr.sercurio.soulseek.data.model.SoulFile
import fr.sercurio.soulseek.presentation.search.components.SectionData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(val soulseekClient: SoulseekClient) : ViewModel() {
  val searchesReplies =
      soulseekClient.fileSearchResults
          .map { resultsMap ->
            resultsMap.values
                .flatten()
                .groupBy { it.username }
                .map { (username, replies) ->
                  SectionData(headerText = username, items = replies.flatMap { it.soulFiles })
                }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList(),
          )

  fun search(searchFileRequest: String) {
    viewModelScope.launch { soulseekClient.fileSearch(searchFileRequest) }
  }

  fun queueUpload(username: String, soulFile: SoulFile) {
    viewModelScope.launch { soulseekClient.queueUpload(username, soulFile) }
  }
}
