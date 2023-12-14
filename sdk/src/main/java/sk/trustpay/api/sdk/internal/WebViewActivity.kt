package sk.trustpay.api.sdk.internal

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import sk.trustpay.api.sdk.BuildConfig
import sk.trustpay.api.sdk.databinding.ActivityWebViewBinding

internal class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("WEB_URL").orEmpty()
        val catchUrl = intent.getStringExtra("CATCH_URL").orEmpty()
        val redirectActivityClassName = intent.getStringExtra("REDIRECT_ACTIVITY_CLASS").orEmpty()
        val redirectActivityPackageName = intent.getStringExtra("REDIRECT_ACTIVITY_PACKAGE").orEmpty()

        val webView = binding.webView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if (request?.url?.scheme.equals("https")) {
                    val browsedUrl = request?.url
                    if (browsedUrl.toString().startsWith(catchUrl)) {
                        val intent = Intent().setClassName(
                            redirectActivityPackageName,
                            redirectActivityClassName
                        )
                        intent.data = browsedUrl
                        startActivity(intent)
                        finish()
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                } else {
                    Log.d(this.javaClass.name, "URL Not Allowed ${request?.url?.scheme}://${request?.url?.host}")
                    true
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                if (BuildConfig.DEBUG) {
                    handler?.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        webView.loadUrl(url)
    }
}
