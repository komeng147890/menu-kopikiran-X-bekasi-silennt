package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CustomerMenuScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.FinancialReportScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KopikiranViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KopikiranApp()
                }
            }
        }
    }
}

@Composable
fun KopikiranApp(viewModel: KopikiranViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition"
        ) { target ->
            when (target) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.CUSTOMER_MENU -> CustomerMenuScreen(viewModel = viewModel)
                AppScreen.POS_CASHIER -> PosScreen(viewModel = viewModel)
                AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                AppScreen.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                AppScreen.EXPENSES -> ExpenseScreen(viewModel = viewModel)
                AppScreen.FINANCIAL_REPORT -> FinancialReportScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }

        // Global Modal Receipt Dialog (shown on top when checkout finishes or order is clicked)
        activeReceipt?.let { txWithItems ->
            ReceiptDialog(
                transactionWithItems = txWithItems,
                settings = settings,
                onDismiss = { viewModel.dismissReceipt() },
                onRefund = { txId -> viewModel.refundTransaction(txId) }
            )
        }
    }
}
