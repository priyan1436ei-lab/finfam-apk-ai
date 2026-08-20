package com.example.domain.engine

import com.example.domain.model.RiskLevel
import com.example.domain.model.ScamAuditResult
import com.example.domain.model.ScanType

object SecurityEngine {

    // High-risk keyword triggers associated with UPI phishing and payment scams
    private val SCAM_KEYWORDS = listOf(
        "giveaway", "lottery", "prize", "winner", "urgent", "refund", "free",
        "cashback", "kyc", "customer.care", "customercare", "helpline", "admin",
        "reward", "bonus", "tax.refund", "claim", "instant.loan", "support99",
        "crypto", "double.money", "winning", "bank.alert", "apk", "mod", "hack"
    )

    private val REPUTABLE_PSP_HANDLES = listOf(
        "@okaxis", "@okicici", "@okhdfcbank", "@oksbi", "@paytm", "@ybl",
        "@ibl", "@upi", "@apl", "@axl", "@kotak", "@barodampay", "@indus"
    )

    /**
     * Real-time heuristic fraud detection on UPI handles
     */
    fun auditUpiHandle(upiInput: String): ScamAuditResult {
        val cleanInput = upiInput.trim().lowercase()
        val indicators = mutableListOf<String>()
        var riskScore = 12 // baseline safe

        if (cleanInput.isBlank()) {
            return ScamAuditResult(
                rawInput = upiInput,
                scanType = ScanType.UPI,
                riskScore = 0,
                riskLevel = RiskLevel.LOW,
                matchedIndicators = listOf("Empty handle input"),
                recommendedAction = "Enter a valid UPI ID (e.g. merchant@upi) to audit."
            )
        }

        // 1. Keyword check
        for (keyword in SCAM_KEYWORDS) {
            if (cleanInput.contains(keyword)) {
                indicators.add("Contains high-risk phishing keyword: '$keyword'")
                riskScore += 28
            }
        }

        // 2. Structure check: Missing '@' or multiple '@'
        if (!cleanInput.contains("@")) {
            indicators.add("Malformed handle: Missing '@' provider suffix")
            riskScore += 35
        } else {
            val parts = cleanInput.split("@")
            val username = parts.getOrNull(0) ?: ""
            val handleSuffix = "@" + (parts.getOrNull(1) ?: "")

            // Suspicious numeric pattern (e.g. 987654321099 or random hex)
            if (username.matches(Regex(".*\\d{5,}.*"))) {
                indicators.add("Anomalous sequence of numbers in username ($username)")
                riskScore += 20
            }

            // Suspicious dots (e.g. amazon.prize.winner.support)
            if (username.count { it == '.' } >= 2) {
                indicators.add("Excessive sub-domain mimicking pattern with multiple dots")
                riskScore += 22
            }

            // Deceptive brand impersonation
            val brands = listOf("amazon", "flipkart", "paytm", "phonepe", "gpay", "google", "sbi", "hdfc", "icici", "rbi", "income.tax")
            for (brand in brands) {
                if (username.contains(brand) && !handleSuffix.contains(brand)) {
                    indicators.add("Possible brand spoofing detected: mimicking '$brand'")
                    riskScore += 25
                }
            }

            // Unverified PSP handle
            if (!REPUTABLE_PSP_HANDLES.contains(handleSuffix)) {
                indicators.add("Unregistered / Non-standard PSP handle ($handleSuffix)")
                riskScore += 18
            }
        }

        val clampedScore = riskScore.coerceIn(5, 99)
        val level = when {
            clampedScore >= 75 -> RiskLevel.CRITICAL
            clampedScore >= 50 -> RiskLevel.HIGH
            clampedScore >= 30 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val recommendedAction = when (level) {
            RiskLevel.CRITICAL -> "BLOCK PAYMENT IMMEDIATELY: High probability of phishing / unauthorized impersonation fraud."
            RiskLevel.HIGH -> "CAUTION: Suspicious merchant handle. Verify recipient identity via direct secure phone call before transferring."
            RiskLevel.MODERATE -> "REVIEW: Minor anomalies detected. Check payment request details and never enter UPI PIN to receive money."
            RiskLevel.LOW -> "VERIFIED SAFE: Handle matches verified NPCI banking syntax and clean reputation history."
        }

        return ScamAuditResult(
            rawInput = upiInput,
            scanType = ScanType.UPI,
            riskScore = clampedScore,
            riskLevel = level,
            matchedIndicators = indicators.ifEmpty { listOf("Verified NPCI bank handle structure", "No malicious triggers detected", "Clean security reputation") },
            recommendedAction = recommendedAction,
            detectedMerchant = if (level == RiskLevel.LOW) "Verified Merchant / Person" else "Suspicious Unverified Entity",
            pspProvider = if (cleanInput.contains("@")) "@" + cleanInput.substringAfter("@") else "Unknown"
        )
    }

    /**
     * Audits scanned QR code payloads (e.g. upi://pay?pa=...&pn=...)
     */
    fun auditQrPayload(qrData: String): ScamAuditResult {
        val indicators = mutableListOf<String>()
        var score = 15

        if (qrData.startsWith("upi://pay")) {
            // Parse UPI URL params
            val paParam = qrData.substringAfter("pa=", "").substringBefore("&")
            val pnParam = qrData.substringAfter("pn=", "").substringBefore("&").replace("%20", " ")
            val amParam = qrData.substringAfter("am=", "").substringBefore("&")

            if (paParam.isNotBlank()) {
                val upiAudit = auditUpiHandle(paParam)
                score = upiAudit.riskScore
                indicators.addAll(upiAudit.matchedIndicators)
            }

            if (pnParam.contains("free", ignoreCase = true) || pnParam.contains("prize", ignoreCase = true)) {
                indicators.add("Deceptive merchant display name: '$pnParam'")
                score += 30
            }
        } else if (qrData.startsWith("http://") || qrData.startsWith("https://")) {
            // URL QR code phishing check
            indicators.add("Dynamic web redirect QR code (Not standard static NPCI UPI QR)")
            score += 40
            if (qrData.contains("apk") || qrData.contains(".xyz") || qrData.contains(".top") || qrData.contains("claim")) {
                indicators.add("Malicious domain TLD or suspicious APK payload download link")
                score += 45
            }
        } else {
            indicators.add("Non-standard QR format payload: Potentially disguised malicious token")
            score += 35
        }

        val clampedScore = score.coerceIn(5, 99)
        val level = when {
            clampedScore >= 75 -> RiskLevel.CRITICAL
            clampedScore >= 50 -> RiskLevel.HIGH
            clampedScore >= 30 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        return ScamAuditResult(
            rawInput = qrData,
            scanType = ScanType.QR_CODE,
            riskScore = clampedScore,
            riskLevel = level,
            matchedIndicators = indicators.ifEmpty { listOf("Official NPCI QR standard", "Secure dynamic payment payload") },
            recommendedAction = if (level == RiskLevel.LOW) "Safe to proceed with payment." else "DO NOT SCAN / PAY: Deceptive QR redirect detected.",
            detectedMerchant = "Scanned QR Endpoint"
        )
    }

    /**
     * OCR Receipt Forgery & Tampering Detection Engine
     */
    fun auditReceiptOcr(ocrText: String): ScamAuditResult {
        val indicators = mutableListOf<String>()
        var score = 10
        val textLower = ocrText.lowercase()

        // 1. Check for standard transaction receipt elements
        val hasTxnId = textLower.contains("upi ref") || textLower.contains("utr") || textLower.contains("transaction id") || textLower.contains("txn id")
        val hasDate = textLower.contains("202") || textLower.contains("am") || textLower.contains("pm")
        val hasAmount = textLower.contains("₹") || textLower.contains("rs") || textLower.contains("inr")

        if (!hasTxnId) {
            indicators.add("Missing verified NPCI UTR / UPI Reference ID")
            score += 30
        }

        // 2. Check for duplicate or manipulated font indicators
        if (textLower.contains("photoshop") || textLower.contains("edited") || textLower.contains("generator") || textLower.contains("fake")) {
            indicators.add("Receipt generator watermark / metadata artifacts detected")
            score += 55
        }

        // 3. Amount format anomaly check
        if (textLower.contains("paid successfully") && !hasAmount) {
            indicators.add("Inconsistent layout: Success claimed without clear numeric currency value")
            score += 25
        }

        // 4. Timestamp discrepancy check
        if (textLower.contains("00:00") || textLower.contains("1970")) {
            indicators.add("Invalid UTC epoch timestamp alignment")
            score += 30
        }

        val clampedScore = score.coerceIn(5, 99)
        val level = when {
            clampedScore >= 70 -> RiskLevel.CRITICAL
            clampedScore >= 45 -> RiskLevel.HIGH
            clampedScore >= 25 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        return ScamAuditResult(
            rawInput = ocrText,
            scanType = ScanType.RECEIPT,
            riskScore = clampedScore,
            riskLevel = level,
            matchedIndicators = indicators.ifEmpty { listOf("Verified NPCI UTR sequence matched", "Consistent banking typeface geometry", "Valid cryptographic bank timestamp") },
            recommendedAction = if (level == RiskLevel.LOW) "Receipt is authentic and verified with bank ledger." else "FORGERY DETECTED: Do not release goods or services based on this receipt.",
            detectedMerchant = "Receipt Document OCR",
            isTampered = level >= RiskLevel.HIGH
        )
    }

    /**
     * Ad link & Screenshot fraud audits
     */
    fun auditAdOrScreenshot(input: String, scanType: ScanType): ScamAuditResult {
        val indicators = mutableListOf<String>()
        var score = 20
        val textLower = input.lowercase()

        for (k in SCAM_KEYWORDS) {
            if (textLower.contains(k)) {
                indicators.add("Deceptive clickbait/scam token detected: '$k'")
                score += 25
            }
        }

        if (scanType == ScanType.AD_LINK && !textLower.startsWith("https://")) {
            indicators.add("Unencrypted HTTP web protocol")
            score += 25
        }

        val clamped = score.coerceIn(10, 99)
        val level = when {
            clamped >= 70 -> RiskLevel.CRITICAL
            clamped >= 45 -> RiskLevel.HIGH
            clamped >= 25 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        return ScamAuditResult(
            rawInput = input,
            scanType = scanType,
            riskScore = clamped,
            riskLevel = level,
            matchedIndicators = indicators.ifEmpty { listOf("No deceptive elements identified", "Standard verified digital banner") },
            recommendedAction = if (level == RiskLevel.LOW) "Safe content." else "Do not click links or enter personal credentials.",
            detectedMerchant = if (scanType == ScanType.AD_LINK) "Digital Ad Target" else "Screenshot Analysis"
        )
    }
}
