package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.BillEntity
import com.example.data.local.model.BudgetEntity
import com.example.data.local.model.EmiEntity
import com.example.data.local.model.FamilyMemberEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.PaymentOrderEntity
import com.example.data.local.model.ScanHistoryEntity
import com.example.data.local.model.TransactionEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalBalance = :newBalance WHERE id = 1")
    suspend fun updateTotalBalance(newBalance: Double)

    @Query("UPDATE user_profile SET healthScore = :score, previousHealthScore = :prevScore WHERE id = 1")
    suspend fun updateHealthScore(score: Int, prevScore: Int)

    @Query("UPDATE user_profile SET isPremium = :isPremium, premiumTier = :tier, premiumValidUntil = :validUntil WHERE id = 1")
    suspend fun updateSubscription(isPremium: Boolean, tier: String, validUntil: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isCredit = 0 ORDER BY timestamp DESC")
    fun getExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isCredit = 1 ORDER BY timestamp DESC")
    fun getIncomes(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isFamilyShared = 1 ORDER BY timestamp DESC")
    fun getFamilyTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY monthlyLimit DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET spent = spent + :amount WHERE category = :category")
    suspend fun addSpendingToCategory(category: String, amount: Double)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun depositToGoal(goalId: Long, amount: Double)

    @Query("UPDATE goals SET currentAmount = MAX(0, currentAmount - :amount) WHERE id = :goalId")
    suspend fun withdrawFromGoal(goalId: Long, amount: Double)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueTimestamp ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueTimestamp ASC")
    fun getPendingBills(): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("UPDATE bills SET isPaid = :isPaid WHERE id = :billId")
    suspend fun markBillPaymentStatus(billId: Long, isPaid: Boolean)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity): Long

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteMember(member: FamilyMemberEntity)
}

@Dao
interface PaymentOrderDao {
    @Query("SELECT * FROM payment_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<PaymentOrderEntity>>

    @Query("SELECT * FROM payment_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderByOrderId(orderId: String): PaymentOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PaymentOrderEntity): Long

    @Update
    suspend fun updateOrder(order: PaymentOrderEntity)

    @Query("UPDATE payment_orders SET status = :status, paymentId = :paymentId, signature = :signature, paidAt = :paidAt WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, paymentId: String?, signature: String?, paidAt: Long? = null)

    @Query("UPDATE payment_orders SET status = :status, refundStatus = :refundStatus, refundId = :refundId WHERE orderId = :orderId")
    suspend fun updateRefundStatus(orderId: String, status: String, refundStatus: String, refundId: String?)

    @Query("UPDATE payment_orders SET status = 'FAILED', failureReason = :reason WHERE orderId = :orderId")
    suspend fun markOrderFailed(orderId: String, reason: String)
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity): Long
}

@Dao
interface EmiDao {
    @Query("SELECT * FROM emis ORDER BY id ASC")
    fun getAllEmis(): Flow<List<EmiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmi(emi: EmiEntity): Long

    @Update
    suspend fun updateEmi(emi: EmiEntity)

    @Delete
    suspend fun deleteEmi(emi: EmiEntity)

    @Query("UPDATE emis SET paidAmount = paidAmount + :amount, paidTenureMonths = paidTenureMonths + 1, isPaidThisMonth = 1, lastPaymentDate = :date WHERE id = :emiId")
    suspend fun recordEmiPayment(emiId: Long, amount: Double, date: String)

    @Query("DELETE FROM emis WHERE id = :id")
    suspend fun deleteEmiById(id: Long)
}

