package com.example.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class SubscriptionPlanTier(
    val planId: String,
    val title: String,
    val priceInr: Double,
    val billingCycle: String,
    val discountBadge: String?,
    val savingsText: String?,
    val features: List<String>
) {
    MONTHLY_PRO(
        planId = "premium_monthly_99",
        title = "FinFam Premium Monthly",
        priceInr = 99.0,
        billingCycle = "/ Month",
        discountBadge = "ESSENTIAL",
        savingsText = "Billed monthly • Cancel anytime",
        features = listOf(
            "AI Financial Coach & Predictive Budgeting",
            "Unlimited Smart Receipt OCR Scanning",
            "Advanced Financial Health Score Analytics",
            "Family Wallet Sync (Up to 3 Members)",
            "Automatic Bill Tracking & SMS Reminders",
            "Real-Time UPI Scam Shield Audit"
        )
    ),
    QUARTERLY_PRO(
        planId = "premium_quarterly_249",
        title = "FinFam Premium Quarterly",
        priceInr = 249.0,
        billingCycle = "/ 3 Months",
        discountBadge = "POPULAR",
        savingsText = "Save ₹48 vs monthly plan",
        features = listOf(
            "Everything in Monthly Plan",
            "Family Vault (Up to 5 Members)",
            "Gemini 2.0 Flash AI Financial Insights",
            "Automated Bank & UPI Reconciliation",
            "Smart Budget Overspend Alerts",
            "Quarterly CA Tax & Expense Summaries"
        )
    ),
    RUPAY_SPECIAL_499(
        planId = "premium_rupay_499",
        title = "FinFam Premium RuPay Special",
        priceInr = 499.0,
        billingCycle = "/ 6 Months",
        discountBadge = "RUPAY SPECIAL (₹499)",
        savingsText = "Save 20% with RuPay & UPI auto-pay",
        features = listOf(
            "Full FinFam Premium Pro Access for 6 Months",
            "Zero Surcharge on RuPay Debit & Credit Cards",
            "Instant UPI Intent & Dynamic QR Scan",
            "AI Financial Coach & Automated Budgeting",
            "Real-Time UPI Scam Shield & Fraud Protection",
            "Family Vault Sync (Up to 6 Members)"
        )
    ),
    ANNUAL_ELITE(
        planId = "premium_annual_799",
        title = "FinFam Premium Yearly",
        priceInr = 799.0,
        billingCycle = "/ Year",
        discountBadge = "BEST VALUE (Save 33%)",
        savingsText = "Save ₹389/yr (33% discount)",
        features = listOf(
            "Everything in Quarterly Tier",
            "Full Family Wallet (Unlimited Members)",
            "Real-Time Gemini 2.0 Flash AI Financial Coach",
            "Export CA-Ready Tax & Accounting PDF Reports",
            "Priority 24/7 Dedicated Concierge Support",
            "VIP Scam Protection & Direct Dispute Assist"
        )
    ),
    LIFETIME_FOUNDER(
        planId = "premium_lifetime",
        title = "FinFam Lifetime Founder Shield",
        priceInr = 1999.0,
        billingCycle = "one-time",
        discountBadge = "LIMITED FOUNDER",
        savingsText = "Pay once, access FinFam Elite forever",
        features = listOf(
            "Perpetual Lifetime Family Access",
            "All Future AI Forecasting Models & Algorithms",
            "VIP Family Wealth Consultation Channel",
            "Exclusive Founder Badge on Family Dashboard"
        )
    )
}

enum class PaymentMethodType(
    val code: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val razorpayMethodCode: String,
    val isRupay: Boolean = false
) {
    RUPAY_CARD(
        code = "RUPAY_CARD",
        title = "RuPay Card (Debit & Credit)",
        subtitle = "Zero surcharge on all Indian RuPay cards",
        iconName = "rupay",
        razorpayMethodCode = "card",
        isRupay = true
    ),
    UPI(
        code = "UPI",
        title = "UPI (GPay, PhonePe, Paytm, BHIM)",
        subtitle = "Instant zero-fee transfer via any UPI app",
        iconName = "upi",
        razorpayMethodCode = "upi"
    ),
    CARD(
        code = "CARD",
        title = "Visa / Mastercard / Amex",
        subtitle = "International & Domestic Credit / Debit Cards",
        iconName = "card",
        razorpayMethodCode = "card"
    ),
    NET_BANKING(
        code = "NET_BANKING",
        title = "Net Banking",
        subtitle = "SBI, HDFC, ICICI, Axis, Kotak & 50+ Banks",
        iconName = "bank",
        razorpayMethodCode = "netbanking"
    ),
    WALLET(
        code = "WALLET",
        title = "Wallets",
        subtitle = "Paytm, PhonePe, Mobikwik, Amazon Pay",
        iconName = "wallet",
        razorpayMethodCode = "wallet"
    ),
    UPI_QR(
        code = "UPI_QR",
        title = "Scan & Pay QR (ZXing)",
        subtitle = "Scan UPI QR codes with real-time Scam Shield",
        iconName = "qr",
        razorpayMethodCode = "upi"
    )
}

enum class BillCategoryType(
    val code: String,
    val title: String,
    val iconName: String,
    val defaultAmount: Double
) {
    MOBILE_RECHARGE(
        code = "MOBILE",
        title = "Mobile Recharge",
        iconName = "phone",
        defaultAmount = 299.0
    ),
    ELECTRICITY(
        code = "ELECTRICITY",
        title = "Electricity Bill",
        iconName = "bolt",
        defaultAmount = 1450.0
    ),
    WATER(
        code = "WATER",
        title = "Water Utility",
        iconName = "water",
        defaultAmount = 380.0
    ),
    GAS(
        code = "GAS",
        title = "Piped Gas / LPG",
        iconName = "gas",
        defaultAmount = 950.0
    ),
    BROADBAND(
        code = "BROADBAND",
        title = "Broadband / WiFi",
        iconName = "wifi",
        defaultAmount = 799.0
    ),
    DTH(
        code = "DTH",
        title = "DTH / Cable TV",
        iconName = "tv",
        defaultAmount = 350.0
    )
}

data class ScannedUpiPayload(
    val vpa: String,
    val payeeName: String,
    val amount: Double?,
    val note: String?,
    val transactionRef: String?,
    val rawQrString: String,
    val isVerifiedMerchant: Boolean = true,
    val safetyScore: Int = 98,
    val riskLevel: String = "SAFE"
)

enum class PaymentStateEnum {
    CREATED,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUND_INITIATED,
    REFUNDED
}

sealed interface RazorpayPaymentUiState {
    data object Idle : RazorpayPaymentUiState
    
    data class CreatingOrder(
        val message: String = "Generating 256-bit encrypted Razorpay Order..."
    ) : RazorpayPaymentUiState
    
    data class CheckoutLaunched(
        val orderId: String,
        val amountPaise: Long,
        val keyId: String,
        val method: PaymentMethodType
    ) : RazorpayPaymentUiState
    
    data class VerifyingSignature(
        val paymentId: String,
        val orderId: String,
        val message: String = "Cryptographically verifying payment on backend..."
    ) : RazorpayPaymentUiState
    
    data class Success(
        val paymentId: String,
        val orderId: String,
        val signature: String,
        val plan: SubscriptionPlanTier,
        val amountInr: Double,
        val paymentMethod: String,
        val paidAt: Long = System.currentTimeMillis(),
        val validUntil: String
    ) : RazorpayPaymentUiState
    
    data class Failed(
        val errorCode: String,
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : RazorpayPaymentUiState
    
    data class Pending(
        val orderId: String,
        val paymentId: String?,
        val message: String = "Your payment is being processed. Please do not make another payment."
    ) : RazorpayPaymentUiState
    
    data class UserCancelled(
        val message: String = "Payment was cancelled by user."
    ) : RazorpayPaymentUiState
    
    data class RefundProcessing(
        val paymentId: String,
        val message: String = "Processing refund with Razorpay gateway..."
    ) : RazorpayPaymentUiState
    
    data class RefundSuccess(
        val paymentId: String,
        val refundId: String,
        val amount: Double
    ) : RazorpayPaymentUiState
}

data class RazorpayTransactionRecord(
    val orderId: String,
    val paymentId: String?,
    val signature: String?,
    val userId: String = "user_priyanshu_sharma",
    val planId: String,
    val planTitle: String = "FinFam Premium",
    val amountInr: Double,
    val currency: String = "INR",
    val status: String, // CREATED, PENDING, SUCCESS, FAILED, CANCELLED, REFUND_INITIATED, REFUNDED
    val paymentMethod: String = "UPI",
    val date: String = "Today",
    val timestamp: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val refundStatus: String? = null,
    val refundId: String? = null,
    val failureReason: String? = null
)

// Network DTOs for Backend REST API
@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "planId") val planId: String,
    @Json(name = "userId") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateOrderResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "orderId") val orderId: String,
    @Json(name = "amountPaise") val amountPaise: Long,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "keyId") val keyId: String,
    @Json(name = "planId") val planId: String,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentRequest(
    @Json(name = "razorpayPaymentId") val razorpayPaymentId: String,
    @Json(name = "razorpayOrderId") val razorpayOrderId: String,
    @Json(name = "razorpaySignature") val razorpaySignature: String,
    @Json(name = "planId") val planId: String,
    @Json(name = "paymentMethod") val paymentMethod: String? = null,
    @Json(name = "userId") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "status") val status: String,
    @Json(name = "paymentId") val paymentId: String,
    @Json(name = "orderId") val orderId: String,
    @Json(name = "validUntil") val validUntil: String,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class RefundPaymentRequest(
    @Json(name = "paymentId") val paymentId: String,
    @Json(name = "orderId") val orderId: String,
    @Json(name = "reason") val reason: String = "Customer requested refund",
    @Json(name = "userId") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class RefundPaymentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "refundId") val refundId: String,
    @Json(name = "status") val status: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentStatusResponse(
    @Json(name = "orderId") val orderId: String,
    @Json(name = "paymentId") val paymentId: String?,
    @Json(name = "status") val status: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "refundStatus") val refundStatus: String? = null
)
