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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.InventoryLogEntity
import com.example.data.model.ProductItem
import com.example.data.model.RawIngredient
import com.example.data.model.toFormattedDate
import com.example.data.model.toRupiah
import com.example.ui.components.KopikiranTopBar
import com.example.ui.components.StockBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: KopikiranViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val products by viewModel.allProducts.collectAsState()
    val rawIngredients by viewModel.allRawIngredients.collectAsState()
    val logs by viewModel.inventoryLogs.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, OUT_OF_STOCK

    // Dialog States
    var productToRestock by remember { mutableStateOf<ProductItem?>(null) }
    var rawToRestock by remember { mutableStateOf<RawIngredient?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddRawDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductItem?>(null) }

    val tabs = listOf("Produk Jadi", "Bahan Baku & Kemasan", "Log Mutasi Stok")

    ScaffoldWithFab(
        onAddClick = {
            if (selectedTabIndex == 1) {
                showAddRawDialog = true
            } else {
                showAddProductDialog = true
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("inventory_screen")
        ) {
            KopikiranTopBar(
                title = "Kelola Inventaris & Stok",
                subtitle = "KOPIKIRAN X BEKASI SILENT",
                onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
            )

            // Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("inventory_tab_$index")
                    )
                }
            }

            // Search and Filter Header (only for tab 0 & 1)
            if (selectedTabIndex != 2) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari barang inventaris...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_search_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            label = { Text("Semua") }
                        )
                        FilterChip(
                            selected = filterType == "LOW_STOCK",
                            onClick = { filterType = "LOW_STOCK" },
                            label = { Text("⚠️ Stok Kritis") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningAmber.copy(alpha = 0.2f),
                                selectedLabelColor = WarningAmber
                            )
                        )
                        FilterChip(
                            selected = filterType == "OUT_OF_STOCK",
                            onClick = { filterType = "OUT_OF_STOCK" },
                            label = { Text("Habis") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DangerRed.copy(alpha = 0.2f),
                                selectedLabelColor = DangerRed
                            )
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Produk Siap Jual List
                    val filtered = products.filter { p ->
                        val matchesQuery = p.name.contains(searchQuery, ignoreCase = true) || p.category.contains(searchQuery, ignoreCase = true)
                        val matchesFilter = when (filterType) {
                            "LOW_STOCK" -> p.stock in 1..p.minStockAlert
                            "OUT_OF_STOCK" -> p.stock <= 0
                            else -> true
                        }
                        matchesQuery && matchesFilter
                    }

                    if (filtered.isEmpty()) {
                        EmptyInventoryState(message = "Tidak ada produk yang sesuai filter.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { product ->
                                ProductInventoryCard(
                                    product = product,
                                    onRestock = { productToRestock = product },
                                    onEdit = { productToEdit = product },
                                    onDelete = { viewModel.deleteProduct(product) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Bahan Baku List
                    val filtered = rawIngredients.filter { raw ->
                        val matchesQuery = raw.name.contains(searchQuery, ignoreCase = true) || raw.category.contains(searchQuery, ignoreCase = true)
                        val matchesFilter = when (filterType) {
                            "LOW_STOCK" -> raw.currentStock in 0.1..raw.minStock
                            "OUT_OF_STOCK" -> raw.currentStock <= 0.0
                            else -> true
                        }
                        matchesQuery && matchesFilter
                    }

                    if (filtered.isEmpty()) {
                        EmptyInventoryState(message = "Tidak ada bahan baku yang sesuai filter.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { raw ->
                                RawIngredientCard(
                                    ingredient = raw,
                                    onRestock = { rawToRestock = raw },
                                    onDelete = { viewModel.deleteRawIngredient(raw) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Audit Logs List
                    if (logs.isEmpty()) {
                        EmptyInventoryState(message = "Belum ada riwayat mutasi stok.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logs, key = { it.id }) { log ->
                                InventoryLogCard(log = log)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Restock Produk
    productToRestock?.let { product ->
        RestockDialog(
            itemName = product.name,
            currentStock = "${product.stock} ${product.unit}",
            onDismiss = { productToRestock = null },
            onConfirm = { qty, reason ->
                viewModel.restockProduct(product.id, qty, reason)
                productToRestock = null
            }
        )
    }

    // Dialog Restock Bahan Baku
    rawToRestock?.let { raw ->
        RestockRawDialog(
            itemName = raw.name,
            currentStock = "${raw.currentStock} ${raw.unit}",
            unit = raw.unit,
            onDismiss = { rawToRestock = null },
            onConfirm = { amount, reason ->
                viewModel.restockRawIngredient(raw.id, amount, reason)
                rawToRestock = null
            }
        )
    }

    // Dialog Tambah Produk Baru
    if (showAddProductDialog) {
        AddEditProductDialog(
            initialProduct = null,
            onDismiss = { showAddProductDialog = false },
            onSave = { newProd ->
                viewModel.addProduct(newProd)
                showAddProductDialog = false
            }
        )
    }

    // Dialog Edit Produk
    productToEdit?.let { prod ->
        AddEditProductDialog(
            initialProduct = prod,
            onDismiss = { productToEdit = null },
            onSave = { updated ->
                viewModel.updateProduct(updated)
                productToEdit = null
            }
        )
    }

    // Dialog Tambah Bahan Baku Baru
    if (showAddRawDialog) {
        AddRawDialog(
            onDismiss = { showAddRawDialog = false },
            onSave = { newRaw ->
                viewModel.addRawIngredient(newRaw)
                showAddRawDialog = false
            }
        )
    }
}

@Composable
private fun ScaffoldWithFab(
    onAddClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = EspressoPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_inventory_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Item")
        }
    }
}

@Composable
private fun ProductInventoryCard(
    product: ProductItem,
    onRestock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("product_inv_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kategori: ${product.category} • HPP: ${product.costPrice.toRupiah()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StockBadge(stock = product.stock, minAlert = product.minStockAlert, unit = product.unit)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Harga Jual", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = product.price.toRupiah(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EspressoPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onRestock,
                        modifier = Modifier.testTag("restock_product_btn_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restock")
                    }

                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Produk") },
            text = { Text("Hapus ${product.name} dari daftar produk?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun RawIngredientCard(
    ingredient: RawIngredient,
    onRestock: () -> Unit,
    onDelete: () -> Unit
) {
    val isLow = ingredient.currentStock <= ingredient.minStock

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("raw_inv_card_${ingredient.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kategori: ${ingredient.category} • Min. Alert: ${ingredient.minStock} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLow) WarningAmber.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${ingredient.currentStock} ${ingredient.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isLow) WarningAmber else SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Biaya Satuan: ${ingredient.costPerUnit.toRupiah()}/${ingredient.unit}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onRestock,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaSecondary),
                        modifier = Modifier.testTag("restock_raw_btn_${ingredient.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Stok")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryLogCard(log: InventoryLogEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (log.changeAmount > 0) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (log.changeAmount > 0) "+${log.changeAmount.toInt()}" else "${log.changeAmount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (log.changeAmount > 0) SuccessGreen else DangerRed
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.itemName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${log.reason} • ${log.timestamp.toFormattedDate()}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.notes.isNotBlank()) {
                    Text(text = log.notes, fontSize = 11.sp, color = EspressoPrimary)
                }
            }
            Text(
                text = "Sisa: ${log.newStock.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RestockDialog(
    itemName: String,
    currentStock: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var addQtyInput by remember { mutableStateOf("10") }
    var reasonInput by remember { mutableStateOf("Restock Pembelian Masuk") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock: $itemName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Stok Saat Ini: $currentStock", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = addQtyInput,
                    onValueChange = { addQtyInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Tambahan Masuk") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("restock_qty_input")
                )
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Keterangan / Alasan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = addQtyInput.toIntOrNull() ?: 0
                    if (qty > 0) onConfirm(qty, reasonInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                modifier = Modifier.testTag("confirm_restock_button")
            ) {
                Text("Simpan Stok")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun RestockRawDialog(
    itemName: String,
    currentStock: String,
    unit: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var addAmountInput by remember { mutableStateOf("1000") }
    var reasonInput by remember { mutableStateOf("Beli Bahan Baku Baru") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Stok Bahan: $itemName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Stok Saat Ini: $currentStock", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = addAmountInput,
                    onValueChange = { addAmountInput = it },
                    label = { Text("Jumlah Masuk ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("restock_raw_amount_input")
                )
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = addAmountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, reasonInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaSecondary),
                modifier = Modifier.testTag("confirm_raw_restock_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProductDialog(
    initialProduct: ProductItem?,
    onDismiss: () -> Unit,
    onSave: (ProductItem) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Kopi") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "20000") }
    var costPriceText by remember { mutableStateOf(initialProduct?.costPrice?.toInt()?.toString() ?: "8000") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "30") }
    var minStockText by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "10") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    val categories = listOf("Kopi", "Non-Kopi", "Makanan", "Biji Kopi")
    var expandedCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "Tambah Produk Baru" else "Edit Produk") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name")
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_price")
                    )
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("HPP Modal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_cost")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stok Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_stock")
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min. Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Singkat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val product = ProductItem(
                            id = initialProduct?.id ?: 0,
                            name = name,
                            category = category,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                            stock = stockText.toIntOrNull() ?: 0,
                            minStockAlert = minStockText.toIntOrNull() ?: 5,
                            description = description
                        )
                        onSave(product)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun AddRawDialog(
    onDismiss: () -> Unit,
    onSave: (RawIngredient) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bahan Baku") }
    var currentStockText by remember { mutableStateOf("1000") }
    var minStockText by remember { mutableStateOf("200") }
    var unit by remember { mutableStateOf("gram") }
    var costText by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Bahan Baku Baru") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Bahan (cth: Susu Oat, Sirup Pandan)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan (gram/ml/pcs)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = { Text("Stok Masuk") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Batas Kritis") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Estimasi Biaya per $unit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            RawIngredient(
                                name = name,
                                category = category,
                                currentStock = currentStockText.toDoubleOrNull() ?: 0.0,
                                minStock = minStockText.toDoubleOrNull() ?: 10.0,
                                unit = unit,
                                costPerUnit = costText.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaSecondary)
            ) {
                Text("Simpan Bahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun EmptyInventoryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
