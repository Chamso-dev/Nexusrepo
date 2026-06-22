package com.nexus.grocerypos.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexus.grocerypos.core.designsystem.components.GlassNavItem
import com.nexus.grocerypos.core.designsystem.components.GlassBottomNavBar
import com.nexus.grocerypos.core.designsystem.components.GlassNavRail
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.Session
import com.nexus.grocerypos.feature.auth.CurrentUserViewModel
import com.nexus.grocerypos.feature.auth.PinLockScreen
import com.nexus.grocerypos.feature.customers.CustomerEditScreen
import com.nexus.grocerypos.feature.customers.CustomersScreen
import com.nexus.grocerypos.feature.dashboard.DashboardScreen
import com.nexus.grocerypos.feature.inventory.InventoryScreen
import com.nexus.grocerypos.feature.pos.PosScreen
import com.nexus.grocerypos.feature.pos.ReceiptScreen
import com.nexus.grocerypos.feature.products.ProductEditScreen
import com.nexus.grocerypos.feature.products.ProductsScreen
import com.nexus.grocerypos.feature.purchases.PurchaseOrderEditScreen
import com.nexus.grocerypos.feature.purchases.PurchaseOrderReceiveScreen
import com.nexus.grocerypos.feature.purchases.PurchasesScreen
import com.nexus.grocerypos.feature.reports.ReportsScreen
import com.nexus.grocerypos.feature.settings.SettingsScreen
import com.nexus.grocerypos.feature.settings.UserManagementScreen
import com.nexus.grocerypos.feature.suppliers.SupplierEditScreen
import com.nexus.grocerypos.feature.suppliers.SuppliersScreen
import kotlinx.coroutines.launch

private val primaryNavItems = listOf(
    GlassNavItem(Destinations.DASHBOARD, "Home", Icons.Filled.Dashboard),
    GlassNavItem(Destinations.PRODUCTS, "Products", Icons.Filled.Inventory2),
    GlassNavItem(Destinations.POS, "POS", Icons.Filled.PointOfSale),
    GlassNavItem(Destinations.INVENTORY, "Stock", Icons.Filled.Warehouse),
    GlassNavItem("more", "More", Icons.Filled.MoreHoriz)
)

private data class MoreMenuEntry(val route: String, val label: String, val icon: ImageVector)

private val moreMenuEntries = listOf(
    MoreMenuEntry(Destinations.CUSTOMERS, "Customers", Icons.Filled.People),
    MoreMenuEntry(Destinations.SUPPLIERS, "Suppliers", Icons.Filled.LocalShipping),
    MoreMenuEntry(Destinations.PURCHASES, "Purchases", Icons.Filled.ShoppingCart),
    MoreMenuEntry(Destinations.REPORTS, "Reports", Icons.Filled.BarChart),
    MoreMenuEntry(Destinations.SETTINGS, "Settings", Icons.Filled.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    session: Session
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showMoreMenu by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) locked = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentUserViewModel: CurrentUserViewModel = hiltViewModel()
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    androidx.compose.runtime.LaunchedEffect(session.userId) {
        currentUserViewModel.loadUser(session.userId)
    }

    if (locked && currentUser?.pinHash != null) {
        PinLockScreen(
            userId = session.userId,
            onUnlocked = { locked = false },
            onSignOutInstead = {
                currentUserViewModel.logout()
                locked = false
            }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 840.dp

        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                GlassNavRail(
                    items = primaryNavItems,
                    selectedRoute = currentRoute ?: Destinations.DASHBOARD,
                    onItemSelected = { route ->
                        if (route == "more") showMoreMenu = true else navigateTopLevel(navController, route)
                    },
                    modifier = Modifier.padding(16.dp)
                )
                Box(modifier = Modifier.fillMaxSize().padding(end = 16.dp, top = 16.dp, bottom = 16.dp)) {
                    MainNavHost(navController = navController, cashierId = session.userId)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)) {
                    MainNavHost(navController = navController, cashierId = session.userId)
                }
                GlassBottomNavBar(
                    items = primaryNavItems,
                    selectedRoute = currentRoute ?: Destinations.DASHBOARD,
                    onItemSelected = { route ->
                        if (route == "more") showMoreMenu = true else navigateTopLevel(navController, route)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }

    if (showMoreMenu) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        val colors = LocalGlassColors.current
        ModalBottomSheet(onDismissRequest = { showMoreMenu = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "More", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                moreMenuEntries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(entry.icon, contentDescription = null, tint = colors.textPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxSize().clickableEntry {
                                scope.launch {
                                    sheetState.hide()
                                    showMoreMenu = false
                                    navigateTopLevel(navController, entry.route)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                        .clickableEntry {
                            scope.launch {
                                sheetState.hide()
                                showMoreMenu = false
                                currentUserViewModel.logout()
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, tint = colors.textPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Sign out", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(Destinations.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun Modifier.clickableEntry(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Composable
private fun MainNavHost(navController: NavHostController, cashierId: Long) {
    NavHost(navController = navController, startDestination = Destinations.DASHBOARD) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToProducts = { navigateTopLevel(navController, Destinations.PRODUCTS) },
                onNavigateToPos = { navigateTopLevel(navController, Destinations.POS) },
                onNavigateToInventory = { navigateTopLevel(navController, Destinations.INVENTORY) },
                onNavigateToReports = { navigateTopLevel(navController, Destinations.REPORTS) }
            )
        }
        composable(Destinations.PRODUCTS) {
            ProductsScreen(
                onAddProduct = { navController.navigate(Destinations.productEdit()) },
                onEditProduct = { id -> navController.navigate(Destinations.productEdit(id)) }
            )
        }
        composable(
            Destinations.PRODUCT_EDIT,
            arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId")?.takeIf { it >= 0 }
            ProductEditScreen(productId = productId, onDone = { navController.popBackStack() })
        }
        composable(Destinations.POS) {
            PosScreen(
                cashierId = cashierId,
                onCheckoutComplete = { saleId -> navController.navigate(Destinations.checkoutReceipt(saleId)) }
            )
        }
        composable(
            Destinations.CHECKOUT_RECEIPT,
            arguments = listOf(navArgument("saleId") { type = NavType.LongType })
        ) { entry ->
            val saleId = entry.arguments?.getLong("saleId") ?: 0L
            ReceiptScreen(saleId = saleId, onDone = { navController.popBackStack(Destinations.POS, inclusive = false) })
        }
        composable(Destinations.INVENTORY) {
            InventoryScreen()
        }
        composable(Destinations.CUSTOMERS) {
            CustomersScreen(
                onAddCustomer = { navController.navigate(Destinations.customerEdit()) },
                onEditCustomer = { id -> navController.navigate(Destinations.customerEdit(id)) }
            )
        }
        composable(
            Destinations.CUSTOMER_EDIT,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val customerId = entry.arguments?.getLong("customerId")?.takeIf { it >= 0 }
            CustomerEditScreen(customerId = customerId, onDone = { navController.popBackStack() })
        }
        composable(Destinations.SUPPLIERS) {
            SuppliersScreen(
                onAddSupplier = { navController.navigate(Destinations.supplierEdit()) },
                onEditSupplier = { id -> navController.navigate(Destinations.supplierEdit(id)) }
            )
        }
        composable(
            Destinations.SUPPLIER_EDIT,
            arguments = listOf(navArgument("supplierId") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val supplierId = entry.arguments?.getLong("supplierId")?.takeIf { it >= 0 }
            SupplierEditScreen(supplierId = supplierId, onDone = { navController.popBackStack() })
        }
        composable(Destinations.PURCHASES) {
            PurchasesScreen(
                onAddOrder = { navController.navigate(Destinations.purchaseOrderEdit()) },
                onOpenOrder = { id -> navController.navigate(Destinations.purchaseOrderEdit(id)) },
                onReceiveOrder = { id -> navController.navigate(Destinations.purchaseOrderReceive(id)) }
            )
        }
        composable(
            Destinations.PURCHASE_ORDER_EDIT,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val orderId = entry.arguments?.getLong("orderId")?.takeIf { it >= 0 }
            PurchaseOrderEditScreen(orderId = orderId, onDone = { navController.popBackStack() })
        }
        composable(
            Destinations.PURCHASE_ORDER_RECEIVE,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { entry ->
            val orderId = entry.arguments?.getLong("orderId") ?: 0L
            PurchaseOrderReceiveScreen(orderId = orderId, actorUserId = cashierId, onDone = { navController.popBackStack() })
        }
        composable(Destinations.REPORTS) {
            ReportsScreen()
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(onManageUsers = { navController.navigate(Destinations.USER_MANAGEMENT) })
        }
        composable(Destinations.USER_MANAGEMENT) {
            UserManagementScreen(onDone = { navController.popBackStack() })
        }
    }
}
