package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Encapsulates the Subscription Data Model for Firebase Firestore.
 * Maps to Firestore document `/subscriptions/{userId}`.
 */
@JsonClass(generateAdapter = true)
data class Subscription(
    @Json(name = "id") val id: String = "",
    @Json(name = "userId") val userId: String = "user_priyanshu_sharma",
    @Json(name = "planId") val planId: String = "premium_annual",
    @Json(name = "planTitle") val planTitle: String = "FinFam Premium Annual",
    @Json(name = "amount") val amount: Double = 1499.0,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "status") val status: String = "ACTIVE", // ACTIVE, EXPIRED, CANCELLED, PENDING
    @Json(name = "paymentMethod") val paymentMethod: String = "UPI",
    @Json(name = "billingCycle") val billingCycle: String = "ANNUAL", // MONTHLY, ANNUAL, LIFETIME
    @Json(name = "startDate") val startDate: Long = System.currentTimeMillis(),
    @Json(name = "endDate") val endDate: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
    @Json(name = "validUntil") val validUntil: String = "",
    @Json(name = "latestPaymentId") val latestPaymentId: String? = null,
    @Json(name = "latestOrderId") val latestOrderId: String? = null,
    @Json(name = "autoRenew") val autoRenew: Boolean = true,
    @Json(name = "transactionMetadata") val transactionMetadata: Map<String, String>? = null,
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

