package fr.sercurio.soulseek.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sercurio.soulseek.SoulseekClient
import fr.sercurio.soulseek.data.model.SoulFile
import kotlinx.coroutines.launch

class SearchViewModel(val soulseekClient: SoulseekClient) : ViewModel() {
    val searchRepliesState = soulseekClient

    fun search(searchFileRequest: String) {
        viewModelScope.launch { soulseekClient.fileSearch(searchFileRequest) }
    }

//    fun queueUpload(username: String, soulFile: SoulFile) {
//        viewModelScope.launch { soulseekClient.queueUpload(username, soulFile) }
//    }
}
