package edu.virginia.cs4720.stepcounter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import edu.virginia.cs4720.stepcounter.data.AppDatabase
import edu.virginia.cs4720.stepcounter.data.StepRepository
import edu.virginia.cs4720.stepcounter.sensors.SensorController
import edu.virginia.cs4720.stepcounter.ui.PermissionState
import edu.virginia.cs4720.stepcounter.ui.StepCounterApp
import edu.virginia.cs4720.stepcounter.ui.theme.StepCounterTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private lateinit var repo: StepRepository
    private lateinit var sensors: SensorController
    private val permission = MutableStateFlow(PermissionState.NOT_ASKED)

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            repo.permissionAsked = true
            val granted = refreshPermission()
            // Normally onResume follows the dialog and restarts sensors; this covers the
            // case where the result arrives while we're already resumed.
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) sensors.start(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repo = StepRepository(
            AppDatabase.get(this).stepDao(),
            getSharedPreferences("step_prefs", MODE_PRIVATE),
        )
        sensors = SensorController(this, lifecycleScope, repo)

        setContent {
            StepCounterTheme {
                val permissionState by permission.collectAsState()
                StepCounterApp(
                    sensors = sensors,
                    repo = repo,
                    permissionState = permissionState,
                    onRequestPermission = ::askForPermission,
                    onOpenSettings = ::openAppSettings,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check every time: the user may have changed the permission in Settings.
        sensors.start(countSteps = refreshPermission())
    }

    override fun onPause() {
        // Stop listening as soon as the user leaves; a running listener drains the battery.
        sensors.stop()
        super.onPause()
    }

    /** Updates [permission] and returns whether step counting is allowed. */
    private fun refreshPermission(): Boolean {
        val granted = hasActivityRecognition()
        permission.value = when {
            granted -> PermissionState.GRANTED
            repo.permissionAsked -> PermissionState.DENIED
            else -> PermissionState.NOT_ASKED
        }
        if (!granted) repo.clearHardwareBaseline()
        return granted
    }

    private fun hasActivityRecognition(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
        )
    }
}
