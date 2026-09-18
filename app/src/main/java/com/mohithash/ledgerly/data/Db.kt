package com.mohithash.ledgerly.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** yyyy-MM-dd */
    val date: String,
    val merchant: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    /** JSON list of LineItem, may be empty. */
    val items: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "budgets")
data class Budget(@PrimaryKey val category: String, val limit: Double)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, createdAt DESC") fun month(monthPrefix: String): Flow<List<Expense>>
    @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%'") suspend fun monthList(monthPrefix: String): List<Expense>
    @Query("SELECT DISTINCT substr(date,1,7) AS m FROM expenses ORDER BY m DESC") fun months(): Flow<List<String>>
    @Insert suspend fun insert(e: Expense): Long
    @Update suspend fun update(e: Expense)
    @Query("DELETE FROM expenses WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets") fun all(): Flow<List<Budget>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: Budget)
    @Query("DELETE FROM budgets WHERE category = :c") suspend fun delete(c: String)
}

@Database(entities = [Expense::class, Budget::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun expenses(): ExpenseDao; abstract fun budgets(): BudgetDao }
