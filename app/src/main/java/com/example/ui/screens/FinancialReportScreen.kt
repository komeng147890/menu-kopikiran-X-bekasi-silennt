package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.toRupiah
import com.example.ui.components.KopikiranTopBar
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.MatchaAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinancialMetrics
import com.example.ui.viewmodel.KopikiranViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.ui.viewmodel.StoreSettings

@Composable
fun FinancialReportScreen(viewModel: KopikiranViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val context = LocalContext.current
    val metrics by viewModel.financialMetrics.collectAsState()
    val selectedPeriod by viewModel.selectedReportPeriod.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val profitMargin = if (metrics.grossRevenue > 0) {
        (metrics.netProfit / metrics.grossRevenue) * 100
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("financial_report_screen")
    ) {
        KopikiranTopBar(
            title = "Laporan Keuangan Otomatis",
            subtitle = "KOPIKIRAN X BEKASI SILENT",
            onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Periods
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReportPeriod.entries.forEach { period ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setReportPeriod(period) },
                            label = { Text(period.label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("period_chip_${period.name}")
                        )
                    }
                }
            }

            // Executive Summary Card: Laba Bersih
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CoffeeBrownDark, Color(0xFF2E1C14), Color(0xFF452B1F))
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
                                Text(
                                    text = "RINGKASAN LABA BERSIH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFDDB8),
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = selectedPeriod.label,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = metrics.netProfit.toRupiah(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (metrics.netProfit >= 0) Color(0xFF81C784) else Color(0xFFFF8A80)
                            )

                            Text(
                                text = "Margin Laba Bersih: ${String.format("%.1f", profitMargin)}%",
                                fontSize = 12.sp,
                                color = Color(0xFFE8D6CB)
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Formula: (Omzet - HPP Modal) - Beban Pengeluaran = Laba Bersih",
                                fontSize = 10.sp,
                                color = Color(0xFFB5A499)
                            )
                        }
                    }
                }
            }

            // Financial Breakdown Grid (4 components)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatBox(
                            title = "Total Pendapatan (Omzet)",
                            value = metrics.grossRevenue.toRupiah(),
                            subtitle = "${metrics.totalTransactions} Transaksi",
                            color = EspressoPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatBox(
                            title = "Beban Pokok (HPP)",
                            value = metrics.totalCostOfGoods.toRupiah(),
                            subtitle = "Modal Bahan Terjual",
                            color = TerracottaSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatBox(
                            title = "Laba Kotor (Gross)",
                            value = metrics.grossProfit.toRupiah(),
                            subtitle = "Omzet - HPP",
                            color = MatchaAccent,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatBox(
                            title = "Beban Operasional",
                            value = metrics.totalExpenses.toRupiah(),
                            subtitle = "Pengeluaran Kas",
                            color = DangerRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Key Performance Indicators (AOV & Transactions)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rata-rata Order (AOV)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(metrics.averageOrderValue.toRupiah(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EspressoPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Pesanan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${metrics.totalTransactions} Order", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EspressoPrimary)
                        }
                    }
                }
            }

            // Top Selling Products Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = EspressoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Produk Terlaris (Best Seller)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (metrics.topSellingItems.isEmpty()) {
                            Text("Belum ada data penjualan produk.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val maxCount = metrics.topSellingItems.maxOfOrNull { it.second } ?: 1
                            metrics.topSellingItems.forEachIndexed { index, (name, count) ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "#${index + 1} $name",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "$count terjual",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EspressoPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (count.toFloat() / maxCount).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                        color = EspressoPrimary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Methods Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Distribusi Metode Pembayaran",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (metrics.paymentDistribution.isEmpty()) {
                            Text("Belum ada data pembayaran.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            metrics.paymentDistribution.forEach { (method, totalAmount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(method, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(totalAmount.toRupiah(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Share / Export Report Button
            item {
                Button(
                    onClick = {
                        shareFinancialReport(context, metrics, selectedPeriod, settings)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("export_report_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan / Export Laporan Keuangan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReportStatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun shareFinancialReport(
    context: Context,
    metrics: FinancialMetrics,
    period: ReportPeriod,
    settings: StoreSettings
) {
    val sb = StringBuilder()
    sb.appendLine("========================================")
    sb.appendLine("📊 LAPORAN KEUANGAN OTOMATIS")
    sb.appendLine("☕ ${settings.storeName}")
    sb.appendLine("📍 ${settings.storeAddress}")
    sb.appendLine("📅 Periode: ${period.label}")
    sb.appendLine("========================================")
    sb.appendLine("1. KINERJA LABA / RUGI:")
    sb.appendLine("   - Total Omzet     : ${metrics.grossRevenue.toRupiah()}")
    sb.appendLine("   - Beban Pokok HPP : ${metrics.totalCostOfGoods.toRupiah()}")
    sb.appendLine("   -------------------------------------")
    sb.appendLine("   - Laba Kotor      : ${metrics.grossProfit.toRupiah()}")
    sb.appendLine("   - Beban Operasional: ${metrics.totalExpenses.toRupiah()}")
    sb.appendLine("   =====================================")
    sb.appendLine("   ★ LABA BERSIH     : ${metrics.netProfit.toRupiah()}")
    sb.appendLine("   - Total Transaksi : ${metrics.totalTransactions} Pesanan")
    sb.appendLine("   - Rata-rata AOV   : ${metrics.averageOrderValue.toRupiah()}")
    sb.appendLine("")
    sb.appendLine("2. PRODUK TERLARIS (TOP 5):")
    metrics.topSellingItems.forEachIndexed { i, (name, qty) ->
        sb.appendLine("   ${i + 1}. $name : $qty terjual")
    }
    sb.appendLine("")
    sb.appendLine("3. METODE PEMBAYARAN:")
    metrics.paymentDistribution.forEach { (m, v) ->
        sb.appendLine("   - $m: ${v.toRupiah()}")
    }
    sb.appendLine("========================================")
    sb.appendLine("Digenerate otomatis oleh Sistem Kasir KOPIKIRAN SILENT.")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Keuangan")
    context.startActivity(shareIntent)
}
