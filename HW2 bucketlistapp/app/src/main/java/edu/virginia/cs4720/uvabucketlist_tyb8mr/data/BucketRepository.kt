package edu.virginia.cs4720.uvabucketlist_tyb8mr.data

import edu.virginia.cs4720.uvabucketlist_tyb8mr.model.BucketItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

object BucketRepository {
    private val _items = MutableStateFlow(seedData())
    val items: StateFlow<List<BucketItem>> = _items.asStateFlow()

    fun addItem(item: BucketItem) {
        _items.update { current -> current + item }
    }

    fun updateItem(updated: BucketItem) {
        _items.update { current -> current.map { if (it.id == updated.id) updated else it } }
    }

    fun getById(id: String): BucketItem? = _items.value.find { it.id == id }

    fun toggleCompletion(id: String) {
        _items.update { current ->
            current.map {
                if (it.id != id) it
                else if (it.completed) it.copy(completed = false, completedDate = null)
                else it.copy(completed = true, completedDate = LocalDate.now())
            }
        }
    }

    private fun seedData(): List<BucketItem> = listOf(
        BucketItem(name = "Eat at Bodo's at 6 AM", dueDate = LocalDate.now().plusDays(30)),
        BucketItem(name = "Watch the sunrise from the Rotunda steps", dueDate = LocalDate.now().plusDays(10)),
        BucketItem(name = "Tailgate at a football game on the Lawn", dueDate = LocalDate.now().plusDays(60))
    )
}

fun List<BucketItem>.sortedForDisplay(): List<BucketItem> =
    sortedWith(compareBy<BucketItem> { it.completed }.thenBy { it.dueDate })
