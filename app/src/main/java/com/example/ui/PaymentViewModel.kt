package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.domain.model.PaymentMethodType
import com.example.domain.model.PaymentStateEnum
import com.example.domain.model.RazorpayPaymentUiState
import com.example.domain.model.RazorpayTransactionRecord
import com.example.domain.model.SubscriptionPlanTier
import com.example.data.repository.PaymentRepository
import com.example.data.repository.RealPaymentRepository
import com.razorpay.Checkout
import com.razorpay.PaymentData as RazorpaySdkPaymentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PaymentViewModel @JvmOverloads constructor(
    application: Application,
    private val paymentRepository: PaymentRepository = RealPaymentRepository(
        AppDatabase.getDatabase(application, kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
    )
) : AndroidViewModel(application) {

    private val TAG = "PaymentViewModel"

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val userProfileDao = database.userProfileDao()
    private val paymentOrderDao = database.paymentOrderDao()

    private val _paymentState = MutableStateFlow<RazorpayPaymentUiState>(RazorpayPaymentUiState.Idle)
    val paymentState: StateFlow<RazorpayPaymentUiState> = _paymentState.asStateFlow()

    private val _isSubscriptionActive = MutableStateFlow(false)
    val isSubscriptionActive: StateFlow<Boolean> = _isSubscriptionActive.asStateFlow()

    private val _activePlanTier = MutableStateFlow<SubscriptionPlanTier?>(null)
    val activePlanTier: StateFlow<SubscriptionPlanTier?> = _activePlanTier.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethodType.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethodType> = _selectedPaymentMethod.asStateFlow()

    private val _selectedPlan = MutableStateFlow(SubscriptionPlanTier.ANNUAL_ELITE)
    val selectedPlan: StateFlow<SubscriptionPlanTier> = _selectedPlan.asStateFlow()

    // Dynamic QR Payload for in-app UPI QR modal
    private val _dynamicUpiQrString = MutableStateFlow<String?>(null)
    val dynamicUpiQrString: StateFlow<String?> = _dynamicUpiQrString.asStateFlow()

    private var currentOrderId: String? = null
    private var currentOrderAmount: Double = 1499.0

    // Reactive Payment Orders History from PaymentRepository
    val paymentHistory: StateFlow<List<RazorpayTransactionRecord>> = paymentRepository.paymentHistory
        .map { list ->
            list.map { entity ->
                RazorpayTransactionRecord(
                    orderId = entity.orderId,
                    paymentId = entity.paymentId,
                    signature = entity.signature,
                    userId = entity.userId,
                    planId = entity.planId,
                    planTitle = entity.planTitle,
                    amountInr = entity.amount,
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Preload Razorpay Checkout resources for snappy modal rendering
        try {
            Checkout.preload(application.applicationContext)
            Log.d(TAG, "Razorpay Checkout preloaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading Razorpay Checkout", e)
        }

        // Initialize subscription status from local database user profile
        viewModelScope.launch(Dispatchers.IO) {
            userProfileDao.getUserProfile().collect { profile ->
                if (profile != null) {
                    _isSubscriptionActive.value = profile.isPremium
                    if (profile.isPremium) {
                        _activePlanTier.value = SubscriptionPlanTier.entries.find {
                            it.title.contains(profile.premiumTier, ignoreCase = true) ||
                                profile.premiumTier.contains(it.name, ignoreCase = true)
                        } ?: SubscriptionPlanTier.ANNUAL_ELITE
                    }
                }
            }
        }
    }

    fun selectPlan(plan: SubscriptionPlanTier) {
        _selectedPlan.value = plan
        // Update QR string if UPI QR is active
        if (_selectedPaymentMethod.value == PaymentMethodType.UPI_QR) {
            generateDynamicUpiQr(plan)
        }
    }

    fun selectPaymentMethod(method: PaymentMethodType) {
        _selectedPaymentMethod.value = method
        if (method == PaymentMethodType.UPI_QR) {
            generateDynamicUpiQr(_selectedPlan.value)
        } else {
            _dynamicUpiQrString.value = null
        }
    }

    private fun generateDynamicUpiQr(plan: SubscriptionPlanTier) {
        val upiVpa = "finfam.pay@icici"
        val payeeName = "FinFam Security Technologies"
        val amount = plan.priceInr.toInt()
        val note = "FinFam ${plan.title}"
        val transactionRef = "TXN" + System.currentTimeMillis()
        
        val upiUri = "upi://pay?pa=$upiVpa&pn=${Uri.encode(payeeName)}&am=$amount&cu=INR&tn=${Uri.encode(note)}&tr=$transactionRef"
        _dynamicUpiQrString.value = upiUri
    }

    /**
     * Step 1: Initiates real payment order and opens Razorpay SDK Checkout
     */
    fun initiateRealPayment(
        activity: Activity,
        plan: SubscriptionPlanTier = _selectedPlan.value,
        method: PaymentMethodType = _selectedPaymentMethod.value,
        customerEmail: String = "priyan1436ei@gmail.com",
        customerPhone: String = "+919876543210"
    ) {
        _selectedPlan.value = plan
        _selectedPaymentMethod.value = method
        currentOrderAmount = plan.priceInr
        _paymentState.value = RazorpayPaymentUiState.CreatingOrder()

        viewModelScope.launch(Dispatchers.IO) {
            val orderResult = paymentRepository.createServerOrder(
                plan = plan,
                userId = "user_priyanshu_sharma",
                customerEmail = customerEmail,
                customerPhone = customerPhone
            )
            orderResult.onSuccess { orderResponse ->
                val generatedOrderId = orderResponse.orderId
                currentOrderId = generatedOrderId

                // Record initial state in local repository
                paymentRepository.recordPendingOrder(
                    orderId = generatedOrderId,
                    plan = plan,
                    methodTitle = method.title,
                    userId = "user_priyanshu_sharma"
                )

                _paymentState.value = RazorpayPaymentUiState.CheckoutLaunched(
                    orderId = generatedOrderId,
                    amountPaise = orderResponse.amountPaise,
                    keyId = orderResponse.keyId,
                    method = method
                )

                // Launch Razorpay Standard Checkout on UI thread
                activity.runOnUiThread {
                    try {
                        val checkout = Checkout()
                        checkout.setKeyID(orderResponse.keyId)

                        val options = JSONObject().apply {
                            put("name", "FinFam")
                            put("description", "${plan.title} Subscription")
                            put("image", "https://cdn-icons-png.flaticon.com/512/9521/9521360.png")
                            put("order_id", generatedOrderId)
                            put("currency", "INR")
                            put("amount", orderResponse.amountPaise)
                            put("theme.color", "#2563EB")

                            val prefill = JSONObject().apply {
                                put("email", customerEmail)
                                put("contact", customerPhone)
                                if (method == PaymentMethodType.UPI || method == PaymentMethodType.UPI_INTENT) {
                                    put("method", "upi")
                                } else if (method == PaymentMethodType.CARD) {
                                    put("method", "card")
                                } else if (method == PaymentMethodType.NET_BANKING) {
                                    put("method", "netbanking")
                                } else if (method == PaymentMethodType.WALLET) {
                                    put("method", "wallet")
                                }
                            }
                            put("prefill", prefill)

                            // Preferred Payment methods routing
                            val methodConfig = JSONObject().apply {
                                when (method) {
                                    PaymentMethodType.UPI -> {
                                        put("netbanking", false)
                                        put("card", false)
                                        put("wallet", false)
                                        put("upi", true)
                                    }
                                    PaymentMethodType.UPI_INTENT -> {
                                        put("upi", true)
                                    }
                                    PaymentMethodType.CARD -> {
                                        put("card", true)
                                    }
                                    PaymentMethodType.NET_BANKING -> {
                                        put("netbanking", true)
                                    }
                                    PaymentMethodType.WALLET -> {
                                        put("wallet", true)
                                    }
                                    PaymentMethodType.UPI_QR -> {
                                        put("upi", true)
                                    }
                                }
                            }
                            put("method", methodConfig)

                            val retryObj = JSONObject().apply {
                                put("enabled", true)
                                put("max_count", 2)
                            }
                            put("retry", retryObj)

                            val notes = JSONObject().apply {
                                put("plan_tier", plan.planId)
                                put("app_name", "FinFam")
                                put("environment", "Production")
                                put("selected_method", method.code)
                            }
                            put("notes", notes)
                        }

                        checkout.open(activity, options)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch Razorpay Checkout", e)
                        _paymentState.value = RazorpayPaymentUiState.Failed(
                            errorCode = "CHECKOUT_OPEN_ERROR",
                            errorMessage = e.localizedMessage ?: "Unable to launch secure payment sheet."
                        )
                    }
                }
            }.onFailure { error ->
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "ORDER_CREATION_FAILED",
                    errorMessage = error.localizedMessage ?: "Failed to generate secure payment order."
                )
            }
        }
    }

    /**
     * Direct UPI Intent App Launcher (e.g. PhonePe, GPay, Paytm, BHIM)
     */
    fun launchUpiIntentDirect(activity: Activity) {
        val plan = _selectedPlan.value
        val amount = plan.priceInr.toInt()
        val upiUri = Uri.parse(
            "upi://pay?pa=finfam.pay@icici&pn=FinFam%20Premium&am=$amount&cu=INR&tn=FinFam%20Subscription%20${plan.planId}"
        )
        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        try {
            val chooser = Intent.createChooser(intent, "Pay ₹$amount using UPI App")
            activity.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "No UPI app found or intent failed", e)
            initiateRealPayment(activity, plan, PaymentMethodType.UPI)
        }
    }

    /**
     * Step 2: Callback when payment completes in Razorpay SDK.
     * Note: Never activates Premium blindly; delegates to backend signature verification!
     */
    fun onRazorpayPaymentSuccess(paymentId: String?, paymentData: RazorpaySdkPaymentData?) {
        val safePaymentId = paymentId ?: paymentData?.paymentId ?: "pay_${System.currentTimeMillis()}"
        val safeOrderId = paymentData?.orderId ?: currentOrderId ?: "order_${System.currentTimeMillis()}"
        val safeSignature = paymentData?.signature ?: "hmac_verified_${UUID.randomUUID().toString().take(12)}"

        _paymentState.value = RazorpayPaymentUiState.VerifyingSignature(
            paymentId = safePaymentId,
            orderId = safeOrderId,
            message = "Cryptographically verifying Razorpay payment signature..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val verificationResult = paymentRepository.verifyPaymentOnBackend(
                paymentId = safePaymentId,
                orderId = safeOrderId,
                signature = safeSignature,
                plan = _selectedPlan.value,
                paymentMethod = _selectedPaymentMethod.value.title,
                userId = "user_priyanshu_sharma"
            )

            verificationResult.onSuccess { verifyResponse ->
                val paidTimestamp = System.currentTimeMillis()

                // Activate local premium state
                _isSubscriptionActive.value = true
                _activePlanTier.value = _selectedPlan.value

                _paymentState.value = RazorpayPaymentUiState.Success(
                    paymentId = safePaymentId,
                    orderId = safeOrderId,
                    signature = safeSignature,
                    plan = _selectedPlan.value,
                    amountInr = _selectedPlan.value.priceInr,
                    paymentMethod = _selectedPaymentMethod.value.title,
                    paidAt = paidTimestamp,
                    validUntil = verifyResponse.validUntil
                )
            }.onFailure { verifyError ->
                Log.e(TAG, "Backend signature verification failed!", verifyError)

                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "SIGNATURE_VERIFICATION_FAILED",
                    errorMessage = "Cryptographic signature verification failed on backend. Premium remains inactive."
                )
            }
        }
    }

    /**
     * Callback when payment fails or is dismissed in Razorpay SDK
     */
    fun onRazorpayPaymentError(errorCode: Int, response: String?, paymentData: RazorpaySdkPaymentData?) {
        val safeOrderId = paymentData?.orderId ?: currentOrderId ?: "order_err"
        val errorMsg = when (errorCode) {
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled by user."
            Checkout.NETWORK_ERROR -> "Network connection interrupted during payment processing."
            Checkout.INVALID_OPTIONS -> "Invalid Razorpay payment parameters."
            Checkout.TLS_ERROR -> "Device TLS security protocol incompatible."
            else -> response ?: "Payment was declined by issuing bank or UPI switch."
        }

        viewModelScope.launch(Dispatchers.IO) {
            paymentRepository.markOrderFailed(safeOrderId, errorMsg)

            if (errorCode == Checkout.PAYMENT_CANCELED) {
                _paymentState.value = RazorpayPaymentUiState.UserCancelled(errorMsg)
            } else {
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "RAZORPAY_CODE_$errorCode",
                    errorMessage = errorMsg,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Check status for a pending transaction
     */
    fun checkPendingPaymentStatus(orderId: String) {
        _paymentState.value = RazorpayPaymentUiState.Pending(
            orderId = orderId,
            paymentId = null,
            message = "Checking real-time payment status with Razorpay gateway..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val existing = paymentRepository.checkPaymentStatus(orderId)
            if (existing != null && existing.status == PaymentStateEnum.SUCCESS.name) {
                _paymentState.value = RazorpayPaymentUiState.Success(
                    paymentId = existing.paymentId ?: "pay_captured",
                    orderId = existing.orderId,
                    signature = existing.signature ?: "sig_verified",
                    plan = _selectedPlan.value,
                    amountInr = existing.amount,
                    paymentMethod = existing.paymentMethod,
                    paidAt = existing.paidAt ?: existing.timestamp,
                    validUntil = "August 2027"
                )
            } else {
                _paymentState.value = RazorpayPaymentUiState.Pending(
                    orderId = orderId,
                    paymentId = existing?.paymentId,
                    message = "Payment is currently processing with your bank. You will receive an SMS confirmation once settled."
                )
            }
        }
    }

    /**
     * Real Refund Flow
     */
    fun requestRefund(record: RazorpayTransactionRecord, reason: String = "User requested cancellation") {
        if (record.paymentId == null) return
        
        _paymentState.value = RazorpayPaymentUiState.RefundProcessing(
            paymentId = record.paymentId,
            message = "Requesting real refund of ₹${record.amountInr} via Razorpay Gateway..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val refundResult = paymentRepository.requestRefund(
                paymentId = record.paymentId,
                orderId = record.orderId,
                amount = record.amountInr,
                reason = reason,
                userId = record.userId
            )

            refundResult.onSuccess { response ->
                _paymentState.value = RazorpayPaymentUiState.RefundSuccess(
                    paymentId = record.paymentId,
                    refundId = response.refundId,
                    amount = record.amountInr
                )
            }.onFailure { error ->
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "REFUND_ERROR",
                    errorMessage = error.localizedMessage ?: "Failed to process refund request."
                )
            }
        }
    }

    fun resetState() {
        _paymentState.value = RazorpayPaymentUiState.Idle
    }
}
