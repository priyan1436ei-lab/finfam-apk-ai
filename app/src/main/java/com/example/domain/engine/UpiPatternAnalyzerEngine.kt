package com.example.domain.engine

import com.example.domain.model.PatternSeverity
import com.example.domain.model.SuspiciousPatternType
import com.example.domain.model.UpiShieldTelemetry
import com.example.domain.model.UpiTransactionPattern
import java.util.UUID

object UpiPatternAnalyzerEngine {

    val INITIAL_PATTERNS = listOf(
        UpiTransactionPattern(
            patternId = "pat_collect_01",
            type = SuspiciousPatternType.UNVERIFIED_COLLECT_REQUEST,
            title = "Deceptive UPI Collect Request Trap",
            description = "Inbound 'Collect ₹49,999' request masquerading as cashback prize payout.",
            severity = PatternSeverity.CRITICAL,
            riskScore = 98,
            targetVpa = "claim.instant.cashback99@paytm",
            merchantOrSender = "Rewards Disbursal Portal",
            amount = 49999.0,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
            detectedFlags = listOf(
                "Requires user UPI PIN to 'receive' funds (Fraud Rule #1)",
                "Sender VPA contains phishing tokens ('cashback', 'instant')",
                "Unregistered non-merchant PSP entity endpoint"
            ),
            confidenceScore = 0.99f,
            recommendedProtocol = "BLOCK & REPORT: UPI PIN is NEVER required to receive money.",
            aiExplanation = "The sender initiated an inbound debit request with the title 'Cashback Approval'. Entering your 4 or 6 digit UPI PIN will instantly deduct ₹49,999 from your linked bank account."
        ),
        UpiTransactionPattern(
            patternId = "pat_velocity_02",
            type = SuspiciousPatternType.VELOCITY_SPIKE,
            title = "Rapid Layered Velocity Spike",
            description = "4 rapid micro-transfers totaling ₹95,000 to an unverified VPA within 180 seconds.",
            severity = PatternSeverity.HIGH,
            riskScore = 86,
            targetVpa = "p2p.quick.transfer88@okicici",
            merchantOrSender = "Unknown P2P Recipient",
            amount = 95000.0,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
            detectedFlags = listOf(
                "Velocity > 3 transfers in under 3 minutes",
                "Approaching ₹1 Lakh daily NPCI P2P threshold",
                "Recipient handle registered less than 48 hours ago"
            ),
            confidenceScore = 0.91f,
            recommendedProtocol = "THROTTLE: Enforce 2-hour cooling period and biometric re-authentication.",
            aiExplanation = "This burst pattern matches structured money mule syndicates attempting to drain funds in successive sub-threshold tranches."
        ),
        UpiTransactionPattern(
            patternId = "pat_reversal_03",
            type = SuspiciousPatternType.MALFORMED_REVERSAL_SCAM,
            title = "Fake SMS Refund / Reversal Scam",
            description = "Payment request triggered after receiving spoofed '₹25,000 credited by mistake' SMS.",
            severity = PatternSeverity.CRITICAL,
            riskScore = 94,
            targetVpa = "support.refund.desk24@oksbi",
            merchantOrSender = "Fake SBI Customer Care",
            amount = 25000.0,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 110,
            detectedFlags = listOf(
                "No corresponding credit entry in official bank ledger",
                "VPA mimics official bank domain (@oksbi) with customer care spoof",
                "Urgency trigger in transaction memo ('Immediate Reversal Required')"
            ),
            confidenceScore = 0.96f,
            recommendedProtocol = "VERIFY LEDGER: Do not transfer. Check your actual bank passbook statement.",
            aiExplanation = "Scammers send a forged bank credit SMS, then call panicking asking for a 'refund'. No money was ever received in your account."
        ),
        UpiTransactionPattern(
            patternId = "pat_impersonation_04",
            type = SuspiciousPatternType.IMPERSONATION_OVERRIDE,
            title = "Brand Impersonation Phishing",
            description = "Payment request to 'amazon.order.refund.desk@ybl' for fake parcel verification.",
            severity = PatternSeverity.HIGH,
            riskScore = 82,
            targetVpa = "amazon.order.refund.desk@ybl",
            merchantOrSender = "Amazon Logistics Desk (Unverified)",
            amount = 12450.0,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
            detectedFlags = listOf(
                "Brand name spoofed ('amazon') on consumer PSP handle (@ybl)",
                "Missing verified blue tick merchant metadata from NPCI"
            ),
            confidenceScore = 0.88f,
            recommendedProtocol = "REJECT: Legitimate ecommerce platforms process refunds directly to source.",
            aiExplanation = "E-commerce companies never request manual UPI transfers or collect requests to issue returns or refunds."
        ),
        UpiTransactionPattern(
            patternId = "pat_midnight_05",
            type = SuspiciousPatternType.TIME_ANOMALY,
            title = "Midnight High-Volume Anomaly",
            description = "₹78,000 transfer initiated at 3:18 AM to a newly added beneficiary.",
            severity = PatternSeverity.MODERATE,
            riskScore = 65,
            targetVpa = "crypto.trader.in99@okaxis",
            merchantOrSender = "P2P Crypto Gateway",
            amount = 78000.0,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 420,
            detectedFlags = listOf(
                "Out-of-pattern time window (Standard active hours: 8 AM - 11 PM)",
                "Zero prior transaction history with recipient",
                "Unusual amount deviation (+340% from 30-day median)"
            ),
            confidenceScore = 0.74f,
            recommendedProtocol = "CHALLENGE: Require Face/Biometric verification and 15-minute confirmation delay.",
            aiExplanation = "High-value transfers during nocturnal hours have a statistically high correlation with unauthorized account access or social engineering."
        )
    )

    fun getInitialTelemetry(): UpiShieldTelemetry {
        return UpiShieldTelemetry(
            totalScannedToday = 142,
            activeThreatsBlocked = INITIAL_PATTERNS.count { it.isBlocked },
            amountProtectedInr = INITIAL_PATTERNS.filter { it.isBlocked }.sumOf { it.amount },
            fraudShieldStatus = "ACTIVE - 256-BIT RADAR",
            patterns = INITIAL_PATTERNS
        )
    }

    /**
     * Injects a real-time synthetic test pattern
     */
    fun createSamplePattern(type: SuspiciousPatternType): UpiTransactionPattern {
        return when (type) {
            SuspiciousPatternType.UNVERIFIED_COLLECT_REQUEST -> UpiTransactionPattern(
                patternId = "pat_" + UUID.randomUUID().toString().take(6),
                type = SuspiciousPatternType.UNVERIFIED_COLLECT_REQUEST,
                title = "Live Test: Inbound Collect Request Trap",
                description = "Suspicious collect request for ₹15,000 claiming lottery jackpot prize payout.",
                severity = PatternSeverity.CRITICAL,
                riskScore = 97,
                targetVpa = "lottery.winner.claim" + (10..99).random() + "@paytm",
                merchantOrSender = "Mega Prize Draw",
                amount = 15000.0,
                detectedFlags = listOf(
                    "Attempting to solicit UPI PIN on collect request",
                    "Keyword anomaly: 'lottery', 'winner'",
                    "High fraud probability indicator"
                ),
                confidenceScore = 0.98f,
                recommendedProtocol = "AUTO-BLOCKED: Never enter UPI PIN to receive winnings.",
                aiExplanation = "Inbound debit requests attempting to trigger fund withdrawal under the guise of an incoming prize."
            )
            SuspiciousPatternType.VELOCITY_SPIKE -> UpiTransactionPattern(
                patternId = "pat_" + UUID.randomUUID().toString().take(6),
                type = SuspiciousPatternType.VELOCITY_SPIKE,
                title = "Live Test: Rapid Velocity Spike",
                description = "3 back-to-back payments totaling ₹48,000 in under 90 seconds.",
                severity = PatternSeverity.HIGH,
                riskScore = 88,
                targetVpa = "quick.mule.gateway" + (10..99).random() + "@upi",
                merchantOrSender = "High-Risk Peer Node",
                amount = 48000.0,
                detectedFlags = listOf(
                    "High transaction frequency anomaly",
                    "Rapid sequence bypassing single-transaction limits"
                ),
                confidenceScore = 0.92f,
                recommendedProtocol = "THROTTLE: 2-Hour Security Lockout Enforced.",
                aiExplanation = "Anomalous payment cadence indicating potential automated script or coerced urgent transfer."
            )
            else -> UpiTransactionPattern(
                patternId = "pat_" + UUID.randomUUID().toString().take(6),
                type = SuspiciousPatternType.MALFORMED_REVERSAL_SCAM,
                title = "Live Test: Fake Customer Care Phishing",
                description = "Reversal request targeting unverified phone number handle.",
                severity = PatternSeverity.CRITICAL,
                riskScore = 93,
                targetVpa = "customercare.desk" + (10..99).random() + "@okhdfcbank",
                merchantOrSender = "HDFC Helpdesk Impersonator",
                amount = 32000.0,
                detectedFlags = listOf(
                    "Impersonating bank support handle",
                    "Unverified individual handle structure"
                ),
                confidenceScore = 0.95f,
                recommendedProtocol = "BLOCK VPA: Bank helplines never collect funds via UPI.",
                aiExplanation = "Direct brand spoofing targeting customer support distress scenarios."
            )
        }
    }
}
