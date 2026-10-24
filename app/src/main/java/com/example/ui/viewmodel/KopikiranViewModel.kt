package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KopikiranDatabase
import com.example.data.model.CartItem
import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithItems
import com.example.data.repository.KopikiranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    DASHBOARD,        // Index / Menu Utama
    CUSTOMER_MENU,    // Index / Katalog Menu Pelanggan
    POS_CASHIER,      // Kasir & Transaksi Real-time
    INVENTORY,        // Kelola Inventaris Produk & Bahan
    TRANSACTIONS,     // Riwayat Transaksi & Struk
    EXPENSES,         // Buku Kas & Pengeluaran
    FINANCIAL_REPORT, // Laporan Keuangan Otomatis
    SETTINGS          // Pengaturan Toko
}

enum class ReportPeriod(val label: String) {
    TODAY("Hari Ini"),
    THIS_WEEK("7 Hari Terakhir"),
    THIS_MONTH("Bulan Ini"),
    ALL_TIME("Semua Waktu")
}

data class FinancialMetrics(
    val grossRevenue: Double = 0.0,
    val totalCostOfGoods: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalTransactions: Int = 0,
    val averageOrderValue: Double = 0.0,
    val topSellingItems: List<Pair<String, Int>> = emptyList(),
    val paymentDistribution: Map<String, Double> = emptyMap()
)

data class StoreSettings(
    val storeName: String = "KOPIKIRAN X BEKASI SILENT",
    val storeAddress: String = "Jl. Boulevard Harapan Indah No. 8, Bekasi",
    val phone: String = "0819-4938-0091",
    val cashierName: String = "Rafi (Silent Barista)",
    val isTaxEnabled: Boolean = false,
    val taxRatePercent: Double = 10.0, // Pajak Restoran PB1 10%
    val receiptFooter: String = "Terima kasih telah singgah di KOPIKIRAN X BEKASI SILENT! Follow IG: @kopikiran.bekasisilent"
)

class KopikiranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KopikiranRepository

    init {
        val db = KopikiranDatabase.getDatabase(application, viewModelScope)
        repository = KopikiranRepository(db.kopikiranDao())
    }

    // --- NAVIGATION ---
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // --- REPOSITORY DATA STREAMS ---
    val allProducts: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductItem>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRawIngredients: StateFlow<List<RawIngredient>> = repository.allRawIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockRawIngredients: StateFlow<List<RawIngredient>> = repository.lowStockRawIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithItems>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryLogs: StateFlow<List<InventoryLogEntity>> = repository.recentInventoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- STORE SETTINGS ---
    private val _settings = MutableStateFlow(StoreSettings())
    val settings: StateFlow<StoreSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: StoreSettings) {
        _settings.value = newSettings
    }

    // --- POS & CART STATE ---
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _orderType = MutableStateFlow("Dine In") // Dine In, Take Away, Ojek Online
    val orderType: StateFlow<String> = _orderType.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("QRIS") // QRIS, CASH, TRANSFER, DEBIT
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _activeReceipt = MutableStateFlow<TransactionWithItems?>(null)
    val activeReceipt: StateFlow<TransactionWithItems?> = _activeReceipt.asStateFlow()

    // POS Filters
    private val _posCategoryFilter = MutableStateFlow("Semua")
    val posCategoryFilter: StateFlow<String> = _posCategoryFilter.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    fun setPosCategory(category: String) {
        _posCategoryFilter.value = category
    }

    fun setPosSearch(query: String) {
        _posSearchQuery.value = query
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun setOrderType(type: String) {
        _orderType.value = type
    }

    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount
    }

    fun addToCart(product: ProductItem, notes: String = "") {
        _cart.update { current ->
            val existing = current.find { it.product.id == product.id && it.notes == notes }
            if (existing != null) {
                current.map {
                    if (it.product.id == product.id && it.notes == notes) {
                        it.copy(quantity = it.quantity + 1)
                    } else it
                }
            } else {
                current + CartItem(product = product, quantity = 1, notes = notes)
            }
        }
    }

    fun updateCartItemQuantity(index: Int, newQty: Int) {
        _cart.update { current ->
            if (index in current.indices) {
                if (newQty <= 0) {
                    current.filterIndexed { i, _ -> i != index }
                } else {
                    current.mapIndexed { i, item ->
                        if (i == index) item.copy(quantity = newQty) else item
                    }
                }
            } else current
        }
    }

    fun updateCartItemNotes(index: Int, notes: String) {
        _cart.update { current ->
            if (index in current.indices) {
                current.mapIndexed { i, item ->
                    if (i == index) item.copy(notes = notes) else item
                }
            } else current
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _customerName.value = ""
        _orderNotes.value = ""
        _discountAmount.value = 0.0
    }

    // Checkout execution
    fun completeCheckout(amountPaid: Double, onFinished: (TransactionWithItems) -> Unit) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        val subtotal = currentCart.sumOf { it.subtotal }
        val discount = _discountAmount.value.coerceAtMost(subtotal)
        val afterDiscount = subtotal - discount
        val tax = if (_settings.value.isTaxEnabled) afterDiscount * (_settings.value.taxRatePercent / 100.0) else 0.0
        val grandTotal = afterDiscount + tax
        val actualPaid = if (_selectedPaymentMethod.value == "CASH") amountPaid else grandTotal
        val change = (actualPaid - grandTotal).coerceAtLeast(0.0)

        viewModelScope.launch {
            val txId = repository.processCheckout(
                cartItems = currentCart,
                customerName = _customerName.value,
                tableOrOrderType = _orderType.value,
                paymentMethod = _selectedPaymentMethod.value,
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = grandTotal,
                amountPaid = actualPaid,
                changeAmount = change,
                cashierName = _settings.value.cashierName,
                notes = _orderNotes.value
            )

            val created = repository.getTransactionById(txId)
            clearCart()
            if (created != null) {
                _activeReceipt.value = created
                onFinished(created)
            }
        }
    }

    fun showReceipt(transaction: TransactionWithItems) {
        _activeReceipt.value = transaction
    }

    fun dismissReceipt() {
        _activeReceipt.value = null
    }

    fun refundTransaction(txId: Long) {
        viewModelScope.launch {
            repository.refundTransaction(txId)
            _activeReceipt.value = null
        }
    }

    // --- INVENTORY MANAGEMENT ---
    fun addProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun updateProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun restockProduct(productId: Long, addQty: Int, reason: String = "Restock Masuk") {
        viewModelScope.launch {
            repository.restockProduct(productId, addQty, reason)
        }
    }

    fun addRawIngredient(ingredient: RawIngredient) {
        viewModelScope.launch {
            repository.addRawIngredient(ingredient)
        }
    }

    fun updateRawIngredient(ingredient: RawIngredient) {
        viewModelScope.launch {
            repository.updateRawIngredient(ingredient)
        }
    }

    fun deleteRawIngredient(ingredient: RawIngredient) {
        viewModelScope.launch {
            repository.deleteRawIngredient(ingredient)
        }
    }

    fun restockRawIngredient(id: Long, addAmount: Double, reason: String = "Restock Masuk") {
        viewModelScope.launch {
            repository.restockRawIngredient(id, addAmount, reason)
        }
    }

    // --- EXPENSES ---
    fun addExpense(title: String, category: String, amount: Double, paymentMethod: String, notes: String) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    title = title,
                    category = category,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- AUTOMATIC FINANCIAL REPORTS ---
    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    val financialMetrics: StateFlow<FinancialMetrics> = combine(
        allTransactions,
        allExpenses,
        _selectedReportPeriod
    ) { transactions, expenses, period ->
        val cutoffTimestamp = getPeriodCutoffTimestamp(period)

        // Filter valid (COMPLETED) transactions in this period
        val periodTransactions = transactions.filter {
            it.transaction.status == "COMPLETED" && it.transaction.timestamp >= cutoffTimestamp
        }

        val periodExpenses = expenses.filter {
            it.timestamp >= cutoffTimestamp
        }

        val grossRevenue = periodTransactions.sumOf { it.transaction.total }

        // Calculate Cost of Goods Sold (HPP)
        var totalCOGS = 0.0
        val productSalesCount = mutableMapOf<String, Int>()
        val paymentMethodMap = mutableMapOf<String, Double>()

        periodTransactions.forEach { txWithItems ->
            txWithItems.items.forEach { item ->
                totalCOGS += (item.costPerUnit * item.quantity)
                productSalesCount[item.productName] = (productSalesCount[item.productName] ?: 0) + item.quantity
            }
            val method = txWithItems.transaction.paymentMethod
            paymentMethodMap[method] = (paymentMethodMap[method] ?: 0.0) + txWithItems.transaction.total
        }

        val grossProfit = grossRevenue - totalCOGS
        val totalExpensesAmount = periodExpenses.sumOf { it.amount }
        val netProfit = grossProfit - totalExpensesAmount

        val txCount = periodTransactions.size
        val aov = if (txCount > 0) grossRevenue / txCount else 0.0

        val topSelling = productSalesCount.toList().sortedByDescending { it.second }.take(5)

        FinancialMetrics(
            grossRevenue = grossRevenue,
            totalCostOfGoods = totalCOGS,
            grossProfit = grossProfit,
            totalExpenses = totalExpensesAmount,
            netProfit = netProfit,
            totalTransactions = txCount,
            averageOrderValue = aov,
            topSellingItems = topSelling,
            paymentDistribution = paymentMethodMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialMetrics())

    private fun getPeriodCutoffTimestamp(period: ReportPeriod): Long {
        val calendar = Calendar.getInstance()
        return when (period) {
            ReportPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.THIS_WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            ReportPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.ALL_TIME -> 0L
        }
    }
}
