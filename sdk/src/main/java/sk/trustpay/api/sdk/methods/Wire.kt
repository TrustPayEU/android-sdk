@file:Suppress("unused")

package sk.trustpay.api.sdk.methods

import sk.trustpay.api.sdk.common.PaymentRequest
import sk.trustpay.api.sdk.common.PaymentResponse
import sk.trustpay.api.sdk.dto.CallbackUrls
import sk.trustpay.api.sdk.dto.MerchantIdentification
import sk.trustpay.api.sdk.dto.PaymentInformation

class WireRequest(
    merchantIdentification: MerchantIdentification,
    paymentInformation: PaymentInformation,
    callbackUrls: CallbackUrls? = null
) : PaymentRequest<WireRequest>(merchantIdentification, paymentInformation, callbackUrls) {
    val paymentMethod: String = "Wire"
}

@Suppress("unused")
class WireResponse : PaymentResponse()