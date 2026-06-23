package com.nexus.grocerypos.data.repository

import androidx.room.withTransaction
import com.nexus.grocerypos.data.local.dao.InventoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.dao.SaleDao
import com.nexus.grocerypos.data.local.db.AppDatabase
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.model.PaymentMethod
import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.repository.CustomerRepository
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SalesRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val inventoryDao: InventoryDao,
    private val customerRepository: CustomerRepository
) : SalesRepository {

    override fun observeSales(fromMillis: Long?, toMillis: Long?) =
        saleDao.observeSales(fromMillis, toMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun getSaleById(id: Long) = saleDao.getById(id)?.toDomain()

    override suspend fun getSalesForCustomer(customerId: Long) =
        saleDao.getForCustomer(customerId).map { it.toDomain() }

    override suspend fun recordSale(sale: Sale): Result<Long> = resultOf {
        database.withTransaction {
            val saleId = saleDao.insertSale(sale.toEntitySkeleton())
            saleDao.insertLineItems(sale.items.map { it.toEntity(saleId) })
            saleDao.insertPayments(sale.payments.map { it.toEntity(saleId) })

            val now = System.currentTimeMillis()
            sale.items.forEach { item ->
                productDao.adjustStock(item.productId, -item.quantity, now)
                val resulting = productDao.getById(item.productId)?.stockQuantity ?: 0.0
                inventoryDao.insert(
                    InventoryTransactionEntity(
                        productId = item.productId,
                        productName = item.productName,
                        type = InventoryTransactionType.SALE,
                        quantityDelta = -item.quantity,
                        resultingQuantity = resulting,
                        reason = "Sale #${sale.receiptNumber}",
                        referenceId = saleId,
                        actorUserId = sale.cashierId,
                        createdAt = now
                    )
                )
            }

            val storeCreditUsed = sale.payments
                .filter { it.method == PaymentMethod.STORE_CREDIT }
                .sumOf { it.amount }
            val customerId = sale.customerId
            if (storeCreditUsed > 0 && customerId != null) {
                customerRepository.adjustBalance(customerId, -storeCreditUsed)
            }

            saleId
        }
    }

    override suspend fun voidSale(id: Long): Result<Unit> = resultOf {
        val sale = saleDao.getById(id) ?: error("Sale not found")
        database.withTransaction {
            saleDao.voidSale(id)
            val now = System.currentTimeMillis()
            sale.items.forEach { item ->
                productDao.adjustStock(item.productId, item.quantity, now)
                val resulting = productDao.getById(item.productId)?.stockQuantity ?: 0.0
                inventoryDao.insert(
                    InventoryTransactionEntity(
                        productId = item.productId,
                        productName = item.productName,
                        type = InventoryTransactionType.RETURN,
                        quantityDelta = item.quantity,
                        resultingQuantity = resulting,
                        reason = "Voided sale #${sale.sale.receiptNumber}",
                        referenceId = id,
                        actorUserId = sale.sale.cashierId,
                        createdAt = now
                    )
                )
            }
        }
    }

    override suspend fun nextReceiptNumber(): String {
        val count = saleDao.count() + 1
        return "RCT-%06d".format(count)
    }

    private fun Sale.toEntitySkeleton() = com.nexus.grocerypos.data.local.entity.SaleEntity(
        receiptNumber = receiptNumber,
        customerId = customerId,
        cashierId = cashierId,
        subtotal = subtotal,
        discountTotal = discountTotal,
        taxTotal = taxTotal,
        grandTotal = grandTotal,
        totalCost = totalCost,
        createdAt = createdAt,
        isVoided = isVoided
    )
}
