package com.mohithash.ledgerly.domain

import kotlinx.serialization.Serializable

val CATEGORIES = listOf("Groceries", "Dining", "Transport", "Shopping", "Bills", "Health", "Entertainment", "Travel", "Education", "Other")

@Serializable
data class Settings(
    val currency: String = "$",
    val monthlyBudget: Double = 2000.0,
    val onboarded: Boolean = false,
)

@Serializable data class LineItem(val name: String, val price: Double = 0.0)

/** What the AI extracts from a receipt photo or a typed line. */
@Serializable
data class ParsedExpense(
    val merchant: String = "",
    val date: String = "",
    val total: Double = 0.0,
    val category: String = "Other",
    val items: List<LineItem> = emptyList(),
    val note: String = "",
)

@Serializable
data class MonthInsight(
    val headline: String = "",
    val biggest_shift: String = "",
    val anomalies: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val month: String = "",
    val generatedAt: Long = 0,
)
