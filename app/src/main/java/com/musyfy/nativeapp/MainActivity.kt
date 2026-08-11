package com.musyfy.nativeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.musyfy.nativeapp.core.ui.theme.MusyfyTheme
import com.musyfy.nativeapp.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import javax.inject.Inject
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsTracker
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsMapper

import androidx.navigation.NavHostController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationAnalyticsTracker: NavigationAnalyticsTracker

    private var activeNavController: NavHostController? = null

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        activeNavController?.handleDeepLink(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }
        super.onCreate(savedInstanceState)
        
        // Request notifications permission at runtime on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* no-op */ }
                    .launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
        setContent {
            com.musyfy.nativeapp.feature.appearance.presentation.ui.AppearanceProvider {
                MusyfyTheme {
                    val navController = rememberNavController()
                    activeNavController = navController

                    DisposableEffect(navController) {
                        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                            val screenInfo = NavigationAnalyticsMapper.mapRouteToScreenInfo(destination.route)
                            if (screenInfo != null) {
                                navigationAnalyticsTracker.logScreenView(screenInfo.name, screenInfo.category)
                            }
                        }
                        navController.addOnDestinationChangedListener(listener)
                        onDispose {
                            navController.removeOnDestinationChangedListener(listener)
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

    }
}
