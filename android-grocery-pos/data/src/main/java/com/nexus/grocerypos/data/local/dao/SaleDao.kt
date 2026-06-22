package com.nexus.grocerypos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nexus.grocerypos.data.local.entity.SaleEntity
import com.nexus.grocerypos.data.local.entity.SaleLineItemEntity
import com.nexus.grocerypos.data.local.entity.SalePaymentEntity
import com.nexus.grocerypos.data.local.entity.SaleWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Transaction
    @Query(
        """
        SELECT * FROM sales
        WHERE isVoided = 0
          AND (:from IS NULL OR createdAt >= :from)
          AND (:to IS NULL OR createdAt <= :to)
        ORDER BY createdAt DESC
        """
    )
    fun observeSales(from: Long?, to: Long?): Flow<List<SaleWithDetails>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SaleWithDetails?

    @Transaction
    @Query("SELECT * FROM sales WHERE customerId = :customerId AND isVoided = 0 ORDER BY createdAt DESC")
    suspend fun getForCustomer(customerId: Long): List<SaleWithDetails>

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertLineItems(items: List<SaleLineItemEntity>)

    @Insert
    suspend fun insertPayments(payments: List<SalePaymentEntity>)

    @Query("UPDATE sales SET isVoided = 1 WHERE id = :id")
    suspend fun voidSale(id: Long)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun count(): Int

    @Query(
        """
        SELECT
            COALESCE(SUM(grandTotal), 0) AS revenue,
            COALESCE(SUM(totalCost), 0) AS cost,
            COUNT(*) AS transactionCount
        FROM sales
        WHERE isVoided = 0 AND createdAt BETWEEN :from AND :to
        """
    )
    suspend fun getSummaryRow(from: Long, to: Long): SalesSummaryRow

    @Query(
        """
        SELECT COALESCE(SUM(sli.quantity), 0) FROM sale_line_items sli
        INNER JOIN sales s ON s.id = sli.saleId
        WHERE s.isVoided = 0 AND s.createdAt BETWEEN :from AND :to
        """
    )
    suspend fun getItemsSold(from: Long, to: Long): Double

    @Query(
        """
        SELECT
            sli.productId AS productId,
            sli.productName AS productName,
            SUM(sli.quantity) AS quantitySold,
            SUM(sli.unitPrice * sli.quantity - sli.discountValue) AS revenue,
            SUM((sli.unitPrice - sli.unitCost) * sli.quantity) AS profit
        FROM sale_line_items sli
        INNER JOIN sales s ON s.id = sli.saleId
        WHERE s.isVoided = 0 AND s.createdAt BETWEEN :from AND :to
        GROUP BY sli.productId
        ORDER BY quantitySold DESC
        LIMIT :limit
        """
    )
    suspend fun getTopSellingProducts(from: Long, to: Long, limit: Int): List<TopSellingProductRow>

    @Query(
        """
        SELECT
            strftime('%Y-%m-%d', createdAt / 1000, 'unixepoch', 'localtime') AS dateLabel,
            COALESCE(SUM(grandTotal), 0) AS revenue,
            COALESCE(SUM(grandTotal - totalCost), 0) AS profit
        FROM sales
        WHERE isVoided = 0 AND createdAt BETWEEN :from AND :to
        GROUP BY dateLabel
        ORDER BY dateLabel ASC
        """
    )
    suspend fun getDailyTrend(from: Long, to: Long): List<DailySalesRow>
}

data class SalesSummaryRow(
    val revenue: Double,
    val cost: Double,
    val transactionCount: Int
)

data class TopSellingProductRow(
    val productId: Long,
    val productName: String,
    val quantitySold: Double,
    val revenue: Double,
    val profit: Double
)

data class DailySalesRow(
    val dateLabel: String,
    val revenue: Double,
    val profit: Double
)
