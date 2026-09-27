package edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.virginia.cs4720.uvabucketlist_tyb8mr.data.BucketRepository
import edu.virginia.cs4720.uvabucketlist_tyb8mr.model.BucketItem
import java.time.LocalDate

class CreateViewModel : ViewModel() {
    var name by mutableStateOf("")
        private set
    var dueDate by mutableStateOf(LocalDate.now())
        private set

    fun onNameChange(value: String) { name = value }
    fun onDueDateChange(value: LocalDate) { dueDate = value }
    fun isValid(): Boolean = name.isNotBlank()

    fun save() {
        BucketRepository.addItem(BucketItem(name = name.trim(), dueDate = dueDate))
    }
}
