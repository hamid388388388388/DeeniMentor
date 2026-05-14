package com.deenimentor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deenimentor.notifications.NotificationHelper
import com.deenimentor.ui.analytics.AnalyticsScreen
import com.deenimentor.ui.analytics.AnalyticsViewModel
import com.deenimentor.ui.auth.AuthViewModel
import com.deenimentor.ui.auth.LoginScreen
import com.deenimentor.ui.auth.RegisterScreen
import com.deenimentor.ui.checkin.CheckInScreen
import com.deenimentor.ui.checkin.CheckInViewModel
import com.deenimentor.ui.dua.DuaScreen
import com.deenimentor.ui.dua.DuaViewModel
import com.deenimentor.ui.goals.GoalsScreen
import com.deenimentor.ui.goals.GoalsViewModel
import com.deenimentor.ui.home.HomeScreen
import com.deenimentor.ui.home.HomeViewModel
import com.deenimentor.ui.onboarding.OnboardingScreen
import com.deenimentor.ui.profile.ProfileScreen
import com.deenimentor.ui.quran.QuranScreen
import com.deenimentor.ui.quran.QuranViewModel
import com.deenimentor.ui.settings.SettingsScreen
import com.deenimentor.ui.splash.SplashScreen
import com.deenimentor.ui.theme.BackgroundLight
import com.deenimentor.ui.theme.DarkBackground
import com.deenimentor.ui.theme.DeeniMentorTheme
import com.deenimentor.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()
    private val analyticsViewModel: AnalyticsViewModel by viewModels()
    private val duaViewModel: DuaViewModel by viewModels()
    private val goalsViewModel: GoalsViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.scheduleDailyReminder(this, 20, 0)
            NotificationHelper.scheduleAllNamazNotifications(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        requestNotificationPermission()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            DeeniMentorTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    authViewModel, checkInViewModel, homeViewModel,
                    quranViewModel, analyticsViewModel, duaViewModel, goalsViewModel,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.scheduleDailyReminder(this, 20, 0)
                NotificationHelper.scheduleAllNamazNotifications(this)
            }
        } else {
            NotificationHelper.scheduleDailyReminder(this, 20, 0)
            NotificationHelper.scheduleAllNamazNotifications(this)
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    checkInViewModel: CheckInViewModel,
    homeViewModel: HomeViewModel,
    quranViewModel: QuranViewModel,
    analyticsViewModel: AnalyticsViewModel,
    duaViewModel: DuaViewModel,
    goalsViewModel: GoalsViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (!authViewModel.isLoggedIn) Routes.LOGIN
        else if (authViewModel.hasCompletedOnboarding()) Routes.HOME
        else Routes.ONBOARDING
    }

    if (startDestination == null) {
        val bg = if (isDarkMode) DarkBackground else GreenPrimary
        Box(modifier = Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BackgroundLight)
        }
        return
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(startDestination!!) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    scope.launch {
                        val dest = if (authViewModel.hasCompletedOnboarding()) Routes.HOME else Routes.ONBOARDING
                        navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.REGISTER) { inclusive = true } }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = authViewModel,
                onOnboardingComplete = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                homeViewModel = homeViewModel,
                authViewModel = authViewModel,
                onNavigateToCheckIn = { navController.navigate(Routes.CHECK_IN) },
                onNavigateToQuran = { navController.navigate(Routes.QURAN) },
                onNavigateToAnalytics = { navController.navigate(Routes.ANALYTICS) },
                onNavigateToDua = { navController.navigate(Routes.DUA) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToGoals = { navController.navigate(Routes.GOALS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }

        composable(Routes.CHECK_IN) {
            CheckInScreen(viewModel = checkInViewModel, onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() })
        }

        composable(Routes.QURAN) {
            QuranScreen(viewModel = quranViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen(viewModel = analyticsViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.DUA) {
            DuaScreen(viewModel = duaViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(analyticsViewModel = analyticsViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.GOALS) {
            GoalsScreen(viewModel = goalsViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                },
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange
            )
        }
    }
}
