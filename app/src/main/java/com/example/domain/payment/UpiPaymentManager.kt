package com.example.domain.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log

enum class UpiAppType(
    val displayName: String,
    val packageName: String,
    val shortName: String
) {
    GOOGLE_PAY(
        displayName = "Google Pay (GPay)",
        packageName = "com.google.android.apps.nbu.paisa.user",
        shortName = "GPay"
    ),
    PHONEPE(
        displayName = "PhonePe",
        packageName = "com.phonepe.app",
        shortName = "PhonePe"
    ),
    PAYTM(
        displayName = "Paytm UPI",
        packageName = "net.one97.paytm",
        shortName = "Paytm"
    ),
    BHIM(
        displayName = "BHIM UPI",
        packageName = "in.org.npci.upiapp",
        shortName = "BHIM"
    ),
    CRED(
        displayName = "CRED UPI",
        packageName = "com.dreamplug.androidapp",
        shortName = "CRED"
    ),
    AMAZON_PAY(
        displayName = "Amazon Pay",
        packageName = "in.amazon.mShop.android.shopping",
        shortName = "Amazon"
    ),
    GENERIC(
        displayName = "Other UPI Apps",
        packageName = "",
        shortName = "UPI App"
    )
}

data class UpiAppInfo(
    val name: String,
    val packageName: String,
    val appType: UpiAppType,
    val isInstalled: Boolean
)

sealed class UpiPaymentResult {
    data class Success(
        val txnId: String,
        val responseCode: String,
        val approvalRefNo: String,
        val txnRef: String,
        val rawResponse: String
    ) : UpiPaymentResult()

    data class Pending(
        val txnId: String,
        val message: String,
        val rawResponse: String
    ) : UpiPaymentResult()

    data class Failed(
        val message: String,
        val rawResponse: String
    ) : UpiPaymentResult()

    data class Cancelled(
        val message: String
    ) : UpiPaymentResult()
}

class UpiPaymentManager(private val context: Context) {

    companion object {
        private const val TAG = "UpiPaymentManager"

        /**
         * Parses NPCI standard UPI response string or intent extras.
         * UPI responses typically come as "txnId=...&responseCode=00&ApprovalRefNo=...&Status=SUCCESS&txnRef=..."
         */
        fun parseUpiResponse(resultCode: Int, intent: Intent?): UpiPaymentResult {
            if (intent == null) {
                return if (resultCode == Activity.RESULT_OK) {
                    UpiPaymentResult.Pending(
                        txnId = "TXN_${System.currentTimeMillis()}",
                        message = "Payment completed. Awaiting bank settlement confirmation.",
                        rawResponse = "RESULT_OK_EMPTY_INTENT"
                    )
                } else {
                    UpiPaymentResult.Cancelled("Payment was cancelled or dismissed.")
                }
            }

            // Extract raw response from Intent data or extras
            val rawData: String? = intent.getStringExtra("response")
                ?: intent.dataString
                ?: intent.extras?.getString("Status")
                ?: intent.extras?.getString("status")

            Log.d(TAG, "Raw UPI Response received (resultCode=$resultCode): $rawData")

            if (rawData.isNullOrBlank()) {
                // Check direct bundle extras
                val statusExtra = intent.getStringExtra("Status") ?: intent.getStringExtra("status")
                if (!statusExtra.isNullOrBlank()) {
                    return evaluateStatusString(statusExtra, intent)
                }
                return if (resultCode == Activity.RESULT_OK) {
                    UpiPaymentResult.Success(
                        txnId = "TXN_${System.currentTimeMillis()}",
                        responseCode = "00",
                        approvalRefNo = "APR_${System.currentTimeMillis().toString().takeLast(6)}",
                        txnRef = "REF_${System.currentTimeMillis().toString().takeLast(8)}",
                        rawResponse = "Activity.RESULT_OK"
                    )
                } else {
                    UpiPaymentResult.Cancelled("Payment was dismissed.")
                }
            }

            // Parse Key-Value pairs from raw response string (e.g. "txnId=UPI123&responseCode=0&Status=SUCCESS")
            val params = mutableMapOf<String, String>()
            val pairs = rawData.split("&")
            for (pair in pairs) {
                val parts = pair.split("=")
                if (parts.size >= 2) {
                    params[parts[0].trim().lowercase()] = parts.subList(1, parts.size).joinToString("=").trim()
                } else if (parts.size == 1) {
                    params[parts[0].trim().lowercase()] = ""
                }
            }

            val status = params["status"]?.uppercase() ?: ""
            val txnId = params["txnid"] ?: params["txn_id"] ?: "TXN_${System.currentTimeMillis()}"
            val responseCode = params["responsecode"] ?: params["response_code"] ?: "00"
            val approvalRefNo = params["approvalrefno"] ?: params["approval_ref_no"] ?: params["refid"] ?: ""
            val txnRef = params["txnref"] ?: params["txn_ref"] ?: ""

            return when {
                status == "SUCCESS" || status == "SUBMITTED" || status.contains("SUCCESS") -> {
                    UpiPaymentResult.Success(
                        txnId = txnId,
                        responseCode = responseCode,
                        approvalRefNo = approvalRefNo,
                        txnRef = txnRef,
                        rawResponse = rawData
                    )
                }
                status == "PENDING" || status.contains("PENDING") -> {
                    UpiPaymentResult.Pending(
                        txnId = txnId,
                        message = "Payment is pending banking authorization. Please check in a few moments.",
                        rawResponse = rawData
                    )
                }
                status == "FAILED" || status == "FAILURE" || status.contains("FAIL") -> {
                    UpiPaymentResult.Failed(
                        message = "UPI Payment was declined by issuing bank or UPI switch.",
                        rawResponse = rawData
                    )
                }
                resultCode == Activity.RESULT_CANCELED && status.isBlank() -> {
                    UpiPaymentResult.Cancelled("Payment cancelled by user.")
                }
                else -> {
                    if (resultCode == Activity.RESULT_OK) {
                        UpiPaymentResult.Success(
                            txnId = txnId,
                            responseCode = responseCode,
                            approvalRefNo = approvalRefNo,
                            txnRef = txnRef,
                            rawResponse = rawData
                        )
                    } else {
                        UpiPaymentResult.Failed(
                            message = "Transaction could not be completed. ($status)",
                            rawResponse = rawData
                        )
                    }
                }
            }
        }

        private fun evaluateStatusString(status: String, intent: Intent): UpiPaymentResult {
            val upper = status.uppercase()
            return when {
                upper.contains("SUCCESS") -> {
                    UpiPaymentResult.Success(
                        txnId = intent.getStringExtra("txnId") ?: "TXN_${System.currentTimeMillis()}",
                        responseCode = intent.getStringExtra("responseCode") ?: "00",
                        approvalRefNo = intent.getStringExtra("ApprovalRefNo") ?: "",
                        txnRef = intent.getStringExtra("txnRef") ?: "",
                        rawResponse = status
                    )
                }
                upper.contains("PENDING") -> {
                    UpiPaymentResult.Pending(
                        txnId = intent.getStringExtra("txnId") ?: "TXN_${System.currentTimeMillis()}",
                        message = "Payment processing at bank.",
                        rawResponse = status
                    )
                }
                upper.contains("FAIL") -> {
                    UpiPaymentResult.Failed(
                        message = "Payment failed at bank switch.",
                        rawResponse = status
                    )
                }
                else -> UpiPaymentResult.Cancelled("Payment was cancelled.")
            }
        }
    }

    /**
     * Returns a list of installed UPI applications on the device.
     */
    fun getInstalledUpiApps(): List<UpiAppInfo> {
        val pm = context.packageManager
        val knownTypes = listOf(
            UpiAppType.GOOGLE_PAY,
            UpiAppType.PHONEPE,
            UpiAppType.PAYTM,
            UpiAppType.BHIM,
            UpiAppType.CRED,
            UpiAppType.AMAZON_PAY
        )

        val list = mutableListOf<UpiAppInfo>()

        for (appType in knownTypes) {
            val isInstalled = isPackageInstalled(appType.packageName, pm)
            list.add(
                UpiAppInfo(
                    name = appType.displayName,
                    packageName = appType.packageName,
                    appType = appType,
                    isInstalled = isInstalled
                )
            )
        }

        return list
    }

    /**
     * Builds the Intent to trigger UPI payment via deep link URI.
     */
    fun createUpiIntent(uriString: String, targetPackage: String? = null): Intent {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            if (!targetPackage.isNullOrBlank()) {
                setPackage(targetPackage)
            }
        }
        return intent
    }

    /**
     * Checks whether any UPI app is available to handle payments.
     */
    fun hasAnyUpiApp(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(intent, 0)
        }
        return activities.isNotEmpty() || getInstalledUpiApps().any { it.isInstalled }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
