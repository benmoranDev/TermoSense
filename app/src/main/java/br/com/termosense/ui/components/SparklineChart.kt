package br.com.termosense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.TelemetryReadingEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveSparklineCard(
    readings: List<TelemetryReadingEntity>,
    minTarget: Double,
    maxTarget: Double,
    modifier: Modifier = Modifier,
    title: String = "Histórico 15m",
    rateLabel: String = "Taxa: 1.0s"
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val displayList = remember(readings) {
        readings.takeLast(30)
    }

    val minVal = (displayList.minOfOrNull { it.temperature } ?: minTarget).coerceAtMost(minTarget - 1.0)
    val maxVal = (displayList.maxOfOrNull { it.temperature } ?: maxTarget).coerceAtLeast(maxTarget + 1.0)
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("live_sparkline_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SsidChart,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(primaryColor, CircleShape)
                    )
                    Text(
                        text = rateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor
                    )
                }
            }

            // Canvas Wave Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(top = 10.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .pointerInput(displayList) {
                            detectTapGestures { offset ->
                                val count = displayList.size
                                if (count > 1) {
                                    val stepX = size.width / (count - 1)
                                    val idx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                    selectedPointIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Background Grid Lines
                    val gridColor = surfaceVariant.copy(alpha = 0.5f)
                    drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), 1f)
                    drawLine(gridColor, Offset(0f, height / 2f), Offset(width, height / 2f), 1f)
                    drawLine(gridColor, Offset(0f, height), Offset(width, height), 1f)

                    // Reference threshold line for Max Target
                    val maxNorm = ((maxTarget - minVal) / range).toFloat().coerceIn(0f, 1f)
                    val maxY = height * (1f - maxNorm)
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.45f),
                        start = Offset(0f, maxY),
                        end = Offset(width, maxY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Reference threshold line for Min Target
                    val minNorm = ((minTarget - minVal) / range).toFloat().coerceIn(0f, 1f)
                    val minY = height * (1f - minNorm)
                    drawLine(
                        color = primaryColor.copy(alpha = 0.45f),
                        start = Offset(0f, minY),
                        end = Offset(width, minY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    if (displayList.size > 1) {
                        val stepX = width / (displayList.size - 1)
                        val points = displayList.mapIndexed { idx, item ->
                            val norm = ((item.temperature - minVal) / range).toFloat().coerceIn(0f, 1f)
                            val y = height * (1f - norm)
                            Offset(idx * stepX, y)
                        }

                        // Gradient Area Fill Path
                        val fillPath = Path().apply {
                            moveTo(points.first().x, height)
                            lineTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                            lineTo(points.last().x, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Smooth Stroke Curve Path
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = strokePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 3f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw Point Indicators for excursions
                        points.forEachIndexed { idx, pt ->
                            val reading = displayList[idx]
                            if (reading.isExcursion) {
                                drawCircle(
                                    color = secondaryColor,
                                    radius = 4.5f,
                                    center = pt
                                )
                            }
                        }

                        // Selection Cursor
                        selectedPointIndex?.let { idx ->
                            if (idx in points.indices) {
                                val pt = points[idx]
                                drawLine(
                                    color = onSurface.copy(alpha = 0.7f),
                                    start = Offset(pt.x, 0f),
                                    end = Offset(pt.x, height),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 6f,
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Tooltip / Info bar below
            val activeIndex = selectedPointIndex ?: (displayList.size - 1)
            val activeReading = displayList.getOrNull(activeIndex)

            if (activeReading != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (selectedPointIndex != null) "Amostra #${activeIndex + 1}" else "Tempo Real",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "T: %.2f°C", activeReading.temperature),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (activeReading.isExcursion) secondaryColor else primaryColor
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "U: %.1f%%", activeReading.humidity),
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-fidelity Recharts-style trend chart for Android Jetpack Compose.
 * Supports:
 * - Dual-series (Temperature and Humidity synchronized stream)
 * - Reference Area shading (ANVISA Target Safe Zone)
 * - Interactive Crosshair cursor & floating tooltip on drag/touch
 * - Trend velocity calculations (rate of change)
 */
@Composable
fun RechartsTrendChartCard(
    readings: List<TelemetryReadingEntity>,
    minTempTarget: Double,
    maxTempTarget: Double,
    minHumTarget: Double = 35.0,
    maxHumTarget: Double = 65.0,
    showHumiditySeries: Boolean = true,
    showTemperatureSeries: Boolean = true,
    modifier: Modifier = Modifier,
    title: String = "Tendência Histórica & Dispersão (Dual Stream)"
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val displayList = remember(readings) {
        readings.takeLast(40)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val background = MaterialTheme.colorScheme.background

    // Calculate Trend Velocity (Delta Temp across the sample window)
    val trendDelta = remember(displayList) {
        if (displayList.size >= 2) {
            val first = displayList.first().temperature
            val last = displayList.last().temperature
            last - first
        } else 0.0
    }

    // Min & Max bounds for Temperature Axis
    val minT = (displayList.minOfOrNull { it.temperature } ?: minTempTarget).coerceAtMost(minTempTarget - 1.5)
    val maxT = (displayList.maxOfOrNull { it.temperature } ?: maxTempTarget).coerceAtLeast(maxTempTarget + 1.5)
    val rangeT = (maxT - minT).coerceAtLeast(1.0)

    // Min & Max bounds for Humidity Axis (0 to 100% standard)
    val minH = (displayList.minOfOrNull { it.humidity } ?: minHumTarget).coerceAtMost(20.0)
    val maxH = (displayList.maxOfOrNull { it.humidity } ?: maxHumTarget).coerceAtLeast(80.0)
    val rangeH = (maxH - minH).coerceAtLeast(10.0)

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("recharts_trend_chart_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with Title and Trend Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SsidChart,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text(
                        text = "Visualização contínua com interpolação Bézier e área segura ANVISA",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Trend Velocity Badge
                val trendColor = if (Math.abs(trendDelta) < 0.15) tertiaryColor else if (trendDelta > 0) secondaryColor else primaryColor
                val trendText = if (Math.abs(trendDelta) < 0.15) "Estável" else String.format(Locale.getDefault(), "%+.2f°C", trendDelta)
                Row(
                    modifier = Modifier
                        .background(trendColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (Math.abs(trendDelta) < 0.15) Icons.Default.TrendingFlat else if (trendDelta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = trendColor
                    )
                }
            }

            // Interactive Legend / Metric Indicators (like Recharts <Legend />)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTemperatureSeries) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                        Text(
                            text = "Temperatura (°C)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = primaryColor
                        )
                    }
                }

                if (showHumiditySeries) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(secondaryColor, RoundedCornerShape(2.dp)))
                        Text(
                            text = "Umidade (% UR)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = secondaryColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp, 10.dp).background(tertiaryColor.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                    Text(
                        text = "Faixa Segura [${minTempTarget}°C - ${maxTempTarget}°C]",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                }
            }

            // Main Canvas Area (Recharts <ResponsiveContainer> simulation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .pointerInput(displayList) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val count = displayList.size
                                    if (count > 1) {
                                        val stepX = size.width / (count - 1)
                                        val idx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                },
                                onDrag = { change, _ ->
                                    val count = displayList.size
                                    if (count > 1) {
                                        val stepX = size.width / (count - 1)
                                        val idx = (change.position.x / stepX).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                },
                                onDragEnd = { /* Keep tooltip visible */ }
                            )
                        }
                        .pointerInput(displayList) {
                            detectTapGestures { offset ->
                                val count = displayList.size
                                if (count > 1) {
                                    val stepX = size.width / (count - 1)
                                    val idx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                    selectedIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw Grid Lines (Recharts <CartesianGrid />)
                    val gridColor = surfaceVariant.copy(alpha = 0.45f)
                    val horizontalLines = 4
                    for (i in 0..horizontalLines) {
                        val y = height * (i.toFloat() / horizontalLines.toFloat())
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // 2. Draw Target Reference Area (Recharts <ReferenceArea />)
                    val maxNorm = ((maxTempTarget - minT) / rangeT).toFloat().coerceIn(0f, 1f)
                    val minNorm = ((minTempTarget - minT) / rangeT).toFloat().coerceIn(0f, 1f)
                    val topSafeY = height * (1f - maxNorm)
                    val bottomSafeY = height * (1f - minNorm)
                    val safeBandHeight = (bottomSafeY - topSafeY).coerceAtLeast(4f)

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tertiaryColor.copy(alpha = 0.12f),
                                tertiaryColor.copy(alpha = 0.05f)
                            ),
                            startY = topSafeY,
                            endY = bottomSafeY
                        ),
                        topLeft = Offset(0f, topSafeY),
                        size = Size(width, safeBandHeight)
                    )

                    // Reference threshold borders
                    drawLine(
                        color = tertiaryColor.copy(alpha = 0.5f),
                        start = Offset(0f, topSafeY),
                        end = Offset(width, topSafeY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                    drawLine(
                        color = tertiaryColor.copy(alpha = 0.5f),
                        start = Offset(0f, bottomSafeY),
                        end = Offset(width, bottomSafeY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )

                    if (displayList.size > 1) {
                        val stepX = width / (displayList.size - 1)

                        // 3. Draw Humidity Series (Secondary Axis)
                        if (showHumiditySeries) {
                            val humPoints = displayList.mapIndexed { idx, item ->
                                val normH = ((item.humidity - minH) / rangeH).toFloat().coerceIn(0f, 1f)
                                val y = height * (1f - normH)
                                Offset(idx * stepX, y)
                            }

                            val humPath = Path().apply {
                                moveTo(humPoints.first().x, humPoints.first().y)
                                for (i in 0 until humPoints.size - 1) {
                                    val p0 = humPoints[i]
                                    val p1 = humPoints[i + 1]
                                    val cx = (p0.x + p1.x) / 2f
                                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }
                            }

                            drawPath(
                                path = humPath,
                                color = secondaryColor.copy(alpha = 0.85f),
                                style = Stroke(
                                    width = 2.2f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 4. Draw Temperature Series (Primary Axis) with Area Fill (Recharts <Area />)
                        if (showTemperatureSeries) {
                            val tempPoints = displayList.mapIndexed { idx, item ->
                                val normT = ((item.temperature - minT) / rangeT).toFloat().coerceIn(0f, 1f)
                                val y = height * (1f - normT)
                                Offset(idx * stepX, y)
                            }

                            val fillPath = Path().apply {
                                moveTo(tempPoints.first().x, height)
                                lineTo(tempPoints.first().x, tempPoints.first().y)
                                for (i in 0 until tempPoints.size - 1) {
                                    val p0 = tempPoints[i]
                                    val p1 = tempPoints[i + 1]
                                    val cx = (p0.x + p1.x) / 2f
                                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }
                                lineTo(tempPoints.last().x, height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.40f),
                                        primaryColor.copy(alpha = 0.10f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = height
                                )
                            )

                            val strokePath = Path().apply {
                                moveTo(tempPoints.first().x, tempPoints.first().y)
                                for (i in 0 until tempPoints.size - 1) {
                                    val p0 = tempPoints[i]
                                    val p1 = tempPoints[i + 1]
                                    val cx = (p0.x + p1.x) / 2f
                                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }
                            }

                            drawPath(
                                path = strokePath,
                                color = primaryColor,
                                style = Stroke(
                                    width = 3.5f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Draw Excursion Point Dots
                            tempPoints.forEachIndexed { idx, pt ->
                                val reading = displayList[idx]
                                if (reading.isExcursion) {
                                    drawCircle(
                                        color = secondaryColor,
                                        radius = 5f,
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.5f,
                                        center = pt
                                    )
                                }
                            }
                        }

                        // 5. Crosshair Cursor (Recharts <Tooltip cursor={{ strokeDasharray: '3 3' }} />)
                        val activeIdx = selectedIndex ?: (displayList.size - 1)
                        if (activeIdx in displayList.indices) {
                            val activeX = activeIdx * stepX
                            val item = displayList[activeIdx]
                            val normT = ((item.temperature - minT) / rangeT).toFloat().coerceIn(0f, 1f)
                            val activeY = height * (1f - normT)

                            drawLine(
                                color = onSurface.copy(alpha = 0.65f),
                                start = Offset(activeX, 0f),
                                end = Offset(activeX, height),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                            )

                            // Active Temperature Dot
                            drawCircle(
                                color = primaryColor,
                                radius = 6.5f,
                                center = Offset(activeX, activeY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f,
                                center = Offset(activeX, activeY)
                            )

                            // Active Humidity Dot
                            if (showHumiditySeries) {
                                val normH = ((item.humidity - minH) / rangeH).toFloat().coerceIn(0f, 1f)
                                val activeHumY = height * (1f - normH)
                                drawCircle(
                                    color = secondaryColor,
                                    radius = 5.5f,
                                    center = Offset(activeX, activeHumY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5f,
                                    center = Offset(activeX, activeHumY)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Interactive Floating Tooltip Card (Recharts <Tooltip content={<CustomTooltip />} />)
            val activeIdx = selectedIndex ?: (displayList.size - 1)
            val selectedReading = displayList.getOrNull(activeIdx)

            if (selectedReading != null) {
                val isSafe = selectedReading.temperature in minTempTarget..maxTempTarget
                val statusText = if (isSafe) "Conforme (ANVISA)" else "Excursão Térmica"
                val statusColor = if (isSafe) tertiaryColor else secondaryColor

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Ponto de Amostragem #${activeIdx + 1} (${timeFormat.format(Date(selectedReading.timestamp))})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = onSurface
                            )
                        }

                        // Sanitary Status Pill
                        Row(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Temperature Readout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(primaryColor, CircleShape))
                            Column {
                                Text(
                                    text = "Temperatura",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f °C", selectedReading.temperature),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                            }
                        }

                        // Humidity Readout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(secondaryColor, CircleShape))
                            Column {
                                Text(
                                    text = "Umidade Relativa",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f %% UR", selectedReading.humidity),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = secondaryColor
                                )
                            }
                        }

                        // Dew Point Readout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(tertiaryColor, CircleShape))
                            Column {
                                Text(
                                    text = "P. Orvalho",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f °C", selectedReading.dewPoint),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tertiaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
