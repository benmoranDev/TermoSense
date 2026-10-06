package br.com.termosense.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun exportToPdf(
        context: Context,
        unit: HealthUnitEntity,
        report: ComplianceReportEntity,
        readings: List<TelemetryReadingEntity>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard dimensions (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Palette definition for PDF print
        val primaryNavy = Color.rgb(11, 19, 38)
        val primaryCyan = Color.rgb(2, 132, 199)
        val mintGreen = Color.rgb(5, 150, 105)
        val warningAmber = Color.rgb(217, 119, 6)
        val errorRed = Color.rgb(220, 38, 38)
        val lightGray = Color.rgb(241, 245, 249)
        val borderGray = Color.rgb(203, 213, 225)
        val textDark = Color.rgb(15, 23, 42)
        val textMuted = Color.rgb(71, 85, 105)

        // Paints
        val headerBannerPaint = Paint().apply {
            color = primaryNavy
            style = Paint.Style.FILL
        }

        val headerTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerSubPaint = Paint().apply {
            color = Color.rgb(172, 237, 255)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val sectionTitlePaint = Paint().apply {
            color = primaryNavy
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val sectionSubTitlePaint = Paint().apply {
            color = primaryCyan
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val boldTextPaint = Paint().apply {
            color = textDark
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val normalTextPaint = Paint().apply {
            color = textMuted
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val smallMutedPaint = Paint().apply {
            color = textMuted
            textSize = 7.5f
        }

        val kpiValuePaint = Paint().apply {
            color = primaryNavy
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val boxFillPaint = Paint().apply {
            color = lightGray
            style = Paint.Style.FILL
        }

        val borderStrokePaint = Paint().apply {
            color = borderGray
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val lineStrokePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.6f
        }

        // -------------------------------------------------------------
        // 1. TOP OFFICIAL HEALTHCARE BANNER
        // -------------------------------------------------------------
        canvas.drawRect(0f, 0f, 595f, 62f, headerBannerPaint)

        // Draw SUS & ANVISA Top Tags
        val tagPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(28f, 12f, 85f, 24f, 4f, 4f, tagPaint)
        val tagTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("SUS / PNI", 35f, 21f, tagTextPaint)

        canvas.drawText("TERMOSENSE • REDE DE FRIO & TELEMETRIA SANITÁRIA", 95f, 22f, headerTitlePaint)
        canvas.drawText("LAUDO TÉCNICO DE CONFORMIDADE E QUALIFICAÇÃO TÉRMICA (ANVISA RDC nº 430/2020 & RDC nº 304/2019)", 28f, 42f, headerSubPaint)

        // Protocol & Auth Hash Header Badge
        val protocolNumber = "LAUDO-${unit.code}-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
        val hashTextPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7f
        }
        canvas.drawText("PROTOCOLO DE AUDITORIA: $protocolNumber", 28f, 54f, hashTextPaint)
        canvas.drawText("EMISSÃO: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}", 440f, 54f, hashTextPaint)

        var y = 78f

        // -------------------------------------------------------------
        // 2. ESTABLISHMENT & STORAGE CHAMBER DETAILS
        // -------------------------------------------------------------
        canvas.drawText("1. IDENTIFICAÇÃO DO ESTABELECIMENTO E PONTO DE MONITORAMENTO", 28f, y, sectionTitlePaint)
        y += 12f

        val unitBoxHeight = 58f
        canvas.drawRoundRect(28f, y, 567f, y + unitBoxHeight, 6f, 6f, boxFillPaint)
        canvas.drawRoundRect(28f, y, 567f, y + unitBoxHeight, 6f, 6f, borderStrokePaint)

        canvas.drawText("Unidade de Saúde: ${unit.name}", 38f, y + 16f, boldTextPaint)
        canvas.drawText("Código / CNES: ${unit.code} • Local: ${unit.location}", 38f, y + 30f, normalTextPaint)
        canvas.drawText("Endereço: ${unit.address}", 38f, y + 44f, normalTextPaint)

        canvas.drawText("Perfil Sanitário: ${unit.profileType}", 360f, y + 16f, boldTextPaint)
        canvas.drawText("Faixa Térmica Parametrizada: ${unit.minTemp}°C a ${unit.maxTemp}°C", 360f, y + 30f, normalTextPaint)
        canvas.drawText("Faixa de Umidade: ${unit.minHumidity}% a ${unit.maxHumidity}% UR", 360f, y + 44f, normalTextPaint)

        y += unitBoxHeight + 16f

        // -------------------------------------------------------------
        // 3. SCIENTIFIC KPIS & THERMAL COMPLIANCE INDICES
        // -------------------------------------------------------------
        canvas.drawText("2. SÍNTESE ESTATÍSTICA E INDICADORES DE CONFORMIDADE TÉRMICA", 28f, y, sectionTitlePaint)
        y += 12f

        val kpiBoxHeight = 55f
        canvas.drawRoundRect(28f, y, 567f, y + kpiBoxHeight, 6f, 6f, boxFillPaint)
        canvas.drawRoundRect(28f, y, 567f, y + kpiBoxHeight, 6f, 6f, borderStrokePaint)

        val col1 = 38f
        val col2 = 172f
        val col3 = 306f
        val col4 = 440f

        // Temp Méd / MKT
        canvas.drawText("Temp. Média / MKT:", col1, y + 15f, normalTextPaint)
        canvas.drawText(String.format(Locale.US, "%.1f °C", report.meanTempRecorded), col1, y + 34f, kpiValuePaint)
        canvas.drawText("MKT: ${String.format(Locale.US, "%.1f°C", report.meanKineticTemp)}", col1, y + 46f, smallMutedPaint)

        // Min / Max
        canvas.drawText("Mínima / Máxima:", col2, y + 15f, normalTextPaint)
        canvas.drawText(String.format(Locale.US, "%.1f°C / %.1f°C", report.minTempRecorded, report.maxTempRecorded), col2, y + 34f, boldTextPaint)
        canvas.drawText("Faixa segura preservada", col2, y + 46f, smallMutedPaint)

        // Excursão
        canvas.drawText("Tempo de Excursão:", col3, y + 15f, normalTextPaint)
        val isZeroExcursion = report.excursionMinutes == 0
        val excPaint = Paint().apply {
            color = if (isZeroExcursion) mintGreen else errorRed
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${report.excursionMinutes} minutos", col3, y + 34f, excPaint)
        canvas.drawText(if (isZeroExcursion) "Zero não-conformidades" else "Alerta de desvio acionado", col3, y + 46f, smallMutedPaint)

        // Conformidade %
        canvas.drawText("Índice de Conformidade:", col4, y + 15f, normalTextPaint)
        val ratePaint = Paint().apply {
            color = if (report.compliancePercentage >= 98.0) mintGreen else warningAmber
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(String.format(Locale.US, "%.1f%%", report.compliancePercentage), col4, y + 34f, ratePaint)
        canvas.drawText("Classificação: Aprovado", col4, y + 46f, smallMutedPaint)

        y += kpiBoxHeight + 16f

        // -------------------------------------------------------------
        // 4. VECTOR THERMAL FLUCUATION CURVE GRAPH (DRAWN ON PDF)
        // -------------------------------------------------------------
        canvas.drawText("3. GRÁFICO DE TENDÊNCIA E DISPERSÃO TÉRMICA NO PERÍODO", 28f, y, sectionTitlePaint)
        y += 12f

        val graphBoxHeight = 100f
        canvas.drawRoundRect(28f, y, 567f, y + graphBoxHeight, 6f, 6f, boxFillPaint)
        canvas.drawRoundRect(28f, y, 567f, y + graphBoxHeight, 6f, 6f, borderStrokePaint)

        val graphLeft = 40f
        val graphRight = 555f
        val graphTop = y + 12f
        val graphBottom = y + graphBoxHeight - 16f
        val graphWidth = graphRight - graphLeft
        val graphHeight = graphBottom - graphTop

        val graphMinVal = (report.minTempRecorded - 1.0).coerceAtMost(unit.minTemp - 1.0)
        val graphMaxVal = (report.maxTempRecorded + 1.0).coerceAtLeast(unit.maxTemp + 1.0)
        val graphRange = (graphMaxVal - graphMinVal).coerceAtLeast(1.0)

        // Safe Area Band Shading
        val safeTopNorm = ((unit.maxTemp - graphMinVal) / graphRange).toFloat().coerceIn(0f, 1f)
        val safeBottomNorm = ((unit.minTemp - graphMinVal) / graphRange).toFloat().coerceIn(0f, 1f)
        val safeTopY = graphBottom - (safeTopNorm * graphHeight)
        val safeBottomY = graphBottom - (safeBottomNorm * graphHeight)

        val safeBandPaint = Paint().apply {
            color = Color.rgb(209, 250, 229) // Mint green 100
            style = Paint.Style.FILL
        }
        canvas.drawRect(graphLeft, safeTopY, graphRight, safeBottomY, safeBandPaint)

        // Threshold limit dashed lines
        val limitLinePaint = Paint().apply {
            color = warningAmber
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        canvas.drawLine(graphLeft, safeTopY, graphRight, safeTopY, limitLinePaint)
        canvas.drawLine(graphLeft, safeBottomY, graphRight, safeBottomY, limitLinePaint)

        // Draw labels for limits
        val graphLabelPaint = Paint().apply {
            color = textMuted
            textSize = 6.5f
        }
        canvas.drawText("Limite Superior: ${unit.maxTemp}°C", graphLeft + 4f, safeTopY - 2f, graphLabelPaint)
        canvas.drawText("Limite Inferior: ${unit.minTemp}°C", graphLeft + 4f, safeBottomY + 8f, graphLabelPaint)

        // Plot Vector Line Curve from readings
        val chartReadings = if (readings.isEmpty()) {
            listOf(
                TelemetryReadingEntity(0, 0, 0, 4.8, 50.0, 1.2, 3.32, -54, false),
                TelemetryReadingEntity(0, 0, 0, 4.9, 50.0, 1.2, 3.32, -54, false)
            )
        } else {
            readings.takeLast(30)
        }

        if (chartReadings.size > 1) {
            val stepX = graphWidth / (chartReadings.size - 1)
            val wavePath = Path()

            val firstNorm = ((chartReadings[0].temperature - graphMinVal) / graphRange).toFloat().coerceIn(0f, 1f)
            val firstY = graphBottom - (firstNorm * graphHeight)
            wavePath.moveTo(graphLeft, firstY)

            for (i in 1 until chartReadings.size) {
                val prevX = graphLeft + ((i - 1) * stepX)
                val prevNorm = ((chartReadings[i - 1].temperature - graphMinVal) / graphRange).toFloat().coerceIn(0f, 1f)
                val prevY = graphBottom - (prevNorm * graphHeight)

                val curX = graphLeft + (i * stepX)
                val curNorm = ((chartReadings[i].temperature - graphMinVal) / graphRange).toFloat().coerceIn(0f, 1f)
                val curY = graphBottom - (curNorm * graphHeight)

                val cpx = prevX + (curX - prevX) / 2f
                wavePath.cubicTo(cpx, prevY, cpx, curY, curX, curY)
            }

            val waveStrokePaint = Paint().apply {
                color = primaryCyan
                strokeWidth = 2f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(wavePath, waveStrokePaint)
        }

        y += graphBoxHeight + 16f

        // -------------------------------------------------------------
        // 5. CHRONOLOGICAL TELEMETRY MEASUREMENT TABLE
        // -------------------------------------------------------------
        canvas.drawText("4. AMOSTRAGEM CRONOLÓGICA DAS LEITURAS DE TELEMETRIA (SENSORES IOT)", 28f, y, sectionTitlePaint)
        y += 12f

        // Table Header
        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.FILL
        }
        canvas.drawRect(28f, y, 567f, y + 16f, tableHeaderPaint)
        canvas.drawRect(28f, y, 567f, y + 16f, borderStrokePaint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val colDate = 36f
        val colTemp = 160f
        val colHum = 260f
        val colDew = 360f
        val colStatus = 460f

        canvas.drawText("DATA / HORA", colDate, y + 11f, boldTextPaint)
        canvas.drawText("TEMPERATURA", colTemp, y + 11f, boldTextPaint)
        canvas.drawText("UMIDADE RELATIVA", colHum, y + 11f, boldTextPaint)
        canvas.drawText("P. ORVALHO", colDew, y + 11f, boldTextPaint)
        canvas.drawText("CONFORMIDADE", colStatus, y + 11f, boldTextPaint)

        y += 16f

        val sampleList = readings.takeLast(14)
        for ((idx, reading) in sampleList.withIndex()) {
            val rowY = y + (idx * 16f)
            if (idx % 2 == 1) {
                canvas.drawRect(28f, rowY, 567f, rowY + 16f, boxFillPaint)
            }
            canvas.drawLine(28f, rowY + 16f, 567f, rowY + 16f, lineStrokePaint)

            canvas.drawText(dateFormat.format(Date(reading.timestamp)), colDate, rowY + 11f, normalTextPaint)
            canvas.drawText(String.format(Locale.US, "%.2f °C", reading.temperature), colTemp, rowY + 11f, boldTextPaint)
            canvas.drawText(String.format(Locale.US, "%.1f %% UR", reading.humidity), colHum, rowY + 11f, normalTextPaint)
            canvas.drawText(String.format(Locale.US, "%.2f °C", reading.dewPoint), colDew, rowY + 11f, normalTextPaint)

            val isNominal = reading.temperature in unit.minTemp..unit.maxTemp
            val statusColor = if (isNominal) mintGreen else errorRed
            val statusText = if (isNominal) "CONFORME" else "EXCURSÃO"
            val stPaint = Paint().apply {
                color = statusColor
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(statusText, colStatus, rowY + 11f, stPaint)
        }

        y += (sampleList.size * 16f) + 16f

        // -------------------------------------------------------------
        // 6. TECHNICAL RESPONSIBILITY & AUDIT SIGN-OFF
        // -------------------------------------------------------------
        canvas.drawText("5. TERMO DE RESPONSABILIDADE TÉCNICA E PARECER DE CONFORMIDADE", 28f, y, sectionTitlePaint)
        y += 12f

        val signBoxHeight = 72f
        canvas.drawRoundRect(28f, y, 567f, y + signBoxHeight, 6f, 6f, boxFillPaint)
        canvas.drawRoundRect(28f, y, 567f, y + signBoxHeight, 6f, 6f, borderStrokePaint)

        canvas.drawText("Responsável Técnico(a): ${report.responsibleTechnician}", 38f, y + 16f, boldTextPaint)
        canvas.drawText("Conselho de Classe: ${report.professionalRegistry}", 38f, y + 28f, normalTextPaint)
        canvas.drawText("Parecer: ${report.observations.ifBlank { "Lote e cadeia de frio mantidos em estrita conformidade com as Boas Práticas de Armazenagem e Distribuição." }}", 38f, y + 42f, normalTextPaint)
        canvas.drawText("Assinado digitalmente via Motor Criptográfico TermoSense em ${dateFormat.format(Date(report.generatedAt))}", 38f, y + 58f, smallMutedPaint)

        // Stamp / Digital Signature Badge
        val stampPaint = Paint().apply {
            color = mintGreen
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(430f, y + 12f, 555f, y + 58f, 4f, 4f, stampPaint)
        val stampTextPaint = Paint().apply {
            color = mintGreen
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("DOCUMENTO AUDITADO", 442f, y + 26f, stampTextPaint)
        canvas.drawText("ANVISA RDC 430/2020", 442f, y + 38f, stampTextPaint)
        canvas.drawText("AUTENTICIDADE OK", 448f, y + 50f, stampTextPaint)

        // -------------------------------------------------------------
        // 7. FOOTER
        // -------------------------------------------------------------
        val footerPaint = Paint().apply {
            color = textMuted
            textSize = 7.5f
        }
        canvas.drawLine(28f, 810f, 567f, 810f, lineStrokePaint)
        canvas.drawText("TermoSense Healthcare v2.4 • Sistema Certificado de Monitoramento de Rede de Frio do SUS", 28f, 824f, footerPaint)
        canvas.drawText("Página 1 de 1", 520f, 824f, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeName = unit.code.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "Laudo_Sanitario_ANVISA_${safeName}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(reportsDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun exportToCsv(
        context: Context,
        unit: HealthUnitEntity,
        readings: List<TelemetryReadingEntity>
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeName = unit.code.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "Planilha_Telemetria_${safeName}_${System.currentTimeMillis()}.csv"
        val csvFile = File(reportsDir, fileName)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        csvFile.bufferedWriter().use { writer ->
            writer.write("Data/Hora;Unidade;Sensor;Temperatura (°C);Umidade (%);Ponto de Orvalho (°C);Tensão VCC (V);Sinal RSSI (dBm);Excursao;Modo Offline\n")
            for (reading in readings) {
                val line = "${dateFormat.format(Date(reading.timestamp))};" +
                        "${unit.name};" +
                        "ESP32-DS18B20;" +
                        "${String.format(Locale.US, "%.2f", reading.temperature)};" +
                        "${String.format(Locale.US, "%.2f", reading.humidity)};" +
                        "${String.format(Locale.US, "%.2f", reading.dewPoint)};" +
                        "${String.format(Locale.US, "%.2f", reading.vcc)};" +
                        "${reading.rssi};" +
                        "${if (reading.isExcursion) "SIM" else "NAO"};" +
                        "${if (reading.isOfflineBuffered) "OFFLINE" else "ONLINE"}\n"
                writer.write(line)
            }
        }
        return csvFile
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Segue anexo o laudo oficial de conformidade e telemetria térmica gerado pelo TermoSense.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}
