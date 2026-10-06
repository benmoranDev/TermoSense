package br.com.termosense.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import java.util.Locale

@Composable
fun SectorsHealthSummary(
    allUnits: List<HealthUnitEntity>,
    selectedUnitId: Long,
    currentReadings: List<TelemetryReadingEntity>,
    onSelectSector: (Long) -> Unit,
    onAddNewSector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Count nominal vs alert sectors
    val sectorStatusList = remember(allUnits, currentReadings, selectedUnitId) {
        allUnits.map { unit ->
            val temp = if (unit.id == selectedUnitId) {
                currentReadings.lastOrNull()?.temperature ?: ((unit.minTemp + unit.maxTemp) / 2.0)
            } else {
                when (unit.profileType) {
                    "VACINAS_PNI" -> 4.8
                    "MEDICAMENTOS" -> 21.2
                    "HEMODERIVADOS" -> 3.9
                    "ESTUFA_LAB" -> 24.5
                    else -> (unit.minTemp + unit.maxTemp) / 2.0
                }
            }

            val hum = if (unit.id == selectedUnitId) {
                currentReadings.lastOrNull()?.humidity ?: ((unit.minHumidity + unit.maxHumidity) / 2.0)
            } else {
                (unit.minHumidity + unit.maxHumidity) / 2.0
            }

            val isTempOk = temp in unit.minTemp..unit.maxTemp
            val isHumOk = hum in unit.minHumidity..unit.maxHumidity
            val isOk = isTempOk && isHumOk

            SectorSummaryData(
                unit = unit,
                currentTemp = temp,
                currentHumidity = hum,
                isOk = isOk
            )
        }
    }

    val totalOk = sectorStatusList.count { it.isOk }
    val totalAlert = sectorStatusList.size - totalOk

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sectors_health_summary_component"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header with Global Health Pill
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
                    imageVector = Icons.Default.DashboardCustomize,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Resumo de Saúde dos Setores",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )
            }

            // Health Status Global Pill
            Row(
                modifier = Modifier
                    .background(surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(7.dp).background(tertiaryColor, CircleShape))
                    Text(
                        text = "$totalOk OK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = tertiaryColor
                    )
                }

                if (totalAlert > 0) {
                    Text("•", color = onSurfaceVariant, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(secondaryColor, CircleShape))
                        Text(
                            text = "$totalAlert ALERTA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = secondaryColor
                        )
                    }
                }
            }
        }

        // Horizontal Carousel of Sectors
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(sectorStatusList, key = { it.unit.id }) { item ->
                val isSelected = item.unit.id == selectedUnitId
                SectorHealthCard(
                    data = item,
                    isSelected = isSelected,
                    onClick = { onSelectSector(item.unit.id) }
                )
            }

            // Quick Add Sector Card at the end
            item {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(115.dp)
                        .background(surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onAddNewSector)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar Setor",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "+ Novo Setor",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

private data class SectorSummaryData(
    val unit: HealthUnitEntity,
    val currentTemp: Double,
    val currentHumidity: Double,
    val isOk: Boolean
)

@Composable
private fun SectorHealthCard(
    data: SectorSummaryData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val unit = data.unit
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val statusColor = if (data.isOk) tertiaryColor else secondaryColor
    val statusText = if (data.isOk) "OK" else "ALERTA"

    // Infinite pulse for alert sectors
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAlert")
    val alertAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertAlpha"
    )

    val border = if (isSelected) {
        BorderStroke(2.dp, primaryColor)
    } else {
        BorderStroke(1.dp, surfaceVariant)
    }

    Card(
        modifier = Modifier
            .width(175.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sector_health_card_${unit.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) surfaceVariant else surfaceColor
        ),
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Sector Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = unit.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = onSurface,
                        maxLines = 1
                    )
                }

                // Health Status Pill
                Row(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .then(if (!data.isOk) Modifier.alpha(alertAlpha) else Modifier)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                }
            }

            // Middle: Live Temperature & Humidity Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Temperature
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = if (data.isOk) primaryColor else secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f°C", data.currentTemp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (data.isOk) onSurface else secondaryColor
                    )
                }

                // Humidity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f%%", data.currentHumidity),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = onSurfaceVariant
                    )
                }
            }

            // Bottom Row: Target Range & Selection Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Faixa: ${unit.minTemp.toInt()}°C a ${unit.maxTemp.toInt()}°C",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = onSurfaceVariant
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "ATIVO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}
