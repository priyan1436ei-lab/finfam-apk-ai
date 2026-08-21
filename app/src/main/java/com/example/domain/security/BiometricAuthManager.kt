package com.example.domain.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Biometric authentication status enum
 */
enum class BiometricStatus(val message: String) {
    AVAILABLE("Biometric authentication is ready and active"),
    NO_HARDWARE("No biometric sensors available on this device"),
    HW_UNAVAILABLE("Biometric hardware is currently unavailable"),
    NONE_ENROLLED("No biometric credentials registered on device"),
    SECURITY_UPDATE_REQUIRED("Security update required for biometric sensors"),
    UNSUPPORTED("Biometric authentication is unsupported")
}

/**
 * Result sealed class for BiometricPrompt callbacks
 */
sealed class BiometricAuthResult {
    data object Success : BiometricAuthResult()
    data class Error(val errorCode: Int, val errString: String) : BiometricAuthResult()
    data object Failed : BiometricAuthResult()
    data object Cancelled : BiometricAuthResult()
}

/**
 * Enterprise-grade Biometric & Device Credential Authentication Manager.
 * Secures application entry and sensitive payment checkout transactions via BiometricPrompt.
 */
object BiometricAuthManager {

    /**
     * Checks if biometric or device credential authentication is available on device.
     */
    fun checkBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.UNSUPPORTED
            else -> BiometricStatus.UNSUPPORTED
        }
    }

    /**
     * Shows the native BiometricPrompt bottom dialog for user verification.
     *
     * @param activity FragmentActivity host
     * @param title Title displayed on the biometric prompt
     * @param subtitle Subtitle description (e.g. transaction amount or purpose)
     * @param description Additional security details
     * @param onResult Callback containing the BiometricAuthResult
     */
    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "FinFam Security Verification",
        subtitle: String = "Verify your fingerprint or face to proceed",
        description: String = "Protecting your financial vault and transaction security",
        negativeButtonText: String = "Cancel",
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onResult(BiometricAuthResult.Cancelled)
                } else {
                    onResult(BiometricAuthResult.Error(errorCode, errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(BiometricAuthResult.Failed)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setConfirmationRequired(true)

        // If DEVICE_CREDENTIAL is allowed (API 30+), negative button must NOT be set
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }

        try {
            prompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            // Fallback for devices without biometric setup or compatibility edge cases
            onResult(BiometricAuthResult.Error(errorCode = -1, errString = e.localizedMessage ?: "Biometric prompt error"))
        }
    }
}
