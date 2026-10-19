package br.com.termosense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val index: Int
) {
    data object AoVivo : Screen("ao_vivo", "Ao Vivo", Icons.Default.Thermostat, 0)
    data object Graficos : Screen("graficos", "Gráficos", Icons.Default.ShowChart, 1)
    data object Relatorios : Screen("relatorios", "Relatórios", Icons.Default.Assessment, 2)
    data object Configuracoes : Screen("configuracoes", "Configurações", Icons.Default.Tune, 3)
}
