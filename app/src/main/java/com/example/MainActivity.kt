package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MedicineViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MedicineViewModel by viewModels()

    // ActivityResultLauncher for requesting dynamic system notification access (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
        } else {
            Log.d("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Proactively ask for POST_NOTIFICATIONS permission on Android 13+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

            MyApplicationTheme(darkTheme = darkModeEnabled) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Splash Screen
                    composable("splash") {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Main Dashboard Container Screen (Contains Home, History, Settings tabs)
                    composable("main") {
                        MainScreen(
                            viewModel = viewModel,
                            onAddMedicineClick = {
                                navController.navigate("add_medicine?id=-1")
                            },
                            onEditMedicineClick = { id ->
                                navController.navigate("add_medicine?id=$id")
                            },
                            onMedicineClick = { id ->
                                navController.navigate("detail/$id")
                            }
                        )
                    }

                    // 3. Add & Edit Medicine Form Screen
                    composable(
                        route = "add_medicine?id={id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: -1
                        AddMedicineScreen(
                            viewModel = viewModel,
                            medicineId = id,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 4. Medicine Details Screen
                    composable(
                        route = "detail/{id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.IntType
                            }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: -1
                        MedicineDetailScreen(
                            viewModel = viewModel,
                            medicineId = id,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onEditClick = { editId ->
                                navController.navigate("add_medicine?id=$editId")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MedicineViewModel,
    onAddMedicineClick: () -> Unit,
    onEditMedicineClick: (Int) -> Unit,
    onMedicineClick: (Int) -> Unit
) {
    var currentTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Today, contentDescription = "Today") },
                    label = { Text("Today") },
                    modifier = Modifier.testTag("tab_today")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        modifier = Modifier.testTag("main_screen_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Account for bottom navigation padding safely
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onAddMedicineClick = onAddMedicineClick,
                    onEditMedicineClick = onEditMedicineClick,
                    onMedicineClick = onMedicineClick
                )
                1 -> HistoryScreen(
                    viewModel = viewModel
                )
                2 -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
