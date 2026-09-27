package edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.virginia.cs4720.uvabucketlist_tyb8mr.data.BucketRepository
import edu.virginia.cs4720.uvabucketlist_tyb8mr.data.sortedForDisplay
import edu.virginia.cs4720.uvabucketlist_tyb8mr.model.BucketItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ListViewModel : ViewModel() {
    val items: StateFlow<List<BucketItem>> = BucketRepository.items
        .map { it.sortedForDisplay() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BucketRepository.items.value.sortedForDisplay()
        )

    fun toggleCompletion(id: String) {
        BucketRepository.toggleCompletion(id)
    }
}
