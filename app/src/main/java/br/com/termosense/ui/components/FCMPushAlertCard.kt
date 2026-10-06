package br.com.termosense.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import br.com.termosense.data.model.HealthUnitEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FCMPushAlertCard(
    fcmToken: String?,
    subscribedTopics: Set<String>,
    currentUnit: HealthUnitEntity?,
    onTestPush: (temp: Double, hum: Double, severity: String) -> Unit,
    onToggleTopic: (topic: String, isSubscribed: Boolean) -> Unit,
    onRefreshToken: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fcm_push_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "FCM Push",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "FIREBASE CLOUD MESSAGING (FCM)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Notificações Push em Segundo Plano",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00C853).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FCM ATIVO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C853)
                        )
                    }
                }
            }

            Text(
                text = "Garante que a equipe hospitalar e os responsáveis técnicos recebam alertas urgentes com som, vibração e banner prioritário (Heads-Up) mesmo quando o app estiver minimizado ou o aparelho bloqueado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // FCM Token Box with Copy Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEVICE REGISTRATION TOKEN:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fcmToken ?: "Obtendo token FCM do Google Play Services...",
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (fcmToken != null) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FCM Registration Token", fcmToken)
                                    clipboard.setPrimaryClip(clip)
                                    onShowToast("Token FCM copiado! Use no Firebase Console ou Postman.")
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("copy_fcm_token_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar Token",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onRefreshToken,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar Token",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Topic Subscriptions
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tópicos de Notificação Push Inscritos:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val topics = listOf(
                        "all_health_units" to "Todas as UBS",
                        "critical_temp_alerts" to "Excursões Críticas",
                        "unit_vacinas" to "Sala de Vacinas PNI",
                        "unit_farmacia" to "Farmácia Satélite"
                    )

                    topics.forEach { (topicKey, topicLabel) ->
                        val isSubscribed = subscribedTopics.contains(topicKey)
                        FilterChip(
                            selected = isSubscribed,
                            onClick = { onToggleTopic(topicKey, !isSubscribed) },
                            label = { Text(topicLabel, fontSize = 11.sp) },
                            leadingIcon = if (isSubscribed) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.18f),
                                selectedLabelColor = primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Background FCM Push Alert Button
                Button(
                    onClick = {
                        val maxAllowed = currentUnit?.maxTemp ?: 8.0
                        onTestPush(maxAllowed + 1.4, 72.0, "CRITICAL")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("test_fcm_push_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Disparar Push FCM (2º Plano)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Preventive Alert Test
                OutlinedButton(
                    onClick = {
                        val minAllowed = currentUnit?.minTemp ?: 2.0
                        onTestPush(minAllowed - 0.8, 48.0, "WARNING")
                    },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Sub-resfriamento",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
