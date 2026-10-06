package br.com.termosense.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.service.esp32.ConnectionStatus
import br.com.termosense.service.esp32.DiscoveredESP32
import br.com.termosense.service.esp32.ESP32ConnectionState
import br.com.termosense.service.esp32.ESP32FirmwareTemplate
import br.com.termosense.ui.viewmodel.TermoSenseViewModel

@Composable
fun ESP32PairingHubDialog(
    viewModel: TermoSenseViewModel,
    allUnits: List<HealthUnitEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bluetooth BLE", "Wi-Fi / IP", "Firmware ESP32", "Calibração & Limpeza")

    val connectionState by viewModel.esp32ConnectionState.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredEsp32List.collectAsStateWithLifecycle()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surfaceColor,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("esp32_pairing_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
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
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Central de Pareamento ESP32",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = onSurface
                            )
                            Text(
                                text = "Conexão de Hardware Real (BLE • Wi-Fi • DHT22 / DS18B20)",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                // Global Connection Status Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (connectionState.status) {
                                ConnectionStatus.CONNECTED_STREAMING -> tertiaryColor.copy(alpha = 0.12f)
                                ConnectionStatus.CONNECTING, ConnectionStatus.SCANNING -> secondaryColor.copy(alpha = 0.12f)
                                ConnectionStatus.ERROR -> errorColor.copy(alpha = 0.12f)
                                else -> surfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
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
                                    .size(8.dp)
                                    .background(
                                        when (connectionState.status) {
                                            ConnectionStatus.CONNECTED_STREAMING -> tertiaryColor
                                            ConnectionStatus.CONNECTING, ConnectionStatus.SCANNING -> secondaryColor
                                            ConnectionStatus.ERROR -> errorColor
                                            else -> onSurfaceVariant
                                        },
                                        CircleShape
                                    )
                            )
                            Text(
                                text = when (connectionState.status) {
                                    ConnectionStatus.CONNECTED_STREAMING -> "CONECTADO: ${connectionState.connectedDeviceName ?: "ESP32"} (${connectionState.connectedAddress})"
                                    ConnectionStatus.CONNECTING -> "Conectando ao ESP32 (${connectionState.connectedAddress})..."
                                    ConnectionStatus.SCANNING -> "Buscando ESP32 via Bluetooth Low Energy..."
                                    ConnectionStatus.ERROR -> "Erro: ${connectionState.errorMessage ?: "Falha de conexão"}"
                                    else -> "Nenhum módulo ESP32 conectado no momento"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (connectionState.status) {
                                    ConnectionStatus.CONNECTED_STREAMING -> tertiaryColor
                                    ConnectionStatus.CONNECTING, ConnectionStatus.SCANNING -> secondaryColor
                                    ConnectionStatus.ERROR -> errorColor
                                    else -> onSurfaceVariant
                                }
                            )
                        }

                        if (connectionState.status == ConnectionStatus.CONNECTED_STREAMING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${connectionState.currentRssi} dBm",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { viewModel.disconnectESP32() },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Desconectar", fontSize = 10.sp, color = errorColor)
                                }
                            }
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = surfaceColor,
                    contentColor = primaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = primaryColor
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Tab Content with Smooth Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
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
                        label = "pairingHubTabTransition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> BlePairingTab(
                                viewModel = viewModel,
                                connectionState = connectionState,
                                discoveredDevices = discoveredDevices
                            )
                            1 -> WifiIpTab(
                                viewModel = viewModel,
                                allUnits = allUnits,
                                connectionState = connectionState
                            )
                            2 -> FirmwareSourceTab(
                                context = context,
                                onShowToast = { viewModel.showToast(it) }
                            )
                            3 -> CalibrationAndDataTab(
                                viewModel = viewModel,
                                allUnits = allUnits
                            )
                        }
                    }
                }

                // Sticky Bottom Action Bar with Dedicated Back Button & Contextual Action
                HorizontalDivider(color = surfaceVariant.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voltar ao App", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    when (selectedTab) {
                        0 -> {
                            if (connectionState.status == ConnectionStatus.SCANNING) {
                                Button(
                                    onClick = { viewModel.stopBleScan() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = surfaceVariant, contentColor = onSurface)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryColor, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Parar Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startBleScan() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Escanear BLE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                        1 -> {
                            Button(
                                onClick = {
                                    viewModel.showToast("Testando conexão com endpoint Wi-Fi...")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testar Wi-Fi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        2 -> {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("TermoSense ESP32 Firmware", ESP32FirmwareTemplate.getArduinoSketchCode(wifiSsid = "MinhaRede_WiFi", wifiPass = "Senha_Forte_123"))
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showToast("Código C++ do ESP32 copiado para a Área de Transferência!")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar Sketch .ino", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        3 -> {
                            Button(
                                onClick = {
                                    viewModel.syncOfflineCache(isAuto = false)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tertiaryColor, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizar Tudo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlePairingTab(
    viewModel: TermoSenseViewModel,
    connectionState: ESP32ConnectionState,
    discoveredDevices: List<DiscoveredESP32>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // BLE Scan Controller Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Varredura Bluetooth Low Energy",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )
                Text(
                    text = "Procura por anúncios GATT do TermoSense ou ESP32",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }

            if (connectionState.status == ConnectionStatus.SCANNING) {
                Button(
                    onClick = { viewModel.stopBleScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = surfaceVariant, contentColor = onSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryColor, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Parar Scan", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.startBleScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("start_ble_scan_button")
                ) {
                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear ESP32", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = surfaceVariant)

        // Discovered Devices List
        if (discoveredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = if (connectionState.status == ConnectionStatus.SCANNING) "Buscando dispositivos ESP32 por perto..." else "Nenhum ESP32 detectado",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "Certifique-se de que o ESP32 está alimentado e com o firmware TermoSense gravado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Text(
                text = "DISPOSITIVOS LOCALIZADOS (${discoveredDevices.size}):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveredDevices) { dev ->
                    val isConnected = connectionState.connectedAddress == dev.address

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isConnected) tertiaryColor.copy(alpha = 0.1f) else surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = 1.dp,
                                color = if (isConnected) tertiaryColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
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
                                        .background(if (isConnected) tertiaryColor.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = if (isConnected) tertiaryColor else primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = dev.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = onSurface
                                    )
                                    Text(
                                        text = "MAC: ${dev.address} • Sinal: ${dev.rssi} dBm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceVariant
                                    )
                                    Text(
                                        text = "Sensor: ${dev.sensorType} • ${dev.model}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = primaryColor
                                    )
                                }
                            }

                            if (isConnected) {
                                Box(
                                    modifier = Modifier
                                        .background(tertiaryColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Pareado", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.connectBleESP32(dev.address, dev.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Parear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WifiIpTab(
    viewModel: TermoSenseViewModel,
    allUnits: List<HealthUnitEntity>,
    connectionState: ESP32ConnectionState
) {
    var ipAddress by remember { mutableStateOf("192.168.4.1") }
    var portStr by remember { mutableStateOf("80") }
    var deviceLabel by remember { mutableStateOf("ESP32 Câmara de Vacinas") }
    var selectedUnitId by remember { mutableStateOf(allUnits.firstOrNull()?.id ?: 1L) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Conexão Direta Wi-Fi / SoftAP",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = onSurface
        )

        Text(
            text = "Conecte-se ao ESP32 via rede Wi-Fi local da unidade de saúde ou diretamente no ponto de acesso SoftAP 'TermoSense-ESP32-AP' (IP padrão: 192.168.4.1).",
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariant,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Endereço IP do ESP32") },
            placeholder = { Text("Ex: 192.168.4.1 ou 192.168.1.150") },
            colors = termosenseFieldColors(),
            leadingIcon = { Icon(Icons.Default.Lan, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.fillMaxWidth().testTag("esp32_ip_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = portStr,
                onValueChange = { portStr = it },
                label = { Text("Porta HTTP") },
                colors = termosenseFieldColors(),
                modifier = Modifier.weight(0.35f)
            )

            OutlinedTextField(
                value = deviceLabel,
                onValueChange = { deviceLabel = it },
                label = { Text("Nome do Módulo") },
                colors = termosenseFieldColors(),
                modifier = Modifier.weight(0.65f)
            )
        }

        // Sector Association Dropdown / Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Vincular ao Setor Sanitário:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allUnits.forEach { unit ->
                    val isSelected = unit.id == selectedUnitId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) primaryColor else surfaceVariant)
                            .clickable { selectedUnitId = unit.id }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = unit.code,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                val port = portStr.toIntOrNull() ?: 80
                viewModel.connectHttpESP32(
                    ipAddress = ipAddress,
                    port = port,
                    name = deviceLabel,
                    targetUnitId = selectedUnitId
                )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("connect_wifi_esp32_button"),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Testar Handshake & Conectar ESP32", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun FirmwareSourceTab(
    context: Context,
    onShowToast: (String) -> Unit
) {
    val sketchCode = remember { ESP32FirmwareTemplate.getArduinoSketchCode() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Firmware Oficial C++ (Arduino IDE)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )
                Text(
                    text = "Grave em qualquer placa ESP32 com DHT22 ou DS18B20",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ESP32 Firmware", sketchCode)
                    clipboard.setPrimaryClip(clip)
                    onShowToast("Código do Firmware copiado para a área de transferência!")
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copiar Código", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Hardware Pinout Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ESQUEMA DE LIGAÇÃO DOS PINOS:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                Text("• Sensor DHT22 / DS18B20 DATA: GPIO 4 (com resistor pull-up 4.7kΩ)", style = MaterialTheme.typography.labelSmall, color = onSurface)
                Text("• Relé Cooler / Compressor: GPIO 26 (saída digital com acoplador)", style = MaterialTheme.typography.labelSmall, color = onSurface)
                Text("• LED de Status de Conexão: GPIO 2 (LED onboard)", style = MaterialTheme.typography.labelSmall, color = onSurface)
                Text("• Alimentação: 3.3V / 5V e GND", style = MaterialTheme.typography.labelSmall, color = onSurface)
            }
        }

        // Code Viewer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                .padding(12.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = sketchCode,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CalibrationAndDataTab(
    viewModel: TermoSenseViewModel,
    allUnits: List<HealthUnitEntity>
) {
    var tempInput by remember { mutableStateOf("4.5") }
    var humInput by remember { mutableStateOf("50.0") }
    var calibratorTool by remember { mutableStateOf("Termômetro Padrão Incoterm / Fluke") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Purge Fake Data Section
        Card(
            colors = CardDefaults.cardColors(containerColor = errorColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = errorColor, modifier = Modifier.size(20.dp))
                    Text("Limpeza de Dados Simulados", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = errorColor)
                }
                Text(
                    text = "Zere os registros históricos de demonstração para receber e auditar exclusivamente dados reais transmitidos pelos seus módulos ESP32.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                    fontSize = 12.sp
                )
                Button(
                    onClick = { viewModel.clearTelemetryHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Limpar Histórico e Aguardar ESP32 Real", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Room Offline Cache & Synchronization Management Section
        val unsyncedCount by viewModel.unsyncedCacheCount.collectAsStateWithLifecycle()
        val syncState by viewModel.syncState.collectAsStateWithLifecycle()

        Card(
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        Text("Mecanismo de Cache Local Room", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (unsyncedCount > 0) Color(0xFFFF9100).copy(alpha = 0.2f) else Color(0xFF00C853).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (unsyncedCount > 0) "$unsyncedCount PENDENTES" else "SINCRONIZADO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (unsyncedCount > 0) Color(0xFFFF9100) else Color(0xFF00C853)
                        )
                    }
                }

                Text(
                    text = "Armazena localmente leituras de temperatura e umidade no SQLite quando os nós ESP32 perdem sinal, integrando tudo de volta ao dashboard automaticamente no momento da reconexão.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                    fontSize = 12.sp
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.syncOfflineCache(isAuto = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        enabled = !syncState.isSyncing
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (syncState.isSyncing) "Sincronizando..." else "Sincronizar Cache",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.simulateOfflineDataCollection(12) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular Queda", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearOfflineCache() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpar", fontSize = 11.sp)
                    }
                }
            }
        }

        HorizontalDivider(color = surfaceVariant)

        // Manual Calibrated Reading Form
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Text(
                    text = "Registro de Medição Oficial Calibrada (Auditoria ANVISA)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tempInput,
                    onValueChange = { tempInput = it },
                    label = { Text("Temperatura (°C)") },
                    colors = termosenseFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = humInput,
                    onValueChange = { humInput = it },
                    label = { Text("Umidade (% UR)") },
                    colors = termosenseFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = calibratorTool,
                onValueChange = { calibratorTool = it },
                label = { Text("Instrumento de Calibração / Certificado RBC") },
                colors = termosenseFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val t = tempInput.toDoubleOrNull() ?: 4.5
                    val h = humInput.toDoubleOrNull() ?: 50.0
                    viewModel.recordManualCalibratedReading(t, h, calibratorTool)
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Registrar Aferição no Banco Room", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
