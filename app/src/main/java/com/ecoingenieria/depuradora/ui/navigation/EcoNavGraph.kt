package com.ecoingenieria.depuradora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ecoingenieria.depuradora.AppContainer
import com.ecoingenieria.depuradora.domain.model.LevelStatus
import com.ecoingenieria.depuradora.ui.engineering.EngineeringPanelScreen
import com.ecoingenieria.depuradora.ui.engineering.EngineeringViewModel
import com.ecoingenieria.depuradora.ui.map.RegionMapScreen
import com.ecoingenieria.depuradora.ui.map.RegionMapViewModel
import com.ecoingenieria.depuradora.ui.office.OfficeScreen
import com.ecoingenieria.depuradora.ui.office.OfficeViewModel
import com.ecoingenieria.depuradora.ui.onboarding.OnboardingScreen
import com.ecoingenieria.depuradora.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun EcoNavGraph(container: AppContainer) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.REGION_MAP) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = { alias, avatarKey ->
                navController.navigate(Routes.REGION_MAP) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.REGION_MAP) {
            val vm: RegionMapViewModel = viewModel(
                factory = SimpleViewModelFactory { RegionMapViewModel(container.gameRepository) }
            )
            val state by vm.uiState.collectAsState()
            val scope = rememberCoroutineScopeCompat()

            // Muestra onboarding la primera vez (regla 16: no repetirlo después).
            if (!state.loading && state.profile != null && !state.profile!!.onboardingCompleted) {
                OnboardingScreen(onFinished = { alias, avatarKey ->
                    scope.launch { container.gameRepository.completeOnboarding(alias, avatarKey) }
                })
            } else {
                RegionMapScreen(
                    state = state,
                    onLevelSelected = { level ->
                        if (level.status != LevelStatus.LOCKED) {
                            navController.navigate(Routes.engineeringPanel(level.id))
                        }
                    },
                    onOpenOffice = { navController.navigate(Routes.BEAVER_OFFICE) }
                )
            }
        }

        composable(
            Routes.ENGINEERING_PANEL,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 0
            val vm: EngineeringViewModel = viewModel(
                factory = SimpleViewModelFactory {
                    EngineeringViewModel(
                        repository = container.gameRepository,
                        validateAssembly = container.validatePlantAssemblyUseCase,
                        simulateFlow = container.simulateWaterFlowUseCase,
                        bacteriaScoreUseCase = container.bacteriaLabScoreUseCase,
                        calculateFinalQuality = container.calculateFinalQualityUseCase
                    )
                }
            )
            val loadedLevelId = remember { levelId }
            remember(loadedLevelId) { vm.loadLevel(loadedLevelId); true }
            val state by vm.uiState.collectAsState()

            EngineeringPanelScreen(
                state = state,
                onPlacePiece = vm::placePiece,
                onRemoveLastPiece = vm::removeLastPiece,
                onConfirmAssembly = vm::confirmAssembly,
                onRetryAssembly = vm::retryAssembly,
                onOxygenChange = vm::updateOxygen,
                onSpeedChange = vm::updateSpeed,
                onConfirmValves = vm::confirmValves,
                onTapBacteria = vm::tapBacteria,
                onBacteriaTick = vm::tickBacteriaTimer,
                onContinue = { navController.popBackStack(Routes.REGION_MAP, inclusive = false) }
            )
        }

        composable(Routes.BEAVER_OFFICE) {
            val vm: OfficeViewModel = viewModel(
                factory = SimpleViewModelFactory { OfficeViewModel(container.gameRepository) }
            )
            val state by vm.uiState.collectAsState()
            OfficeScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onToggleSound = vm::toggleSound,
                onToggleHaptics = vm::toggleHaptics
            )
        }
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
