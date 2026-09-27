package edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.virginia.cs4720.uvabucketlist_tyb8mr.data.BucketRepository
import edu.virginia.cs4720.uvabucketlist_tyb8mr.model.BucketItem
import java.time.LocalDate

class DetailViewModel(private val itemId: String) : ViewModel() {
    private val original: BucketItem? = BucketRepository.getById(itemId)

    var name by mutableStateOf(original?.name ?: "")
        private set
    var dueDate by mutableStateOf(original?.dueDate ?: LocalDate.now())
        private set
    var completed by mutableStateOf(original?.completed ?: false)
        private set
    var completedDate by mutableStateOf(original?.completedDate)
        private set

    fun onNameChange(value: String) { name = value }
    fun onDueDateChange(value: LocalDate) { dueDate = value }
    fun onCompletedToggle() {
        completed = !completed
        completedDate = if (completed) LocalDate.now() else null
    }
    fun isValid(): Boolean = name.isNotBlank()

    fun save() {
        BucketRepository.updateItem(
            BucketItem(id = itemId, name = name.trim(), dueDate = dueDate, completed = completed, completedDate = completedDate)
        )
    }
}

class DetailViewModelFactory(private val itemId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(itemId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
