package sk.trustpay.api.sdk.common

import sk.trustpay.api.sdk.dto.BaseRequest
import sk.trustpay.api.sdk.dto.CallbackUrls
import sk.trustpay.api.sdk.dto.MerchantIdentification
import sk.trustpay.api.sdk.dto.PaymentInformation
import sk.trustpay.api.sdk.shared.HttpUtil
import sk.trustpay.api.sdk.shared.JsonHelper
import sk.trustpay.api.sdk.shared.PAYMENT_ENDPOINT
import java.io.IOException

abstract class PaymentRequest<T>(merchantIdentification: MerchantIdentification,
    paymentInformation: PaymentInformation, callbackUrls: CallbackUrls? = null)
    : BaseRequest<T>(merchantIdentification, paymentInformation, callbackUrls) where T : PaymentRequest<T> {
    override fun toString(): String {
        return JsonHelper.toJson(this)
    }

    suspend fun createPaymentRequest(tokenProvider: TokenProvider): Result<PaymentResponse> {
        val tokenResponse = tokenProvider.getToken()
        if (tokenResponse.isFailure) {
            return Result.failure(
                tokenResponse.exceptionOrNull() ?: IOException("Unable to get token")
            )
        }

        val response = HttpUtil.performRequest<PaymentResponse>(
            PAYMENT_ENDPOINT,
            mapOf("Authorization" to "Bearer ${tokenResponse.getOrThrow().accessToken}"),
            toString(),
            "application/json"
        )

        if (response.isFailure) {
            return response
        }

        val responseObject = response.getOrThrow()
        if (responseObject.gatewayUrl.isNullOrBlank()) {
            return Result.failure(IOException("Unable to create payment request: ${responseObject.resultInfo?.resultCode} ${responseObject.resultInfo?.additionalInfo}"))
        }

        return response
    }
}