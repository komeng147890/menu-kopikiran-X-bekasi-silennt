package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface KopikiranDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductItem?

    @Query("SELECT * FROM products WHERE stock <= minStockAlert")
    fun getLowStockProducts(): Flow<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductItem>)

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("DELETE FROM products WHERE name NOT IN (:allowedNames)")
    suspend fun deleteProductsNotIn(allowedNames: List<String>)

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductItem?

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("UPDATE products SET stock = stock + :changeAmount WHERE id = :productId")
    suspend fun adjustProductStock(productId: Long, changeAmount: Int)

    // --- RAW INGREDIENTS ---
    @Query("SELECT * FROM raw_ingredients ORDER BY category ASC, name ASC")
    fun getAllRawIngredients(): Flow<List<RawIngredient>>

    @Query("SELECT * FROM raw_ingredients WHERE id = :id LIMIT 1")
    suspend fun getRawIngredientById(id: Long): RawIngredient?

    @Query("SELECT * FROM raw_ingredients WHERE currentStock <= minStock")
    fun getLowStockRawIngredients(): Flow<List<RawIngredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawIngredient(ingredient: RawIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRawIngredients(ingredients: List<RawIngredient>)

    @Update
    suspend fun updateRawIngredient(ingredient: RawIngredient)

    @Delete
    suspend fun deleteRawIngredient(ingredient: RawIngredient)

    @Query("UPDATE raw_ingredients SET currentStock = currentStock + :changeAmount WHERE id = :id")
    suspend fun adjustRawIngredientStock(id: Long, changeAmount: Double)

    // --- TRANSACTIONS ---
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsWithItems(): Flow<List<TransactionWithItems>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionWithItemsById(id: Long): TransactionWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: Long, status: String)

    // --- EXPENSES (PEMBUKUAN) ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // --- INVENTORY LOGS ---
    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentInventoryLogs(): Flow<List<InventoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryLog(log: InventoryLogEntity): Long
}
