package edu.virginia.cs4720.scores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.virginia.cs4720.scores.ui.ScoresScreen
import edu.virginia.cs4720.scores.ui.ScoresViewModel
import edu.virginia.cs4720.scores.ui.ScoresViewModelFactory
import edu.virginia.cs4720.scores.ui.theme.ScoresTheme

class MainActivity : ComponentActivity() {

    private val vm: ScoresViewModel by viewModels {
        ScoresViewModelFactory((application as ScoresApp).container.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoresTheme {
                ScoresScreen(vm)
            }
        }
    }
}