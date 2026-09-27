package edu.virginia.cs4720.uvabucketlist_tyb8mr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.screens.CreateScreen
import edu.virginia.cs4720.uvabucketlist_tyb8mr.ui.theme.Uvabucketlisttyb8mrTheme
import edu.virginia.cs4720.uvabucketlist_tyb8mr.viewmodel.CreateViewModel

class CreateActivity : ComponentActivity() {

    private val viewModel: CreateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Uvabucketlisttyb8mrTheme {
                CreateScreen(
                    name = viewModel.name,
                    dueDate = viewModel.dueDate,
                    onNameChange = viewModel::onNameChange,
                    onDueDateChange = viewModel::onDueDateChange,
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