package br.com.termosense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog

@Composable
fun ThresholdConfigDialog(
    initialMinTemp: Double,
    initialMaxTemp: Double,
    initialMinHum: Double,
    initialMaxHum: Double,
    onDismiss: () -> Unit,
    onSave: (minTemp: Double, maxTemp: Double, minHum: Double, maxHum: Double) -> Unit
) {
    var minTempStr by remember { mutableStateOf(initialMinTemp.toString()) }
    var maxTempStr by remember { mutableStateOf(initialMaxTemp.toString()) }
    var minHumStr by remember { mutableStateOf(initialMinHum.toString()) }
    var maxHumStr by remember { mutableStateOf(initialMaxHum.toString()) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth().testTag("threshold_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Icon(Icons.Default.Tune, contentDescription = null, tint = primaryColor)
                        Text(
                            text = "Configurar Limiares",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                Text(
                    text = "Ajuste as faixas de tolerância sanitária para disparo automático de alarmes e atuadores.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )

                // Presets Rápidos
                Text(
                    text = "PRESETS RÁPIDOS (ANVISA / PNI)",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip("Vacinas PNI (+2..+8°C)", modifier = Modifier.weight(1f)) {
                        minTempStr = "2.0"; maxTempStr = "8.0"; minHumStr = "40.0"; maxHumStr = "65.0"
                    }
                    PresetChip("Medicamentos (15..25°C)", modifier = Modifier.weight(1f)) {
                        minTempStr = "15.0"; maxTempStr = "25.0"; minHumStr = "35.0"; maxHumStr = "60.0"
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip("Hemoderivados (+2..+6°C)", modifier = Modifier.weight(1f)) {
                        minTempStr = "2.0"; maxTempStr = "6.0"; minHumStr = "45.0"; maxHumStr = "65.0"
                    }
                    PresetChip("Data Center (20..26°C)", modifier = Modifier.weight(1f)) {
                        minTempStr = "20.0"; maxTempStr = "26.0"; minHumStr = "45.0"; maxHumStr = "65.0"
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Temperature inputs
                Text(
                    text = "FAIXA DE TEMPERATURA (°C)",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minTempStr,
                        onValueChange = { minTempStr = it },
                        label = { Text("Temp Mín (°C)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxTempStr,
                        onValueChange = { maxTempStr = it },
                        label = { Text("Temp Máx (°C)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Custom Humidity inputs
                Text(
                    text = "FAIXA DE UMIDADE (% UR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = tertiaryColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minHumStr,
                        onValueChange = { minHumStr = it },
                        label = { Text("Umidade Mín (%)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxHumStr,
                        onValueChange = { maxHumStr = it },
                        label = { Text("Umidade Máx (%)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val minT = minTempStr.toDoubleOrNull() ?: initialMinTemp
                            val maxT = maxTempStr.toDoubleOrNull() ?: initialMaxTemp
                            val minH = minHumStr.toDoubleOrNull() ?: initialMinHum
                            val maxH = maxHumStr.toDoubleOrNull() ?: initialMaxHum
                            onSave(minT, maxT, minH, maxH)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Salvar Limiares", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun NewDeviceDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, model: String, sensorType: String, ip: String, mac: String, samplingMs: Long) -> Unit
) {
    var name by remember { mutableStateOf("ESP32-S3 Câmara Nova") }
    var model by remember { mutableStateOf("ESP32-S3") }
    var sensorType by remember { mutableStateOf("DS18B20 (Imersão)") }
    var ip by remember { mutableStateOf("192.168.1.188") }
    var mac by remember { mutableStateOf("24:6F:28:CF:88:99") }
    var samplingRateStr by remember { mutableStateOf("2000") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth().testTag("new_device_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.DeviceHub, contentDescription = null, tint = primaryColor)
                        Text(
                            text = "Parear Novo ESP32",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Módulo / Localização") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Microcontrolador") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sensorType,
                        onValueChange = { sensorType = it },
                        label = { Text("Sensor Térmico") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IP Local / Hostname") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = samplingRateStr,
                        onValueChange = { samplingRateStr = it },
                        label = { Text("Amostragem (ms)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it },
                    label = { Text("Endereço MAC / BLE") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val rate = samplingRateStr.toLongOrNull() ?: 2000L
                            onSave(name, model, sensorType, ip, mac, rate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Conectar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewRuleDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, condition: String, action: String, targetType: String) -> Unit
) {
    var name by remember { mutableStateOf("Proteção Térmica Relé Extra") }
    var condition by remember { mutableStateOf("SE Temp > 8.0°C ou < 2.0°C") }
    var action by remember { mutableStateOf("ENTÃO: Acionar Relé Pino D23 + Push Alarme") }
    var targetType by remember { mutableStateOf("RELAY_COOLER") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth().testTag("new_rule_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = primaryColor)
                        Text(
                            text = "Nova Automação",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Título da Regra") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condição de Disparo (IF)") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Ação do Microcontrolador (THEN)") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onSave(name, condition, action, targetType)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Armar Regra", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportReportDialog(
    unitName: String,
    onDismiss: () -> Unit,
    onExport: (isPdf: Boolean, techName: String, registry: String, month: String, obs: String) -> Unit
) {
    var isPdf by remember { mutableStateOf(true) }
    var techName by remember { mutableStateOf("Dra. Mariana Vasconcelos") }
    var registry by remember { mutableStateOf("CRF/SP 48.912") }
    var monthPeriod by remember { mutableStateOf("Setembro / 2026") }
    var observations by remember {
        mutableStateOf("Conforme RDC ANVISA nº 430/2020 e Manual de Rede de Frio do PNI/MS.")
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth().testTag("export_report_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = primaryColor)
                        Text(
                            text = "Exportar Laudo / Relatório",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                Text(
                    text = "Gere relatórios mensais oficiais de conformidade sanitária para auditorias ANVISA/SUS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )

                // Format selector
                Text(
                    text = "FORMATO DE EXPORTAÇÃO",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(if (isPdf) surfaceVariant else surfaceColor, RoundedCornerShape(8.dp))
                            .clickable { isPdf = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = isPdf,
                            onClick = { isPdf = true },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Text("PDF Oficial", color = onSurface, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(if (!isPdf) surfaceVariant else surfaceColor, RoundedCornerShape(8.dp))
                            .clickable { isPdf = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = !isPdf,
                            onClick = { isPdf = false },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Text("Excel / CSV", color = onSurface, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = monthPeriod,
                    onValueChange = { monthPeriod = it },
                    label = { Text("Mês / Período da Auditoria") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = techName,
                    onValueChange = { techName = it },
                    label = { Text("Responsável Técnico(a)") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = registry,
                    onValueChange = { registry = it },
                    label = { Text("Registro Profissional (CRF / COREN / CRM)") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    label = { Text("Parecer Técnico / Observações") },
                    maxLines = 3,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onExport(isPdf, techName, registry, monthPeriod, observations)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(if (isPdf) "Gerar PDF" else "Gerar CSV", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun termosenseFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
fun NewSectorDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        code: String,
        profileType: String,
        location: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double,
        coolerPin: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("UBS Central - Sala de Insumos Críticos") }
    var code by remember { mutableStateOf("UBS-CEN-INS05") }
    var location by remember { mutableStateOf("Bloco B • Sala 04") }
    var profileType by remember { mutableStateOf("VACINAS_PNI") }
    var minTempStr by remember { mutableStateOf("2.0") }
    var maxTempStr by remember { mutableStateOf("8.0") }
    var minHumStr by remember { mutableStateOf("35.0") }
    var maxHumStr by remember { mutableStateOf("65.0") }
    var coolerPin by remember { mutableStateOf("GPIO 26") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth().testTag("new_sector_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.Tune, contentDescription = null, tint = primaryColor)
                        Text(
                            text = "Cadastrar Novo Setor",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = onSurfaceVariant)
                    }
                }

                Text(
                    text = "Configure os parâmetros aceitáveis de conformidade térmica para esta sala ou câmara.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Setor / Sala") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código / CNES") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Localização") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Profile Selector Preset Buttons
                Text(
                    text = "PERFIL SANITÁRIO (PRESETS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(
                        label = "Vacinas (+2 a +8°C)",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            profileType = "VACINAS_PNI"
                            minTempStr = "2.0"
                            maxTempStr = "8.0"
                            minHumStr = "35.0"
                            maxHumStr = "65.0"
                        }
                    )
                    PresetChip(
                        label = "Remédios (+15 a +25°C)",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            profileType = "MEDICAMENTOS"
                            minTempStr = "15.0"
                            maxTempStr = "25.0"
                            minHumStr = "30.0"
                            maxHumStr = "60.0"
                        }
                    )
                    PresetChip(
                        label = "Sangue (+2 a +6°C)",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            profileType = "HEMODERIVADOS"
                            minTempStr = "2.0"
                            maxTempStr = "6.0"
                            minHumStr = "30.0"
                            maxHumStr = "60.0"
                        }
                    )
                }

                // Thresholds inputs
                Text(
                    text = "LIMITES ACEITÁVEIS PARAMETRIZADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minTempStr,
                        onValueChange = { minTempStr = it },
                        label = { Text("Temp Mín (°C)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxTempStr,
                        onValueChange = { maxTempStr = it },
                        label = { Text("Temp Máx (°C)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minHumStr,
                        onValueChange = { minHumStr = it },
                        label = { Text("Umid Mín (%)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxHumStr,
                        onValueChange = { maxHumStr = it },
                        label = { Text("Umid Máx (%)") },
                        singleLine = true,
                        colors = termosenseFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = coolerPin,
                    onValueChange = { coolerPin = it },
                    label = { Text("Pino GPIO do Relé Cooler/Compressor") },
                    singleLine = true,
                    colors = termosenseFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val minT = minTempStr.toDoubleOrNull() ?: 2.0
                            val maxT = maxTempStr.toDoubleOrNull() ?: 8.0
                            val minH = minHumStr.toDoubleOrNull() ?: 35.0
                            val maxH = maxHumStr.toDoubleOrNull() ?: 65.0
                            onSave(name, code, profileType, location, minT, maxT, minH, maxH, coolerPin)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Criar Setor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
