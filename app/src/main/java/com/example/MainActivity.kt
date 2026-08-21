package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.PaymentViewModel
import com.example.ui.components.FinFamBottomNavBar
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.EmiCalculatorScreen
import com.example.ui.screens.EmiManagerScreen
import com.example.ui.screens.FamilyAndBillsScreen
import com.example.ui.screens.GoalsAndBudgetsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MonthlySpendingTrendsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PaymentPortalScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RealTimeDataTransferScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.FinGuardTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : FragmentActivity(), PaymentResultWithDataListener {

    private lateinit var paymentViewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pvm: PaymentViewModel = viewModel()
            paymentViewModel = pvm
            FinGuardTheme {
                FinFamApp(paymentViewModel = pvm)
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        if (::paymentViewModel.isInitialized) {
            paymentViewModel.onRazorpayPaymentSuccess(razorpayPaymentId, paymentData)
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?, paymentData: PaymentData?) {
        if (::paymentViewModel.isInitialized) {
            paymentViewModel.onRazorpayPaymentError(errorCode, response, paymentData)
        }
    }
}

@Composable
fun FinFamApp(
    viewModel: MainViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    val bottomNavRoutes = setOf("home", "payment_gateway", "pay", "payment", "analytics", "goals", "family", "advisor")
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                FinFamBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (targetRoute != currentRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            }
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("onboarding") {
                OnboardingScreen(
                    onGetStarted = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("analytics") {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("goals") {
                GoalsAndBudgetsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("family") {
                FamilyAndBillsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("advisor") {
                AiAdvisorScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("emi") {
                EmiManagerScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("emi_calculator") {
                EmiCalculatorScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("payment") {
                PaymentScreen(
                    paymentViewModel = paymentViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("payment_gateway") {
                PaymentScreen(
                    paymentViewModel = paymentViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("pay") {
                PaymentScreen(
                    paymentViewModel = paymentViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("trends") {
                MonthlySpendingTrendsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("transfer") {
                RealTimeDataTransferScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("real_time_transfer") {
                RealTimeDataTransferScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
        }
    }
}
