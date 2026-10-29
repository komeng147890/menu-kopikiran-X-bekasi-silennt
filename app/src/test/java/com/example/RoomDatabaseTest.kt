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
        // Test Product Entity matching menu photo
        val product = ProductItem(
            name = "Good Day Freeze",
            category = "Minuman",
            price = 10000.0,
            costPrice = 4000.0,
            stock = 50,
            minStockAlert = 10
        )
        val productId = dao.insertProduct(product)

        val retrievedProduct = dao.getProductById(productId)
        assertNotNull(retrievedProduct)
        assertEquals("Good Day Freeze", retrievedProduct?.name)
        assertEquals(50, retrievedProduct?.stock)

        // Adjust stock
        dao.adjustProductStock(productId, -5)
        val updatedProduct = dao.getProductById(productId)
        assertEquals(45, updatedProduct?.stock)

        // Test Raw Ingredient Entity
        val rawIngredient = RawIngredient(
            name = "Good Day Freeze (Sachet)",
            category = "Bahan Baku",
            currentStock = 100.0,
            minStock = 20.0,
            unit = "pcs",
            costPerUnit = 2500.0
        )
        val rawId = dao.insertRawIngredient(rawIngredient)
        val retrievedRaw = dao.getRawIngredientById(rawId)
        assertNotNull(retrievedRaw)
        assertEquals("Good Day Freeze (Sachet)", retrievedRaw?.name)
        assertEquals(100.0, retrievedRaw?.currentStock ?: 0.0, 0.01)
    }

    @Test
    fun testDeleteObsoleteProductsRetention() = runBlocking {
        // Insert official and old obsolete product
        val validProduct = ProductItem(name = "Es Teh", category = "Minuman", price = 7000.0, costPrice = 2000.0, stock = 60)
        val oldObsoleteProduct = ProductItem(name = "Kopi Susu Bekasi Silent", category = "Kopi", price = 20000.0, costPrice = 8000.0, stock = 20)
        dao.insertProduct(validProduct)
        dao.insertProduct(oldObsoleteProduct)

        val allowedList = listOf(
            "Es Teh", "Good Day Freeze", "Good Day Cappuccino", "Good Day Coklat Panas",
            "Good Day Merah Panas", "Roti Bakar", "French Fries"
        )
        dao.deleteProductsNotIn(allowedList)

        val allProducts = dao.getAllProducts().first()
        assertEquals(1, allProducts.size)
        assertEquals("Es Teh", allProducts[0].name)
    }

    @Test
    fun testTransactionHistoryWithItemsRelation() = runBlocking {
        // 1. Insert Transaction
        val tx = TransactionEntity(
            invoiceNumber = "KPK-20261024-TEST",
            customerName = "Dimas Surya",
            tableOrOrderType = "Dine In",
            paymentMethod = "QRIS",
            subtotal = 20000.0,
            discount = 0.0,
            tax = 0.0,
            total = 20000.0,
            amountPaid = 20000.0,
            changeAmount = 0.0,
            status = "COMPLETED"
        )
        val txId = dao.insertTransaction(tx)

        // 2. Insert Transaction Items
        val item1 = TransactionItemEntity(
            transactionId = txId,
            productId = 1L,
            productName = "Good Day Freeze",
            quantity = 2,
            pricePerUnit = 10000.0,
            costPerUnit = 4000.0,
            subtotal = 20000.0,
            notes = "Dingin"
        )
        dao.insertTransactionItems(listOf(item1))

        // 3. Query Transaction with Items Relation
        val txWithItems = dao.getTransactionWithItemsById(txId)
        assertNotNull(txWithItems)
        assertEquals("KPK-20261024-TEST", txWithItems?.transaction?.invoiceNumber)
        assertEquals(1, txWithItems?.items?.size)
        assertEquals("Good Day Freeze", txWithItems?.items?.first()?.productName)
        assertEquals(2, txWithItems?.items?.first()?.quantity)
    }

    @Test
    fun testBookkeepingExpensesAndInventoryLogs() = runBlocking {
        // Test Expense Entity
        val expense = ExpenseEntity(
            title = "Beli Good Day Kopi & Coklat Grosir",
            category = "Bahan Baku",
            amount = 320000.0,
            paymentMethod = "CASH",
            notes = "20 renceng varian Good Day"
        )
        dao.insertExpense(expense)

        val allExpenses = dao.getAllExpenses().first()
        assertEquals(1, allExpenses.size)
        assertEquals("Beli Good Day Kopi & Coklat Grosir", allExpenses[0].title)
        assertEquals(320000.0, allExpenses[0].amount, 0.01)

        // Test Inventory Log Entity
        val log = InventoryLogEntity(
            itemName = "Roti Bakar",
            itemType = "PRODUK",
            changeAmount = -1.0,
            previousStock = 35.0,
            newStock = 34.0,
            reason = "Penjualan POS",
            notes = "KPK-20261024-001"
        )
        dao.insertInventoryLog(log)

        val logs = dao.getRecentInventoryLogs().first()
        assertEquals(1, logs.size)
        assertEquals("Penjualan POS", logs[0].reason)
        assertEquals(34.0, logs[0].newStock, 0.01)
    }
}
