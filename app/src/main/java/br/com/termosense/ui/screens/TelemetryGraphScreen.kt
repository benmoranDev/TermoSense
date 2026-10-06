package br.com.termosense.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import br.com.termosense.ui.viewmodel.MetricType
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import br.com.termosense.ui.viewmodel.TimeframeFilter
import java.util.Locale

import br.com.termosense.ui.components.RechartsTrendChartCard

@Composable
fun TelemetryGraphScreen(
    viewModel: TermoSenseViewModel,
    unit: HealthUnitEntity?,
    readings: List<TelemetryReadingEntity>,
    selectedTimeframe: TimeframeFilter,
    selectedMetric: MetricType,
    modifier: Modifier = Modifier
) {
    val displayList = remember(readings, selectedMetric) {
        if (readings.isEmpty()) emptyList() else readings.takeLast(40)
    }

    val values = displayList.map {
        when (selectedMetric) {
            MetricType.TEMPERATURE -> it.temperature
            MetricType.HUMIDITY -> it.humidity
            MetricType.COMBINED -> it.temperature
            MetricType.DEW_POINT -> it.dewPoint
        }
    }

    val minTarget = when (selectedMetric) {
        MetricType.TEMPERATURE, MetricType.COMBINED -> unit?.minTemp ?: 2.0
        MetricType.HUMIDITY -> unit?.minHumidity ?: 35.0
        MetricType.DEW_POINT -> 0.0
    }
    val maxTarget = when (selectedMetric) {
        MetricType.TEMPERATURE, MetricType.COMBINED -> unit?.maxTemp ?: 8.0
        MetricType.HUMIDITY -> unit?.maxHumidity ?: 65.0
        MetricType.DEW_POINT -> 18.0
    }

    val minRecorded = if (values.isNotEmpty()) values.minOrNull() ?: minTarget else minTarget
    val maxRecorded = if (values.isNotEmpty()) values.maxOrNull() ?: maxTarget else maxTarget
    val meanRecorded = if (values.isNotEmpty()) values.average() else (minTarget + maxTarget) / 2.0

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("telemetry_graph_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Node Banner
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Análise de Telemetria",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(surfaceVariant, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(tertiaryColor, CircleShape))
                        Text(
                            text = "Node: ${unit?.code ?: "ESP32"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "Série temporal detalhada, dispersão térmica e disparos de atuador em tempo real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
            }
        }

        // Timeframe Selector Pill Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(surfaceVariant, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeframeFilter.entries.forEach { tf ->
                        val isSelected = tf == selectedTimeframe
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) primaryColor else Color.Transparent,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { viewModel.setTimeframe(tf) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.openThresholdDialog() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(surfaceVariant, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Configurar Limiares", tint = primaryColor, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Metric Switcher 3-Segment Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MetricType.entries.forEach { metric ->
                    val isSelected = metric == selectedMetric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) surfaceVariant else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setMetric(metric) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) primaryColor else onSurfaceVariant
                            )
                            Text(
                                text = metric.unit,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.8f) else onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Main Interactive Recharts-Style Telemetry Graph Card
        item {
            when (selectedMetric) {
                MetricType.COMBINED -> {
                    RechartsTrendChartCard(
                        readings = readings,
                        minTempTarget = unit?.minTemp ?: 2.0,
                        maxTempTarget = unit?.maxTemp ?: 8.0,
                        minHumTarget = unit?.minHumidity ?: 35.0,
                        maxHumTarget = unit?.maxHumidity ?: 65.0,
                        showHumiditySeries = true,
                        showTemperatureSeries = true,
                        title = "Tendência Térmica & Umidade (Dual Stream)"
                    )
                }
                MetricType.TEMPERATURE -> {
                    RechartsTrendChartCard(
                        readings = readings,
                        minTempTarget = minTarget,
                        maxTempTarget = maxTarget,
                        showHumiditySeries = false,
                        showTemperatureSeries = true,
                        title = "Curva de Temperatura & Área Segura ANVISA"
                    )
                }
                MetricType.HUMIDITY -> {
                    RechartsTrendChartCard(
                        readings = readings,
                        minTempTarget = unit?.minTemp ?: 2.0,
                        maxTempTarget = unit?.maxTemp ?: 8.0,
                        minHumTarget = minTarget,
                        maxHumTarget = maxTarget,
                        showHumiditySeries = true,
                        showTemperatureSeries = false,
                        title = "Tendência de Umidade Relativa (% UR)"
                    )
                }
                MetricType.DEW_POINT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceColor, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(tertiaryColor, CircleShape))
                                    Text(
                                        text = "Curva de Ponto de Orvalho",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = onSurface
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, "Média %.2f°C", meanRecorded),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = tertiaryColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 10.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    val minVal = (minRecorded - 1.0).coerceAtMost(0.0)
                                    val maxVal = (maxRecorded + 1.0).coerceAtLeast(18.0)
                                    val range = (maxVal - minVal).coerceAtLeast(1.0)

                                    if (values.size > 1) {
                                        val stepX = width / (values.size - 1)
                                        val path = Path()
                                        val fillPath = Path()
                                        val firstY = (height - ((values[0] - minVal) / range * height)).toFloat()
                                        path.moveTo(0f, firstY)
                                        fillPath.moveTo(0f, height)
                                        fillPath.lineTo(0f, firstY)

                                        for (i in 1 until values.size) {
                                            val prevX = (i - 1) * stepX
                                            val prevY = (height - ((values[i - 1] - minVal) / range * height)).toFloat()
                                            val curX = i * stepX
                                            val curY = (height - ((values[i] - minVal) / range * height)).toFloat()
                                            val cpx = prevX + (curX - prevX) / 2f
                                            path.cubicTo(cpx, prevY, cpx, curY, curX, curY)
                                            fillPath.cubicTo(cpx, prevY, cpx, curY, curX, curY)
                                        }

                                        fillPath.lineTo(width, height)
                                        fillPath.close()

                                        drawPath(
                                            fillPath,
                                            Brush.verticalGradient(listOf(tertiaryColor.copy(alpha = 0.35f), Color.Transparent))
                                        )
                                        drawPath(path, tertiaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4 Statistical Summary KPI Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiStatCard(
                        title = "Temp. Máxima",
                        value = String.format(Locale.US, "%.1f", maxRecorded),
                        unit = "°C",
                        subtitle = "Pico registrado hoje",
                        valueColor = secondaryColor,
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Temp. Mínima",
                        value = String.format(Locale.US, "%.1f", minRecorded),
                        unit = "°C",
                        subtitle = "Mínima noturna",
                        valueColor = onSurface,
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiStatCard(
                        title = "Média ${selectedTimeframe.label}",
                        value = String.format(Locale.US, "%.1f", meanRecorded),
                        unit = "°C",
                        subtitle = "Zona Térmica Estável",
                        valueColor = tertiaryColor,
                        icon = Icons.Default.Functions,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Desvio Padrão",
                        value = "±0.6",
                        unit = "°C",
                        subtitle = "Volatilidade Baixa (0.3%)",
                        valueColor = onSurface,
                        icon = Icons.Default.Tune,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Picos e Eventos Recentes Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Picos e Eventos Recentes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text("3 registros hoje", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                }

                EventItemRow(
                    title = "Pico de calor detectado",
                    value = "7.9°C",
                    description = "Câmara atingiu o limiar superior de segurança.",
                    footer = "Hoje às 14:15:02 • ESP32 GPIO 21",
                    icon = Icons.Default.Warning,
                    iconBg = secondaryColor.copy(alpha = 0.15f),
                    iconTint = secondaryColor
                )

                EventItemRow(
                    title = "Cooler ativado automaticamente",
                    value = "6.5°C",
                    description = "Relé 01 acionado com ciclo PWM em 100%.",
                    footer = "Hoje às 13:48:19 • Acionamento Autônomo",
                    icon = Icons.Default.Air,
                    iconBg = primaryColor.copy(alpha = 0.15f),
                    iconTint = primaryColor
                )

                EventItemRow(
                    title = "Mínima matinal atingida",
                    value = "2.8°C",
                    description = "Dentro da janela ideal de preservação de vacinas PNI.",
                    footer = "Hoje às 05:30:41 • Calibrado DS18B20",
                    icon = Icons.Default.CheckCircle,
                    iconBg = tertiaryColor.copy(alpha = 0.15f),
                    iconTint = tertiaryColor
                )
            }
        }

        // Export CSV CTA Button
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { viewModel.openExportDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Exportar CSV / PDF Laudo Sanitário", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Histórico persistido em banco Room com integridade offline por 90 dias",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun KpiStatCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    valueColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(18.dp))
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    color = valueColor
                )
                Text(
                    unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = valueColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EventItemRow(
    title: String,
    value: String,
    description: String,
    footer: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = iconTint)
                }
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(footer, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
