package com.mohithash.ledgerly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.ledgerly.App
import com.mohithash.ledgerly.ai.AiSettings
import com.mohithash.ledgerly.data.Budget
import com.mohithash.ledgerly.data.Expense
import com.mohithash.ledgerly.domain.LineItem
import com.mohithash.ledgerly.domain.MonthInsight
import com.mohithash.ledgerly.domain.ParsedExpense
import com.mohithash.ledgerly.domain.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.YearMonth

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    val insight: StateFlow<MonthInsight> = app.store.flow("insight", MonthInsight.serializer(), MonthInsight())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val month = MutableStateFlow(YearMonth.now().toString())
    val months = db.expenses().months().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<Expense>> = month.flatMapLatest { db.expenses().month(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val budgets = db.budgets().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _parse = MutableStateFlow<Job<ParsedExpense>>(Job.Idle)
    val parse: StateFlow<Job<ParsedExpense>> = _parse
    private val _insightJob = MutableStateFlow<Job<MonthInsight>>(Job.Idle)
    val insightJob: StateFlow<Job<MonthInsight>> = _insightJob

    fun shiftMonth(delta: Int) { month.value = YearMonth.parse(month.value).plusMonths(delta.toLong()).toString() }

    fun parseExpense(image: String?, text: String) {
        _parse.value = Job.Loading
        viewModelScope.launch { _parse.value = runCatching { app.ledger.parse(ai.value, settings.value, image, text) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") }) }
    }
    fun clearParse() { _parse.value = Job.Idle }

    fun add(date: String, merchant: String, amount: Double, category: String, note: String = "", items: List<LineItem> = emptyList()) = viewModelScope.launch {
        val d = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now()).toString()
        db.expenses().insert(Expense(date = d, merchant = merchant.ifBlank { category }, amount = amount, category = category, note = note,
            items = if (items.isEmpty()) "" else json.encodeToString(ListSerializer(LineItem.serializer()), items)))
        _parse.value = Job.Idle
    }
    fun delete(id: Long) = viewModelScope.launch { db.expenses().delete(id) }
    fun items(e: Expense): List<LineItem> = e.items.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString(ListSerializer(LineItem.serializer()), it) }.getOrNull() } ?: emptyList()

    fun setBudget(category: String, limit: Double) = viewModelScope.launch { if (limit <= 0) db.budgets().delete(category) else db.budgets().upsert(Budget(category, limit)) }

    fun generateInsight() {
        _insightJob.value = Job.Loading
        viewModelScope.launch {
            val m = month.value
            val cur = db.expenses().monthList(m); val prev = db.expenses().monthList(YearMonth.parse(m).minusMonths(1).toString())
            _insightJob.value = if (cur.size < 3) Job.Failed("Log at least three expenses this month first.")
            else runCatching { app.ledger.insight(ai.value, settings.value, m, cur, prev) }.fold({ app.store.set("insight", MonthInsight.serializer(), it); Job.Done(it) }, { Job.Failed(it.message ?: "Failed") })
        }
    }
}
