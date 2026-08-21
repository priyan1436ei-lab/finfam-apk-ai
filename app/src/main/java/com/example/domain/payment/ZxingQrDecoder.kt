package com.example.domain.payment

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.domain.model.ScannedUpiPayload
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * ZXing-powered UPI QR Code Scanner & Scam Shield Risk Analyzer.
 */
object ZxingQrDecoder {
    private const val TAG = "ZxingQrDecoder"

    // Known trusted bank & payment gateway UPI handles
    private val TRUSTED_UPI_HANDLES = setOf(
        "okhdfcbank", "okaxis", "oksbi", "okicici", "ibl", "ybl", "axl", "paytm",
        "upi", "apl", "barodampay", "federal", "kotak", "indus", "postbank", "airtel"
    )

    // Suspicious phishing patterns in UPI IDs
    private val SUSPICIOUS_PATTERNS = listOf(
        "lottery", "cashback-claim", "refund-support", "helpline", "kyc-update", "reward", "winner"
    )

    /**
     * Decodes a QR code from a Bitmap image using ZXing reader.
     */
    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val hints = mapOf(
                DecodeHintType.CHARACTER_SET to "UTF-8",
                DecodeHintType.TRY_HARDER to true
            )

            val result = MultiFormatReader().decode(binaryBitmap, hints)
            result.text
        } catch (e: Exception) {
            Log.w(TAG, "ZXing QR decode failed: ${e.message}")
            null
        }
    }

    /**
     * Parses standard NPCI UPI URI string into structured ScannedUpiPayload.
     * URI format: upi://pay?pa=recipient@bank&pn=PayeeName&am=100.00&cu=INR&tn=Dinner
     */
    fun parseUpiString(qrContent: String): ScannedUpiPayload? {
        val trimmed = qrContent.trim()
        if (!trimmed.startsWith("upi://pay", ignoreCase = true) && !trimmed.contains("pa=")) {
            // Check if it's a bare UPI ID (e.g. priyan1436ei@okhdfcbank)
            if (trimmed.contains("@") && !trimmed.contains(" ") && trimmed.length in 5..50) {
                val (score, risk) = analyzeUpiRisk(trimmed, "Merchant")
                return ScannedUpiPayload(
                    vpa = trimmed,
                    payeeName = "Verified Merchant",
                    amount = null,
                    note = "Direct UPI Transfer",
                    transactionRef = "REF_${System.currentTimeMillis()}",
                    rawQrString = "upi://pay?pa=$trimmed&pn=Merchant&cu=INR",
                    isVerifiedMerchant = score >= 80,
                    safetyScore = score,
                    riskLevel = risk
                )
            }
            return null
        }

        return try {
            val uri = Uri.parse(trimmed)
            val vpa = uri.getQueryParameter("pa") ?: return null
            val payeeName = uri.getQueryParameter("pn") ?: "UPI Beneficiary"
            val amountStr = uri.getQueryParameter("am")
            val amount = amountStr?.toDoubleOrNull()
            val note = uri.getQueryParameter("tn") ?: uri.getQueryParameter("mc") ?: "Payment"
            val txnRef = uri.getQueryParameter("tr") ?: uri.getQueryParameter("tid")

            val (safetyScore, riskLevel) = analyzeUpiRisk(vpa, payeeName)

            ScannedUpiPayload(
                vpa = vpa,
                payeeName = payeeName,
                amount = amount,
                note = note,
                transactionRef = txnRef,
                rawQrString = trimmed,
                isVerifiedMerchant = safetyScore >= 80,
                safetyScore = safetyScore,
                riskLevel = riskLevel
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing UPI payload", e)
            null
        }
    }

    /**
     * UPI Scam Shield Risk Analyzer.
     * Checks VPA handle against trusted bank providers and phishing signatures.
     */
    fun analyzeUpiRisk(vpa: String, payeeName: String): Pair<Int, String> {
        val lowerVpa = vpa.lowercase()
        val parts = lowerVpa.split("@")
        if (parts.size != 2) return Pair(40, "SUSPICIOUS")

        val handle = parts[1]
        val username = parts[0]

        var score = 95
        var risk = "SAFE"

        // 1. Phishing keyword check
        for (badWord in SUSPICIOUS_PATTERNS) {
            if (username.contains(badWord) || lowerVpa.contains(badWord)) {
                score -= 50
                risk = "HIGH_RISK_SCAM"
                break
            }
        }

        // 2. Trusted bank handle verification
        if (!TRUSTED_UPI_HANDLES.contains(handle) && !handle.contains("bank") && !handle.contains("pay")) {
            score -= 25
            if (risk != "HIGH_RISK_SCAM") risk = "UNVERIFIED_HANDLE"
        }

        // 3. Official FinFam / Owner verification bonus
        if (vpa.equals(UpiMerchantConfig.MERCHANT_UPI_ID, ignoreCase = true) ||
            payeeName.contains("FinFam", ignoreCase = true) ||
            payeeName.contains("Priyan", ignoreCase = true)
        ) {
            score = 100
            risk = "VERIFIED_OFFICIAL"
        }

        return Pair(score.coerceIn(10, 100), risk)
    }

    /**
     * Sample preset UPI QR codes for instant testing and verification.
     */
    val SAMPLE_UPI_QRS = listOf(
        ScannedUpiPayload(
            vpa = UpiMerchantConfig.MERCHANT_UPI_ID,
            payeeName = "Priyan (FinFam Official)",
            amount = 99.0,
            note = "FinFam Monthly Pro Upgrade",
            transactionRef = "REF_SUB99",
            rawQrString = UpiMerchantConfig.getPlanUpiUri(com.example.domain.model.SubscriptionPlanTier.MONTHLY_PRO),
            isVerifiedMerchant = true,
            safetyScore = 100,
            riskLevel = "VERIFIED_OFFICIAL"
        ),
        ScannedUpiPayload(
            vpa = "freshmart.groceries@okhdfcbank",
            payeeName = "FreshMart Supermarket",
            amount = 640.0,
            note = "Weekly Groceries & Vegetables",
            transactionRef = "TXN_FM8910",
            rawQrString = "upi://pay?pa=freshmart.groceries@okhdfcbank&pn=FreshMart%20Supermarket&am=640&cu=INR&tn=Weekly%20Groceries",
            isVerifiedMerchant = true,
            safetyScore = 98,
            riskLevel = "SAFE"
        ),
        ScannedUpiPayload(
            vpa = "bluecafe.orders@paytm",
            payeeName = "Blue Horizon Cafe",
            amount = 280.0,
            note = "Artisan Cappuccino & Croissant",
            transactionRef = "TXN_BC2026",
            rawQrString = "upi://pay?pa=bluecafe.orders@paytm&pn=Blue%20Horizon%20Cafe&am=280&cu=INR&tn=Coffee%20and%20Snacks",
            isVerifiedMerchant = true,
            safetyScore = 96,
            riskLevel = "SAFE"
        )
    )
}
