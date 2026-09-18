@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.domain.CATEGORIES
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.Label
import com.mohithash.ledgerly.ui.StatCard
import com.mohithash.ledgerly.ui.money0

@Composable
fun BudgetsScreen(vm: AppViewModel) {
    val budgets by vm.budgets.collectAsState()
    val list by vm.expenses.collectAsState()
    val s by vm.settings.collectAsState()
    val cs = MaterialTheme.colorScheme
    val spentBy = list.groupBy { it.category }.mapValues { (_, l) -> l.sumOf { it.amount } }
    Scaffold(topBar = { TopAppBar(title = { Text("Category budgets") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Set a monthly limit per category. Leave blank for no limit.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant) }
            items(CATEGORIES) { c ->
                val limit = budgets.firstOrNull { it.category == c }?.limit
                val spent = spentBy[c] ?: 0.0
                var text by remember(limit) { mutableStateOf(limit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
                val col = colorFor(c, CATEGORIES)
                StatCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(c, style = MaterialTheme.typography.titleMedium)
                            Text(if (limit == null) "${spent.money0(s.currency)} spent" else "${spent.money0(s.currency)} of ${limit.money0(s.currency)}" + if (spent > limit) " · over" else "",
                                style = MaterialTheme.typography.bodySmall, color = if (limit != null && spent > limit) cs.error else cs.onSurfaceVariant)
                        }
                        OutlinedTextField(text, { text = it.filter { ch -> ch.isDigit() || ch == '.' }; vm.setBudget(c, text.toDoubleOrNull() ?: 0.0) }, prefix = { Text(s.currency) }, singleLine = true,
                            modifier = Modifier.width(120.dp), shape = MaterialTheme.shapes.large, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), placeholder = { Text("limit") })
                    }
                    if (limit != null) LinearWavyProgressIndicator(progress = { (spent / limit).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = if (spent > limit) cs.error else col)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
