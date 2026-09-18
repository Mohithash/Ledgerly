@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.HeroCard
import com.mohithash.ledgerly.ui.KeyValue
import com.mohithash.ledgerly.ui.Label
import com.mohithash.ledgerly.ui.StatCard
import com.mohithash.ledgerly.ui.TrendChart
import com.mohithash.ledgerly.ui.animatedProgress
import com.mohithash.ledgerly.ui.money
import com.mohithash.ledgerly.ui.money0
import com.mohithash.ledgerly.ui.theme.Brand
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val CATEGORY_COLORS = listOf(Color(0xFF00897B), Color(0xFFF4B400), Color(0xFF6A4CB0), Color(0xFFE64A19), Color(0xFF1E88E5), Color(0xFFD81B60), Color(0xFF43A047), Color(0xFF8D6E63), Color(0xFF00ACC1), Color(0xFF757575))
fun colorFor(category: String, all: List<String>): Color = CATEGORY_COLORS[(all.indexOf(category).takeIf { it >= 0 } ?: 9) % CATEGORY_COLORS.size]

@Composable
fun Donut(slices: List<Pair<String, Double>>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.size(120.dp)) {
        val stroke = Stroke(width = 22f)
        val rect = Size(size.width - 22f, size.height - 22f); val off = Offset(11f, 11f)
        drawArc(track, 0f, 360f, false, off, rect, style = stroke)
        var start = -90f
        slices.forEachIndexed { i, (_, v) ->
            val sweep = (v / total * 360).toFloat()
            drawArc(colors[i], start, sweep - 2f, false, off, rect, style = stroke)
            start += sweep
        }
    }
}

@Composable
fun HomeScreen(vm: AppViewModel, onAdd: () -> Unit, onSettings: () -> Unit) {
    val s by vm.settings.collectAsState()
    val month by vm.month.collectAsState()
    val list by vm.expenses.collectAsState()
    val cs = MaterialTheme.colorScheme
    val cur = s.currency
    val spent = list.sumOf { it.amount }
    val ym = YearMonth.parse(month)
    val isCurrent = ym == YearMonth.now()
    val daysIn = ym.lengthOfMonth(); val dayOf = if (isCurrent) LocalDate.now().dayOfMonth else daysIn
    val byCat = list.groupBy { it.category }.map { (c, l) -> c to l.sumOf { it.amount } }.sortedByDescending { it.second }
    val catNames = byCat.map { it.first }
    val daily = (1..daysIn).map { d -> list.filter { it.date.endsWith("-%02d".format(d)) }.sumOf { it.amount } }
    val cumulative = daily.runningFold(0.0) { a, b -> a + b }.drop(1).take(dayOf).map { it.toFloat() }
    val left = s.monthlyBudget - spent
    val pace = if (dayOf > 0) spent / dayOf * daysIn else 0.0

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ vm.shiftMonth(-1) }) { Icon(Icons.Default.ChevronLeft, null) }
                    Text(ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.titleLarge)
                    IconButton({ vm.shiftMonth(1) }, enabled = !isCurrent) { Icon(Icons.Default.ChevronRight, null) }
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } })
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add expense") }, containerColor = cs.primary, contentColor = cs.onPrimary) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) {
                val on = cs.onPrimary
                Label(if (isCurrent) "Spent so far" else "Spent", on.copy(alpha = 0.8f))
                Text(spent.money(cur), style = MaterialTheme.typography.displayMedium, color = on)
                Text(if (left >= 0) "${left.money0(cur)} left of ${s.monthlyBudget.money0(cur)}" else "${(-left).money0(cur)} over budget", color = if (left >= 0) on.copy(alpha = 0.85f) else cs.errorContainer, style = MaterialTheme.typography.bodyLarge)
                val prog = animatedProgress((spent / s.monthlyBudget).toFloat())
                LinearWavyProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = if (left >= 0) cs.secondary else cs.errorContainer, trackColor = on.copy(alpha = 0.18f))
                if (isCurrent) Text("Day $dayOf of $daysIn · on pace for ${pace.money0(cur)}", style = MaterialTheme.typography.labelLarge, color = on.copy(alpha = 0.8f))
            }
            StatCard {
                Label("By category")
                if (byCat.isEmpty()) Text("No expenses this month.", color = cs.onSurfaceVariant)
                else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Donut(byCat, byCat.map { colorFor(it.first, catNames) })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        byCat.take(5).forEach { (c, v) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(10.dp).background(colorFor(c, catNames), CircleShape))
                                Text(c, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text(v.money(cur), style = MaterialTheme.typography.bodyMedium)
                                Text("${(v / spent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            StatCard {
                Label("Cumulative spend")
                if (cumulative.size >= 2) TrendChart(cumulative, Modifier.padding(top = 8.dp), target = s.monthlyBudget.toFloat())
                else Text("Chart appears after a couple of days of data.", color = cs.onSurfaceVariant)
                Text("Dashed line = monthly budget", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            StatCard {
                Label("This month")
                KeyValue("Transactions", "${list.size}")
                KeyValue("Average per day", (if (dayOf > 0) spent / dayOf else 0.0).money(cur))
                list.maxByOrNull { it.amount }?.let { KeyValue("Largest", "${it.merchant} · ${it.amount.money(cur)}") }
            }
            Spacer(Modifier.height(88.dp))
        }
    }
}
