package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KopikiranTopBar
import com.example.ui.theme.EspressoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel
import com.example.ui.viewmodel.StoreSettings

@Composable
fun SettingsScreen(viewModel: KopikiranViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val currentSettings by viewModel.settings.collectAsState()

    var storeName by remember { mutableStateOf(currentSettings.storeName) }
    var address by remember { mutableStateOf(currentSettings.storeAddress) }
    var phone by remember { mutableStateOf(currentSettings.phone) }
    var cashierName by remember { mutableStateOf(currentSettings.cashierName) }
    var isTaxEnabled by remember { mutableStateOf(currentSettings.isTaxEnabled) }
    var footerText by remember { mutableStateOf(currentSettings.receiptFooter) }
    var savedSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        KopikiranTopBar(
            title = "Pengaturan Toko",
            subtitle = "KOPIKIRAN X BEKASI SILENT",
            onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profil Toko
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = EspressoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Profil Outlet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it; savedSuccess = false },
                            label = { Text("Nama Toko / Brand") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("settings_store_name")
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; savedSuccess = false },
                            label = { Text("Alamat Outlet") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; savedSuccess = false },
                            label = { Text("No. Telepon / WhatsApp") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Pengaturan Kasir & Pajak
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Kasir & Pajak Penjualan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = cashierName,
                            onValueChange = { cashierName = it; savedSuccess = false },
                            label = { Text("Nama Kasir Bertugas") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("settings_cashier_name")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pajak Restoran PB1 (10%)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Tambahkan pajak 10% otomatis pada setiap struk transaksi",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isTaxEnabled,
                                onCheckedChange = { isTaxEnabled = it; savedSuccess = false },
                                colors = SwitchDefaults.colors(checkedThumbColor = EspressoPrimary),
                                modifier = Modifier.testTag("settings_tax_switch")
                            )
                        }
                    }
                }
            }

            // Pengaturan Catatan Struk
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Teks Footer Struk Kasir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = footerText,
                            onValueChange = { footerText = it; savedSuccess = false },
                            label = { Text("Pesan Ucapan Terima Kasih") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("settings_footer_text")
                        )
                    }
                }
            }

            // About App
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EspressoPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("KOPIKIRAN X BEKASI SILENT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Versi 1.0 • Sistem Kasir POS & Pembukuan Otomatis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val newSettings = StoreSettings(
                            storeName = storeName,
                            storeAddress = address,
                            phone = phone,
                            cashierName = cashierName,
                            isTaxEnabled = isTaxEnabled,
                            receiptFooter = footerText
                        )
                        viewModel.updateSettings(newSettings)
                        savedSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (savedSuccess) SuccessGreen else EspressoPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (savedSuccess) "Pengaturan Tersimpan!" else "Simpan Pengaturan Toko", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
