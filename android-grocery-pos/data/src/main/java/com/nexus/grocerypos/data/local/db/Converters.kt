package com.nexus.grocerypos.data.local.db

import androidx.room.TypeConverter
import com.nexus.grocerypos.domain.model.DiscountType
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.model.PaymentMethod
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.model.StockUnit
import com.nexus.grocerypos.domain.model.UserRole

class Converters {
    @TypeConverter fun toUserRole(value: String) = UserRole.valueOf(value)
    @TypeConverter fun fromUserRole(value: UserRole) = value.name

    @TypeConverter fun toStockUnit(value: String) = StockUnit.valueOf(value)
    @TypeConverter fun fromStockUnit(value: StockUnit) = value.name

    @TypeConverter fun toDiscountType(value: String) = DiscountType.valueOf(value)
    @TypeConverter fun fromDiscountType(value: DiscountType) = value.name

    @TypeConverter fun toPaymentMethod(value: String) = PaymentMethod.valueOf(value)
    @TypeConverter fun fromPaymentMethod(value: PaymentMethod) = value.name

    @TypeConverter fun toInventoryTransactionType(value: String) = InventoryTransactionType.valueOf(value)
    @TypeConverter fun fromInventoryTransactionType(value: InventoryTransactionType) = value.name

    @TypeConverter fun toPurchaseOrderStatus(value: String) = PurchaseOrderStatus.valueOf(value)
    @TypeConverter fun fromPurchaseOrderStatus(value: PurchaseOrderStatus) = value.name
}
