package com.shopwise.ui.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopwise.ui.MainViewModel
import com.shopwise.ui.screen.home.DashboardScreen
import com.shopwise.ui.screen.onboarding.OnBoardingScreen
import com.shopwise.ui.screen.scan.AnalysisResultScreen
import com.shopwise.ui.screen.scan.CameraScreen
import com.shopwise.ui.screen.scan.ScanHistoryScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val ALLERGY_PROFILE = "allergy_profile"
    const val MODEL_SETUP = "model_setup"
    const val CHAT = "chat"
    const val CAMERA = "camera"
    const val ANALYSIS_RESULT = "analysis_result"
    const val SCAN_HISTORY = "scan_history"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Inisialisasi ViewModel langsung di dalam compose view
    val viewModel: MainViewModel = viewModel()
    
    // Gunakan remember agar nilai startDestination tidak berubah-ubah saat recomposition
    val startDestination = remember { viewModel.getStartDestination() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {

        composable(Routes.ONBOARDING) {
            OnBoardingScreen(navController)
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(navController)
        }

        composable(Routes.CAMERA) {
            CameraScreen(navController)
        }

        composable(
            route = "${Routes.ANALYSIS_RESULT}/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val imageUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
            AnalysisResultScreen(navController, imageUri)
        }

        composable(Routes.SCAN_HISTORY) {
            ScanHistoryScreen(navController)
        }

        composable(Routes.ALLERGY_PROFILE) {
//            AllergyProfileScreen()
        }

        composable(Routes.MODEL_SETUP) {
//            ModelSetupScreen()
        }

        composable(Routes.CHAT) {
//            ChatScreen()
        }
    }
}