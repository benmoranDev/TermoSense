package br.com.termosense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.termosense.ui.components.ESP32PairingHubDialog
import br.com.termosense.ui.components.ExportReportDialog
import br.com.termosense.ui.components.NewDeviceDialog
import br.com.termosense.ui.components.NewRuleDialog
import br.com.termosense.ui.components.NewSectorDialog
import br.com.termosense.ui.components.ThresholdConfigDialog
import br.com.termosense.ui.navigation.Screen
import br.com.termosense.ui.screens.ComplianceReportScreen
import br.com.termosense.ui.screens.LiveMonitoringScreen
import br.com.termosense.ui.screens.SectorSettingsScreen
import br.com.termosense.ui.screens.SplashScreen
import br.com.termosense.ui.screens.TelemetryGraphScreen
import br.com.termosense.ui.theme.MyApplicationTheme
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TermoSenseViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppHost(viewModel = viewModel, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun MainAppHost(
    viewModel: TermoSenseViewModel = viewModel(),
    isDarkMode: Boolean = true
) {
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.AoVivo) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* Handled */ }
    )

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
    } else {
        val currentUnit by viewModel.currentUnit.collectAsStateWithLifecycle()
        val allUnits by viewModel.allUnits.collectAsStateWithLifecycle()
        val recentReadings by viewModel.recentReadings.collectAsStateWithLifecycle()
        val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
        val activeAlertsCount by viewModel.activeAlertsCount.collectAsStateWithLifecycle()
        val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
        val allRules by viewModel.allRules.collectAsStateWithLifecycle()
        val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
        val allReports by viewModel.allReports.collectAsStateWithLifecycle()
        val serialLogs by viewModel.serialLogs.collectAsStateWithLifecycle()
        val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
        val selectedMetric by viewModel.selectedMetric.collectAsStateWithLifecycle()

        val showPairingDialog by viewModel.showPairingDialog.collectAsStateWithLifecycle()
        val showThresholdDialog by viewModel.showThresholdDialog.collectAsStateWithLifecycle()
        val showNewDeviceDialog by viewModel.showNewDeviceDialog.collectAsStateWithLifecycle()
        val showNewRuleDialog by viewModel.showNewRuleDialog.collectAsStateWithLifecycle()
        val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
        val showNewSectorDialog by viewModel.showNewSectorDialog.collectAsStateWithLifecycle()
        val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

        val navItems = listOf(
            Screen.AoVivo,
            Screen.Graficos,
            Screen.Relatorios,
            Screen.Configuracoes
        )

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .shadow(16.dp)
                            .testTag("bottom_navigation_bar")
                    ) {
                        navItems.forEach { screen ->
                            val selected = currentScreen == screen
                            val isConfig = screen == Screen.Configuracoes

                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    if (isConfig && activeAlertsCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "$activeAlertsCount",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                // Native Back Button Handler (pops back to Live Monitoring if on secondary screen)
                if (currentScreen != Screen.AoVivo) {
                    BackHandler {
                        currentScreen = Screen.AoVivo
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                                slideInHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> if (targetState.index > initialState.index) fullWidth / 4 else -fullWidth / 4 }
                                ))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(
                                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                                            targetOffsetX = { fullWidth -> if (targetState.index > initialState.index) -fullWidth / 4 else fullWidth / 4 }
                                        )
                                )
                        },
                        label = "screenTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            Screen.AoVivo -> LiveMonitoringScreen(
                                viewModel = viewModel,
                                unit = currentUnit,
                                allUnits = allUnits,
                                readings = recentReadings
                            )
                            Screen.Graficos -> TelemetryGraphScreen(
                                viewModel = viewModel,
                                unit = currentUnit,
                                readings = recentReadings,
                                selectedTimeframe = selectedTimeframe,
                                selectedMetric = selectedMetric
                            )
                            Screen.Relatorios -> ComplianceReportScreen(
                                viewModel = viewModel,
                                unit = currentUnit,
                                reports = allReports
                            )
                            Screen.Configuracoes -> SectorSettingsScreen(
                                viewModel = viewModel,
                                allUnits = allUnits,
                                selectedUnitId = currentUnit?.id ?: 1L,
                                activeAlerts = activeAlerts,
                                allAlerts = allAlerts,
                                rules = allRules,
                                devices = allDevices,
                                serialLogs = serialLogs
                            )
                        }
                    }
                }
            }

            // Global Floating Toast
            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    delay(3000)
                    viewModel.clearToast()
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
                        .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .shadow(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }

            // Dialogs
            if (showThresholdDialog) {
                ThresholdConfigDialog(
                    initialMinTemp = currentUnit?.minTemp ?: 2.0,
                    initialMaxTemp = currentUnit?.maxTemp ?: 8.0,
                    initialMinHum = currentUnit?.minHumidity ?: 35.0,
                    initialMaxHum = currentUnit?.maxHumidity ?: 65.0,
                    onDismiss = { viewModel.closeThresholdDialog() },
                    onSave = { minT, maxT, minH, maxH ->
                        viewModel.updateThresholds(minT, maxT, minH, maxH)
                    }
                )
            }

            if (showNewDeviceDialog) {
                NewDeviceDialog(
                    onDismiss = { viewModel.closeNewDeviceDialog() },
                    onSave = { name, model, sensor, ip, mac, rate ->
                        viewModel.addNewDevice(name, model, sensor, ip, mac, rate)
                    }
                )
            }

            if (showNewRuleDialog) {
                NewRuleDialog(
                    onDismiss = { viewModel.closeNewRuleDialog() },
                    onSave = { name, cond, act, target ->
                        viewModel.addNewRule(name, cond, act, target)
                    }
                )
            }

            if (showExportDialog) {
                ExportReportDialog(
                    unitName = currentUnit?.name ?: "UBS Central",
                    onDismiss = { viewModel.closeExportDialog() },
                    onExport = { isPdf, tech, reg, month, obs ->
                        viewModel.generateAndShareReport(isPdf, tech, reg, month, obs)
                    }
                )
            }

            if (showNewSectorDialog) {
                NewSectorDialog(
                    onDismiss = { viewModel.closeNewSectorDialog() },
                    onSave = { name, code, profile, location, minT, maxT, minH, maxH, pin ->
                        viewModel.addNewSector(name, code, profile, location, minT, maxT, minH, maxH, pin)
                    }
                )
            }

            if (showPairingDialog) {
                ESP32PairingHubDialog(
                    viewModel = viewModel,
                    allUnits = allUnits,
                    onDismiss = { viewModel.closePairingDialog() }
                )
            }
        }
    }
}
