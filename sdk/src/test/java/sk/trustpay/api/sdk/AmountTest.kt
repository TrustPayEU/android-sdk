package sk.trustpay.api.sdk

import org.junit.Assert
import org.junit.Test
import sk.trustpay.api.sdk.dto.AmountWithCurrency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AmountTest {
    @Test
    fun formatFromDecimal_isCorrect() {
        val amount = AmountWithCurrency(BigDecimal(10), "EUR")
        Assert.assertEquals("10.00", amount.amount)
    }

    @Test
    fun formatFromString_isCorrect() {
        val amount = AmountWithCurrency("10.00", "EUR")
        Assert.assertEquals("10.00", amount.amount)
    }

    @Test
    fun formatFromRandom_isCorrect() {
        val amountDecimal = BigDecimal("10.00")
        val symbols = DecimalFormatSymbols().apply {
            decimalSeparator = '.'
        }
        val decimalFormat = DecimalFormat("0.00", symbols)
        val amountFormatted = decimalFormat.format(amountDecimal)

        Assert.assertEquals("10.00", amountFormatted)
    }
}