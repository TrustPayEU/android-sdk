@file:Suppress("unused")

package sk.trustpay.api.sdk.dto

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate

abstract class BaseRequest<T>(
    var merchantIdentification: MerchantIdentification,
    var paymentInformation: PaymentInformation,
    var callbackUrls: CallbackUrls? = null
) where T : BaseRequest<T>

data class MerchantIdentification(
    var projectId: String
)

data class PaymentInformation(
    var amount: AmountWithCurrency,
    var references: References,
    var localization: String,
    var debtor: PartyIdentification? = null,
    var debtorAccount: FinancialAccount? = null,
    var debtorAgent: FinancialInstitution? = null,
    var dueDate: LocalDate? = null,
    var remittanceInformation: String? = null,
    var sepaDirectDebitInformation: SddInformation? = null,
    var isRedirect: Boolean? = null,
    var country: String? = null,
    var cardTransaction: CardTransaction? = null
)

class AmountWithCurrency(var amount: String, var currency: String) {

    companion object {
        fun formatDecimal(amount: BigDecimal): String {
            val symbols = DecimalFormatSymbols().apply {
                decimalSeparator = '.'
            }
            val decimalFormat = DecimalFormat("0.00", symbols)
            return decimalFormat.format(amount)
        }
    }

    constructor(amount: BigDecimal, currency: String) : this(
        formatDecimal(amount),
        currency
    )
}

data class References(
    var merchantReference: String,
)

data class PartyIdentification(
    var name: String,
    var id: String
)

data class FinancialAccount(
    var accountNumber: String,
    var iban: String
)

data class FinancialInstitution(
    var institutionName: String,
    var institutionCode: String
)

data class SddInformation(
    var information1: String,
    var information2: String
)

data class CardTransaction(
    var paymentType: CardPaymentType,
    var transactionId: String? = null,
)

enum class CardPaymentType {
    Purchase, Preauthorization
}

data class CallbackUrls(
    var success: String? = null,
    var cancel: String? = null,
    var error: String? = null,
    var notification: String? = null,
)