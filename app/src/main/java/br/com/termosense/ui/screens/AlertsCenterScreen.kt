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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.ui.components.FCMPushAlertCard
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun AlertsCenterScreen(
    viewModel: TermoSenseViewModel,
    activeAlerts: List<AlertEventEntity>,
    allAlerts: List<AlertEventEntity>,
    rules: List<AutomationRuleEntity>,
    modifier: Modifier = Modifier
) {
    val resolvedCount = allAlerts.count { it.isResolved }
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val subscribedTopics by viewModel.subscribedTopics.collectAsStateWithLifecycle()
    val currentUnit by viewModel.currentUnit.collectAsStateWithLifecycle()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("alerts_center_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Ambient Telemetry Header Strip
        item {
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Central de Alertas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .background(surfaceVariant, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(secondaryColor, CircleShape))
                    Text(
                        text = "MODO COCKPIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = secondaryColor
                    )
                }
            }
        }

        // Metric Counters Bento Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Alerts Count Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(surfaceColor, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alertas Ativos", style = MaterialTheme.typography.labelMedium, color = onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .background(secondaryColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text("Crítico", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d", activeAlerts.size),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                color = secondaryColor
                            )
                            Text("em curso", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(13.dp))
                            Text("+1 última hora", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = secondaryColor)
                        }
                    }
                }

                // Resolved Notifications Count Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(surfaceColor, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resolvidos", style = MaterialTheme.typography.labelMedium, color = onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .background(tertiaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text("24h", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = tertiaryColor)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$resolvedCount",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                color = tertiaryColor
                            )
                            Text("recuperados", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(13.dp))
                            Text("Taxa 98.4%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = tertiaryColor)
                        }
                    }
                }
            }
        }

        // Firebase Cloud Messaging (FCM) Push Notifications Management Card
        item {
            FCMPushAlertCard(
                fcmToken = fcmToken,
                subscribedTopics = subscribedTopics,
                currentUnit = currentUnit,
                onTestPush = { temp, hum, severity ->
                    viewModel.testFcmPushBackgroundAlert(
                        simulatedTemp = temp,
                        simulatedHum = hum,
                        severity = severity
                    )
                },
                onToggleTopic = { topic, isSubscribed ->
                    viewModel.toggleFcmTopic(topic, isSubscribed)
                },
                onRefreshToken = { viewModel.refreshFcmToken() },
                onShowToast = { msg -> viewModel.showToast(msg) }
            )
        }

        // Section 1: Active Critical Alerts
        item {
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
                        Box(modifier = Modifier.size(7.dp).background(secondaryColor, CircleShape))
                        Text(
                            text = "Alertas Críticos / Recentes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text("${activeAlerts.size} PENDENTES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = secondaryColor)
                }

                if (activeAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceColor, RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(36.dp))
                            Text("Todos os limiares e nós estão nominais!", style = MaterialTheme.typography.bodyMedium, color = onSurface)
                            Text("Nenhuma violação de cadeia de frio ativa.", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                        }
                    }
                } else {
                    activeAlerts.forEach { alert ->
                        AlertCardItem(
                            alert = alert,
                            onSilence = { viewModel.silenceAlert(alert.id) },
                            onResolve = { viewModel.resolveAlert(alert.id) }
                        )
                    }
                }
            }
        }

        // Section 2: Automated Rules & Triggers
        item {
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
                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Gatilhos & Automações",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text("${rules.count { it.isEnabled }} ATIVAS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tertiaryColor)
                }

                rules.forEach { rule ->
                    AutomationRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(rule) },
                        onConfigure = { viewModel.openThresholdDialog() }
                    )
                }
            }
        }

        // Primary CTA Button
        item {
            Button(
                onClick = { viewModel.openNewRuleDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Nova Regra de Alerta", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AlertCardItem(
    alert: AlertEventEntity,
    onSilence: () -> Unit,
    onResolve: () -> Unit
) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            .size(36.dp)
                            .background(secondaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(alert.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = secondaryColor)
                        Text(alert.nodeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(secondaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Há 12 min", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = secondaryColor)
                }
            }

            // Value breach box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Valor Atual:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format(Locale.US, "%.1f", alert.currentTemp),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = secondaryColor
                    )
                    Text("°C", style = MaterialTheme.typography.titleMedium, color = secondaryColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("LIMITE MÁX", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format(Locale.US, "%.1f °C (+%.1f°C)", alert.limitTemp, (alert.currentTemp - alert.limitTemp).coerceAtLeast(0.0)),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = errorColor
                    )
                }
            }

            // Micro sparkline Canvas trace
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp)) {
                val path = Path()
                path.moveTo(0f, size.height * 0.8f)
                path.lineTo(size.width * 0.3f, size.height * 0.7f)
                path.lineTo(size.width * 0.6f, size.height * 0.4f)
                path.lineTo(size.width * 0.9f, size.height * 0.2f)
                path.lineTo(size.width, size.height * 0.1f)
                drawPath(path, secondaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(secondaryColor, radius = 4.dp.toPx(), center = Offset(size.width, size.height * 0.1f))
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable(onClick = onSilence)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Text("Silenciar 30m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(secondaryColor, RoundedCornerShape(8.dp))
                        .clickable(onClick = onResolve)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
                        Text("Resolver", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun AutomationRuleCard(
    rule: AutomationRuleEntity,
    onToggle: () -> Unit,
    onConfigure: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(if (rule.isEnabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                    Text(rule.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Input, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                    Text(rule.conditionText, style = MaterialTheme.typography.labelSmall, color = primaryColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(14.dp))
                    Text(rule.actionText, style = MaterialTheme.typography.labelSmall, color = tertiaryColor)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (rule.isEnabled) tertiaryColor else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                    Text(
                        text = if (rule.isEnabled) "Armado • ${rule.activationsCount} ativações hoje" else "Desativado temporariamente",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (rule.isEnabled) tertiaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.clickable(onClick = onConfigure),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Configurar", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                    Icon(Icons.Default.Tune, contentDescription = null, tint = primaryColor, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}
