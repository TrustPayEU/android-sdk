@file:Suppress("unused")

package sk.trustpay.api.sdk.shared

import sk.trustpay.api.sdk.BuildConfig

internal const val AUTH_ENDPOINT = "${BuildConfig.API_BASE_URL}/api/oauth2/token"
internal const val PAYMENT_ENDPOINT = "${BuildConfig.API_BASE_URL}/api/payments/payment"

const val WEB_URL = "WEB_URL"
const val CATCH_URL = "CATCH_URL"
const val REDIRECT_ACTIVITY_CLASS = "REDIRECT_ACTIVITY_CLASS"
const val REDIRECT_ACTIVITY_PACKAGE = "REDIRECT_ACTIVITY_PACKAGE"

const val CARD_PAYMENT_TYPE_PURCHASE = "Purchase"
const val CARD_PAYMENT_TYPE_PREAUHTORIZATION = "Preauthorization"
