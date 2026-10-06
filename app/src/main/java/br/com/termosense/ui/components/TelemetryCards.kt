package br.com.termosense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun TacticalMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    icon: ImageVector,
    isNominal: Boolean = true,
    showWifiBars: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .background(surfaceColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isNominal) primaryColor else tertiaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = onSurface
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onSurfaceVariant
                )

                if (showWifiBars) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(modifier = Modifier.size(3.dp, 5.dp).background(tertiaryColor, RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.size(3.dp, 8.dp).background(tertiaryColor, RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.size(3.dp, 11.dp).background(tertiaryColor, RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.size(3.dp, 14.dp).background(tertiaryColor, RoundedCornerShape(1.dp)))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (isNominal) tertiaryColor else primaryColor, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun TacticalMetricsGrid(
    humidity: Double,
    dewPoint: Double,
    vcc: Double = 3.32,
    rssi: Int = -54,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalMetricCard(
                title = "Umidade",
                value = String.format(Locale.US, "%.1f", humidity),
                unit = "%",
                subtitle = "Ideal 40-65%",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
            TacticalMetricCard(
                title = "P. Orvalho",
                value = String.format(Locale.US, "%.1f", dewPoint),
                unit = "°C",
                subtitle = "Sem condensação",
                icon = Icons.Default.Opacity,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalMetricCard(
                title = "VCC Rail",
                value = String.format(Locale.US, "%.2f", vcc),
                unit = "V",
                subtitle = "ESP32 Regulado",
                icon = Icons.Default.Bolt,
                modifier = Modifier.weight(1f)
            )
            TacticalMetricCard(
                title = "Sinal RSSI",
                value = "$rssi",
                unit = "dBm",
                subtitle = RSSIHelper.getQualityLabel(rssi, isOnline = true),
                icon = Icons.Default.Wifi,
                showWifiBars = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
