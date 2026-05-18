package com.intentos.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.intentos.app.presentation.home.HomeScreen
import com.intentos.app.presentation.home.HomeViewModel
import com.intentos.app.presentation.settings.SettingsScreen

@Composable
fun IntentOsNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: HomeViewModel = hiltViewModel()
            val isIncognitoEnabled = viewModel.isIncognitoEnabled.collectAsState(initial = false).value
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                isIncognitoEnabled = isIncognitoEnabled,
                onToggleIncognito = { viewModel.toggleIncognito(it) },
                onPurgeMemory = {
                    viewModel.purgeMemory()
                    navController.popBackStack()
                }
            )
        }
    }
}
