package com.mohithash.ledgerly.ai

import com.mohithash.ledgerly.data.Expense
import com.mohithash.ledgerly.domain.CATEGORIES
import com.mohithash.ledgerly.domain.MonthInsight
import com.mohithash.ledgerly.domain.ParsedExpense
import com.mohithash.ledgerly.domain.Settings
import java.time.LocalDate

class LedgerAi(private val client: AiClient) {
    private val parseSchema = Schema.obj(
        "merchant" to Schema.str, "date" to Schema.str, "total" to Schema.num, "category" to Schema.enum(*CATEGORIES.toTypedArray()),
        "items" to Schema.arr(Schema.obj("name" to Schema.str, "price" to Schema.num)), "note" to Schema.str,
    )
    private val insightSchema = Schema.obj("headline" to Schema.str, "biggest_shift" to Schema.str, "anomalies" to Schema.arr(Schema.str), "tips" to Schema.arr(Schema.str))

    suspend fun parse(ai: AiSettings, s: Settings, image: String?, text: String): ParsedExpense {
        val system = """You extract expenses. Today is ${LocalDate.now()}. Currency symbol: ${s.currency}.
            |From a receipt photo or a short typed description, return merchant, date (yyyy-MM-dd; today if unknown), the grand total actually paid,
            |a category from the allowed list, up to 12 line items with prices, and a short note only if something is ambiguous.
            |If the input says something like "coffee 4.5", merchant is "Coffee", total 4.5, category Dining.""".trimMargin()
        return client.ask(ai, system, text.ifBlank { "Extract this receipt." }, parseSchema, image, 3000)
    }

    suspend fun insight(ai: AiSettings, s: Settings, month: String, expenses: List<Expense>, previous: List<Expense>): MonthInsight {
        val system = """You are a plain-spoken personal finance coach. Currency: ${s.currency}. Monthly budget: ${s.monthlyBudget}.
            |headline: one sentence on the month. biggest_shift: the category that changed most vs last month, with numbers.
            |anomalies: 1-3 unusual transactions or patterns (name the merchant/amount). tips: 3 specific, realistic savings actions based on THIS data, each under 20 words.""".trimMargin()
        fun dump(l: List<Expense>) = l.groupBy { it.category }.entries.sortedByDescending { e -> e.value.sumOf { it.amount } }
            .joinToString("\n") { (c, l) -> "$c: ${"%.2f".format(l.sumOf { it.amount })} (${l.size} tx) — " + l.sortedByDescending { it.amount }.take(4).joinToString { "${it.merchant} ${"%.2f".format(it.amount)}" } }
        val user = "Month $month total ${"%.2f".format(expenses.sumOf { it.amount })}:\n${dump(expenses)}\n\nPrevious month total ${"%.2f".format(previous.sumOf { it.amount })}:\n${dump(previous).ifBlank { "(no data)" }}"
        val m: MonthInsight = client.ask(ai, system, user, insightSchema, maxTokens = 2000)
        return m.copy(month = month, generatedAt = System.currentTimeMillis())
    }
}
