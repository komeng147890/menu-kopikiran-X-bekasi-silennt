package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.model.toRupiah
import com.example.ui.components.KopikiranTopBar
import com.example.ui.components.PaymentMethodChip
import com.example.ui.components.QrisDialog
import com.example.ui.components.StockBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: KopikiranViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val products by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedCategory by viewModel.posCategoryFilter.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var showQrisModal by remember { mutableStateOf(false) }
    var cashPaidInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("Semua", "Minuman", "Makanan")

    val filteredProducts = products.filter {
        val matchesCategory = (selectedCategory == "Semua" || it.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalCartItems = cart.sumOf { it.quantity }
    val subtotal = cart.sumOf { it.subtotal }
    val actualDiscount = discountAmount.coerceAtMost(subtotal)
    val taxableAmount = subtotal - actualDiscount
    val tax = if (settings.isTaxEnabled) taxableAmount * (settings.taxRatePercent / 100.0) else 0.0
    val grandTotal = taxableAmount + tax

    Box(modifier = Modifier.fillMaxSize().testTag("pos_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            KopikiranTopBar(
                title = "Kasir POS Real-Time",
                subtitle = "KOPIKIRAN X BEKASI SILENT",
                onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setPosSearch(it) },
                placeholder = { Text("Cari kopi, minuman, makanan...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setPosSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("pos_search_input")
            )

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setPosCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("pos_category_chip_$category")
                    )
                }
            }

            // Product Cards Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada produk ditemukan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = if (cart.isNotEmpty()) 90.dp else 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductPosCard(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Summary Bar
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("bottom_cart_bar"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EspressoPrimary)
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$totalCartItems",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Pesanan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = grandTotal.toRupiah(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EspressoPrimary
                        )
                    }

                    Button(
                        onClick = { showCartSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_cart_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Pesanan")
                    }
                }
            }
        }
    }

    // Cart Modal Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Detail Transaksi Kasir",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "KOPIKIRAN X BEKASI SILENT",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoPrimary
                            )
                        }
                        Text(
                            text = "Kosongkan",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.clearCart()
                                    showCartSheet = false
                                }
                                .testTag("clear_cart_button")
                        )
                    }
                }

                // Customer info & Order Type
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { viewModel.setCustomerName(it) },
                            label = { Text("Nama Pelanggan") },
                            placeholder = { Text("misal: Dimas") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("cart_customer_name_input")
                        )

                        // Order Type Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tipe Pesanan", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Dine In", "Take Away").forEach { type ->
                                    val isSelected = orderType == type
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EspressoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setOrderType(type) }
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Cart Items
                items(cart.size) { index ->
                    val item = cart[index]
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.updateCartItemQuantity(index, item.quantity + 1) },
                        onDecrease = { viewModel.updateCartItemQuantity(index, item.quantity - 1) },
                        onNotesChanged = { viewModel.updateCartItemNotes(index, it) }
                    )
                }

                item {
                    HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Discount Selector
                item {
                    Column {
                        Text("Diskon / Promo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0.0, 5000.0, 10000.0).forEach { disc ->
                                val isSelected = discountAmount == disc
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setDiscount(disc) }
                                ) {
                                    Text(
                                        text = if (disc == 0.0) "Tanpa Diskon" else "Rp ${disc.toInt() / 1000}k",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Column {
                        Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("QRIS", "CASH", "TRANSFER", "DEBIT").forEach { method ->
                                PaymentMethodChip(
                                    method = method,
                                    isSelected = paymentMethod == method,
                                    onClick = {
                                        viewModel.setPaymentMethod(method)
                                        if (method == "CASH" && cashPaidInput.isBlank()) {
                                            cashPaidInput = grandTotal.toInt().toString()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // If CASH: Cash Calculator
                if (paymentMethod == "CASH") {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Uang Tunai Diterima", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = cashPaidInput,
                                    onValueChange = { cashPaidInput = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Nominal Tunai (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cash_paid_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                // Fast Cash Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val exact = grandTotal.toInt()
                                    val options = listOf(
                                        Pair("Pas", exact),
                                        Pair("50k", 50000),
                                        Pair("100k", 100000),
                                        Pair("200k", 200000)
                                    )
                                    options.forEach { (label, amt) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { cashPaidInput = amt.toString() }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                val paidVal = cashPaidInput.toDoubleOrNull() ?: 0.0
                                val changeVal = (paidVal - grandTotal).coerceAtLeast(0.0)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kembalian:", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = changeVal.toRupiah(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (paidVal >= grandTotal) SuccessGreen else DangerRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Calculation breakdown
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(subtotal.toRupiah(), fontSize = 12.sp)
                        }
                        if (actualDiscount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diskon", fontSize = 12.sp, color = DangerRed)
                                Text("-${actualDiscount.toRupiah()}", fontSize = 12.sp, color = DangerRed)
                            }
                        }
                        if (tax > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pajak PB1 (${settings.taxRatePercent}%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(tax.toRupiah(), fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL BAYAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                grandTotal.toRupiah(),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = EspressoPrimary
                            )
                        }
                    }
                }

                // Process Checkout Button
                item {
                    Button(
                        onClick = {
                            if (paymentMethod == "QRIS") {
                                showQrisModal = true
                            } else {
                                val paidAmt = cashPaidInput.toDoubleOrNull() ?: grandTotal
                                viewModel.completeCheckout(amountPaid = paidAmt) {
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showCartSheet = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_checkout_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (paymentMethod == "QRIS") "Tampilkan QRIS & Bayar" else "Selesaikan Transaksi & Buat Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // QRIS Scanner Modal
    if (showQrisModal) {
        QrisDialog(
            amount = grandTotal,
            onDismiss = { showQrisModal = false },
            onConfirmPaid = {
                showQrisModal = false
                viewModel.completeCheckout(amountPaid = grandTotal) {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCartSheet = false
                    }
                }
            }
        )
    }
}

@Composable
private fun ProductPosCard(
    product: ProductItem,
    onAddToCart: () -> Unit
) {
    val isOutOfStock = product.stock <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onAddToCart() }
            .testTag("product_pos_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EspressoPrimary
                )
                StockBadge(stock = product.stock, minAlert = product.minStockAlert, unit = product.unit)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.price.toRupiah(),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (isOutOfStock) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isOutOfStock) MaterialTheme.colorScheme.outlineVariant else EspressoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onNotesChanged: (String) -> Unit
) {
    var showNoteInput by remember { mutableStateOf(item.notes.isNotBlank()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "@${item.product.price.toRupiah()}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                        contentDescription = "Kurang",
                        modifier = Modifier.size(16.dp),
                        tint = if (item.quantity == 1) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = item.subtotal.toRupiah(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EspressoPrimary
                )
            }
        }

        // Modifiers / Notes input
        if (showNoteInput) {
            OutlinedTextField(
                value = item.notes,
                onValueChange = onNotesChanged,
                placeholder = { Text("Catatan: less ice, gula aren terpisah...", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        } else {
            Text(
                text = "+ Tambah catatan khusus pesanan",
                fontSize = 11.sp,
                color = EspressoPrimary,
                modifier = Modifier
                    .clickable { showNoteInput = true }
                    .padding(top = 2.dp)
            )
        }
    }
}
