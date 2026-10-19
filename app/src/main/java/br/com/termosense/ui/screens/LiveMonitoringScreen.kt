package br.com.termosense.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.termosense.R
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import br.com.termosense.service.esp32.ConnectionStatus
import br.com.termosense.ui.components.CompactESP32SignalPill
import br.com.termosense.ui.components.ESP32DashboardSignalCard
import br.com.termosense.ui.components.HeroGaugeCard
import br.com.termosense.ui.components.OfflineCacheSyncCard
import br.com.termosense.ui.components.RechartsTrendChartCard
import br.com.termosense.ui.components.SectorsHealthSummary
import br.com.termosense.ui.components.TacticalMetricsGrid
import br.com.termosense.ui.viewmodel.TermoSenseViewModel

@Composable
fun LiveMonitoringScreen(
    viewModel: TermoSenseViewModel,
    unit: HealthUnitEntity?,
    allUnits: List<HealthUnitEntity>,
    readings: List<TelemetryReadingEntity>,
    modifier: Modifier = Modifier
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val currentT = readings.lastOrNull()?.temperature ?: 0.0
    val currentH = readings.lastOrNull()?.humidity ?: 0.0
    val currentDew = readings.lastOrNull()?.dewPoint ?: 0.0
    val currentVcc = readings.lastOrNull()?.vcc ?: 0.0
    val currentRssi = readings.lastOrNull()?.rssi ?: 0

    val isNominal = unit?.let { currentT in it.minTemp..it.maxTemp } ?: true

    val infiniteTransition = rememberInfiniteTransition(label = "fanSpin")
    val fanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fanSpinRot"
    )

    val esp32ConnectionState by viewModel.esp32ConnectionState.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val unsyncedCacheCount by viewModel.unsyncedCacheCount.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val isConnected = esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING
    val activeRssi = if (esp32ConnectionState.currentRssi != 0) esp32ConnectionState.currentRssi else currentRssi

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("live_monitoring_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Top Bar with Real-Time ESP32 Signal Pill, Theme Switcher & Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TermoSense",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ESP32",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Compact Signal Pill in top bar
                        CompactESP32SignalPill(
                            deviceName = unit?.name ?: "ESP32",
                            rssi = activeRssi,
                            isOnline = isConnected || allDevices.any { it.isOnline },
                            onClick = { viewModel.openPairingDialog() }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ESP32 Pairing Button
                    IconButton(
                        onClick = { viewModel.openPairingDialog() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isConnected)
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("open_esp32_pairing_btn")
                    ) {
                        Icon(
                            imageVector = if (isConnected)
                                Icons.Default.BluetoothConnected
                            else Icons.Default.Bluetooth,
                            contentDescription = "Parear ESP32",
                            tint = if (isConnected)
                                MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Theme Toggle Button (Light / Dark)
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = if (isDarkMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openPairingDialog() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Hardware ESP32",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Real-Time ESP32 Multi-Device Connection & RSSI Signal Monitor Card
        item {
            ESP32DashboardSignalCard(
                devices = allDevices,
                allUnits = allUnits,
                activeConnectionState = esp32ConnectionState,
                selectedUnitId = unit?.id ?: 1L,
                onSelectDevice = { device ->
                    viewModel.selectUnit(device.unitId)
                },
                onOpenPairing = { viewModel.openPairingDialog() },
                onScanBle = { viewModel.startBleScan() },
                onAddNewDevice = { viewModel.openNewDeviceDialog() }
            )
        }

        // Room Offline Cache & Real-Time Sync Resilience Card
        item {
            OfflineCacheSyncCard(
                unsyncedCount = unsyncedCacheCount,
                syncState = syncState,
                esp32State = esp32ConnectionState,
                onSyncNow = { viewModel.syncOfflineCache(isAuto = false) },
                onOpenCacheDetails = { viewModel.openPairingDialog() }
            )
        }

        // Sectors Health Summary Strip (Cards with live OK/Alert status and 1-tap navigation)
        item {
            SectorsHealthSummary(
                allUnits = allUnits,
                selectedUnitId = unit?.id ?: 1L,
                currentReadings = readings,
                onSelectSector = { sectorId -> viewModel.selectUnit(sectorId) },
                onAddNewSector = { viewModel.openNewSectorDialog() }
            )
        }

        // Health Unit Selector Dropdown Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = unit?.code ?: "UBS-01",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = unit?.profileType ?: "VACINAS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = unit?.name ?: "Sala de Vacinas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { unitMenuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = "Selecionar Unidade",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                allUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(u.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${u.code} • ${u.minTemp}°C a ${u.maxTemp}°C",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectUnit(u.id)
                                            unitMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Protocol Sub-pill & Packet Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                            Text(
                                text = "MQTT / WS ATIVO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                Text("Pacote: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("há 1s", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Offline buffer mode toggle
                            Box(
                                modifier = Modifier
                                    .background(if (unit?.isOfflineMode == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.toggleOfflineMode() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (unit?.isOfflineMode == true) "OFFLINE BUFFER" else "ONLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (unit?.isOfflineMode == true) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Temperature Arc Gauge
        item {
            HeroGaugeCard(
                currentTemp = currentT,
                minTarget = unit?.minTemp ?: 2.0,
                maxTarget = unit?.maxTemp ?: 8.0,
                sensorLabel = "SENSOR DS18B20 #1 • ${unit?.code ?: "UBS"}",
                delta24h = if (isNominal) "+0.2°C" else "+1.8°C (Excursão)",
                isNominal = isNominal
            )
        }

        // 4 Tactical Data Cards
        item {
            TacticalMetricsGrid(
                humidity = currentH,
                dewPoint = currentDew,
                vcc = currentVcc,
                rssi = currentRssi
            )
        }

        // Recharts-Style Dual Trend Chart (Temperature + Humidity + ANVISA Reference Area)
        item {
            RechartsTrendChartCard(
                readings = readings,
                minTempTarget = unit?.minTemp ?: 2.0,
                maxTempTarget = unit?.maxTemp ?: 8.0,
                minHumTarget = unit?.minHumidity ?: 35.0,
                maxHumTarget = unit?.maxHumidity ?: 65.0,
                showHumiditySeries = true,
                showTemperatureSeries = true,
                title = "Histórico & Tendência (Dual Stream)"
            )
        }

        // Quick Control Actions Bento
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comandos Rápidos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "GPIO Relays • Atuador",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Toggle Relay Cooler
                    val coolerActive = unit?.isCoolerActive ?: true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (coolerActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = if (coolerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .then(if (coolerActive) Modifier.rotate(fanRotation) else Modifier)
                                )
                            }
                            Column {
                                Text(
                                    text = "Cooler / Compressor (${unit?.coolerRelayPin ?: "GPIO 26"})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (coolerActive) "LIGADO • Automático" else "DESLIGADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (coolerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = coolerActive,
                            onCheckedChange = { viewModel.toggleCooler() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // Secondary Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable { viewModel.openPairingDialog() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Parear ESP32", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable { viewModel.openThresholdDialog() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Exposure, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Ajustar Limiares", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Hardware Diagnostics Footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "METADADOS DE DIAGNÓSTICO ESP32",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiagnosticBox("IP LOCAL", "192.168.1.145", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                        DiagnosticBox("UPTIME", "18d 04h 12m", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                        DiagnosticBox("FIRMWARE", "v2.4.1-ota", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticBox(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
                maxLines = 1
            )
        }
    }
}
