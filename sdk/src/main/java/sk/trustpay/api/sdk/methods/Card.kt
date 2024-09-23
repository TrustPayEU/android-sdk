@file:Suppress("unused")

package sk.trustpay.api.sdk.methods

import sk.trustpay.api.sdk.common.PaymentRequest
import sk.trustpay.api.sdk.common.PaymentResponse
import sk.trustpay.api.sdk.dto.CallbackUrls
import sk.trustpay.api.sdk.dto.CardTransaction
import sk.trustpay.api.sdk.dto.MerchantIdentification
import sk.trustpay.api.sdk.dto.PaymentInformation

class CardRequest(
    merchantIdentification: MerchantIdentification,
    paymentInformation: PaymentInformation,
    cardTransaction: CardTransaction,
    callbackUrls: CallbackUrls? = null,
    hideGooglePay: Boolean = false
) : PaymentRequest<CardRequest>(merchantIdentification, paymentInformation, callbackUrls) {
    init {
        this.paymentInformation.cardTransaction = cardTransaction
    }

    val paymentMethod: String = "Card"
    var options: Int? = if (hideGooglePay) Options.HideGooglePay.value else null
}

enum class Options(val value: Int) {
    HideGooglePay(1)
}

class CardResponse : PaymentResponse()