package br.com.termosense.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.termosense.R
import br.com.termosense.ui.theme.BackgroundDark
import br.com.termosense.ui.theme.NeonCyanGlow
import br.com.termosense.ui.theme.PrimaryCyan
import br.com.termosense.ui.theme.SecondaryAmber
import br.com.termosense.ui.theme.SurfaceContainer
import br.com.termosense.ui.theme.TertiaryGreen
import br.com.termosense.ui.theme.TextOnSurface
import br.com.termosense.ui.theme.TextOnSurfaceVariant
import kotlinx.coroutines.delay

data class StartupStep(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val color: Color
)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var progressVal by remember { mutableStateOf(0.1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashAura")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progressVal,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "animatedProgress"
    )

    val startupSteps = listOf(
        StartupStep(
            icon = Icons.Default.Memory,
            title = "Hardware ESP32 IoT",
            detail = "Inicializando drivers BLE GATT & Wi-Fi HTTP...",
            color = PrimaryCyan
        ),
        StartupStep(
            icon = Icons.Default.Storage,
            title = "Persistência Local Room",
            detail = "Verificando buffer e cache offline SQLite...",
            color = TertiaryGreen
        ),
        StartupStep(
            icon = Icons.Default.CloudDone,
            title = "Firebase Cloud Messaging",
            detail = "Conectando ao serviço de alertas push FCM...",
            color = Color(0xFF38BDF8)
        ),
        StartupStep(
            icon = Icons.Default.HealthAndSafety,
            title = "Qualificação ANVISA RDC 430",
            detail = "Validando limiares térmicos da Rede de Frio SUS...",
            color = SecondaryAmber
        )
    )

    LaunchedEffect(Unit) {
        // Logo Entrance
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(1f, animationSpec = tween(400))

        // Step 1
        progressVal = 0.3f
        delay(400)
        currentStepIndex = 1

        // Step 2
        progressVal = 0.6f
        delay(400)
        currentStepIndex = 2

        // Step 3
        progressVal = 0.85f
        delay(400)
        currentStepIndex = 3

        // Step 4 Complete
        progressVal = 1.0f
        delay(450)

        // Exit transition
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070E1E),
                        BackgroundDark,
                        Color(0xFF030712)
                    )
                )
            )
            .clickable { onSplashFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Animated Ambient Neon Halo Background
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale.value)
                .background(NeonCyanGlow.copy(alpha = pulseGlow), CircleShape)
                .blur(72.dp)
        )

        // Geometric Radar Canvas Rings
        Canvas(modifier = Modifier.size(260.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            drawCircle(
                color = PrimaryCyan.copy(alpha = 0.12f),
                radius = radius,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PrimaryCyan.copy(alpha = 0.08f),
                radius = radius * 0.72f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PrimaryCyan.copy(alpha = 0.05f),
                radius = radius * 0.45f,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Rotating Orbital Ring + Shield Logo Container
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Orbital Dotted Ring
                Canvas(
                    modifier = Modifier
                        .size(126.dp)
                        .rotate(orbitRotation)
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                PrimaryCyan,
                                Color.Transparent,
                                TertiaryGreen,
                                Color.Transparent,
                                PrimaryCyan
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 300f,
                        useCenter = false,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Logo Card
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = SurfaceContainer.copy(alpha = 0.92f),
                    border = BorderStroke(
                        1.5.dp,
                        PrimaryCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(92.dp),
                    shadowElevation = 12.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "TermoSense Logo",
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name & Subtitle
            Text(
                text = "TERMOSENSE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = TextOnSurface
            )

            Text(
                text = "REDE DE FRIO & TELEMETRIA SANITÁRIA",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryCyan,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Diagnostic Step Box
            val currentStep = startupSteps.getOrElse(currentStepIndex) { startupSteps.last() }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceContainer.copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    currentStep.color.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(currentStep.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentStep.icon,
                            contentDescription = null,
                            tint = currentStep.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentStep.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = currentStep.color
                        )
                        Text(
                            text = currentStep.detail,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextOnSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "OK",
                        tint = TertiaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar & Percentage
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = PrimaryCyan,
                    trackColor = SurfaceContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANVISA RDC nº 430/2020 • PNI SUS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextOnSurfaceVariant
                    )

                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Toque em qualquer lugar para avançar",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextOnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
