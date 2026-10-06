package br.com.termosense.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.ui.components.FCMPushAlertCard
import br.com.termosense.ui.components.termosenseFieldColors
import br.com.termosense.ui.viewmodel.SerialLogItem
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorSettingsScreen(
    viewModel: TermoSenseViewModel,
    allUnits: List<HealthUnitEntity>,
    selectedUnitId: Long,
    activeAlerts: List<AlertEventEntity>,
    allAlerts: List<AlertEventEntity>,
    rules: List<AutomationRuleEntity>,
    devices: List<DeviceNodeEntity>,
    serialLogs: List<SerialLogItem>,
    modifier: Modifier = Modifier
) {
    var selectedSettingsTab by remember { mutableIntStateOf(0) }
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val activeAlertsCount = activeAlerts.size

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val settingsTabs = listOf(
        SettingsTabItem("Setores", Icons.Default.Tune, 0),
        SettingsTabItem("Alertas", Icons.Default.Notifications, activeAlertsCount),
        SettingsTabItem("Dispositivos", Icons.Default.Memory, devices.size),
        SettingsTabItem("Sistema", Icons.Default.Palette, 0)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("sector_settings_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Central de Configurações",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                        Text(
                            text = "Gestão de Setores • Alertas • Dispositivos ESP32",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }
                }

                when (selectedSettingsTab) {
                    0 -> {
                        Button(
                            onClick = { viewModel.openNewSectorDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Setor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    1 -> {
                        Button(
                            onClick = { viewModel.openNewRuleDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Regra", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    2 -> {
                        Button(
                            onClick = { viewModel.openPairingDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Parear ESP32", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }

            // M3 TabRow
            TabRow(
                selectedTabIndex = selectedSettingsTab,
                containerColor = surfaceColor,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSettingsTab]),
                        color = primaryColor
                    )
                },
                divider = { HorizontalDivider(color = surfaceVariant.copy(alpha = 0.5f)) }
            ) {
                settingsTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedSettingsTab == index,
                        onClick = { selectedSettingsTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (tab.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = if (index == 1) secondaryColor else primaryColor
                                            ) {
                                                Text("${tab.badgeCount}", fontSize = 9.sp)
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSettingsTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }
        }

        // Tab Content with Smooth Slide Animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = selectedSettingsTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> if (targetState > initialState) fullWidth / 4 else -fullWidth / 4 }
                        ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                                slideOutHorizontally(
                                    animationSpec = tween(160, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> if (targetState > initialState) -fullWidth / 4 else fullWidth / 4 }
                                )
                        )
                },
                label = "settingsTabTransition"
            ) { tabIdx ->
                when (tabIdx) {
                    0 -> SectorsTabContent(
                        viewModel = viewModel,
                        allUnits = allUnits,
                        selectedUnitId = selectedUnitId
                    )
                    1 -> AlertsTabContent(
                        viewModel = viewModel,
                        activeAlerts = activeAlerts,
                        allAlerts = allAlerts,
                        rules = rules
                    )
                    2 -> DevicesTabContent(
                        viewModel = viewModel,
                        devices = devices,
                        serialLogs = serialLogs
                    )
                    3 -> SystemTabContent(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

data class SettingsTabItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

// ==========================================
// 1. ABA DE SETORES & CÂMARAS FRIAS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectorsTabContent(
    viewModel: TermoSenseViewModel,
    allUnits: List<HealthUnitEntity>,
    selectedUnitId: Long
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Padrões Sanitários Oficiais (ANVISA / SUS)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text(
                        text = "• Vacinas & Imunobiológicos (PNI): +2,0°C a +8,0°C | Umidade 40% a 65%\n• Medicamentos Termolábeis: +15,0°C a +25,0°C | Umidade 35% a 60%\n• Sangue & Hemoderivados: +2,0°C a +6,0°C | Umidade 45% a 65%",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "SETORIZADORES CADASTRADOS (${allUnits.size}):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = onSurfaceVariant
            )
        }

        items(allUnits, key = { it.id }) { unit ->
            val isSelected = unit.id == selectedUnitId
            var isExpanded by remember { mutableStateOf(isSelected) }

            var minT by remember(unit.minTemp) { mutableFloatStateOf(unit.minTemp.toFloat()) }
            var maxT by remember(unit.maxTemp) { mutableFloatStateOf(unit.maxTemp.toFloat()) }
            var minH by remember(unit.minHumidity) { mutableFloatStateOf(unit.minHumidity.toFloat()) }
            var maxH by remember(unit.maxHumidity) { mutableFloatStateOf(unit.maxHumidity.toFloat()) }
            var coolerRelay by remember(unit.coolerRelayPin) { mutableStateOf(unit.coolerRelayPin) }
            var coolerActive by remember(unit.isCoolerActive) { mutableStateOf(unit.isCoolerActive) }

            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) primaryColor else surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        when (unit.profileType) {
                                            "VACINAS_PNI" -> primaryColor.copy(alpha = 0.15f)
                                            "HEMODERIVADOS" -> secondaryColor.copy(alpha = 0.15f)
                                            else -> tertiaryColor.copy(alpha = 0.15f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (unit.profileType) {
                                        "VACINAS_PNI" -> Icons.Default.MedicalServices
                                        "HEMODERIVADOS" -> Icons.Default.MedicalServices
                                        else -> Icons.Default.Thermostat
                                    },
                                    contentDescription = null,
                                    tint = when (unit.profileType) {
                                        "VACINAS_PNI" -> primaryColor
                                        "HEMODERIVADOS" -> secondaryColor
                                        else -> tertiaryColor
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = onSurface
                                )
                                Text(
                                    text = "${unit.code} • ${unit.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expandir",
                                tint = onSurfaceVariant
                            )
                        }
                    }

                    // Compact Info Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Thermostat, contentDescription = null, tint = primaryColor, modifier = Modifier.size(15.dp))
                                Text("${String.format(Locale.US, "%.1f", unit.minTemp)}°C a ${String.format(Locale.US, "%.1f", unit.maxTemp)}°C", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = onSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(14.dp))
                                Text("${unit.minHumidity.toInt()}% a ${unit.maxHumidity.toInt()}% UR", fontSize = 11.5.sp, color = onSurfaceVariant)
                            }
                        }

                        if (!isSelected) {
                            OutlinedButton(
                                onClick = { viewModel.selectUnit(unit.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Monitorar", fontSize = 10.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(tertiaryColor, CircleShape))
                                Text("Ativo", fontSize = 10.sp, color = tertiaryColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Expanded Editable Sliders and Controls
                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 4.dp)) {
                            HorizontalDivider(color = surfaceVariant.copy(alpha = 0.6f))

                            // Temperature Range Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Faixa de Temperatura Segura", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = onSurface)
                                    Text("${String.format(Locale.US, "%.1f", minT)}°C até ${String.format(Locale.US, "%.1f", maxT)}°C", style = MaterialTheme.typography.labelSmall, color = primaryColor, fontWeight = FontWeight.Bold)
                                }
                                RangeSlider(
                                    value = minT..maxT,
                                    onValueChange = { range ->
                                        minT = Math.round(range.start * 10f) / 10f
                                        maxT = Math.round(range.endInclusive * 10f) / 10f
                                    },
                                    valueRange = -10f..40f,
                                    steps = 99,
                                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                                )
                            }

                            // Humidity Range Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Faixa de Umidade Aceitável (% UR)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = onSurface)
                                    Text("${minH.toInt()}% até ${maxH.toInt()}%", style = MaterialTheme.typography.labelSmall, color = tertiaryColor, fontWeight = FontWeight.Bold)
                                }
                                RangeSlider(
                                    value = minH..maxH,
                                    onValueChange = { range ->
                                        minH = range.start
                                        maxH = range.endInclusive
                                    },
                                    valueRange = 10f..95f,
                                    steps = 84,
                                    colors = SliderDefaults.colors(thumbColor = tertiaryColor, activeTrackColor = tertiaryColor)
                                )
                            }

                            // Relay Configuration Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Relé de Acionamento Cooler", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = onSurface)
                                    Text("Pino de saída digital no ESP32 ($coolerRelay)", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = coolerActive,
                                    onCheckedChange = { coolerActive = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                                )
                            }

                            // Save Unit Changes Button
                            Button(
                                onClick = {
                                    viewModel.updateSectorThresholds(
                                        unitId = unit.id,
                                        minTemp = minT.toDouble(),
                                        maxTemp = maxT.toDouble(),
                                        minHum = minH.toDouble(),
                                        maxHum = maxH.toDouble()
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salvar Tolerâncias Sanitárias", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ABA DE ALERTAS & REGRAS DE AUTOMAÇÃO
// ==========================================
@Composable
private fun AlertsTabContent(
    viewModel: TermoSenseViewModel,
    activeAlerts: List<AlertEventEntity>,
    allAlerts: List<AlertEventEntity>,
    rules: List<AutomationRuleEntity>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val subscribedTopics by viewModel.subscribedTopics.collectAsStateWithLifecycle()
    val currentUnit by viewModel.currentUnit.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // FCM Push Notifications Banner
        item {
            FCMPushAlertCard(
                fcmToken = fcmToken,
                subscribedTopics = subscribedTopics,
                currentUnit = currentUnit,
                onTestPush = { temp, hum, severity ->
                    viewModel.testFcmPushBackgroundAlert(
                        simulatedTemp = temp,
                        simulatedHum = hum,
                        severity = severity
                    )
                },
                onToggleTopic = { topic, isSubscribed ->
                    viewModel.toggleFcmTopic(topic, isSubscribed)
                },
                onRefreshToken = { viewModel.refreshFcmToken() },
                onShowToast = { msg -> viewModel.showToast(msg) }
            )
        }

        // Active Alarms Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(if (activeAlerts.isNotEmpty()) secondaryColor else tertiaryColor, CircleShape))
                    Text(
                        text = "ALARMES ATIVOS (${activeAlerts.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = onSurfaceVariant
                    )
                }
            }
        }

        if (activeAlerts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(28.dp))
                        Column {
                            Text("Nenhum alarme de excursão ativo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = onSurface)
                            Text("Todas as câmaras frias estão operando dentro dos limiares da ANVISA.", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        } else {
            items(activeAlerts, key = { it.id }) { alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(alert.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = secondaryColor)
                            Text(alert.severity, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = secondaryColor)
                        }
                        Text(alert.message, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { viewModel.silenceAlert(alert.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Silenciar", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.resolveAlert(alert.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tertiaryColor),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resolver", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Automation Rules Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGRAS DE AUTOMAÇÃO & RELÉ (${rules.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = onSurfaceVariant
                )
            }
        }

        items(rules, key = { it.id }) { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(rule.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = onSurface)
                        Switch(
                            checked = rule.isEnabled,
                            onCheckedChange = { viewModel.toggleRule(rule) },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                        )
                    }
                    Text(rule.conditionText, style = MaterialTheme.typography.bodySmall, color = primaryColor, fontFamily = FontFamily.Monospace, fontSize = 11.5.sp)
                    Text(rule.actionText, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, fontSize = 11.5.sp)
                }
            }
        }
    }
}

// ==========================================
// 3. ABA DE DISPOSITIVOS & ESP32
// ==========================================
@Composable
private fun DevicesTabContent(
    viewModel: TermoSenseViewModel,
    devices: List<DeviceNodeEntity>,
    serialLogs: List<SerialLogItem>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    var searchQuery by remember { mutableStateOf("") }
    val filteredDevices = devices.filter { dev ->
        dev.name.contains(searchQuery, ignoreCase = true) ||
        dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
        dev.sensorType.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Módulos ESP32 Pareados", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = onSurface)
                            Text("${devices.size} nós configurados na rede", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = { viewModel.openPairingDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hub BLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nome, IP ou sensor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = onSurfaceVariant) },
                colors = termosenseFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Device Items List
        items(filteredDevices, key = { it.id }) { device ->
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(if (device.isOnline) tertiaryColor else MaterialTheme.colorScheme.error, CircleShape))
                            Text(device.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = onSurface)
                        }
                        Text(device.sensorType, fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("IP: ${device.ipAddress}", fontSize = 11.sp, color = onSurfaceVariant, fontFamily = FontFamily.Monospace)
                        Text("Taxa: ${device.samplingRateMs / 1000}s", fontSize = 11.sp, color = onSurfaceVariant)
                        Text("Uptime: ${device.uptimeString}", fontSize = 11.sp, color = onSurfaceVariant)
                    }
                }
            }
        }

        // Serial Terminal Snippet
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Text("Monitor Serial ESP32 (Logs em Tempo Real)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    HorizontalDivider(color = Color.DarkGray)
                    serialLogs.takeLast(6).forEach { log ->
                        Text(
                            text = "[${log.timestamp}] ${log.message}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = when (log.type) {
                                "PUB" -> Color(0xFF38BDF8)
                                "WARN" -> Color(0xFFFBBF24)
                                else -> Color(0xFF94A3B8)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. ABA DE SISTEMA & APARÊNCIA
// ==========================================
@Composable
private fun SystemTabContent(
    viewModel: TermoSenseViewModel,
    isDarkMode: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HospitalThemeSettingsCard(
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.toggleTheme() },
                onSelectDarkMode = { if (!isDarkMode) viewModel.toggleTheme() },
                onSelectLightMode = { if (isDarkMode) viewModel.toggleTheme() }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(20.dp))
                        Text("Validação Sanitária & Protocolo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = onSurface)
                    }
                    Text(
                        text = "TermoSense v2.4.0 — Qualificado para atendimento das diretrizes de Boas Práticas de Armazenagem e Transporte de Medicamentos (ANVISA RDC nº 430/2020) e Manual de Rede de Frio do Programa Nacional de Imunizações (PNI/MS).",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HospitalThemeSettingsCard(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onSelectDarkMode: () -> Unit,
    onSelectLightMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null, tint = primaryColor)
                    Column {
                        Text("Aparência & Ambiente Clínico", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = onSurface)
                        Text(if (isDarkMode) "Modo Telemetria Escura (Alto Contraste)" else "Modo Clínico Claro (Luz do Dia)", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                    }
                }
                Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() }, colors = SwitchDefaults.colors(checkedThumbColor = primaryColor))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) primaryColor.copy(alpha = 0.15f) else surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(width = if (isDarkMode) 1.5.dp else 1.dp, color = if (isDarkMode) primaryColor else Color.Transparent),
                    modifier = Modifier.weight(1f).clickable { onSelectDarkMode() }
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = if (isDarkMode) primaryColor else onSurfaceVariant, modifier = Modifier.size(22.dp))
                        Text("Modo Escuro", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (isDarkMode) primaryColor else onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isDarkMode) primaryColor.copy(alpha = 0.15f) else surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(width = if (!isDarkMode) 1.5.dp else 1.dp, color = if (!isDarkMode) primaryColor else Color.Transparent),
                    modifier = Modifier.weight(1f).clickable { onSelectLightMode() }
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LightMode, contentDescription = null, tint = if (!isDarkMode) primaryColor else onSurfaceVariant, modifier = Modifier.size(22.dp))
                        Text("Modo Claro", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (!isDarkMode) primaryColor else onSurfaceVariant)
                    }
                }
            }
        }
    }
}
