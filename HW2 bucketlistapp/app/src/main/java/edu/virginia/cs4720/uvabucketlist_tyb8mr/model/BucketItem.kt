package edu.virginia.cs4720.uvabucketlist_tyb8mr.model

import java.time.LocalDate
import java.util.UUID

data class BucketItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dueDate: LocalDate,
    val completed: Boolean = false,
    val completedDate: LocalDate? = null
)
