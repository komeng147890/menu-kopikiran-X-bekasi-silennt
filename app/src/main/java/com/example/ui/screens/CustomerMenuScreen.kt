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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductItem
import com.example.data.model.toRupiah
import com.example.ui.components.KopikiranTopBar
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.MatchaAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaSecondary
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.CartItem
import java.net.URLEncoder
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel

@Composable
fun CustomerMenuScreen(viewModel: KopikiranViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val products by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedTable by remember { mutableStateOf("Meja 01") }
    var selectedProductForDetail by remember { mutableStateOf<ProductItem?>(null) }

    val categories = listOf("Semua", "Minuman", "Makanan")
    val tables = listOf("Meja 01", "Meja 02", "Meja 03", "Meja 04", "Meja 05", "Bar Counter", "Take Away")

    val filteredProducts = products.filter {
        val matchesCategory = (selectedCategory == "Semua" || it.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalCartItems = cart.sumOf { it.quantity }
    val totalCartAmount = cart.sumOf { it.subtotal }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customer_menu_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            KopikiranTopBar(
                title = "Menu Kafe Kami",
                subtitle = "KOPIKIRAN X BEKASI SILENT",
                onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = if (cart.isNotEmpty()) 100.dp else 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Customer Hero Banner with Facilities
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CoffeeBrownDark, Color(0xFF352016), Color(0xFF523323))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MENU KAFE KAMI",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFDDB8),
                                        letterSpacing = 1.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "OPEN ORDER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "KOPIKIRAN X BEKASI SILENT",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Pilihan minuman dingin & hangat serta cemilan roti bakar & platter favorit.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2D4CB)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Facilities Badge from image: AC, Wi-Fi & Sofa
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Fasilitas Kami: ❄️ AC, 📶 Wi-Fi & 🛋️ Sofa",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFDDB8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Table Selector
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TableRestaurant,
                                contentDescription = null,
                                tint = EspressoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lokasi Meja Anda:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tables) { table ->
                                val isSelected = selectedTable == table
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) EspressoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedTable = table
                                            viewModel.setOrderType("Dine In ($table)")
                                        }
                                        .testTag("customer_table_chip_$table")
                                ) {
                                    Text(
                                        text = table,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari minuman, rasa, makanan...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("customer_menu_search_input")
                    )
                }

                // Category Chips
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EspressoPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("customer_category_chip_$category")
                            )
                        }
                    }
                }

                // Product Cards List
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Menu tidak ditemukan. Silakan cari kata kunci lain.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        CustomerProductCard(
                            product = product,
                            onItemClick = { selectedProductForDetail = product },
                            onQuickOrder = {
                                viewModel.addToCart(product, notes = "Pesanan Pelanggan ($selectedTable)")
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Customer Cart Bar
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("customer_bottom_order_bar"),
                shape = RoundedCornerShape(20.dp),
                color = CoffeeBrownDark,
                shadowElevation = 8.dp
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
                                text = "Pesanan Anda ($selectedTable)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFDDB8)
                            )
                        }
                        Text(
                            text = totalCartAmount.toRupiah(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                sendOrderToWhatsApp(context, selectedTable, cart, totalCartAmount)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("customer_whatsapp_order_button")
                        ) {
                            Text("Kirim WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.POS_CASHIER) },
                            colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("customer_view_order_button")
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kasir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Detail Menu Modal Dialog with Customization
    selectedProductForDetail?.let { product ->
        CustomerProductDetailDialog(
            product = product,
            selectedTable = selectedTable,
            onDismiss = { selectedProductForDetail = null },
            onAddToCart = { notes ->
                viewModel.addToCart(product, notes = notes)
                selectedProductForDetail = null
            }
        )
    }
}

@Composable
private fun CustomerProductCard(
    product: ProductItem,
    onItemClick: () -> Unit,
    onQuickOrder: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isSignature = product.name in listOf("Good Day Freeze", "Good Day Cappuccino", "Mix Platter", "Roti Bakar")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(enabled = !isOutOfStock) { onItemClick() }
            .testTag("customer_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category & Signature Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoPrimary
                    )

                    if (isSignature) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFDDB8))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "★ SIGNATURE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A2800)
                            )
                        }
                    }
                }

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Habis Hari Ini",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tersedia",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSignature) EspressoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (product.category == "Makanan") Icons.Default.RestaurantMenu else Icons.Default.Coffee,
                        contentDescription = null,
                        tint = EspressoPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Harga", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = product.price.toRupiah(),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isOutOfStock) MaterialTheme.colorScheme.outline else EspressoPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onItemClick,
                        enabled = !isOutOfStock,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Detail", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onQuickOrder,
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("customer_quick_add_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Pesan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerProductDetailDialog(
    product: ProductItem,
    selectedTable: String,
    onDismiss: () -> Unit,
    onAddToCart: (notes: String) -> Unit
) {
    var temperatureChoice by remember { mutableStateOf("Es Dingin") }
    var sugarChoice by remember { mutableStateOf("Normal (100%)") }
    var extraNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EspressoPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Coffee, contentDescription = null, tint = EspressoPrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = product.price.toRupiah(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EspressoPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Temperature Choice (if drink)
                if (product.category.equals("Minuman", ignoreCase = true)) {
                    Text("Pilihan Suhu Minuman:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Es Dingin", "Hangat (Hot)").forEach { temp ->
                            val isSelected = temperatureChoice == temp
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EspressoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { temperatureChoice = temp }
                            ) {
                                Text(
                                    text = temp,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Sugar Choice
                    Text("Tingkat Manis (Gula / Susu):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Normal (100%)", "Sedang (50%)", "Tanpa Gula (0%)").forEach { sug ->
                            val isSelected = sugarChoice == sug
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { sugarChoice = sug }
                            ) {
                                Text(
                                    text = sug.replace(" (", "\n("),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Custom Notes
                OutlinedTextField(
                    value = extraNote,
                    onValueChange = { extraNote = it },
                    label = { Text("Catatan Tambahan untuk Barista") },
                    placeholder = { Text("Cth: dipisah es, extra sedotan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullNotesList = mutableListOf<String>()
                    if (product.category.equals("Minuman", ignoreCase = true)) {
                        fullNotesList.add(temperatureChoice)
                        fullNotesList.add("Gula: $sugarChoice")
                    }
                    if (extraNote.isNotBlank()) {
                        fullNotesList.add(extraNote)
                    }
                    fullNotesList.add("Meja: $selectedTable")

                    val consolidatedNotes = fullNotesList.joinToString(", ")
                    onAddToCart(consolidatedNotes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                modifier = Modifier.testTag("customer_confirm_add_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah ke Pesanan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

private fun sendOrderToWhatsApp(
    context: Context,
    table: String,
    cart: List<CartItem>,
    totalAmount: Double
) {
    val sb = StringBuilder()
    sb.appendLine("*PESANAN MEJA - KOPIKIRAN X BEKASI SILENT*")
    sb.appendLine("📍 Lokasi: $table")
    sb.appendLine("--------------------------------")
    cart.forEach { item ->
        sb.appendLine("• ${item.quantity}x ${item.product.name} (${item.subtotal.toRupiah()})")
        if (item.notes.isNotBlank()) {
            sb.appendLine("   catatan: ${item.notes}")
        }
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("💰 *TOTAL BAYAR*: ${totalAmount.toRupiah()}")
    sb.appendLine("Mohon diproses ya Barista, terima kasih! ☕")

    val encoded = URLEncoder.encode(sb.toString(), "UTF-8")
    val url = "https://wa.me/6281949380091?text=$encoded"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
