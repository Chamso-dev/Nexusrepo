package com.nexus.grocerypos.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.nexus.grocerypos.domain.model.DiscountType
import com.nexus.grocerypos.domain.model.PaymentMethod

@Entity(
    tableName = "sales",
    indices = [Index("receiptNumber", unique = true), Index("customerId"), Index("createdAt")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val customerId: Long?,
    val cashierId: Long,
    val subtotal: Double,
    val discountTotal: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val totalCost: Double,
    val createdAt: Long,
    val isVoided: Boolean
)

@Entity(
    tableName = "sale_line_items",
    indices = [Index("saleId"), Index("productId")],
    foreignKeys = [
        ForeignKey(entity = SaleEntity::class, parentColumns = ["id"], childColumns = ["saleId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SaleLineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val unitCost: Double,
    val quantity: Double,
    val discountType: DiscountType,
    val discountValue: Double
)

@Entity(
    tableName = "sale_payments",
    indices = [Index("saleId")],
    foreignKeys = [
        ForeignKey(entity = SaleEntity::class, parentColumns = ["id"], childColumns = ["saleId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SalePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val method: PaymentMethod,
    val amount: Double,
    val tenderedAmount: Double,
    val changeDue: Double
)

data class SaleWithDetails(
    @Embedded val sale: SaleEntity,
    @Relation(parentColumn = "id", entityColumn = "saleId")
    val items: List<SaleLineItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "saleId")
    val payments: List<SalePaymentEntity>
)
