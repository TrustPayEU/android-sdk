package sk.trustpay.api.sdk.internal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PaymentField", text)
        clipboard.setPrimaryClip(clip)
    }
}

