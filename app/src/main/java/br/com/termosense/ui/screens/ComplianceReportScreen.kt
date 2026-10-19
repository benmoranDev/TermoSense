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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.ui.viewmodel.TermoSenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ComplianceReportScreen(
    viewModel: TermoSenseViewModel,
    unit: HealthUnitEntity?,
    reports: List<ComplianceReportEntity>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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
            .testTag("compliance_report_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(24.dp))
                        Text(
                            text = "Conformidade Sanitária",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(tertiaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("ANVISA RDC 430", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tertiaryColor)
                    }
                }
                Text(
                    text = "Geração de relatórios oficiais mensais, cálculo de MKT e auditoria para Rede de Frio do PNI/SUS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
            }
        }

        // Compliance Scorecard Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).background(tertiaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("STATUS SANITÁRIO ATUAL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                                Text("CONFORME & AUDITADO", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tertiaryColor)
                            }
                        }

                        Text("100.0%", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = tertiaryColor)
                    }

                    // 3 KPI columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TEMPERATURA MKT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                            Text("4.7 °C", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                            Text("Cinética Média", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                        }

                        Column {
                            Text("EXCURSÃO TOTAL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                            Text("0 min", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = tertiaryColor)
                            Text("100% na faixa", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("FAIXA PARAMETRIZADA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                            Text("${unit?.minTemp ?: 2.0}°C a ${unit?.maxTemp ?: 8.0}°C", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = onSurface)
                            Text(unit?.profileType ?: "VACINAS_PNI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Export Actions Section
        item {
            Button(
                onClick = { viewModel.openExportDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Emitir Novo Relatório Oficial (PDF / CSV)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // Historical Generated Reports List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Histórico de Relatórios Emitidos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                    }
                    Text("${reports.size} relatórios", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                }

                if (reports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceColor, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum relatório gerado ainda.", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                    }
                } else {
                    reports.forEach { report ->
                        ReportCardItem(
                            report = report,
                            dateString = dateFormat.format(Date(report.generatedAt)),
                            onExportAgain = { viewModel.openExportDialog() }
                        )
                    }
                }
            }
        }

        // Offline Integrity Notice Footer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = tertiaryColor, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = "Garantia de Não-Repúdio & Continuidade Offline",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = onSurface
                    )
                    Text(
                        text = "Os registros persistem localmente via Room Database mesmo durante quedas de internet e falta de energia na UBS/AMA.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCardItem(
    report: ComplianceReportEntity,
    dateString: String,
    onExportAgain: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(report.monthPeriod, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = onSurface)
                        Text("Resp: ${report.responsibleTechnician} • ${report.professionalRegistry}", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .background(tertiaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("${report.compliancePercentage}% Conformidade", style = MaterialTheme.typography.labelSmall, color = tertiaryColor)
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mín: ${report.minTempRecorded}°C", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                Text("Méd: ${report.meanTempRecorded}°C", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                Text("Máx: ${report.maxTempRecorded}°C", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                Text("Excursão: ${report.excursionMinutes} min", style = MaterialTheme.typography.labelSmall, color = if (report.excursionMinutes == 0) tertiaryColor else MaterialTheme.colorScheme.secondary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emitido em $dateString",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onSurfaceVariant
                )

                Row(
                    modifier = Modifier.clickable(onClick = onExportAgain),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                    Text("Compartilhar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                }
            }
        }
    }
}
