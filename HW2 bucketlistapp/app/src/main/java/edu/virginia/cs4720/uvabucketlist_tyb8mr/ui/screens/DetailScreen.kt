package edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.components.DueDateField
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.components.formatted
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    name: String,
    dueDate: LocalDate,
    completed: Boolean,
    completedDate: LocalDate?,
    onNameChange: (String) -> Unit,
    onDueDateChange: (LocalDate) -> Unit,
    onCompletedToggle: () -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Bucket List Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Item name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DueDateField(label = "Due date", date = dueDate, onDateSelected = onDueDateChange)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completed, onCheckedChange = { onCompletedToggle() })
                Text("Completed", style = MaterialTheme.typography.bodyLarge)
            }

            Text(
                text = if (completedDate != null) {
                    "Completed on ${completedDate.formatted()}"
                } else {
                    "Not completed yet"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(onClick = onSave, enabled = canSave, modifier = Modifier.weight(1f)) {
                    Text("Save")
                }
            }
        }
    }
}
