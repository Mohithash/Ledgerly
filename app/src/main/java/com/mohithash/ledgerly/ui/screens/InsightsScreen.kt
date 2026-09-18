@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.EmptyState
import com.mohithash.ledgerly.ui.HeroCard
import com.mohithash.ledgerly.ui.Job
import com.mohithash.ledgerly.ui.Label
import com.mohithash.ledgerly.ui.ShapeIcon
import com.mohithash.ledgerly.ui.StatCard
import com.mohithash.ledgerly.ui.theme.Brand

@Composable
fun InsightsScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val ins by vm.insight.collectAsState()
    val job by vm.insightJob.collectAsState()
    val ai by vm.ai.collectAsState()
    val month by vm.month.collectAsState()
    val cs = MaterialTheme.colorScheme
    Scaffold(topBar = { TopAppBar(title = { Text("Insights") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({ vm.generateInsight() }, enabled = ai.configured && job != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (job == Job.Loading) { LoadingIndicator(Modifier.size(20.dp)); Spacer(Modifier.size(8.dp)); Text("Crunching $month…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Analyse $month") }
            }
            (job as? Job.Failed)?.let { StatCard(container = cs.errorContainer) { Text(it.message, color = cs.onErrorContainer) } }
            if (ins.generatedAt == 0L) EmptyState(Icons.Default.Insights, "No analysis yet", "Get a plain‑spoken read on where the money went, what changed, and three things to try.")
            else {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) { Label(ins.month, cs.onPrimary.copy(alpha = 0.8f)); Text(ins.headline, style = MaterialTheme.typography.headlineSmall, color = cs.onPrimary) }
                Section(Icons.Default.SwapVert, "Biggest shift", listOf(ins.biggest_shift), cs.tertiaryContainer, cs.onTertiaryContainer)
                Section(Icons.Default.Warning, "Worth a look", ins.anomalies, cs.secondaryContainer, cs.onSecondaryContainer)
                Section(Icons.Default.Lightbulb, "Try this", ins.tips, cs.primaryContainer, cs.onPrimaryContainer)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, items: List<String>, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    if (items.all { it.isBlank() }) return
    StatCard(container = bg) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { ShapeIcon(icon, fg.copy(alpha = 0.12f), fg, MaterialShapes.Sunny); Text(title, style = MaterialTheme.typography.titleMedium, color = fg) }
        items.filter { it.isNotBlank() }.forEach { Text("•  $it", style = MaterialTheme.typography.bodyLarge, color = fg, modifier = Modifier.padding(start = 4.dp, top = 2.dp)) }
    }
}
