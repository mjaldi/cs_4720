package edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import edu.virginia.cs4720.uvabucketlist_tyb8mr.model.BucketItem
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.components.formatted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    items: List<BucketItem>,
    onToggle: (String) -> Unit,
    onEdit: (String) -> Unit,
    onAdd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UVA Bucket List", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add new bucket list item")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Your bucket list is empty -- tap + to add your first UVA adventure!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = items, key = { it.id }) { item ->
                    BucketItemRow(
                        item = item,
                        onToggle = { onToggle(item.id) },
                        onEdit = { onEdit(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BucketItemRow(item: BucketItem, onToggle: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.completed) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.completed, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    "Due ${item.dueDate.formatted()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val completedDate = item.completedDate
                if (item.completed && completedDate != null) {
                    Text(
                        "Completed ${completedDate.formatted()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit ${item.name}")
            }
        }
    }
}
