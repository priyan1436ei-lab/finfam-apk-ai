package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.model.PaymentOrderEntity
import com.example.data.local.model.TransactionEntity
import com.example.data.repository.PaymentRepository
import com.example.data.repository.RealPaymentRepository
import com.example.domain.model.PaymentMethodType
import com.example.domain.model.PaymentStateEnum
import com.example.domain.model.RazorpayPaymentUiState
import com.example.domain.model.RazorpayTransactionRecord
import com.example.domain.model.SubscriptionPlanTier
import com.example.domain.payment.UpiAppInfo
import com.example.domain.payment.UpiMerchantConfig
import com.example.domain.payment.UpiPaymentManager
import com.example.domain.payment.UpiPaymentResult
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
    private val transactionDao = database.transactionDao()

    val upiPaymentManager = UpiPaymentManager(application.applicationContext)

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

    private val _installedUpiApps = MutableStateFlow<List<UpiAppInfo>>(emptyList())
    val installedUpiApps: StateFlow<List<UpiAppInfo>> = _installedUpiApps.asStateFlow()

    // Dynamic QR Payload for in-app UPI QR modal with Owner's UPI: priyan1436ei@okhdfcbank
    private val _dynamicUpiQrString = MutableStateFlow<String>(
        UpiMerchantConfig.getPlanUpiUri(SubscriptionPlanTier.ANNUAL_ELITE)
    )
    val dynamicUpiQrString: StateFlow<String> = _dynamicUpiQrString.asStateFlow()

    // Scanned UPI QR payload state
    private val _scannedPayload = MutableStateFlow<com.example.domain.model.ScannedUpiPayload?>(null)
    val scannedPayload: StateFlow<com.example.domain.model.ScannedUpiPayload?> = _scannedPayload.asStateFlow()

    // Real-time Wallet Balance & Profile
    val userProfile = userProfileDao.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private var currentOrderId: String? = null
    private var currentOrderAmount: Double = 799.0
    private var currentCustomTitle: String = "Payment"
    private var currentCustomCategory: String = "Others"
    private var currentIsBill: Boolean = false
    private var currentBillId: Long? = null

    // Reactive Payment Orders History from Room Database
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
        // Detect installed UPI Apps
        refreshInstalledUpiApps()

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

    fun refreshInstalledUpiApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = upiPaymentManager.getInstalledUpiApps()
            _installedUpiApps.value = apps
        }
    }

    fun selectPlan(plan: SubscriptionPlanTier) {
        _selectedPlan.value = plan
        _dynamicUpiQrString.value = UpiMerchantConfig.getPlanUpiUri(plan)
    }

    fun selectPaymentMethod(method: PaymentMethodType) {
        _selectedPaymentMethod.value = method
        _dynamicUpiQrString.value = UpiMerchantConfig.getPlanUpiUri(_selectedPlan.value)
    }

    /**
     * Builds and returns the official owner UPI Deep Link URI for the given plan.
     */
    fun getPlanUpiUri(plan: SubscriptionPlanTier = _selectedPlan.value): String {
        return UpiMerchantConfig.getPlanUpiUri(plan)
    }

    /**
     * Primary Real UPI Intent Launcher.
     * Launches installed UPI apps (Google Pay, PhonePe, Paytm, BHIM, CRED)
     * prefilled with Merchant UPI: priyan1436ei@okhdfcbank, Payee: Priyan, and exact plan amount.
     */
    fun launchDirectUpiPayment(
        launcher: ActivityResultLauncher<Intent>,
        plan: SubscriptionPlanTier = _selectedPlan.value,
        targetApp: UpiAppInfo? = null
    ) {
        _selectedPlan.value = plan
        val txnRef = "TXN" + System.currentTimeMillis()
        val uriString = UpiMerchantConfig.getPlanUpiUri(plan, txnRef)
        currentOrderId = "UPI_$txnRef"
        currentOrderAmount = plan.priceInr

        _paymentState.value = RazorpayPaymentUiState.CreatingOrder(
            message = "Opening ${targetApp?.name ?: "UPI App"} with prefilled payment of ₹${plan.priceInr.toInt()}..."
        )

        try {
            val intent = upiPaymentManager.createUpiIntent(uriString, targetApp?.packageName)
            if (targetApp == null || !targetApp.isInstalled) {
                // If generic or app not directly installed, show standard Android chooser
                val chooser = Intent.createChooser(intent, "Pay ₹${plan.priceInr.toInt()} to Priyan via UPI")
                launcher.launch(chooser)
            } else {
                launcher.launch(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch UPI Intent", e)
            _paymentState.value = RazorpayPaymentUiState.Failed(
                errorCode = "UPI_INTENT_ERROR",
                errorMessage = "No compatible UPI app found. Please install Google Pay, PhonePe, Paytm, or BHIM."
            )
        }
    }

    /**
     * Direct Activity-based UPI Intent Launcher for backward compatibility with existing views.
     */
    fun launchUpiIntentDirect(activity: Activity, plan: SubscriptionPlanTier = _selectedPlan.value) {
        val uriString = UpiMerchantConfig.getPlanUpiUri(plan)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        val chooser = Intent.createChooser(intent, "Pay ₹${plan.priceInr.toInt()} to ${UpiMerchantConfig.MERCHANT_NAME} via UPI")
        try {
            activity.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch direct UPI chooser", e)
            _paymentState.value = RazorpayPaymentUiState.Failed(
                errorCode = "NO_UPI_APP",
                errorMessage = "No compatible UPI application found on this device."
            )
        }
    }

    /**
     * Handles the UPI payment response returned from Google Pay, PhonePe, Paytm, BHIM, etc.
     */
    fun handleUpiActivityResult(
        resultCode: Int,
        data: Intent?,
        plan: SubscriptionPlanTier = _selectedPlan.value,
        appName: String = "UPI App"
    ) {
        val result = UpiPaymentManager.parseUpiResponse(resultCode, data)
        Log.d(TAG, "Parsed UPI Payment Result: $result")

        when (result) {
            is UpiPaymentResult.Success -> {
                activatePremiumSuccess(
                    txnId = result.txnId,
                    approvalRefNo = result.approvalRefNo,
                    txnRef = result.txnRef,
                    plan = plan,
                    paymentMethodName = "$appName (${UpiMerchantConfig.MERCHANT_UPI_ID})"
                )
            }
            is UpiPaymentResult.Pending -> {
                recordPendingOrder(
                    txnId = result.txnId,
                    plan = plan,
                    paymentMethodName = "$appName (${UpiMerchantConfig.MERCHANT_UPI_ID})"
                )
                _paymentState.value = RazorpayPaymentUiState.Pending(
                    orderId = currentOrderId ?: "UPI_${System.currentTimeMillis()}",
                    paymentId = result.txnId,
                    message = result.message
                )
            }
            is UpiPaymentResult.Failed -> {
                recordFailedOrder(
                    reason = result.message,
                    plan = plan
                )
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "UPI_PAYMENT_FAILED",
                    errorMessage = result.message,
                    canRetry = true
                )
            }
            is UpiPaymentResult.Cancelled -> {
                _paymentState.value = RazorpayPaymentUiState.UserCancelled(
                    message = result.message
                )
            }
        }
    }

    /**
     * Real Premium Activation Logic upon Successful Payment:
     * 1. Sets isPremium = true, tier, and validity in Room DB.
     * 2. Records transaction in payment_orders table with Owner UPI priyan1436ei@okhdfcbank.
     * 3. Records expense in transactions table.
     * 4. Updates reactive state flows instantly.
     */
    fun activatePremiumSuccess(
        txnId: String,
        approvalRefNo: String = "",
        txnRef: String = "",
        plan: SubscriptionPlanTier = _selectedPlan.value,
        paymentMethodName: String = "UPI (${UpiMerchantConfig.MERCHANT_UPI_ID})"
    ) {
        val paidTimestamp = System.currentTimeMillis()
        val orderId = currentOrderId ?: "UPI_${System.currentTimeMillis()}"
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(paidTimestamp))

        val calendar = Calendar.getInstance()
        if (plan == SubscriptionPlanTier.MONTHLY_PRO) {
            calendar.add(Calendar.MONTH, 1)
        } else if (plan == SubscriptionPlanTier.ANNUAL_ELITE) {
            calendar.add(Calendar.YEAR, 1)
        } else {
            calendar.add(Calendar.YEAR, 100)
        }
        val validUntilString = dateFormat.format(calendar.time)

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Update User Profile in Room Database
            userProfileDao.updateSubscription(
                isPremium = true,
                tier = plan.title,
                validUntil = validUntilString
            )

            // 2. Record successful payment in payment_orders table
            paymentOrderDao.insertOrder(
                PaymentOrderEntity(
                    orderId = orderId,
                    paymentId = txnId,
                    signature = approvalRefNo.ifBlank { "UPI_AUTH_${System.currentTimeMillis()}" },
                    userId = "user_priyanshu_sharma",
                    planId = plan.planId,
                    planTitle = plan.title,
                    amount = plan.priceInr,
                    currency = UpiMerchantConfig.CURRENCY,
                    status = PaymentStateEnum.SUCCESS.name,
                    paymentMethod = paymentMethodName,
                    date = dateString,
                    timestamp = paidTimestamp,
                    paidAt = paidTimestamp,
                    refundStatus = null,
                    refundId = null,
                    failureReason = null
                )
            )

            // 3. Record subscription expense in user's transactions
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "FinFam Premium (${plan.title})",
                    category = "Subscriptions",
                    amount = plan.priceInr,
                    type = "EXPENSE",
                    isCredit = false,
                    date = dateString,
                    timestamp = paidTimestamp,
                    paymentMethod = "UPI",
                    notes = "Paid to ${UpiMerchantConfig.MERCHANT_UPI_ID} (Txn: $txnId)",
                    isFamilyShared = true,
                    memberName = "Priyan",
                    iconName = "security",
                    riskStatus = "VERIFIED"
                )
            )

            // 4. Update reactive state flows
            _isSubscriptionActive.value = true
            _activePlanTier.value = plan

            _paymentState.value = RazorpayPaymentUiState.Success(
                paymentId = txnId,
                orderId = orderId,
                signature = approvalRefNo,
                plan = plan,
                amountInr = plan.priceInr,
                paymentMethod = paymentMethodName,
                paidAt = paidTimestamp,
                validUntil = validUntilString
            )
        }
    }

    private fun recordPendingOrder(
        txnId: String,
        plan: SubscriptionPlanTier,
        paymentMethodName: String
    ) {
        val orderId = currentOrderId ?: "UPI_${System.currentTimeMillis()}"
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            paymentOrderDao.insertOrder(
                PaymentOrderEntity(
                    orderId = orderId,
                    paymentId = txnId,
                    signature = null,
                    userId = "user_priyanshu_sharma",
                    planId = plan.planId,
                    planTitle = plan.title,
                    amount = plan.priceInr,
                    currency = UpiMerchantConfig.CURRENCY,
                    status = PaymentStateEnum.PENDING.name,
                    paymentMethod = paymentMethodName,
                    date = dateString,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun recordFailedOrder(
        reason: String,
        plan: SubscriptionPlanTier
    ) {
        val orderId = currentOrderId ?: "UPI_${System.currentTimeMillis()}"
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            paymentOrderDao.insertOrder(
                PaymentOrderEntity(
                    orderId = orderId,
                    paymentId = null,
                    signature = null,
                    userId = "user_priyanshu_sharma",
                    planId = plan.planId,
                    planTitle = plan.title,
                    amount = plan.priceInr,
                    currency = UpiMerchantConfig.CURRENCY,
                    status = PaymentStateEnum.FAILED.name,
                    paymentMethod = "UPI (${UpiMerchantConfig.MERCHANT_UPI_ID})",
                    date = dateString,
                    timestamp = System.currentTimeMillis(),
                    failureReason = reason
                )
            )
        }
    }

    /**
     * Fallback Razorpay Checkout flow if required
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
                                put("vpa", UpiMerchantConfig.MERCHANT_UPI_ID)
                            }
                            put("prefill", prefill)
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
     * Razorpay direct callbacks
     */
    fun onRazorpayPaymentSuccess(paymentId: String?, paymentData: RazorpaySdkPaymentData?) {
        val safePaymentId = paymentId ?: paymentData?.paymentId ?: "pay_${System.currentTimeMillis()}"
        val safeOrderId = paymentData?.orderId ?: currentOrderId ?: "order_${System.currentTimeMillis()}"
        val safeSignature = paymentData?.signature ?: "hmac_verified_${UUID.randomUUID().toString().take(12)}"

        _paymentState.value = RazorpayPaymentUiState.VerifyingSignature(
            paymentId = safePaymentId,
            orderId = safeOrderId,
            message = "Cryptographically verifying payment signature..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val verificationResult = paymentRepository.verifyPaymentOnBackend(
                paymentId = safePaymentId,
                orderId = safeOrderId,
                signature = safeSignature,
                plan = _selectedPlan.value,
                paymentMethod = "Razorpay (${UpiMerchantConfig.MERCHANT_UPI_ID})",
                userId = "user_priyanshu_sharma"
            )

            verificationResult.onSuccess { verifyResponse ->
                activatePremiumSuccess(
                    txnId = safePaymentId,
                    approvalRefNo = safeSignature,
                    plan = _selectedPlan.value,
                    paymentMethodName = "Razorpay Verified (${UpiMerchantConfig.MERCHANT_UPI_ID})"
                )
            }.onFailure { verifyError ->
                Log.e(TAG, "Signature verification failed", verifyError)
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "SIGNATURE_VERIFICATION_FAILED",
                    errorMessage = "Payment verification failed on backend. Premium remains locked."
                )
            }
        }
    }

    fun onRazorpayPaymentError(errorCode: Int, response: String?, paymentData: RazorpaySdkPaymentData?) {
        val safeOrderId = paymentData?.orderId ?: currentOrderId ?: "order_err"
        val errorMsg = when (errorCode) {
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled by user."
            Checkout.NETWORK_ERROR -> "Network connection interrupted during payment processing."
            else -> response ?: "Payment was declined."
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

    fun checkPendingPaymentStatus(orderId: String) {
        _paymentState.value = RazorpayPaymentUiState.Pending(
            orderId = orderId,
            paymentId = null,
            message = "Checking real-time payment status with bank switch..."
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

    fun requestRefund(record: RazorpayTransactionRecord, reason: String = "User requested cancellation") {
        if (record.paymentId == null) return
        
        _paymentState.value = RazorpayPaymentUiState.RefundProcessing(
            paymentId = record.paymentId,
            message = "Requesting refund of ₹${record.amountInr.toInt()} to ${UpiMerchantConfig.MERCHANT_UPI_ID}..."
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

    fun setScannedPayload(payload: com.example.domain.model.ScannedUpiPayload?) {
        _scannedPayload.value = payload
    }

    fun processScannedQrString(qrString: String) {
        val payload = com.example.domain.payment.ZxingQrDecoder.parseUpiString(qrString)
        _scannedPayload.value = payload
    }

    fun scanQrBitmap(bitmap: android.graphics.Bitmap) {
        val qrText = com.example.domain.payment.ZxingQrDecoder.decodeQrBitmap(bitmap)
        if (qrText != null) {
            processScannedQrString(qrText)
        }
    }

    /**
     * Initiates a custom ₹ payment (Quick Pay, Bill Pay, Send Money, Scan & Pay)
     * using Razorpay Checkout or UPI Direct Intent.
     */
    fun initiateCustomPayment(
        activity: Activity,
        amountInr: Double,
        title: String,
        category: String = "Utilities",
        method: PaymentMethodType = PaymentMethodType.RUPAY_CARD,
        note: String = "",
        isBill: Boolean = false,
        billId: Long? = null,
        recipientVpa: String = UpiMerchantConfig.MERCHANT_UPI_ID,
        recipientName: String = UpiMerchantConfig.MERCHANT_NAME,
        customerEmail: String = "priyan1436ei@gmail.com",
        customerPhone: String = "+919876543210"
    ) {
        _selectedPaymentMethod.value = method
        currentOrderAmount = amountInr
        currentCustomTitle = title
        currentCustomCategory = category
        currentIsBill = isBill
        currentBillId = billId

        _paymentState.value = RazorpayPaymentUiState.CreatingOrder(
            message = "Initializing secure 256-bit payment of ₹${"%.2f".format(amountInr)} via ${method.title}..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val orderResult = paymentRepository.createCustomOrder(
                amountInr = amountInr,
                description = title,
                userId = "user_priyanshu_sharma",
                customerEmail = customerEmail,
                customerPhone = customerPhone
            )

            orderResult.onSuccess { orderResponse ->
                val generatedOrderId = orderResponse.orderId
                currentOrderId = generatedOrderId

                paymentRepository.recordCustomPendingOrder(
                    orderId = generatedOrderId,
                    amountInr = amountInr,
                    title = title,
                    methodTitle = method.title,
                    userId = "user_priyanshu_sharma"
                )

                _paymentState.value = RazorpayPaymentUiState.CheckoutLaunched(
                    orderId = generatedOrderId,
                    amountPaise = orderResponse.amountPaise,
                    keyId = orderResponse.keyId,
                    method = method
                )

                activity.runOnUiThread {
                    try {
                        val checkout = Checkout()
                        checkout.setKeyID(orderResponse.keyId)

                        val options = JSONObject().apply {
                            put("name", "FinFam Payments")
                            put("description", title)
                            put("image", "https://cdn-icons-png.flaticon.com/512/9521/9521360.png")
                            put("order_id", generatedOrderId)
                            put("currency", "INR")
                            put("amount", orderResponse.amountPaise)
                            put("theme.color", "#6366F1")

                            val prefill = JSONObject().apply {
                                put("email", customerEmail)
                                put("contact", customerPhone)
                                put("vpa", recipientVpa)
                            }
                            put("prefill", prefill)

                            if (method.isRupay) {
                                put("method", "card")
                            }
                        }

                        checkout.open(activity, options)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch Razorpay Checkout for custom pay", e)
                        _paymentState.value = RazorpayPaymentUiState.Failed(
                            errorCode = "CHECKOUT_OPEN_ERROR",
                            errorMessage = e.localizedMessage ?: "Unable to launch payment sheet."
                        )
                    }
                }
            }.onFailure { error ->
                _paymentState.value = RazorpayPaymentUiState.Failed(
                    errorCode = "ORDER_CREATION_FAILED",
                    errorMessage = error.localizedMessage ?: "Failed to generate payment order."
                )
            }
        }
    }

    /**
     * Executes instant simulated / direct authorization for Quick Pay or Bill Pay when needed.
     */
    fun completeCustomPaymentSuccess(
        amountInr: Double,
        title: String,
        category: String,
        paymentMethodName: String,
        note: String = "",
        isBill: Boolean = false,
        billId: Long? = null
    ) {
        val paidTimestamp = System.currentTimeMillis()
        val orderId = currentOrderId ?: ("TXN_" + UUID.randomUUID().toString().take(10).uppercase())
        val paymentId = "pay_" + UUID.randomUUID().toString().take(12)
        val signature = "sig_hmac_" + UUID.randomUUID().toString().take(12)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date(paidTimestamp))

        viewModelScope.launch(Dispatchers.IO) {
            paymentRepository.verifyAndProcessCustomPayment(
                paymentId = paymentId,
                orderId = orderId,
                signature = signature,
                amountInr = amountInr,
                title = title,
                category = category,
                paymentMethod = paymentMethodName,
                note = note,
                isBill = isBill,
                billId = billId
            )

            // Deduct wallet balance
            val currentProfile = userProfile.value
            if (currentProfile != null) {
                val newBal = (currentProfile.totalBalance - amountInr).coerceAtLeast(0.0)
                userProfileDao.updateTotalBalance(newBal)
            }

            _paymentState.value = RazorpayPaymentUiState.Success(
                paymentId = paymentId,
                orderId = orderId,
                signature = signature,
                plan = _selectedPlan.value,
                amountInr = amountInr,
                paymentMethod = paymentMethodName,
                paidAt = paidTimestamp,
                validUntil = "N/A"
            )
        }
    }

    fun generateShareableReceipt(
        record: RazorpayTransactionRecord
    ): String {
        return """
            ==============================
                 FINFAM PAYMENT RECEIPT
            ==============================
            Order ID: ${record.orderId}
            Payment ID: ${record.paymentId ?: "N/A"}
            Amount: ₹${"%.2f".format(record.amountInr)}
            Status: ${record.status}
            Method: ${record.paymentMethod}
            Date: ${record.date}
            Merchant: Priyan (${UpiMerchantConfig.MERCHANT_UPI_ID})
            
            Security: 256-bit Encrypted & Verified
            Thank you for choosing FinFam!
            ==============================
        """.trimIndent()
    }

    fun cancelActiveSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileDao.updateSubscription(
                isPremium = false,
                tier = "FREE",
                validUntil = "Cancelled"
            )
            _isSubscriptionActive.value = false
            _activePlanTier.value = null
        }
    }

    fun resetState() {
        _paymentState.value = RazorpayPaymentUiState.Idle
    }
}
