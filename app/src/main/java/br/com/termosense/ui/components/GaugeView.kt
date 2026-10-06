package br.com.termosense.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HeroGaugeCard(
    currentTemp: Double,
    minTarget: Double,
    maxTarget: Double,
    sensorLabel: String,
    delta24h: String = "+0.2°C",
    isNominal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val sweepFraction = ((currentTemp - (minTarget - 5.0)) / ((maxTarget + 5.0) - (minTarget - 5.0)))
        .coerceIn(0.05, 0.95).toFloat()

    val animatedSweep by animateFloatAsState(
        targetValue = sweepFraction,
        animationSpec = tween(durationMillis = 700),
        label = "gaugeSweep"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackBgColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("hero_gauge_card"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient glow backdrop
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopCenter)
                .background(
                    if (isNominal) primaryColor.copy(alpha = 0.15f) else secondaryColor.copy(alpha = 0.2f),
                    CircleShape
                )
                .blur(48.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isNominal) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (isNominal) tertiaryColor else secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isNominal) "Estável & Nominal" else "Excursão de Limite",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isNominal) tertiaryColor else secondaryColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "Alvo: %.1f°C - %.1f°C", minTarget, maxTarget),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Semi-circular Canvas Arc Gauge
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(145.dp)
                    .padding(top = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.size(240.dp, 130.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background Track
                    drawArc(
                        color = trackBgColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Colored Track
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(primaryColor, tertiaryColor, secondaryColor)
                    )
                    drawArc(
                        brush = gradient,
                        startAngle = 180f,
                        sweepAngle = 180f * animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Numerical readout in center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", currentTemp),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "°C",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = primaryColor,
                            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                        )
                    }
                    Text(
                        text = sensorLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Micro Stat Pill Row under Hero
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "VARIAÇÃO 24H",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = delta24h,
                            style = MaterialTheme.typography.labelMedium,
                            color = tertiaryColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(secondaryColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LIMIAR CRÍTICO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "Max: %.1f°C", maxTarget),
                            style = MaterialTheme.typography.labelMedium,
                            color = secondaryColor
                        )
                    }
                }
            }
        }
    }
}
