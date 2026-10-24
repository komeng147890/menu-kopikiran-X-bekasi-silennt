package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductItem::class,
        RawIngredient::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        ExpenseEntity::class,
        InventoryLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KopikiranDatabase : RoomDatabase() {

    abstract fun kopikiranDao(): KopikiranDao

    companion object {
        @Volatile
        private var INSTANCE: KopikiranDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KopikiranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KopikiranDatabase::class.java,
                    "kopikiran_bekasi_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.kopikiranDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: KopikiranDao) {
            // 1. Initial Products matching exact menu image
            val initialProducts = listOf(
                ProductItem(
                    name = "Es Teh",
                    category = "Minuman",
                    price = 7000.0,
                    costPrice = 2000.0,
                    stock = 60,
                    minStockAlert = 10,
                    description = "Es teh manis segar dingin pelepas dahaga"
                ),
                ProductItem(
                    name = "Good Day Freeze",
                    category = "Minuman",
                    price = 10000.0,
                    costPrice = 4000.0,
                    stock = 50,
                    minStockAlert = 10,
                    description = "Kopi Good Day Freeze dingin dengan es kocok segar"
                ),
                ProductItem(
                    name = "Good Day Cappuccino",
                    category = "Minuman",
                    price = 10000.0,
                    costPrice = 4000.0,
                    stock = 45,
                    minStockAlert = 10,
                    description = "Good Day Cappuccino creamy dengan taburan choco granule"
                ),
                ProductItem(
                    name = "Good Day Coklat Panas",
                    category = "Minuman",
                    price = 8000.0,
                    costPrice = 3500.0,
                    stock = 40,
                    minStockAlert = 10,
                    description = "Minuman coklat Good Day hangat nikmat & manis pas"
                ),
                ProductItem(
                    name = "Good Day Merah Panas",
                    category = "Minuman",
                    price = 8000.0,
                    costPrice = 3000.0,
                    stock = 40,
                    minStockAlert = 10,
                    description = "Kopi Good Day Merah (The Original) seduh panas mantap"
                ),
                ProductItem(
                    name = "Roti Bakar",
                    category = "Makanan",
                    price = 5000.0,
                    costPrice = 2500.0,
                    stock = 35,
                    minStockAlert = 5,
                    unit = "Porsi",
                    description = "Roti bakar renyah hangat dengan olesan manis gurih"
                ),
                ProductItem(
                    name = "Mix Platter",
                    category = "Makanan",
                    price = 10000.0,
                    costPrice = 5000.0,
                    stock = 30,
                    minStockAlert = 5,
                    unit = "Porsi",
                    description = "Kombinasi sosis, nugget & kentang goreng renyah"
                ),
                ProductItem(
                    name = "Kentang Goreng",
                    category = "Makanan",
                    price = 5000.0,
                    costPrice = 2500.0,
                    stock = 30,
                    minStockAlert = 5,
                    unit = "Porsi",
                    description = "Kentang goreng gurih renyah krispi dengan saus sambal"
                )
            )
            dao.insertAllProducts(initialProducts)

            // 2. Initial Raw Ingredients (Bahan Baku & Kemasan)
            val initialIngredients = listOf(
                RawIngredient(
                    name = "Good Day Freeze (Sachet)",
                    category = "Bahan Baku",
                    currentStock = 60.0,
                    minStock = 10.0,
                    unit = "pcs",
                    costPerUnit = 2500.0
                ),
                RawIngredient(
                    name = "Good Day Cappuccino (Sachet)",
                    category = "Bahan Baku",
                    currentStock = 50.0,
                    minStock = 10.0,
                    unit = "pcs",
                    costPerUnit = 2500.0
                ),
                RawIngredient(
                    name = "Good Day Coklat (Sachet)",
                    category = "Bahan Baku",
                    currentStock = 40.0,
                    minStock = 10.0,
                    unit = "pcs",
                    costPerUnit = 2200.0
                ),
                RawIngredient(
                    name = "Good Day Merah (Sachet)",
                    category = "Bahan Baku",
                    currentStock = 45.0,
                    minStock = 10.0,
                    unit = "pcs",
                    costPerUnit = 2000.0
                ),
                RawIngredient(
                    name = "Teh Celup & Gula Pasir",
                    category = "Bahan Baku",
                    currentStock = 100.0,
                    minStock = 20.0,
                    unit = "porsi",
                    costPerUnit = 1200.0
                ),
                RawIngredient(
                    name = "Roti Tawar & Selai",
                    category = "Bahan Baku",
                    currentStock = 35.0,
                    minStock = 8.0,
                    unit = "porsi",
                    costPerUnit = 2000.0
                ),
                RawIngredient(
                    name = "Kentang Beku (French Fries)",
                    category = "Bahan Baku",
                    currentStock = 4000.0,
                    minStock = 800.0,
                    unit = "gram",
                    costPerUnit = 45.0
                ),
                RawIngredient(
                    name = "Bahan Mix Platter (Sosis/Nugget)",
                    category = "Bahan Baku",
                    currentStock = 30.0,
                    minStock = 6.0,
                    unit = "porsi",
                    costPerUnit = 4000.0
                ),
                RawIngredient(
                    name = "Cup Plastik 12oz & Sedotan",
                    category = "Packaging",
                    currentStock = 150.0,
                    minStock = 30.0,
                    unit = "pcs",
                    costPerUnit = 800.0
                )
            )
            dao.insertAllRawIngredients(initialIngredients)

            // 3. Initial Expenses (Pembukuan awal)
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val initialExpenses = listOf(
                ExpenseEntity(
                    timestamp = now - (dayMillis * 3),
                    title = "Beli Biji Kopi Arabika 5kg",
                    category = "Bahan Baku",
                    amount = 650000.0,
                    paymentMethod = "TRANSFER",
                    notes = "Restock supplier lokal Bandung"
                ),
                ExpenseEntity(
                    timestamp = now - (dayMillis * 2),
                    title = "Beli Susu Fresh Milk 20 Liter",
                    category = "Bahan Baku",
                    amount = 380000.0,
                    paymentMethod = "CASH",
                    notes = "Greenfields Pasteurisasi"
                ),
                ExpenseEntity(
                    timestamp = now - dayMillis,
                    title = "Token Listrik Outlet Bekasi",
                    category = "Listrik & Air",
                    amount = 250000.0,
                    paymentMethod = "TRANSFER",
                    notes = "Operasional mesin espresso & AC"
                ),
                ExpenseEntity(
                    timestamp = now - 7200000L,
                    title = "Beli Plastik Cup & Sedotan Ramah Lingkungan",
                    category = "Operasional",
                    amount = 145000.0,
                    paymentMethod = "CASH",
                    notes = "Grosir packaging 500 pcs"
                )
            )
            dao.insertAllExpenses(initialExpenses)

            // 4. Initial Sample Completed Transactions
            val sampleTx1 = TransactionEntity(
                invoiceNumber = "KPK-20261024-001",
                timestamp = now - 14400000L,
                customerName = "Dimas Surya",
                tableOrOrderType = "Dine In (Meja 04)",
                paymentMethod = "QRIS",
                subtotal = 15000.0,
                discount = 0.0,
                tax = 0.0,
                total = 15000.0,
                amountPaid = 15000.0,
                changeAmount = 0.0,
                cashierName = "Rafi - Silent Barista",
                status = "COMPLETED",
                notes = "Es banyak"
            )
            val tx1Id = dao.insertTransaction(sampleTx1)
            dao.insertTransactionItems(listOf(
                TransactionItemEntity(
                    transactionId = tx1Id,
                    productId = 2,
                    productName = "Good Day Freeze",
                    quantity = 1,
                    pricePerUnit = 10000.0,
                    costPerUnit = 4000.0,
                    subtotal = 10000.0,
                    notes = "Dingin"
                ),
                TransactionItemEntity(
                    transactionId = tx1Id,
                    productId = 6,
                    productName = "Roti Bakar",
                    quantity = 1,
                    pricePerUnit = 5000.0,
                    costPerUnit = 2500.0,
                    subtotal = 5000.0,
                    notes = "Hangat"
                )
            ))

            val sampleTx2 = TransactionEntity(
                invoiceNumber = "KPK-20261024-002",
                timestamp = now - 7200000L,
                customerName = "Nadia Putri",
                tableOrOrderType = "Take Away",
                paymentMethod = "CASH",
                subtotal = 24000.0,
                discount = 2000.0,
                tax = 0.0,
                total = 22000.0,
                amountPaid = 50000.0,
                changeAmount = 28000.0,
                cashierName = "Rafi - Silent Barista",
                status = "COMPLETED",
                notes = "Saus dipisah"
            )
            val tx2Id = dao.insertTransaction(sampleTx2)
            dao.insertTransactionItems(listOf(
                TransactionItemEntity(
                    transactionId = tx2Id,
                    productId = 1,
                    productName = "Es Teh",
                    quantity = 2,
                    pricePerUnit = 7000.0,
                    costPerUnit = 2000.0,
                    subtotal = 14000.0,
                    notes = "Manis pas"
                ),
                TransactionItemEntity(
                    transactionId = tx2Id,
                    productId = 7,
                    productName = "Mix Platter",
                    quantity = 1,
                    pricePerUnit = 10000.0,
                    costPerUnit = 5000.0,
                    subtotal = 10000.0,
                    notes = "Pedas manis"
                )
            ))

            // Initial Inventory Log
            dao.insertInventoryLog(
                InventoryLogEntity(
                    timestamp = now,
                    itemName = "French Fries Truffle Herb",
                    itemType = "PRODUK",
                    changeAmount = -1.0,
                    previousStock = 5.0,
                    newStock = 4.0,
                    reason = "Penjualan POS",
                    notes = "Stok menipis, disarankan restock"
                )
            )
        }
    }
}
