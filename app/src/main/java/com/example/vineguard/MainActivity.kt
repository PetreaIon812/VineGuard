package com.example.vineguard

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import com.example.vineguard.CommonFeatures.BottomNavigation
import com.example.vineguard.CommonFeatures.data.RetrieveDiseases
import com.example.vineguard.CommonFeatures.data.RetrieveSensorData
import com.example.vineguard.CommonFeatures.domain.CalculateDiseaseListRisk
import com.example.vineguard.CommonFeatures.domain.GrowthStage
import com.example.vineguard.CommonFeatures.domain.Route
import com.example.vineguard.DiseaseLibraryScreen.presentation.DiseaseLibraryScreen
import com.example.vineguard.DiseaseScreen.presentation.DiseaseScreen
import com.example.vineguard.HomeScreen.presentation.SensorData
import com.example.vineguard.LogInScreen.presentation.GoogleAuthUiClient
import com.example.vineguard.LogInScreen.presentation.LogInScreen
import com.example.vineguard.LogInScreen.presentation.SignInViewModel
import com.example.vineguard.ProfileScreen.presentation.ProfileScreen
import com.example.vineguard.StatsScreen.presentation.StatsScreen
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.VineguardAppTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy{
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                val sensorData = RetrieveSensorData(lookbackDays = 7)
                val diseases = RetrieveDiseases()
                CalculateDiseaseListRisk(sensorData15m = sensorData, diseases = diseases, growthStage = GrowthStage.BERRY_GROWTH)
            }

            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            SideEffect {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            val navController = rememberNavController()
//            val startDestination = if(googleAuthUiClient.getSignedInUser() != null) Route.MainScreen else Route.LogInScreen
            val startDestination = Route.MainScreen
            var isOpened by remember{
                mutableStateOf(true)
            }
            var selectedTabIndex by remember {
                mutableStateOf(0)
            }
            VineguardAppTheme() {
                Scaffold(bottomBar = { BottomNavigation(navController = navController,isOpened = isOpened,selectedTabIndex = selectedTabIndex) }) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable<Route.MainScreen> {
//                            HomeScreen(
//                                modifier = Modifier.padding(padding),
//                                userData = googleAuthUiClient.getSignedInUser()?: UserData(username = "", userID = "", profilePictureUrl = ""),
//                                onSignOut = {
//                                    lifecycleScope.launch {
//                                        googleAuthUiClient.signOut()
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Signed out",
//                                            Toast.LENGTH_LONG
//                                        )
//                                        navController.popBackStack()
//                                    }
//                                },
//                                onGoToDisease = {sensorData ->
//                                    navController.navigate(Route.DiseaseScreen(
//                                        rain = sensorData.rain,
//                                        light = sensorData.light,
//                                        soilMoisture = sensorData.soilMoisture,
//                                        airHumidity = sensorData.airHumidity,
//                                        airTemp = sensorData.airTemp,
//                                        soilTemp = sensorData.soilTemp,
//                                        soilPH = sensorData.soilPH,
//                                        pressure = sensorData.pressure
//                                    ))
//                                }
//                                )
                            com.example.vineguard.NHomeScreen.HomeScreen()
                                isOpened = true
                            selectedTabIndex = 0
//                            StatsScreen()

                        }
                        composable<Route.StatsScreen> {
                            StatsScreen(
                                onGetBack = {
                                    navController.popBackStack()
                                }
                            )
                            isOpened = true
                            selectedTabIndex = 1
                        }
                        composable<Route.DiseaseLibraryScreen> {
                                DiseaseLibraryScreen(
                                    onGetBack = { navController.popBackStack() }
                                )
                            isOpened = true
                            selectedTabIndex = 2
                        }
                        composable<Route.ProfileScreen> {
                            ProfileScreen(
                                onNavigateBack = {navController.popBackStack()}
                            )
                            selectedTabIndex = 3
                        }
                        composable<Route.LogInScreen> {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate(Route.MainScreen)
                                    viewModel.resetState()
                                }
                            }
                            LogInScreen(
                                state = state,
                                onSingInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }

                                }
                            )
                            isOpened = false
                        }
                        composable<Route.DiseaseScreen> { entry ->
                            val args = entry.toRoute<Route.DiseaseScreen>()
                            Surface(modifier = Modifier.fillMaxSize(), color = Primary) {
                                    DiseaseScreen(
                                        sensorData = SensorData(
                                            rain = args.rain,
                                            light = args.light,
                                            soilMoisture = args.soilMoisture,
                                            airHumidity = args.airHumidity,
                                            airTemp = args.airTemp,
                                            soilTemp = args.soilTemp,
                                            soilPH = args.soilPH,
                                            pressure = args.pressure
                                        ),
                                        getToHomeScreen = { navController.popBackStack() },
                                        modifier = Modifier.padding(padding)
                                    )
                                }
                            isOpened = true
                            }
                    }
                }
            }
        }
    }

}
