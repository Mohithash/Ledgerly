@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.domain.Settings
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.Label
import com.mohithash.ledgerly.ui.StatCard

@Composable
fun SettingsForm(initial: Settings, onChange: (Settings) -> Unit) {
    var cur by remember { mutableStateOf(initial.currency) }
    var budget by remember { mutableStateOf(if (initial.monthlyBudget % 1.0 == 0.0) initial.monthlyBudget.toInt().toString() else initial.monthlyBudget.toString()) }
    fun emit() = onChange(initial.copy(currency = cur, monthlyBudget = budget.toDoubleOrNull() ?: initial.monthlyBudget))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Label("Currency")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("$", "€", "£", "₹", "¥", "₩", "R$", "CHF ", "A$", "C$").forEach { c -> FilterChip(selected = cur == c, onClick = { cur = c; emit() }, label = { Text(c.trim()) }) } }
        OutlinedTextField(cur, { cur = it; emit() }, label = { Text("Or type a symbol / code") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        OutlinedTextField(budget, { budget = it.filter { ch -> ch.isDigit() || ch == '.' }; emit() }, label = { Text("Monthly budget") }, prefix = { Text(cur) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Settings()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Know where it goes") }, subtitle = { Text("Snap receipts, type one‑liners, set budgets, get a monthly read.") }, scrollBehavior = scroll) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { SettingsForm(Settings()) { draft = it } }
            Text("Everything stays on your phone. Receipts are sent to your own AI provider only when you tap Extract.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ vm.saveSettings(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Start tracking", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
