@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.domain.CATEGORIES
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.EmptyState
import com.mohithash.ledgerly.ui.ShapeIcon
import com.mohithash.ledgerly.ui.money
import com.mohithash.ledgerly.ui.prettyDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(vm: AppViewModel, onAdd: () -> Unit) {
    val list by vm.expenses.collectAsState()
    val s by vm.settings.collectAsState()
    val month by vm.month.collectAsState()
    val cs = MaterialTheme.colorScheme
    var q by remember { mutableStateOf("") }
    val shown = list.filter { q.isBlank() || it.merchant.contains(q, true) || it.category.contains(q, true) || it.note.contains(q, true) }
    Scaffold(
        topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { IconButton({ vm.shiftMonth(-1) }) { Icon(Icons.Default.ChevronLeft, null) }; Text(YearMonth.parse(month).format(DateTimeFormatter.ofPattern("MMM yyyy"))); IconButton({ vm.shiftMonth(1) }) { Icon(Icons.Default.ChevronRight, null) } } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) },
        floatingActionButton = { FloatingActionButton(onAdd, containerColor = cs.primary, contentColor = cs.onPrimary) { Icon(Icons.Default.Add, null) } },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(q, { q = it }, placeholder = { Text("Search merchant, category, note") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) }
            if (shown.isEmpty()) item { EmptyState(Icons.Default.ReceiptLong, "Nothing here", "Add an expense with the + button.") }
            shown.groupBy { it.date }.forEach { (d, txs) ->
                item(key = "h$d") { Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(d.prettyDate(), style = MaterialTheme.typography.labelLarge, color = cs.primary); Text(txs.sumOf { it.amount }.money(s.currency), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant) } }
                items(txs, key = { it.id }) { e ->
                    val col = colorFor(e.category, CATEGORIES)
                    ListItem(
                        leadingContent = { ShapeIcon(Icons.Default.ReceiptLong, col.copy(alpha = 0.18f), col, MaterialShapes.Cookie6Sided) },
                        headlineContent = { Text(e.merchant) },
                        supportingContent = { Text(listOf(e.category, e.note, vm.items(e).takeIf { it.isNotEmpty() }?.let { "${it.size} items" } ?: "").filter { it.isNotBlank() }.joinToString(" · ")) },
                        trailingContent = { Row(verticalAlignment = Alignment.CenterVertically) { Text(e.amount.money(s.currency), style = MaterialTheme.typography.titleMedium); IconButton({ vm.delete(e.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) } } },
                        colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow), modifier = Modifier.clip(MaterialTheme.shapes.large),
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
