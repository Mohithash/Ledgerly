@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.ledgerly.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.ledgerly.ui.screens.AddScreen
import com.mohithash.ledgerly.ui.screens.BudgetsScreen
import com.mohithash.ledgerly.ui.screens.HomeScreen
import com.mohithash.ledgerly.ui.screens.InsightsScreen
import com.mohithash.ledgerly.ui.screens.OnboardingScreen
import com.mohithash.ledgerly.ui.screens.SettingsScreen
import com.mohithash.ledgerly.ui.screens.TransactionsScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    HOME("home", "Overview", Icons.Outlined.PieChart, Icons.Filled.PieChart),
    TX("tx", "Transactions", Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
    BUDGETS("budgets", "Budgets", Icons.Outlined.Savings, Icons.Filled.Savings),
    INSIGHTS("insights", "Insights", Icons.Outlined.Insights, Icons.Filled.Insights),
}

@Composable
fun Nav(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    if (!s.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val current = back?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }
    Scaffold(bottomBar = {
        if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Tab.entries.forEach { tab ->
                val sel = current?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(selected = sel, onClick = { nav.navigate(tab.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(if (sel) tab.selected else tab.icon, tab.label) }, label = { Text(tab.label) })
            }
        }
    }) { pad ->
        NavHost(nav, Tab.HOME.route, Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.HOME.route) { HomeScreen(vm, onAdd = { nav.navigate("add") }, onSettings = { nav.navigate("settings") }) }
            composable(Tab.TX.route) { TransactionsScreen(vm, onAdd = { nav.navigate("add") }) }
            composable(Tab.BUDGETS.route) { BudgetsScreen(vm) }
            composable(Tab.INSIGHTS.route) { InsightsScreen(vm, onSettings = { nav.navigate("settings") }) }
            composable("add") { AddScreen(vm, onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
