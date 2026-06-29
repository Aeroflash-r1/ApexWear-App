package com.echostream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.echostream.ui.screens.LibraryScreen
import com.echostream.ui.screens.PlayerScreen
import com.echostream.ui.screens.SearchScreen
import com.echostream.ui.theme.EchoStreamTheme
import com.echostream.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("EchoStream", "Notification permission denied - playback may be interrupted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        requestNotificationPermission()

        try {
            val viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
            setContent {
                EchoStreamTheme {
                    val navController = rememberSwipeDismissableNavController()
                    SwipeDismissableNavHost(
                        navController = navController,
                        startDestination = "search"
                    ) {
                        composable("search") {
                            SearchScreen(
                                viewModel = viewModel,
                                onTrackSelected = { navController.navigate("player") },
                                onOpenLibrary = { navController.navigate("library") }
                            )
                        }
                        composable("player") {
                            PlayerScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("library") {
                            LibraryScreen(
                                viewModel = viewModel,
                                onTrackSelected = { navController.navigate("player") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EchoStream", "setContent crashed: ${e.message}", e)
            setContentView(
                android.widget.TextView(this).apply {
                    text = "EchoStream startup error. Check Logcat for details."
                }
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
