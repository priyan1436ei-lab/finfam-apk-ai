package com.example.domain.model

enum class SuspiciousPatternType(val displayName: String, val iconName: String) {
    UNVERIFIED_COLLECT_REQUEST("Collect Request Trap", "call_received"),
    VELOCITY_SPIKE("Rapid Velocity Spike", "speed"),
    MALFORMED_REVERSAL_SCAM("Fake Refund / Reversal", "published_with_changes"),
    ROUND_TRIP_SPLIT("Structured Micro-Split", "call_split"),
    IMPERSONATION_OVERRIDE("Brand Spoofing", "person_off"),
    TIME_ANOMALY("Midnight Anomaly", "nightlight"),
    HIGH_VALUE_FIRST_TIME("Unverified High-Value", "warning")
}

enum class PatternSeverity(val label: String, val colorHex: String) {
    LOW("LOW RISK", "#10B981"),
    MODERATE("MODERATE RISK", "#F59E0B"),
    HIGH("HIGH RISK", "#EF4444"),
    CRITICAL("CRITICAL FRAUD", "#DC2626")
}

data class UpiTransactionPattern(
    val patternId: String,
    val type: SuspiciousPatternType,
    val title: String,
    val description: String,
    val severity: PatternSeverity,
    val riskScore: Int, // 0 - 100
    val targetVpa: String,
    val merchantOrSender: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedFlags: List<String>,
    val confidenceScore: Float, // 0.0 - 1.0
    val recommendedProtocol: String,
    val aiExplanation: String,
    val isBlocked: Boolean = true
)

data class UpiShieldTelemetry(
    val totalScannedToday: Int = 142,
    val activeThreatsBlocked: Int = 8,
    val amountProtectedInr: Double = 184500.0,
    val fraudShieldStatus: String = "ACTIVE - 256-BIT RADAR",
    val patterns: List<UpiTransactionPattern> = emptyList()
)
