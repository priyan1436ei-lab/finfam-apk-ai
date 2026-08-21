package com.example.domain.model

import java.util.UUID
import kotlin.random.Random

enum class RealTimeTransferType(val label: String, val category: String) {
    FUNDS_TRANSFER("Instant Money Transfer", "Transfer"),
    FAMILY_ALLOWANCE("Family Vault Beam", "Allowance"),
    DATA_SYNC_BEAM("Real-Time Ledger Sync", "Data Sync"),
    BACKUP_TRANSFER("Encrypted Backup Beam", "Security"),
    INSTANT_UPI("Direct UPI Instant Settlement", "UPI")
}

enum class RealTimeTransferStatus(val label: String) {
    INITIALIZING("Handshake"),
    ENCRYPTING("256-Bit Encrypting"),
    STREAMING("Streaming Packets"),
    SETTLED("Settled in Real-Time"),
    FAILED("Transfer Failed")
}

data class RealTimeTransferRecord(
    val id: String = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
    val utrNumber: String = "UTR${System.currentTimeMillis().toString().takeLast(10)}${Random.nextInt(10, 99)}",
    val senderName: String,
    val senderVpaOrAcc: String,
    val receiverName: String,
    val receiverVpaOrAcc: String,
    val amount: Double? = null,
    val payloadSizeKb: Double? = null,
    val transferType: RealTimeTransferType = RealTimeTransferType.FUNDS_TRANSFER,
    val status: RealTimeTransferStatus = RealTimeTransferStatus.SETTLED,
    val protocol: String = "WSS://finfam.p2p.live • AES-256-GCM",
    val latencyMs: Int = Random.nextInt(8, 24),
    val sha256Hash: String = "0x" + UUID.randomUUID().toString().replace("-", "").take(16),
    val note: String = "",
    val timestampFormatted: String = "Just now",
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LivePeerNode(
    val id: String,
    val name: String,
    val relationship: String,
    val vpa: String,
    val ipAddress: String,
    val pingMs: Int,
    val isOnline: Boolean,
    val avatarColorHex: Long,
    val lastSyncText: String
)
