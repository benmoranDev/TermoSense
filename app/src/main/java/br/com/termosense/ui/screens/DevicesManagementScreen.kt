package br.com.termosense.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.service.esp32.ConnectionProtocol
import br.com.termosense.service.esp32.ConnectionStatus
import br.com.termosense.ui.components.ESP32SignalBars
import br.com.termosense.ui.components.RSSIHelper
import br.com.termosense.ui.components.termosenseFieldColors
import br.com.termosense.ui.viewmodel.SerialLogItem
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import java.util.Locale

@Composable
fun DevicesManagementScreen(
    viewModel: TermoSenseViewModel,
    devices: List<DeviceNodeEntity>,
    serialLogs: List<SerialLogItem>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("Todos") }
    var showTerminalDeviceId by remember { mutableStateOf<Long?>(null) }

    val filteredDevices = devices.filter { dev ->
        val matchesQuery = dev.name.contains(searchQuery, ignoreCase = true) ||
                dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
                dev.sensorType.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilterChip) {
            "Online" -> dev.isOnline
            "Offline" -> !dev.isOnline
            else -> true
        }
        matchesQuery && matchesFilter
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    val esp32ConnectionState by viewModel.esp32ConnectionState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("devices_management_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Infrastructure Protocol Status Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Text(
                                text = "HARDWARE & PROTOCOLOS ESP32",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                color = onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor.copy(alpha = 0.15f)
                                    else surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier.size(6.dp).background(
                                        if (esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor else onSurfaceVariant,
                                        CircleShape
                                    )
                                )
                                Text(
                                    text = if (esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) "ESP32 Ativo (${esp32ConnectionState.connectedAddress})" else "Aguardando Pareamento",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor else onSurfaceVariant
                                )
                            }
                        }
                    }

                    // BLE & HTTP rows
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(surfaceColor, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text("GATT Server BLE 5.0", style = MaterialTheme.typography.labelMedium, color = onSurface)
                                    Text("UUID: 4fafc201-1fb5-459e-8fcc-c5c9c331914b", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (esp32ConnectionState.protocol == ConnectionProtocol.BLE_GATT && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor.copy(alpha = 0.15f)
                                        else surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (esp32ConnectionState.protocol == ConnectionProtocol.BLE_GATT && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) "Conectado" else "Disponível",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (esp32ConnectionState.protocol == ConnectionProtocol.BLE_GATT && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor else onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(surfaceColor, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lan, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text("HTTP REST / SoftAP Local", style = MaterialTheme.typography.labelMedium, color = onSurface)
                                    Text("http://192.168.4.1/telemetry", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, fontSize = 10.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (esp32ConnectionState.protocol == ConnectionProtocol.WIFI_HTTP_REST && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor.copy(alpha = 0.15f)
                                        else primaryColor.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (esp32ConnectionState.protocol == ConnectionProtocol.WIFI_HTTP_REST && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) "Transmitindo" else "Porta 80",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (esp32ConnectionState.protocol == ConnectionProtocol.WIFI_HTTP_REST && esp32ConnectionState.status == ConnectionStatus.CONNECTED_STREAMING) tertiaryColor else primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Header & ESP32 Pairing Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Módulos ESP32",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "${devices.size} módulos cadastrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.openPairingDialog() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Parear ESP32", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.openNewDeviceDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Novo", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por IP, MAC, Sala ou Sensor...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = onSurfaceVariant) },
                singleLine = true,
                colors = termosenseFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Online", "Offline").forEach { chip ->
                    val isSelected = selectedFilterChip == chip
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) primaryColor else surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedFilterChip = chip }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$chip (${
                                when (chip) {
                                    "Online" -> devices.count { it.isOnline }
                                    "Offline" -> devices.count { !it.isOnline }
                                    else -> devices.size
                                }
                            })",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Device Cards List
        items(filteredDevices.size) { idx ->
            val dev = filteredDevices[idx]
            val isTerminalOpen = showTerminalDeviceId == dev.id

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(surfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(dev.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = onSurface)
                                    Box(
                                        modifier = Modifier.background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(dev.model, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = primaryColor)
                                    }
                                }
                                Text("Sensor ${dev.sensorType}", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(if (dev.isOnline) tertiaryColor.copy(alpha = 0.15f) else secondaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(if (dev.isOnline) tertiaryColor else secondaryColor, CircleShape))
                                Text(if (dev.isOnline) "Online" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (dev.isOnline) tertiaryColor else secondaryColor)
                            }
                        }
                    }

                    // Reading & Signal Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Temperatura / Umi", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    String.format(Locale.US, "%.1f", dev.currentTemp),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                                Text("°C", style = MaterialTheme.typography.bodySmall, color = primaryColor)
                                Text(
                                    String.format(Locale.US, " / %.0f%%", dev.currentHumidity),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Uptime • Sinal", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                            Text(dev.uptimeString, style = MaterialTheme.typography.labelMedium, color = tertiaryColor)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ESP32SignalBars(rssi = dev.rssi, isOnline = dev.isOnline, maxHeight = 10, barWidth = 2, barSpacing = 1)
                                Text(
                                    if (dev.isOnline) "${dev.rssi} dBm (${RSSIHelper.getQualityLabel(dev.rssi, true)})" else "Offline",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = RSSIHelper.getSignalColor(dev.rssi, dev.isOnline)
                                )
                            }
                        }
                    }

                    // Specs (IP, MAC)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).background(surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Lan, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Text(dev.ipAddress, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, maxLines = 1)
                            }
                        }
                        Box(modifier = Modifier.weight(1f).background(surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Text(dev.macAddress, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }

                    // Sampling rate stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(15.dp))
                            Text("Taxa de Amostragem", style = MaterialTheme.typography.bodySmall, color = onSurface)
                        }

                        Row(
                            modifier = Modifier
                                .background(surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "-",
                                modifier = Modifier
                                    .clickable { viewModel.adjustSamplingRate(dev.id, -250L) }
                                    .padding(horizontal = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = primaryColor
                            )
                            Text(
                                "${dev.samplingRateMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = primaryColor
                            )
                            Text(
                                "+",
                                modifier = Modifier
                                    .clickable { viewModel.adjustSamplingRate(dev.id, 250L) }
                                    .padding(horizontal = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = primaryColor
                            )
                        }
                    }

                    // Action buttons (Reiniciar, Serial, Config)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable { viewModel.showToast("Reiniciando microcontrolador ${dev.name}...") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(15.dp))
                                Text("Reiniciar", style = MaterialTheme.typography.labelSmall, color = onSurface)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isTerminalOpen) primaryColor.copy(alpha = 0.2f) else surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable {
                                    showTerminalDeviceId = if (isTerminalOpen) null else dev.id
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(15.dp))
                                Text("Serial", style = MaterialTheme.typography.labelSmall, color = onSurface)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable { viewModel.openThresholdDialog() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = primaryColor, modifier = Modifier.size(15.dp))
                                Text("Config", style = MaterialTheme.typography.labelSmall, color = onSurface)
                            }
                        }
                    }

                    // Serial Terminal Drawer
                    if (isTerminalOpen) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(5.dp).background(primaryColor, CircleShape))
                                        Text("UART0 • 115200 BAUD LIVE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = primaryColor)
                                    }
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fechar Terminal",
                                        tint = onSurfaceVariant,
                                        modifier = Modifier.size(14.dp).clickable { showTerminalDeviceId = null }
                                    )
                                }

                                serialLogs.takeLast(5).forEach { log ->
                                    Text(
                                        text = "[${log.timestamp}] ${log.message}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (log.type == "PUB") tertiaryColor else onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
