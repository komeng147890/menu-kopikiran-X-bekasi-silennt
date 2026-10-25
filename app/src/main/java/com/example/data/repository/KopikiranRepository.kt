package com.example.data.repository

import com.example.data.local.KopikiranDao
import com.example.data.model.CartItem
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class KopikiranRepository(private val dao: KopikiranDao) {

    val allProducts: Flow<List<ProductItem>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<ProductItem>> = dao.getLowStockProducts()

    val allRawIngredients: Flow<List<RawIngredient>> = dao.getAllRawIngredients()
    val lowStockRawIngredients: Flow<List<RawIngredient>> = dao.getLowStockRawIngredients()

    val allTransactions: Flow<List<TransactionWithItems>> = dao.getAllTransactionsWithItems()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val recentInventoryLogs: Flow<List<InventoryLogEntity>> = dao.getRecentInventoryLogs()

    suspend fun getTransactionById(id: Long): TransactionWithItems? {
        return dao.getTransactionWithItemsById(id)
    }

    // Process complete POS order and update inventory atomically
    suspend fun processCheckout(
        cartItems: List<CartItem>,
        customerName: String,
        tableOrOrderType: String,
        paymentMethod: String,
        subtotal: Double,
        discount: Double,
        tax: Double,
        total: Double,
        amountPaid: Double,
        changeAmount: Double,
        cashierName: String,
        notes: String
    ): Long {
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomSuffix = Random.nextInt(100, 999)
        val invoiceNumber = "KPK-$dateCode-$randomSuffix"

        val transaction = TransactionEntity(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            customerName = customerName.ifBlank { "Pelanggan" },
            tableOrOrderType = tableOrOrderType,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total,
            amountPaid = amountPaid,
            changeAmount = changeAmount,
            cashierName = cashierName,
            status = "COMPLETED",
            notes = notes
        )

        val txId = dao.insertTransaction(transaction)

        val itemsToInsert = cartItems.map { item ->
            TransactionItemEntity(
                transactionId = txId,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                pricePerUnit = item.product.price,
                costPerUnit = item.product.costPrice,
                subtotal = item.subtotal,
                notes = item.notes
            )
        }
        dao.insertTransactionItems(itemsToInsert)

        // Decrement product inventory and log
        cartItems.forEach { item ->
            val product = dao.getProductById(item.product.id)
            if (product != null) {
                val prev = product.stock
                val newStock = (prev - item.quantity).coerceAtLeast(0)
                dao.adjustProductStock(item.product.id, -item.quantity)

                dao.insertInventoryLog(
                    InventoryLogEntity(
                        timestamp = System.currentTimeMillis(),
                        itemName = product.name,
                        itemType = "PRODUK",
                        changeAmount = -item.quantity.toDouble(),
                        previousStock = prev.toDouble(),
                        newStock = newStock.toDouble(),
                        reason = "Penjualan POS ($invoiceNumber)",
                        notes = "Qty: ${item.quantity} ${product.unit}"
                    )
                )
            }
        }

        return txId
    }

    // Product management
    suspend fun addProduct(product: ProductItem): Long {
        val id = dao.insertProduct(product)
        dao.insertInventoryLog(
            InventoryLogEntity(
                itemName = product.name,
                itemType = "PRODUK",
                changeAmount = product.stock.toDouble(),
                previousStock = 0.0,
                newStock = product.stock.toDouble(),
                reason = "Produk Baru Ditambahkan",
                notes = "Harga: Rp ${product.price.toInt()}, Kategori: ${product.category}"
            )
        )
        return id
    }

    suspend fun updateProduct(product: ProductItem) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductItem) {
        dao.deleteProduct(product)
    }

    suspend fun restockProduct(productId: Long, addQty: Int, reason: String = "Restock Masuk") {
        val product = dao.getProductById(productId) ?: return
        val prev = product.stock
        val newStock = prev + addQty
        dao.adjustProductStock(productId, addQty)
        dao.insertInventoryLog(
            InventoryLogEntity(
                itemName = product.name,
                itemType = "PRODUK",
                changeAmount = addQty.toDouble(),
                previousStock = prev.toDouble(),
                newStock = newStock.toDouble(),
                reason = reason,
                notes = "Restock +$addQty ${product.unit}"
            )
        )
    }

    // Raw Ingredient management
    suspend fun addRawIngredient(ingredient: RawIngredient): Long {
        val id = dao.insertRawIngredient(ingredient)
        dao.insertInventoryLog(
            InventoryLogEntity(
                itemName = ingredient.name,
                itemType = "BAHAN_BAKU",
                changeAmount = ingredient.currentStock,
                previousStock = 0.0,
                newStock = ingredient.currentStock,
                reason = "Bahan Baku Baru",
                notes = "Satuan: ${ingredient.unit}"
            )
        )
        return id
    }

    suspend fun updateRawIngredient(ingredient: RawIngredient) {
        dao.updateRawIngredient(ingredient)
    }

    suspend fun deleteRawIngredient(ingredient: RawIngredient) {
        dao.deleteRawIngredient(ingredient)
    }

    suspend fun restockRawIngredient(id: Long, addAmount: Double, reason: String = "Restock Bahan Masuk") {
        val ingredient = dao.getRawIngredientById(id) ?: return
        val prev = ingredient.currentStock
        val newStock = prev + addAmount
        dao.adjustRawIngredientStock(id, addAmount)
        dao.insertInventoryLog(
            InventoryLogEntity(
                itemName = ingredient.name,
                itemType = "BAHAN_BAKU",
                changeAmount = addAmount,
                previousStock = prev,
                newStock = newStock,
                reason = reason,
                notes = "Restock +$addAmount ${ingredient.unit}"
            )
        )
    }

    // Expenses (Pembukuan)
    suspend fun addExpense(expense: ExpenseEntity): Long {
        return dao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // Void / Refund Transaction
    suspend fun refundTransaction(transactionId: Long) {
        val txWithItems = dao.getTransactionWithItemsById(transactionId) ?: return
        dao.updateTransactionStatus(transactionId, "REFUNDED")

        // Return stock
        txWithItems.items.forEach { item ->
            val product = dao.getProductById(item.productId)
            if (product != null) {
                val prev = product.stock
                val newStock = prev + item.quantity
                dao.adjustProductStock(item.productId, item.quantity)
                dao.insertInventoryLog(
                    InventoryLogEntity(
                        itemName = product.name,
                        itemType = "PRODUK",
                        changeAmount = item.quantity.toDouble(),
                        previousStock = prev.toDouble(),
                        newStock = newStock.toDouble(),
                        reason = "Pengembalian Transaksi (${txWithItems.transaction.invoiceNumber})",
                        notes = "Stok dikembalikan ke inventaris"
                    )
                )
            }
        }
    }

    // Force sync to strictly keep the 8 items from the menu
    suspend fun enforceExactPhotoMenu() {
        val oldKentang = dao.getProductByName("Kentang Goreng")
        if (oldKentang != null) {
            val existingFF = dao.getProductByName("French Fries")
            if (existingFF == null) {
                dao.updateProduct(oldKentang.copy(name = "French Fries"))
            } else {
                dao.deleteProduct(oldKentang)
            }
        }

        val allowedOfficialNames = listOf(
            "Es Teh",
            "Good Day Freeze",
            "Good Day Cappuccino",
            "Good Day Coklat Panas",
            "Good Day Merah Panas",
            "Roti Bakar",
            "Mix Platter",
            "French Fries"
        )
        dao.deleteProductsNotIn(allowedOfficialNames)

        val officialProducts = listOf(
            ProductItem(
                name = "Es Teh",
                category = "Minuman",
                price = 7000.0,
                costPrice = 2000.0,
                stock = 60,
                minStockAlert = 10,
                unit = "Porsi",
                description = "Es teh manis segar dingin pelepas dahaga"
            ),
            ProductItem(
                name = "Good Day Freeze",
                category = "Minuman",
                price = 10000.0,
                costPrice = 4000.0,
                stock = 50,
                minStockAlert = 10,
                unit = "Cup",
                description = "Kopi Good Day Freeze dingin dengan es kocok segar"
            ),
            ProductItem(
                name = "Good Day Cappuccino",
                category = "Minuman",
                price = 10000.0,
                costPrice = 4000.0,
                stock = 45,
                minStockAlert = 10,
                unit = "Cup",
                description = "Good Day Cappuccino creamy dengan taburan choco granule"
            ),
            ProductItem(
                name = "Good Day Coklat Panas",
                category = "Minuman",
                price = 8000.0,
                costPrice = 3500.0,
                stock = 40,
                minStockAlert = 10,
                unit = "Cup",
                description = "Minuman coklat Good Day hangat nikmat & manis pas"
            ),
            ProductItem(
                name = "Good Day Merah Panas",
                category = "Minuman",
                price = 8000.0,
                costPrice = 3000.0,
                stock = 40,
                minStockAlert = 10,
                unit = "Cup",
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
                name = "French Fries",
                category = "Makanan",
                price = 5000.0,
                costPrice = 2500.0,
                stock = 30,
                minStockAlert = 5,
                unit = "Porsi",
                description = "Kentang goreng gurih renyah krispi dengan saus sambal"
            )
        )

        for (item in officialProducts) {
            val existing = dao.getProductByName(item.name)
            if (existing == null) {
                dao.insertProduct(item)
            } else {
                dao.updateProduct(existing.copy(
                    category = item.category,
                    price = item.price,
                    costPrice = item.costPrice,
                    description = item.description
                ))
            }
        }
    }
}
