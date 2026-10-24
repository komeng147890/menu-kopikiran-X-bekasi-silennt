package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.toFormattedDate
import com.example.data.model.toRupiah
import com.example.ui.components.KopikiranTopBar
import com.example.ui.components.SummaryMetricCard
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.MatchaAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel

data class MainMenuItem(
    val title: String,
    val subtitle: String,
    val screen: AppScreen,
    val icon: ImageVector,
    val iconColor: Color,
    val badgeText: String? = null
)

@Composable
fun DashboardScreen(viewModel: KopikiranViewModel) {
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val lowStockIngredients by viewModel.lowStockRawIngredients.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val metrics by viewModel.financialMetrics.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val totalLowStockCount = lowStockProducts.size + lowStockIngredients.size

    val menuItems = listOf(
        MainMenuItem(
            title = "Menu Pelanggan",
            subtitle = "Katalog digital & meja",
            screen = AppScreen.CUSTOMER_MENU,
            icon = Icons.Default.RestaurantMenu,
            iconColor = Color(0xFFC0732A),
            badgeText = "Katalog Tamu"
        ),
        MainMenuItem(
            title = "Kasir POS",
            subtitle = "Catat pesanan & struk",
            screen = AppScreen.POS_CASHIER,
            icon = Icons.Default.PointOfSale,
            iconColor = EspressoPrimary
        ),
        MainMenuItem(
            title = "Kelola Inventaris",
            subtitle = "Stok produk & bahan baku",
            screen = AppScreen.INVENTORY,
            icon = Icons.Default.Inventory2,
            iconColor = TerracottaSecondary,
            badgeText = if (totalLowStockCount > 0) "$totalLowStockCount Kritis" else null
        ),
        MainMenuItem(
            title = "Riwayat Transaksi",
            subtitle = "Daftar penjualan real-time",
            screen = AppScreen.TRANSACTIONS,
            icon = Icons.Default.ReceiptLong,
            iconColor = MatchaAccent
        ),
        MainMenuItem(
            title = "Buku Kas & Biaya",
            subtitle = "Pengeluaran & operasional",
            screen = AppScreen.EXPENSES,
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = Color(0xFFD84315)
        ),
        MainMenuItem(
            title = "Laporan Keuangan",
            subtitle = "Laba/rugi otomatis",
            screen = AppScreen.FINANCIAL_REPORT,
            icon = Icons.Default.Assessment,
            iconColor = Color(0xFF1565C0)
        ),
        MainMenuItem(
            title = "Pengaturan Toko",
            subtitle = "Profil, kasir & pajak",
            screen = AppScreen.SETTINGS,
            icon = Icons.Default.Settings,
            iconColor = Color(0xFF5D4037)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            KopikiranTopBar(
                title = settings.storeName,
                subtitle = "Bekasi Silent Outlet • Kasir: ${settings.cashierName}"
            )
        }

        // Hero Brand Banner
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CoffeeBrownDark, Color(0xFF331D14), Color(0xFF4A2B1D))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OUTLET BUKA • LIVE REAL-TIME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFDDB8)
                                )
                            }
                            Text(
                                text = "BEKASI SILENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "KOPIKIRAN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Aplikasi Kasir POS & Pembukuan Otomatis",
                            fontSize = 13.sp,
                            color = Color(0xFFE8D6CB)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Stat Highlights
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Pendapatan Hari Ini", fontSize = 11.sp, color = Color(0xFFD6C6BC))
                                    Text(
                                        text = metrics.grossRevenue.toRupiah(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Laba Bersih Hari Ini", fontSize = 11.sp, color = Color(0xFFD6C6BC))
                                    Text(
                                        text = metrics.netProfit.toRupiah(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (metrics.netProfit >= 0) Color(0xFF81C784) else Color(0xFFFF8A80)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Low stock warning banner if any item is critically low
        if (totalLowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.INVENTORY) }
                        .testTag("low_stock_alert_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Perhatian Stok Kritis ($totalLowStockCount Item)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WarningAmber
                            )
                            Text(
                                text = "Beberapa produk / bahan baku hampir habis. Ketuk untuk cek & restock.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Menu Utama (Index)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "MENU UTAMA (INDEX)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Akses Cepat Pengelolaan Operasional & Kasir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Grid of Menu Utama
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(440.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(menuItems) { menu ->
                    MainNavigationCard(
                        menu = menu,
                        onClick = { viewModel.navigateTo(menu.screen) }
                    )
                }
            }
        }

        // Recent Transactions Overview
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terkini (Real-time)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.TRANSACTIONS) }
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Belum ada transaksi hari ini.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            val recentList = transactions.take(3)
            items(recentList.size) { index ->
                val txWithItems = recentList[index]
                val tx = txWithItems.transaction
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clickable { viewModel.showReceipt(txWithItems) }
                        .testTag("recent_tx_item_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EspressoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = null,
                                tint = EspressoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tx.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = tx.paymentMethod,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(
                                text = "${tx.customerName} • ${tx.timestamp.toFormattedDate()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = tx.total.toRupiah(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EspressoPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainNavigationCard(
    menu: MainMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
            .testTag("menu_card_${menu.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(menu.iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = menu.icon,
                        contentDescription = menu.title,
                        tint = menu.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (menu.badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = menu.badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            }

            Column {
                Text(
                    text = menu.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = menu.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
