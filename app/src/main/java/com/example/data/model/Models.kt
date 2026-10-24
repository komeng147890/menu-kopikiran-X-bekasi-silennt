package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Utility extension for clean Indonesian Rupiah formatting
fun Double.toRupiah(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(this).replace("Rp", "Rp ")
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(this))
}

fun Long.toShortDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
    return sdf.format(Date(this))
}

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Kopi, Non-Kopi, Makanan, Biji Kopi
    val price: Double,
    val costPrice: Double, // HPP (Harga Pokok Penjualan)
    val stock: Int,
    val minStockAlert: Int = 5,
    val unit: String = "Cup",
    val isAvailable: Boolean = true,
    val description: String = ""
)

@Entity(tableName = "raw_ingredients")
data class RawIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Bahan Baku, Susu & Sirup, Packaging
    val currentStock: Double,
    val minStock: Double = 10.0,
    val unit: String, // gram, ml, pcs
    val costPerUnit: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Pelanggan",
    val tableOrOrderType: String = "Dine In", // Dine In, Take Away, Ojek Online
    val paymentMethod: String = "CASH", // CASH, QRIS, TRANSFER, DEBIT
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double,
    val amountPaid: Double,
    val changeAmount: Double,
    val cashierName: String = "Barista Bekasi",
    val status: String = "COMPLETED", // COMPLETED, REFUNDED
    val notes: String = ""
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"])]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val costPerUnit: Double,
    val subtotal: Double,
    val notes: String = "" // e.g. "Less sugar, Less ice"
)

data class TransactionWithItems(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItemEntity>
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val category: String, // Bahan Baku, Operasional, Listrik & Air, Sewa, Gaji, Pemasaran, Lainnya
    val amount: Double,
    val paymentMethod: String = "CASH",
    val notes: String = ""
)

@Entity(tableName = "inventory_logs")
data class InventoryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val itemName: String,
    val itemType: String, // PRODUK atau BAHAN_BAKU
    val changeAmount: Double,
    val previousStock: Double,
    val newStock: Double,
    val reason: String, // Restock, Penjualan POS, Rusak/Basi, Koreksi
    val notes: String = ""
)

// UI Cart Item Model
data class CartItem(
    val product: ProductItem,
    val quantity: Int = 1,
    val notes: String = ""
) {
    val subtotal: Double get() = product.price * quantity
    val totalCost: Double get() = product.costPrice * quantity
}
