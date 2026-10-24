package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KopikiranDao
import com.example.data.local.KopikiranDatabase
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var db: KopikiranDatabase
    private lateinit var dao: KopikiranDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KopikiranDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.kopikiranDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testProductAndInventoryIngredientFlow() = runBlocking {
        // Test Product Entity
        val product = ProductItem(
            name = "Kopi Susu Bekasi Silent",
            category = "Kopi",
            price = 20000.0,
            costPrice = 8000.0,
            stock = 50,
            minStockAlert = 10
        )
        val productId = dao.insertProduct(product)

        val retrievedProduct = dao.getProductById(productId)
        assertNotNull(retrievedProduct)
        assertEquals("Kopi Susu Bekasi Silent", retrievedProduct?.name)
        assertEquals(50, retrievedProduct?.stock)

        // Adjust stock
        dao.adjustProductStock(productId, -5)
        val updatedProduct = dao.getProductById(productId)
        assertEquals(45, updatedProduct?.stock)

        // Test Raw Ingredient Entity
        val rawIngredient = RawIngredient(
            name = "Biji Kopi Arabika Silent",
            category = "Bahan Baku",
            currentStock = 5000.0,
            minStock = 1000.0,
            unit = "gram",
            costPerUnit = 250.0
        )
        val rawId = dao.insertRawIngredient(rawIngredient)
        val retrievedRaw = dao.getRawIngredientById(rawId)
        assertNotNull(retrievedRaw)
        assertEquals("Biji Kopi Arabika Silent", retrievedRaw?.name)
        assertEquals(5000.0, retrievedRaw?.currentStock ?: 0.0, 0.01)
    }

    @Test
    fun testTransactionHistoryWithItemsRelation() = runBlocking {
        // 1. Insert Transaction
        val tx = TransactionEntity(
            invoiceNumber = "KPK-20261024-TEST",
            customerName = "Dimas Surya",
            tableOrOrderType = "Dine In",
            paymentMethod = "QRIS",
            subtotal = 40000.0,
            discount = 0.0,
            tax = 0.0,
            total = 40000.0,
            amountPaid = 40000.0,
            changeAmount = 0.0,
            status = "COMPLETED"
        )
        val txId = dao.insertTransaction(tx)

        // 2. Insert Transaction Items
        val item1 = TransactionItemEntity(
            transactionId = txId,
            productId = 1L,
            productName = "Kopi Susu Bekasi Silent",
            quantity = 2,
            pricePerUnit = 20000.0,
            costPerUnit = 8000.0,
            subtotal = 40000.0,
            notes = "Less sugar"
        )
        dao.insertTransactionItems(listOf(item1))

        // 3. Query Transaction with Items Relation
        val txWithItems = dao.getTransactionWithItemsById(txId)
        assertNotNull(txWithItems)
        assertEquals("KPK-20261024-TEST", txWithItems?.transaction?.invoiceNumber)
        assertEquals(1, txWithItems?.items?.size)
        assertEquals("Kopi Susu Bekasi Silent", txWithItems?.items?.first()?.productName)
        assertEquals(2, txWithItems?.items?.first()?.quantity)
    }

    @Test
    fun testBookkeepingExpensesAndInventoryLogs() = runBlocking {
        // Test Expense Entity
        val expense = ExpenseEntity(
            title = "Beli Susu Fresh Milk 10L",
            category = "Bahan Baku",
            amount = 180000.0,
            paymentMethod = "CASH",
            notes = "Greenfields Pasteurisasi"
        )
        dao.insertExpense(expense)

        val allExpenses = dao.getAllExpenses().first()
        assertEquals(1, allExpenses.size)
        assertEquals("Beli Susu Fresh Milk 10L", allExpenses[0].title)
        assertEquals(180000.0, allExpenses[0].amount, 0.01)

        // Test Inventory Log Entity
        val log = InventoryLogEntity(
            itemName = "Kopi Susu Bekasi Silent",
            itemType = "PRODUK",
            changeAmount = -2.0,
            previousStock = 50.0,
            newStock = 48.0,
            reason = "Penjualan POS",
            notes = "KPK-20261024-001"
        )
        dao.insertInventoryLog(log)

        val logs = dao.getRecentInventoryLogs().first()
        assertEquals(1, logs.size)
        assertEquals("Penjualan POS", logs[0].reason)
        assertEquals(48.0, logs[0].newStock, 0.01)
    }
}
