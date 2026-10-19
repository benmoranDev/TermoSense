package br.com.termosense.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.service.esp32.ConnectionStatus
import br.com.termosense.service.esp32.ESP32ConnectionState
import java.util.Locale

/**
 * Visual signal level calculation helper from RSSI (dBm)
 */
object RSSIHelper {
    fun calculateBars(rssi: Int, isOnline: Boolean): Int {
        if (!isOnline || rssi == 0) return 0
        return when {
            rssi >= -60 -> 4 // Excellent
            rssi >= -72 -> 3 // Good
            rssi >= -84 -> 2 // Fair
            rssi >= -95 -> 1 // Weak
            else -> 1
        }
    }

    fun calculatePercentage(rssi: Int, isOnline: Boolean): Int {
        if (!isOnline || rssi == 0) return 0
        // dBm typically ranges from -100 (0%) to -50 (100%)
        val clamped = rssi.coerceIn(-100, -50)
        return ((clamped + 100) * 2).coerceIn(0, 100)
    }

    fun getQualityLabel(rssi: Int, isOnline: Boolean): String {
        if (!isOnline) return "Desconectado"
        return when {
            rssi >= -60 -> "Excelente (-$rssi dBm)"
            rssi >= -72 -> "Bom (-$rssi dBm)"
            rssi >= -84 -> "Regular (-$rssi dBm)"
            else -> "Sinal Fraco (-$rssi dBm)"
        }
    }

    fun getQualityDescription(rssi: Int, isOnline: Boolean): String {
        if (!isOnline) return "Sem transmissão de telemetria"
        return when {
            rssi >= -60 -> "Sinal Excelente • Link de alta confiabilidade"
            rssi >= -72 -> "Sinal Estável • Transmissão contínua"
            rssi >= -84 -> "Sinal Moderado • Atenção a barreiras físicas"
            else -> "Sinal Fraco • Risco de latência sanitária"
        }
    }

    @Composable
    fun getSignalColor(rssi: Int, isOnline: Boolean): Color {
        if (!isOnline) return MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        return when {
            rssi >= -60 -> MaterialTheme.colorScheme.tertiary // Emerald Green
            rssi >= -72 -> MaterialTheme.colorScheme.primary // Cyan / Blue
            rssi >= -84 -> MaterialTheme.colorScheme.secondary // Amber
            else -> MaterialTheme.colorScheme.error // Red
        }
    }
}

/**
 * 4-Bar Visual Signal Strength Indicator
 */
@Composable
fun ESP32SignalBars(
    rssi: Int,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    barWidth: Int = 3,
    barSpacing: Int = 2,
    maxHeight: Int = 16
) {
    val activeBars = RSSIHelper.calculateBars(rssi, isOnline)
    val signalColor = RSSIHelper.getSignalColor(rssi, isOnline)
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(barSpacing.dp)
    ) {
        // Bar 1 (Height 35%)
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height((maxHeight * 0.35f).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (activeBars >= 1) signalColor else inactiveColor)
        )
        // Bar 2 (Height 55%)
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height((maxHeight * 0.55f).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (activeBars >= 2) signalColor else inactiveColor)
        )
        // Bar 3 (Height 75%)
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height((maxHeight * 0.75f).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (activeBars >= 3) signalColor else inactiveColor)
        )
        // Bar 4 (Height 100%)
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height(maxHeight.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (activeBars >= 4) signalColor else inactiveColor)
        )
    }
}

/**
 * Live Dashboard Component: ESP32 Multi-Device Connection & RSSI Monitor Card
 */
@Composable
fun ESP32DashboardSignalCard(
    devices: List<DeviceNodeEntity>,
    allUnits: List<HealthUnitEntity>,
    activeConnectionState: ESP32ConnectionState,
    selectedUnitId: Long,
    onSelectDevice: (DeviceNodeEntity) -> Unit,
    onOpenPairing: () -> Unit,
    onScanBle: () -> Unit,
    onAddNewDevice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val isLiveStreaming = activeConnectionState.status == ConnectionStatus.CONNECTED_STREAMING
    val isScanning = activeConnectionState.status == ConnectionStatus.SCANNING

    // Pulse animation for online indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val onlineCount = devices.count { it.isOnline || isLiveStreaming }
    val totalDevices = devices.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("esp32_dashboard_signal_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Title, Online Stats, and Quick Pairing Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (onlineCount > 0) tertiaryColor.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiveStreaming) Icons.Default.BluetoothConnected else Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (onlineCount > 0) tertiaryColor else primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nós ESP32 & Sinal RSSI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = onSurface
                            )
                            // Live pulse tag
                            if (onlineCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(tertiaryColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .alpha(pulseAlpha)
                                                .background(tertiaryColor, CircleShape)
                                        )
                                        Text(
                                            text = "TEMPO REAL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = tertiaryColor
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "$onlineCount de $totalDevices módulos online na unidade",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }
                }

                // Pairing & Scan Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onScanBle,
                        modifier = Modifier
                            .size(34.dp)
                            .background(surfaceVariant, RoundedCornerShape(8.dp)),
                        enabled = !isScanning
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar Sinal",
                            tint = if (isScanning) secondaryColor else primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = onAddNewDevice,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Placa",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = onOpenPairing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Parear",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                }
            }

            // Carousel of Paired ESP32 Modules with Live RSSI Meters
            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onOpenPairing() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Nenhum módulo ESP32 pareado no momento",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                        Text(
                            text = "Toque aqui para escanear via BLE ou conectar por IP Wi-Fi",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    items(devices, key = { it.id }) { device ->
                        val sector = allUnits.find { it.id == device.unitId }
                        val isCurrentSelectedSector = device.unitId == selectedUnitId

                        // Live connection override if this device is the active streaming node
                        val effectiveIsOnline = if (isLiveStreaming && activeConnectionState.connectedAddress?.equals(device.macAddress, ignoreCase = true) == true) {
                            true
                        } else {
                            device.isOnline
                        }

                        val effectiveRssi = if (isLiveStreaming && activeConnectionState.currentRssi != 0) {
                            activeConnectionState.currentRssi
                        } else {
                            device.rssi
                        }

                        ESP32DeviceStatusTile(
                            device = device,
                            sectorName = sector?.name ?: "Setor ${device.unitId}",
                            isSelectedSector = isCurrentSelectedSector,
                            isOnline = effectiveIsOnline,
                            rssi = effectiveRssi,
                            onClick = { onSelectDevice(device) },
                            onOpenPairing = onOpenPairing
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual ESP32 Device Tile with High-Density RSSI Signal Level
 */
@Composable
fun ESP32DeviceStatusTile(
    device: DeviceNodeEntity,
    sectorName: String,
    isSelectedSector: Boolean,
    isOnline: Boolean,
    rssi: Int,
    onClick: () -> Unit,
    onOpenPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val signalColor = RSSIHelper.getSignalColor(rssi, isOnline)
    val signalPercentage = RSSIHelper.calculatePercentage(rssi, isOnline)
    val qualityLabel = RSSIHelper.getQualityLabel(rssi, isOnline)

    val borderStroke = if (isSelectedSector) {
        BorderStroke(1.5.dp, primaryColor)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    Card(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("esp32_tile_${device.id}"),
        shape = RoundedCornerShape(14.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedSector) primaryColor.copy(alpha = 0.08f) else surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Sector Pill & Online/Offline Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelectedSector) primaryColor.copy(alpha = 0.18f) else surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sectorName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        color = if (isSelectedSector) primaryColor else onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Online/Offline Pill with dot
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOnline) tertiaryColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isOnline) tertiaryColor else errorColor, CircleShape)
                    )
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        ),
                        color = if (isOnline) tertiaryColor else errorColor
                    )
                }
            }

            // Device Name & Hardware Model
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${device.model} • ${device.sensorType}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onSurfaceVariant,
                    maxLines = 1
                )
            }

            // RSSI Signal Strength Visual Meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isOnline) signalColor.copy(alpha = 0.1f) else surfaceVariant.copy(alpha = 0.4f)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ESP32SignalBars(
                            rssi = rssi,
                            isOnline = isOnline,
                            barWidth = 3,
                            barSpacing = 2,
                            maxHeight = 14
                        )
                        Column {
                            Text(
                                text = if (isOnline) "$rssi dBm" else "-- dBm",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isOnline) signalColor else onSurfaceVariant
                            )
                            Text(
                                text = if (isOnline) "$signalPercentage% Sinal" else "Sem link",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = onSurfaceVariant
                            )
                        }
                    }

                    // Quality Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(signalColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                !isOnline -> "OFF"
                                rssi >= -60 -> "ÓTIMO"
                                rssi >= -72 -> "BOM"
                                rssi >= -84 -> "REG"
                                else -> "FRACO"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp
                            ),
                            color = signalColor
                        )
                    }
                }
            }

            // Footer: Protocol Info and Voltage / Uptime
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (device.ipAddress.isNotEmpty() && !device.ipAddress.contains(":")) Icons.Default.Wifi else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (device.ipAddress.isNotEmpty() && !device.ipAddress.contains(":")) "Wi-Fi" else "BLE 5.0",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = onSurfaceVariant
                    )
                }

                Text(
                    text = String.format(Locale.US, "VCC %.2fV", device.vccRail),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp
                    ),
                    color = onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact Live RSSI & Hardware Connection Pill for Dashboard Top Bar / Gauge Header
 */
@Composable
fun CompactESP32SignalPill(
    deviceName: String,
    rssi: Int,
    isOnline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val signalColor = RSSIHelper.getSignalColor(rssi, isOnline)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceVariant.copy(alpha = 0.8f))
            .border(
                1.dp,
                if (isOnline) signalColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("compact_esp32_signal_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ESP32SignalBars(
                rssi = rssi,
                isOnline = isOnline,
                barWidth = 2,
                barSpacing = 1,
                maxHeight = 11
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isOnline) tertiaryColor else errorColor, CircleShape)
                )
                Text(
                    text = if (isOnline) "$rssi dBm" else "Offline",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = if (isOnline) onSurface else onSurfaceVariant
                )
            }
        }
    }
}
