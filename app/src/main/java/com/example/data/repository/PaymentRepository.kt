package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.PaymentOrderEntity
import com.example.data.local.model.TransactionEntity
import com.example.data.model.PaymentData
import com.example.data.model.Subscription
import com.example.data.network.RealPaymentBackendClient
import com.example.domain.model.CreateOrderResponse
import com.example.domain.model.PaymentStateEnum
import com.example.domain.model.RefundPaymentResponse
import com.example.domain.model.SubscriptionPlanTier
import com.example.domain.model.VerifyPaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface PaymentRepository {

    val paymentHistory: Flow<List<PaymentData>>

    val activeSubscription: Flow<Subscription?>

    suspend fun createServerOrder(
        plan: SubscriptionPlanTier,
        userId: String = "user_priyanshu_sharma",
        customerEmail: String? = null,
        customerPhone: String? = null
    ): Result<CreateOrderResponse>

    suspend fun createCustomOrder(
        amountInr: Double,
        description: String = "FinFam Payment",
        userId: String = "user_priyanshu_sharma",
        customerEmail: String? = null,
        customerPhone: String? = null
    ): Result<CreateOrderResponse>

    suspend fun verifyPaymentOnBackend(
        paymentId: String,
        orderId: String,
        signature: String,
        plan: SubscriptionPlanTier,
        paymentMethod: String = "UPI",
        userId: String = "user_priyanshu_sharma"
    ): Result<VerifyPaymentResponse>

    suspend fun verifyAndProcessCustomPayment(
        paymentId: String,
        orderId: String,
        signature: String,
        amountInr: Double,
        title: String,
        category: String = "Utilities",
        paymentMethod: String = "RuPay",
        note: String = "",
        isBill: Boolean = false,
        billId: Long? = null,
        userId: String = "user_priyanshu_sharma"
    ): Result<VerifyPaymentResponse>

    suspend fun requestRefund(
        paymentId: String,
        orderId: String,
        amount: Double,
        reason: String = "Customer requested cancellation",
        userId: String = "user_priyanshu_sharma"
    ): Result<RefundPaymentResponse>

    suspend fun recordPendingOrder(
        orderId: String,
        plan: SubscriptionPlanTier,
        methodTitle: String,
        userId: String = "user_priyanshu_sharma"
    )

    suspend fun recordCustomPendingOrder(
        orderId: String,
        amountInr: Double,
        title: String,
        methodTitle: String,
        userId: String = "user_priyanshu_sharma"
    )

    suspend fun markOrderFailed(
        orderId: String,
        reason: String
    )

    suspend fun checkPaymentStatus(orderId: String): PaymentOrderEntity?
}

class RealPaymentRepository(
    private val database: AppDatabase,
    private val backendClient: RealPaymentBackendClient = RealPaymentBackendClient()
) : PaymentRepository {

    private val paymentOrderDao = database.paymentOrderDao()
    private val userProfileDao = database.userProfileDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val billDao = database.billDao()

    override val paymentHistory: Flow<List<PaymentData>> = paymentOrderDao.getAllOrders().map { list ->
        list.map { entity ->
            PaymentData(
                orderId = entity.orderId,
                paymentId = entity.paymentId,
                signature = entity.signature,
                userId = entity.userId,
                planId = entity.planId,
                planTitle = entity.planTitle,
                amount = entity.amount,
                amountPaise = (entity.amount * 100).toLong(),
                currency = entity.currency,
                status = entity.status,
                paymentMethod = entity.paymentMethod,
                date = entity.date,
                timestamp = entity.timestamp,
                paidAt = entity.paidAt,
                refundStatus = entity.refundStatus,
                refundId = entity.refundId,
                failureReason = entity.failureReason
            )
        }
    }

    override val activeSubscription: Flow<Subscription?> = userProfileDao.getUserProfile().map { profile ->
        if (profile != null && profile.isPremium) {
            Subscription(
                id = "sub_${profile.id}",
                userId = "user_priyanshu_sharma",
                planId = if (profile.premiumTier.contains("Monthly", ignoreCase = true)) "premium_monthly"
                else if (profile.premiumTier.contains("Lifetime", ignoreCase = true)) "premium_lifetime"
                else "premium_annual",
                planTitle = profile.premiumTier,
                amount = if (profile.premiumTier.contains("Monthly", ignoreCase = true)) 199.0
                else if (profile.premiumTier.contains("Lifetime", ignoreCase = true)) 3999.0
                else 1499.0,
                status = "ACTIVE",
                validUntil = profile.premiumValidUntil,
                billingCycle = if (profile.premiumTier.contains("Monthly", ignoreCase = true)) "MONTHLY"
                else if (profile.premiumTier.contains("Lifetime", ignoreCase = true)) "LIFETIME"
                else "ANNUAL"
            )
        } else {
            null
        }
    }

    override suspend fun createServerOrder(
        plan: SubscriptionPlanTier,
        userId: String,
        customerEmail: String?,
        customerPhone: String?
    ): Result<CreateOrderResponse> = withContext(Dispatchers.IO) {
        backendClient.createOrder(plan = plan, userId = userId)
    }

    override suspend fun verifyPaymentOnBackend(
        paymentId: String,
        orderId: String,
        signature: String,
        plan: SubscriptionPlanTier,
        paymentMethod: String,
        userId: String
    ): Result<VerifyPaymentResponse> = withContext(Dispatchers.IO) {
        val verifyResult = backendClient.verifyPayment(
            paymentId = paymentId,
            orderId = orderId,
            signature = signature,
            plan = plan,
            paymentMethod = paymentMethod,
            userId = userId
        )

        verifyResult.onSuccess { response ->
            val paidTimestamp = System.currentTimeMillis()

            // 1. Update Room DB payment order status to SUCCESS
            paymentOrderDao.updateOrderStatus(
                orderId = orderId,
                status = PaymentStateEnum.SUCCESS.name,
                paymentId = paymentId,
                signature = signature,
                paidAt = paidTimestamp
            )

            // 2. Insert verified transaction into financial ledger
            transactionDao.insertTransaction(
                TransactionEntity(
                    id = 0,
                    title = "${plan.title} Upgrade",
                    category = "Subscriptions",
                    amount = plan.priceInr,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "Today, Just now",
                    timestamp = paidTimestamp,
                    paymentMethod = paymentMethod,
                    notes = "Order: $orderId • Ref: $paymentId",
                    iconName = "shield",
                    riskStatus = "VERIFIED",
                    memberName = "Priyanshu"
                )
            )

            // 3. Update User Profile Subscription & Valid Until
            userProfileDao.updateSubscription(
                isPremium = true,
                tier = plan.title,
                validUntil = response.validUntil
            )
        }.onFailure { error ->
            paymentOrderDao.markOrderFailed(orderId, error.message ?: "Verification failed")
        }

        verifyResult
    }

    override suspend fun recordPendingOrder(
        orderId: String,
        plan: SubscriptionPlanTier,
        methodTitle: String,
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        paymentOrderDao.insertOrder(
            PaymentOrderEntity(
                orderId = orderId,
                paymentId = null,
                signature = null,
                userId = userId,
                planId = plan.planId,
                planTitle = plan.title,
                amount = plan.priceInr,
                currency = "INR",
                status = PaymentStateEnum.CREATED.name,
                paymentMethod = methodTitle,
                date = dateStr,
                timestamp = System.currentTimeMillis()
            )
        )
        Unit
    }

    override suspend fun createCustomOrder(
        amountInr: Double,
        description: String,
        userId: String,
        customerEmail: String?,
        customerPhone: String?
    ): Result<CreateOrderResponse> = withContext(Dispatchers.IO) {
        backendClient.createCustomOrder(amountInr = amountInr, description = description, userId = userId)
    }

    override suspend fun verifyAndProcessCustomPayment(
        paymentId: String,
        orderId: String,
        signature: String,
        amountInr: Double,
        title: String,
        category: String,
        paymentMethod: String,
        note: String,
        isBill: Boolean,
        billId: Long?,
        userId: String
    ): Result<VerifyPaymentResponse> = withContext(Dispatchers.IO) {
        val paidTimestamp = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(paidTimestamp))

        // 1. Update payment order
        paymentOrderDao.updateOrderStatus(
            orderId = orderId,
            status = PaymentStateEnum.SUCCESS.name,
            paymentId = paymentId,
            signature = signature,
            paidAt = paidTimestamp
        )

        // 2. Insert verified transaction into financial ledger
        transactionDao.insertTransaction(
            TransactionEntity(
                id = 0,
                title = title,
                category = category,
                amount = amountInr,
                type = "EXPENSE",
                isCredit = false,
                date = "Today, Just now",
                timestamp = paidTimestamp,
                paymentMethod = paymentMethod,
                notes = if (note.isNotBlank()) note else "Order: $orderId • Ref: $paymentId",
                iconName = if (isBill) "receipt_long" else "payments",
                riskStatus = "VERIFIED",
                memberName = "Priyanshu"
            )
        )

        // 3. Auto-update budget spent for this category
        try {
            budgetDao.addSpendingToCategory(category, amountInr)
        } catch (e: Exception) {
            // Ignore if category budget not set
        }

        // 4. Mark bill as paid if this was a bill payment
        if (isBill && billId != null) {
            billDao.markBillPaymentStatus(billId, true)
        }

        Result.success(
            VerifyPaymentResponse(
                success = true,
                status = "SUCCESS",
                paymentId = paymentId,
                orderId = orderId,
                validUntil = "N/A",
                message = "Payment of ₹$amountInr processed and recorded."
            )
        )
    }

    override suspend fun recordCustomPendingOrder(
        orderId: String,
        amountInr: Double,
        title: String,
        methodTitle: String,
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        paymentOrderDao.insertOrder(
            PaymentOrderEntity(
                orderId = orderId,
                paymentId = null,
                signature = null,
                userId = userId,
                planId = "custom_pay",
                planTitle = title,
                amount = amountInr,
                currency = "INR",
                status = PaymentStateEnum.CREATED.name,
                paymentMethod = methodTitle,
                date = dateStr,
                timestamp = System.currentTimeMillis()
            )
        )
        Unit
    }

    override suspend fun markOrderFailed(
        orderId: String,
        reason: String
    ): Unit = withContext(Dispatchers.IO) {
        paymentOrderDao.updateOrderStatus(
            orderId = orderId,
            status = PaymentStateEnum.FAILED.name,
            paymentId = null,
            signature = null
        )
        paymentOrderDao.markOrderFailed(orderId, reason)
        Unit
    }

    override suspend fun checkPaymentStatus(orderId: String): PaymentOrderEntity? = withContext(Dispatchers.IO) {
        paymentOrderDao.getOrderByOrderId(orderId)
    }

    override suspend fun requestRefund(
        paymentId: String,
        orderId: String,
        amount: Double,
        reason: String,
        userId: String
    ): Result<RefundPaymentResponse> = withContext(Dispatchers.IO) {
        val refundResult = backendClient.requestRefund(
            paymentId = paymentId,
            orderId = orderId,
            amount = amount,
            reason = reason,
            userId = userId
        )

        refundResult.onSuccess { response ->
            paymentOrderDao.updateRefundStatus(
                orderId = orderId,
                status = PaymentStateEnum.REFUNDED.name,
                refundStatus = "REFUNDED",
                refundId = response.refundId
            )
        }

        refundResult
    }
}
