package com.nexus.grocerypos.navigation

object Destinations {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val POS = "pos"
    const val INVENTORY = "inventory"

    const val CUSTOMERS = "customers"
    const val SUPPLIERS = "suppliers"
    const val PURCHASES = "purchases"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    const val PRODUCT_EDIT = "product_edit?productId={productId}"
    fun productEdit(productId: Long? = null) = "product_edit?productId=${productId ?: -1}"

    const val CUSTOMER_EDIT = "customer_edit?customerId={customerId}"
    fun customerEdit(customerId: Long? = null) = "customer_edit?customerId=${customerId ?: -1}"

    const val SUPPLIER_EDIT = "supplier_edit?supplierId={supplierId}"
    fun supplierEdit(supplierId: Long? = null) = "supplier_edit?supplierId=${supplierId ?: -1}"

    const val PURCHASE_ORDER_EDIT = "purchase_order_edit?orderId={orderId}"
    fun purchaseOrderEdit(orderId: Long? = null) = "purchase_order_edit?orderId=${orderId ?: -1}"

    const val PURCHASE_ORDER_RECEIVE = "purchase_order_receive/{orderId}"
    fun purchaseOrderReceive(orderId: Long) = "purchase_order_receive/$orderId"

    const val USER_MANAGEMENT = "user_management"
    const val CHECKOUT_RECEIPT = "checkout_receipt/{saleId}"
    fun checkoutReceipt(saleId: Long) = "checkout_receipt/$saleId"
}
