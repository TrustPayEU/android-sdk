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
    callbackUrls: CallbackUrls? = null
) : PaymentRequest<CardRequest>(merchantIdentification, paymentInformation, callbackUrls) {
    init {
        this.paymentInformation.cardTransaction = cardTransaction
    }

    val paymentMethod: String = "Card"

}

class CardResponse : PaymentResponse()