package com.example.domain.payment

import android.net.Uri
import com.example.domain.model.SubscriptionPlanTier

/**
 * Official Merchant & Owner UPI Configuration for FinFam.
 * App Owner: Priyan
 * Merchant UPI ID: priyan1436ei@okhdfcbank
 */
object UpiMerchantConfig {
    const val OWNER_NAME = "Priyan"
    const val MERCHANT_NAME = "Priyan"
    const val MERCHANT_UPI_ID = "priyan1436ei@okhdfcbank"
    const val CURRENCY = "INR"

    /**
     * Exact monthly URI (₹99):
     */
    fun getMonthlyUpiUri(transactionRef: String? = null): String {
        val base = "upi://pay?pa=$MERCHANT_UPI_ID&pn=${Uri.encode(MERCHANT_NAME)}&am=99&cu=$CURRENCY&tn=${Uri.encode("FinFam Premium Monthly")}"
        return if (transactionRef != null) "$base&tr=$transactionRef" else base
    }

    /**
     * Exact yearly URI (₹799):
     */
    fun getYearlyUpiUri(transactionRef: String? = null): String {
        val base = "upi://pay?pa=$MERCHANT_UPI_ID&pn=${Uri.encode(MERCHANT_NAME)}&am=799&cu=$CURRENCY&tn=${Uri.encode("FinFam Premium Yearly")}"
        return if (transactionRef != null) "$base&tr=$transactionRef" else base
    }

    /**
     * Dynamic UPI URI generator based on selected plan.
     */
    fun getPlanUpiUri(plan: SubscriptionPlanTier, transactionRef: String? = null): String {
        val amount = plan.priceInr.toInt()
        val note = "FinFam ${plan.title}"
        val base = "upi://pay?pa=$MERCHANT_UPI_ID&pn=${Uri.encode(MERCHANT_NAME)}&am=$amount&cu=$CURRENCY&tn=${Uri.encode(note)}"
        return if (transactionRef != null) "$base&tr=$transactionRef" else base
    }

    /**
     * Custom Amount UPI URI generator for Quick Pay, Bill Pay & Scan & Pay.
     */
    fun getCustomAmountUpiUri(
        amountInr: Double,
        recipientVpa: String = MERCHANT_UPI_ID,
        recipientName: String = MERCHANT_NAME,
        note: String = "FinFam Payment",
        transactionRef: String? = null
    ): String {
        val formattedAmount = if (amountInr % 1.0 == 0.0) amountInr.toInt().toString() else "%.2f".format(amountInr)
        val base = "upi://pay?pa=$recipientVpa&pn=${Uri.encode(recipientName)}&am=$formattedAmount&cu=$CURRENCY&tn=${Uri.encode(note)}"
        return if (transactionRef != null) "$base&tr=$transactionRef" else base
    }
}
