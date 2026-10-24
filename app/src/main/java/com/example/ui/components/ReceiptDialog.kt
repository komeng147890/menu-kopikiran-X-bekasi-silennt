package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionWithItems
import com.example.data.model.toFormattedDate
import com.example.data.model.toRupiah
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoPrimary
import com.example.ui.viewmodel.StoreSettings

@Composable
fun ReceiptDialog(
    transactionWithItems: TransactionWithItems,
    settings: StoreSettings,
    onDismiss: () -> Unit,
    onRefund: (Long) -> Unit
) {
    val context = LocalContext.current
    val tx = transactionWithItems.transaction
    val items = transactionWithItems.items
    var showRefundConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Struk Digital",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Thermal Paper Card Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAF8F5))
                        .border(1.dp, Color(0xFFE2D9D0), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Store Header
                        Text(
                            text = settings.storeName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E130E),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = settings.storeAddress,
                            fontSize = 11.sp,
                            color = Color(0xFF6B5C52),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Telp/WA: ${settings.phone}",
                            fontSize = 11.sp,
                            color = Color(0xFF6B5C52),
                            textAlign = TextAlign.Center
                        )

                        DashedDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Transaction Metadata
                        ReceiptRow(label = "No. Struk", value = tx.invoiceNumber)
                        ReceiptRow(label = "Tanggal", value = tx.timestamp.toFormattedDate())
                        ReceiptRow(label = "Kasir", value = tx.cashierName)
                        ReceiptRow(label = "Pelanggan", value = tx.customerName)
                        ReceiptRow(label = "Tipe Order", value = tx.tableOrOrderType)
                        ReceiptRow(label = "Metode Bayar", value = tx.paymentMethod)
                        if (tx.status == "REFUNDED") {
                            ReceiptRow(label = "STATUS", value = "DIBATALKAN / REFUND", isBold = true, color = DangerRed)
                        }

                        DashedDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Items
                        items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.productName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E130E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = item.subtotal.toRupiah(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E130E)
                                    )
                                }
                                if (item.notes.isNotBlank()) {
                                    Text(
                                        text = "   catatan: ${item.notes}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8A776B)
                                    )
                                }
                            }
                        }

                        DashedDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Totals
                        ReceiptRow(label = "Subtotal", value = tx.subtotal.toRupiah())
                        if (tx.discount > 0) {
                            ReceiptRow(label = "Diskon", value = "-${tx.discount.toRupiah()}", color = DangerRed)
                        }
                        if (tx.tax > 0) {
                            ReceiptRow(label = "Pajak PB1 (10%)", value = tx.tax.toRupiah())
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        ReceiptRow(
                            label = "TOTAL AKHIR",
                            value = tx.total.toRupiah(),
                            isBold = true,
                            fontSize = 15.sp,
                            color = EspressoPrimary
                        )
                        ReceiptRow(label = "Bayar (${tx.paymentMethod})", value = tx.amountPaid.toRupiah())
                        ReceiptRow(label = "Kembalian", value = tx.changeAmount.toRupiah(), isBold = true)

                        if (tx.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan: ${tx.notes}",
                                fontSize = 11.sp,
                                color = Color(0xFF6B5C52),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        DashedDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Footer Note
                        Text(
                            text = settings.receiptFooter,
                            fontSize = 11.sp,
                            color = Color(0xFF5A4D45),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "*** KOPIKIRAN SILENT POS ***",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A776B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share, Done, Refund
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceiptText(context, transactionWithItems, settings)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan")
                    }

                    Button(
                        onClick = {
                            sendReceiptToWhatsApp(context, transactionWithItems, settings)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("whatsapp_receipt_button")
                    ) {
                        Text("Kirim WA", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EspressoPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("done_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Selesai")
                    }
                }

                if (tx.status != "REFUNDED") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showRefundConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("refund_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Batalkan / Refund Pesanan")
                    }
                }
            }
        }
    }

    if (showRefundConfirm) {
        AlertDialog(
            onDismissRequest = { showRefundConfirm = false },
            title = { Text("Konfirmasi Refund") },
            text = { Text("Apakah Anda yakin ingin membatalkan transaksi ${tx.invoiceNumber}? Stok produk yang terjual akan otomatis dikembalikan ke inventaris.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRefundConfirm = false
                        onRefund(tx.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Batalkan Transaksi")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRefundConfirm = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    color: Color = Color(0xFF1E130E)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) color else Color(0xFF6B5C52)
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = Color(0xFFD6CBC1)
    )
}

private fun shareReceiptText(
    context: Context,
    transactionWithItems: TransactionWithItems,
    settings: StoreSettings
) {
    val tx = transactionWithItems.transaction
    val items = transactionWithItems.items

    val sb = StringBuilder()
    sb.appendLine("================================")
    sb.appendLine("   ${settings.storeName}")
    sb.appendLine("   ${settings.storeAddress}")
    sb.appendLine("   Telp: ${settings.phone}")
    sb.appendLine("================================")
    sb.appendLine("No. Struk : ${tx.invoiceNumber}")
    sb.appendLine("Waktu     : ${tx.timestamp.toFormattedDate()}")
    sb.appendLine("Kasir     : ${tx.cashierName}")
    sb.appendLine("Pelanggan : ${tx.customerName}")
    sb.appendLine("Tipe      : ${tx.tableOrOrderType}")
    sb.appendLine("Metode    : ${tx.paymentMethod}")
    if (tx.status == "REFUNDED") {
        sb.appendLine("STATUS    : [DIBATALKAN / REFUND]")
    }
    sb.appendLine("--------------------------------")
    items.forEach { item ->
        sb.appendLine("${item.quantity}x ${item.productName}")
        sb.appendLine("   @${item.pricePerUnit.toRupiah()} = ${item.subtotal.toRupiah()}")
        if (item.notes.isNotBlank()) {
            sb.appendLine("   (${item.notes})")
        }
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal  : ${tx.subtotal.toRupiah()}")
    if (tx.discount > 0) {
        sb.appendLine("Diskon    : -${tx.discount.toRupiah()}")
    }
    if (tx.tax > 0) {
        sb.appendLine("Pajak PB1 : ${tx.tax.toRupiah()}")
    }
    sb.appendLine("TOTAL     : ${tx.total.toRupiah()}")
    sb.appendLine("Bayar     : ${tx.amountPaid.toRupiah()}")
    sb.appendLine("Kembalian : ${tx.changeAmount.toRupiah()}")
    sb.appendLine("================================")
    sb.appendLine(settings.receiptFooter)

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Digital")
    context.startActivity(shareIntent)
}

private fun sendReceiptToWhatsApp(
    context: Context,
    transactionWithItems: TransactionWithItems,
    settings: StoreSettings
) {
    val tx = transactionWithItems.transaction
    val items = transactionWithItems.items

    val sb = StringBuilder()
    sb.appendLine("*STRUK PEMBELIAN - ${settings.storeName}*")
    sb.appendLine("📍 ${settings.storeAddress}")
    sb.appendLine("No. Struk : ${tx.invoiceNumber}")
    sb.appendLine("Waktu     : ${tx.timestamp.toFormattedDate()}")
    sb.appendLine("Pelanggan : ${tx.customerName} (${tx.tableOrOrderType})")
    sb.appendLine("Kasir     : ${tx.cashierName}")
    sb.appendLine("--------------------------------")
    items.forEach { item ->
        sb.appendLine("• ${item.quantity}x ${item.productName} = ${item.subtotal.toRupiah()}")
        if (item.notes.isNotBlank()) {
            sb.appendLine("  _catatan: ${item.notes}_")
        }
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal  : ${tx.subtotal.toRupiah()}")
    if (tx.discount > 0) sb.appendLine("Diskon    : -${tx.discount.toRupiah()}")
    if (tx.tax > 0) sb.appendLine("Pajak PB1 : ${tx.tax.toRupiah()}")
    sb.appendLine("💰 *TOTAL* : ${tx.total.toRupiah()}")
    sb.appendLine("Bayar     : ${tx.amountPaid.toRupiah()} (${tx.paymentMethod})")
    sb.appendLine("Kembalian : ${tx.changeAmount.toRupiah()}")
    sb.appendLine("================================")
    sb.appendLine(settings.receiptFooter)

    val encoded = java.net.URLEncoder.encode(sb.toString(), "UTF-8")
    val url = "https://wa.me/6281949380091?text=$encoded"
    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
}
