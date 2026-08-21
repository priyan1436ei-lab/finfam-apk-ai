package com.example

import android.content.Intent
import com.example.domain.model.SubscriptionPlanTier
import com.example.domain.payment.UpiMerchantConfig
import com.example.domain.payment.UpiPaymentManager
import com.example.domain.payment.UpiPaymentResult
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

  @Test
  fun testMonthlyUpiUriFormatting() {
    val uri = UpiMerchantConfig.getMonthlyUpiUri()
    assertTrue(uri.contains("pa=priyan1436ei@okhdfcbank"))
    assertTrue(uri.contains("pn=Priyan"))
    assertTrue(uri.contains("am=199"))
    assertTrue(uri.contains("cu=INR"))
    assertTrue(uri.contains("tn=FinFam+Premium+Monthly") || uri.contains("tn=FinFam%20Premium%20Monthly"))
  }

  @Test
  fun testYearlyUpiUriFormatting() {
    val uri = UpiMerchantConfig.getYearlyUpiUri()
    assertTrue(uri.contains("pa=priyan1436ei@okhdfcbank"))
    assertTrue(uri.contains("pn=Priyan"))
    assertTrue(uri.contains("am=1499"))
    assertTrue(uri.contains("cu=INR"))
  }

  @Test
  fun testUpiResponseSuccessParsing() {
    val intent = Intent().apply {
      putExtra("response", "txnId=UPI987654&responseCode=00&ApprovalRefNo=APR123456&Status=SUCCESS&txnRef=REF789")
    }
    val result = UpiPaymentManager.parseUpiResponse(android.app.Activity.RESULT_OK, intent)
    assertTrue(result is UpiPaymentResult.Success)
    val success = result as UpiPaymentResult.Success
    assertEquals("UPI987654", success.txnId)
    assertEquals("APR123456", success.approvalRefNo)
  }

  @Test
  fun testUpiResponseFailedParsing() {
    val intent = Intent().apply {
      putExtra("response", "txnId=UPI987654&responseCode=ZM&Status=FAILED&txnRef=REF789")
    }
    val result = UpiPaymentManager.parseUpiResponse(android.app.Activity.RESULT_OK, intent)
    assertTrue(result is UpiPaymentResult.Failed)
  }
}

