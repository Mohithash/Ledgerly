package com.mohithash.ledgerly.ui

import java.util.Locale

fun Double.money(cur: String): String = if (this >= 1000) String.format(Locale.US, "%s%,.0f", cur, this) else String.format(Locale.US, "%s%.2f", cur, this)
fun Double.money0(cur: String): String = String.format(Locale.US, "%s%,.0f", cur, this)
