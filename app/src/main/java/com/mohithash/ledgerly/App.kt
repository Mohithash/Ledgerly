package com.mohithash.ledgerly

import android.app.Application
import androidx.room.Room
import com.mohithash.ledgerly.ai.AiClient
import com.mohithash.ledgerly.ai.LedgerAi
import com.mohithash.ledgerly.data.AppDb
import com.mohithash.ledgerly.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val ledger by lazy { LedgerAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "ledgerly.db").build()
        store = JsonStore(this)
    }
}
