package com.example.domain.engine

import com.example.domain.model.DepositPurpose
import com.example.domain.model.PaymentReceipt
import java.util.UUID
import kotlin.random.Random

object PaymentEngine {

    val DEPOSIT_PURPOSES = listOf(
        DepositPurpose(
            id = "emergency",
            title = "Emergency Fund Reserve",
            tag = "High Security",
            description = "Instant liquidity pool for unexpected emergencies & shock buffer",
            iconName = "shield"
        ),
        DepositPurpose(
            id = "goal_savings",
            title = "Goal Savings Top-up",
            tag = "Wealth Building",
            description = "Direct contribution to active short-term and long-term milestones",
            iconName = "flag"
        ),
        DepositPurpose(
            id = "mutual_fund",
            title = "Mutual Fund / Equity SIP",
            tag = "Compound Growth",
            description = "Systematic Investment Plan contribution targeting 12%+ CAGR",
            iconName = "trending_up"
        ),
        DepositPurpose(
            id = "loan_prepay",
            title = "Loan EMI Pre-payment",
            tag = "Debt Free",
            description = "Reduce principal debt & compound interest burden ahead of schedule",
            iconName = "account_balance"
        ),
        DepositPurpose(
            id = "utility_bills",
            title = "Utility & Financial Bills",
            tag = "Instant Receipt",
            description = "Pay essential recurring utilities and credit card statements securely",
            iconName = "receipt_long"
        )
    )

    fun calculateFeeBreakdown(amount: Double): Triple<Double, Double, Double> {
        val platformFee = 0.0 // 100% Waived for FinGuard Vault
        val gst = 0.0
        val totalPayable = amount + platformFee + gst
        return Triple(platformFee, gst, totalPayable)
    }

    fun generateReceipt(
        purposeTitle: String,
        amount: Double,
        paymentMethod: String,
        recipient: String = "FinGuard Encrypted Vault"
    ): PaymentReceipt {
        val randomNum = Random.nextLong(100000000000L, 999999999999L)
        val npciRef = "NPCI-FG-$randomNum"
        val txnId = "TXN-" + UUID.randomUUID().toString().take(8).uppercase()

        val (fee, gst, total) = calculateFeeBreakdown(amount)

        return PaymentReceipt(
            transactionId = txnId,
            npciRefId = npciRef,
            purposeTitle = purposeTitle,
            amount = amount,
            platformFee = fee,
            gst = gst,
            totalPaid = total,
            paymentMethod = paymentMethod,
            recipient = recipient,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS",
            securityVerificationStatus = "SHIELD 256-BIT NPCI VERIFIED"
        )
    }
}
