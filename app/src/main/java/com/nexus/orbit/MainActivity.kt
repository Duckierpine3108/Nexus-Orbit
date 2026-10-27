package com.nexus.orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nexus.orbit.launch.AppLaunchController
import com.nexus.orbit.launcher.InstalledAppRepository
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.ui.NexusHomeScreen
import com.nexus.orbit.ui.theme.NexusOrbitTheme

class MainActivity : ComponentActivity() {

    private lateinit var appRepository: InstalledAppRepository
    private lateinit var settings: LauncherSettings
    private lateinit var launchController: AppLaunchController
    private val launcherState = LauncherState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appRepository = InstalledAppRepository(applicationContext)
        settings = LauncherSettings(applicationContext)
        launchController = AppLaunchController(this)

        launcherState.avatarOffsetY = settings.avatarOffsetY
        launcherState.avatarScale = settings.avatarScale
        launcherState.rightArmX = settings.rightArmX
        launcherState.rightArmY = settings.rightArmY
        launcherState.rightArmZ = settings.rightArmZ
        launcherState.leftArmX = settings.leftArmX
        launcherState.leftArmY = settings.leftArmY
        launcherState.leftArmZ = settings.leftArmZ

        setContent {
            NexusOrbitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BackHandler {
                        if (launcherState.showDiagnostics) {
                            launcherState.showDiagnostics = false
                        } else if (launcherState.showSettings) {
                            launcherState.showSettings = false
                        }
                    }

                    NexusHomeScreen(
                        state = launcherState,
                        settings = settings,
                        repository = appRepository,
                        launchController = launchController
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherState.isLaunching = false
    }

    override fun onDestroy() {
        super.onDestroy()
        appRepository.unregister()
    }
}