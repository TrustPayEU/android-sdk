@file:Suppress("unused")

package sk.trustpay.api.sdk.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import sk.trustpay.api.sdk.dto.BaseResponse
import sk.trustpay.api.sdk.internal.WebViewActivity


open class PaymentResponse : BaseResponse() {
    val paymentRequestId: Long? = null
    val gatewayUrl: String? = null

    fun launchPopupWebView(context: Context, redirectUrl: String, redirectActivityClassName: String, redirectActivityPackageName: String) {
        val webViewIntent = Intent(context, WebViewActivity::class.java)
        webViewIntent.putExtra("WEB_URL", this.gatewayUrl)
        webViewIntent.putExtra("CATCH_URL", redirectUrl)
        webViewIntent.putExtra("REDIRECT_ACTIVITY_CLASS", redirectActivityClassName)
        webViewIntent.putExtra("REDIRECT_ACTIVITY_PACKAGE", redirectActivityPackageName)
        webViewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        webViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(webViewIntent)
    }

    fun launchChromeCustomTabs(context: Context, options: PopUpOptions? = null) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        if (options != null) {
            if (options.titleColor != null) {
                builder.setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(options.titleColor)
                        .build()
                )
            }
            if (options.tileAlternativeColor != null) {
                builder.setColorSchemeParams(
                    CustomTabsIntent.COLOR_SCHEME_DARK, CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(options.tileAlternativeColor)
                        .build()
                )
            }
            if (options.startAnimation != null) {
                builder.setStartAnimations(
                    context,
                    options.startAnimation.enterAnimation,
                    options.startAnimation.enterAnimation
                )
            }
            if (options.endAnimation != null) {
                builder.setExitAnimations(
                    context,
                    options.endAnimation.enterAnimation,
                    options.endAnimation.enterAnimation
                )
            }
        }

        val customTabsIntent: CustomTabsIntent = builder
            .setUrlBarHidingEnabled(true)
            .setShowTitle(true)
            .build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        customTabsIntent.launchUrl(context, Uri.parse(this.gatewayUrl))
    }

    data class PopUpOptions(
        val titleColor: Int? = null,
        val tileAlternativeColor: Int? = null,
        val startAnimation: Animation? = null,
        val endAnimation: Animation? = null
    )

    data class Animation(val enterAnimation: Int, val exitAnimation: Int)
}
