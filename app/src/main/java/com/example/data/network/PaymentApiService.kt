package com.example.data.network

import com.example.domain.model.CreateOrderRequest
import com.example.domain.model.CreateOrderResponse
import com.example.domain.model.PaymentStatusResponse
import com.example.domain.model.RefundPaymentRequest
import com.example.domain.model.RefundPaymentResponse
import com.example.domain.model.VerifyPaymentRequest
import com.example.domain.model.VerifyPaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApiService {

    @POST("api/payment/create-order")
    suspend fun createOrder(
        @Body request: CreateOrderRequest,
        @Header("Authorization") authToken: String? = null
    ): Response<CreateOrderResponse>

    @POST("api/payment/verify")
    suspend fun verifyPayment(
        @Body request: VerifyPaymentRequest,
        @Header("Authorization") authToken: String? = null
    ): Response<VerifyPaymentResponse>

    @POST("api/payment/refund")
    suspend fun requestRefund(
        @Body request: RefundPaymentRequest,
        @Header("Authorization") authToken: String? = null
    ): Response<RefundPaymentResponse>

    @GET("api/payment/status/{orderId}")
    suspend fun getPaymentStatus(
        @Path("orderId") orderId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<PaymentStatusResponse>
}
