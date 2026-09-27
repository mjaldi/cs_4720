package edu.virginia.cs4720.uvabucketlist_tyb8mr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.screens.DetailScreen
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.theme.Uvabucketlisttyb8mrTheme
import edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel.DetailViewModel
import edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel.DetailViewModelFactory

class DetailActivity : ComponentActivity() {

    private val viewModel: DetailViewModel by viewModels {
        val itemId = requireNotNull(intent.getStringExtra(EXTRA_ITEM_ID)) {
            "DetailActivity requires EXTRA_ITEM_ID"
        }
        DetailViewModelFactory(itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Uvabucketlisttyb8mrTheme {
                DetailScreen(
                    name = viewModel.name,
                    dueDate = viewModel.dueDate,
                    completed = viewModel.completed,
                    completedDate = viewModel.completedDate,
                    onNameChange = viewModel::onNameChange,
                    onDueDateChange = viewModel::onDueDateChange,
                    onCompletedToggle = viewModel::onCompletedToggle,
                    canSave = viewModel.isValid(),
                    onSave = {
                        viewModel.save()
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}