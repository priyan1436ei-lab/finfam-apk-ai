package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Encapsulates the Payment Data Model for Firebase Firestore & Razorpay transaction lifecycle.
 * Maps to Firestore collection `/payments/{paymentId}` and `/orders/{orderId}`.
 */
@JsonClass(generateAdapter = true)
data class PaymentData(
    @Json(name = "orderId") val orderId: String = "",
    @Json(name = "paymentId") val paymentId: String? = null,
    @Json(name = "signature") val signature: String? = null,
    @Json(name = "userId") val userId: String = "user_priyanshu_sharma",
    @Json(name = "planId") val planId: String = "premium_annual",
    @Json(name = "planTitle") val planTitle: String = "FinFam Premium Annual",
    @Json(name = "amount") val amount: Double = 1499.0,
    @Json(name = "amountPaise") val amountPaise: Long = (amount * 100).toLong(),
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "status") val status: String = "CREATED", // CREATED, PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    @Json(name = "paymentMethod") val paymentMethod: String = "UPI",
    @Json(name = "userEmail") val userEmail: String? = null,
    @Json(name = "userContact") val userContact: String? = null,
    @Json(name = "date") val date: String = "",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "paidAt") val paidAt: Long? = null,
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "refundStatus") val refundStatus: String? = null,
    @Json(name = "refundId") val refundId: String? = null,
    @Json(name = "refundedAt") val refundedAt: Long? = null,
    @Json(name = "failureReason") val failureReason: String? = null,
    @Json(name = "transactionMetadata") val transactionMetadata: Map<String, String>? = null,
    @Json(name = "notes") val notes: Map<String, String>? = null
)

