package edu.virginia.cs4720.uvabucketlist_tyb8mr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.screens.ListScreen
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.theme.Uvabucketlisttyb8mrTheme
import edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel.ListViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Uvabucketlisttyb8mrTheme {
                val items by viewModel.items.collectAsStateWithLifecycle()
                ListScreen(
                    items = items,
                    onToggle = viewModel::toggleCompletion,
                    onEdit = { id ->
                        startActivity(Intent(this, DetailActivity::class.java).putExtra(EXTRA_ITEM_ID, id))
                    },
                    onAdd = {
                        startActivity(Intent(this, CreateActivity::class.java))
                    }
                )
            }
        }
    }
}