package com.nexus.grocerypos.data.local.db

import com.nexus.grocerypos.data.local.entity.BrandEntity
import com.nexus.grocerypos.data.local.entity.CategoryEntity
import com.nexus.grocerypos.data.local.entity.CustomerEntity
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import com.nexus.grocerypos.data.local.entity.ProductEntity
import com.nexus.grocerypos.data.local.entity.ProductWithDetailsRow
import com.nexus.grocerypos.data.local.entity.PurchaseOrderLineItemEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderWithItems
import com.nexus.grocerypos.data.local.entity.SaleLineItemEntity
import com.nexus.grocerypos.data.local.entity.SalePaymentEntity
import com.nexus.grocerypos.data.local.entity.SaleWithDetails
import com.nexus.grocerypos.data.local.entity.SupplierEntity
import com.nexus.grocerypos.data.local.entity.UserEntity
import com.nexus.grocerypos.domain.model.Brand
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.model.InventoryTransaction
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderLineItem
import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.model.SaleLineItem
import com.nexus.grocerypos.domain.model.SalePayment
import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.model.User

fun UserEntity.toDomain() = User(id, fullName, username, passwordHash, pinHash, role, isActive, createdAt)
fun User.toEntity() = UserEntity(id, fullName, username, passwordHash, pinHash, role, isActive, createdAt)

fun CategoryEntity.toDomain() = Category(id, name, colorHex, createdAt)
fun Category.toEntity() = CategoryEntity(id, name, colorHex, createdAt)

fun BrandEntity.toDomain() = Brand(id, name, createdAt)
fun Brand.toEntity() = BrandEntity(id, name, createdAt)

fun ProductEntity.toDomain() = Product(
    id, name, sku, barcode, categoryId, brandId, costPrice, sellingPrice,
    stockQuantity, lowStockThreshold, unit, imageUri, isActive, createdAt, updatedAt
)

fun Product.toEntity() = ProductEntity(
    id, name, sku, barcode, categoryId, brandId, costPrice, sellingPrice,
    stockQuantity, lowStockThreshold, unit, imageUri, isActive, createdAt, updatedAt
)

fun ProductWithDetailsRow.toDomain() = ProductWithDetails(
    product = Product(
        id, name, sku, barcode, categoryId, brandId, costPrice, sellingPrice,
        stockQuantity, lowStockThreshold, unit, imageUri, isActive, createdAt, updatedAt
    ),
    categoryName = categoryName,
    brandName = brandName
)

fun CustomerEntity.toDomain() = Customer(id, name, phone, email, address, balance, notes, createdAt)
fun Customer.toEntity() = CustomerEntity(id, name, phone, email, address, balance, notes, createdAt)

fun SupplierEntity.toDomain() = Supplier(id, name, contactPerson, phone, email, address, balanceOwed, notes, createdAt)
fun Supplier.toEntity() = SupplierEntity(id, name, contactPerson, phone, email, address, balanceOwed, notes, createdAt)

fun SaleLineItemEntity.toDomain() = SaleLineItem(id, saleId, productId, productName, unitPrice, unitCost, quantity, discountType, discountValue)
fun SaleLineItem.toEntity(saleId: Long) = SaleLineItemEntity(id, saleId, productId, productName, unitPrice, unitCost, quantity, discountType, discountValue)

fun SalePaymentEntity.toDomain() = SalePayment(id, saleId, method, amount, tenderedAmount, changeDue)
fun SalePayment.toEntity(saleId: Long) = SalePaymentEntity(id, saleId, method, amount, tenderedAmount, changeDue)

fun SaleWithDetails.toDomain() = Sale(
    id = sale.id,
    receiptNumber = sale.receiptNumber,
    customerId = sale.customerId,
    cashierId = sale.cashierId,
    items = items.map { it.toDomain() },
    payments = payments.map { it.toDomain() },
    subtotal = sale.subtotal,
    discountTotal = sale.discountTotal,
    taxTotal = sale.taxTotal,
    grandTotal = sale.grandTotal,
    totalCost = sale.totalCost,
    createdAt = sale.createdAt,
    isVoided = sale.isVoided
)

fun InventoryTransactionEntity.toDomain() = InventoryTransaction(
    id, productId, productName, type, quantityDelta, resultingQuantity, reason, referenceId, actorUserId, createdAt
)

fun PurchaseOrderLineItemEntity.toDomain() = PurchaseOrderLineItem(id, purchaseOrderId, productId, productName, quantityOrdered, quantityReceived, unitCost)
fun PurchaseOrderLineItem.toEntity(orderId: Long) = PurchaseOrderLineItemEntity(id, orderId, productId, productName, quantityOrdered, quantityReceived, unitCost)

fun PurchaseOrderWithItems.toDomain() = PurchaseOrder(
    id = order.id,
    orderNumber = order.orderNumber,
    supplierId = order.supplierId,
    supplierName = order.supplierName,
    status = order.status,
    items = items.map { it.toDomain() },
    invoiceNumber = order.invoiceNumber,
    notes = order.notes,
    createdAt = order.createdAt,
    receivedAt = order.receivedAt
)
