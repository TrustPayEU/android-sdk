package sk.trustpay.api.sdk.internal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.trustpay.api.sdk.BuildConfig
import sk.trustpay.api.sdk.databinding.ActivityWebViewBinding
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID


internal class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private lateinit var progressDialog: AlertDialog
    private var webBrowserLoaded = false

    private lateinit var redirectActivityClassName: String
    private lateinit var redirectActivityPackageName: String
    private lateinit var catchUrl: String
    private lateinit var browseUrl: String

    private var filePathToMove: String? = null

    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var downloadAndShowLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        browseUrl = intent.getStringExtra("WEB_URL").orEmpty()
        catchUrl = intent.getStringExtra("CATCH_URL").orEmpty()
        redirectActivityClassName = intent.getStringExtra("REDIRECT_ACTIVITY_CLASS").orEmpty()
        redirectActivityPackageName = intent.getStringExtra("REDIRECT_ACTIVITY_PACKAGE").orEmpty()

        downloadAndShowLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) {
            if (it != null) {
                lifecycleScope.launch {
                    saveFileToUri(it)
                }
            }
        }

        setProgressDialog(this, "Loading...")
        progressDialog.show()
        setupWebView()
    }

    override fun onDestroy() {
        if(progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        removeDownloads()
        super.onDestroy()
    }

    override fun onPause() {
        if(progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        super.onPause()
    }

    private suspend fun downloadPdf(url: String): String {
        return withContext(Dispatchers.IO) {
            val urlFile = URL(url)
            val connection = urlFile.openConnection()
            connection.connect()
            val input = BufferedInputStream(urlFile.openStream())

            val downloadDir = File(cacheDir, "download")
            if (!downloadDir.exists()) {
                downloadDir.mkdir()
            }

            downloadDir.deleteOnExit()
            val ext = MimeTypeMap.getFileExtensionFromUrl(url)
            val filePath = "${downloadDir.path}/${UUID.randomUUID()}.$ext"
            val output = FileOutputStream(filePath)

            val data = ByteArray(1024)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            return@withContext filePath
        }
    }

    private fun setupWebView() {
                val webView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.setDownloadListener { downloadUrl, _, _, mimetype, _ ->
            if (downloadUrl.isNullOrEmpty()) {
               return@setDownloadListener
            }
            handleDownloadFile(downloadUrl, mimetype)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl("""javascript:(function() { 
                    navigator.permissions = { 
                        query:function(opts) { 
                            return Promise.resolve({state:opts.name === 'clipboard-write' 
                                ? 'granted'
                                : 'denied'
                            });
                        }
                    };
                    clipboard.writeText = (e) => { 
                        return Android.copyToClipboard(e);
                    };
                })();""".trimIndent())
                if(!webBrowserLoaded){
                    progressDialog.dismiss()
                    webBrowserLoaded = true
                }
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return catchRedirect(request?.url)
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
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        webView.loadUrl(browseUrl)
    }

    private fun catchRedirect(url: Uri?): Boolean {
        if (url?.scheme.equals("https") || BuildConfig.DEBUG) {
            val browsedUrl = url.toString()
            if (browsedUrl.startsWith(catchUrl)) {
                val intent = Intent().setClassName(
                    redirectActivityPackageName,
                    redirectActivityClassName
                )
                intent.data = url
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            return false
        } else {
            Log.d(this.javaClass.name, "URL Not Allowed ${url?.scheme}://${url?.host}")
            return true
        }
    }

    private fun handleDownloadFile(url: String, mimetype: String) {
        if(mimetype != "application/pdf"){
            return
        }

        lifecycleScope.launch {
            var filePath: String? = null
            try{
                filePath = downloadPdf(url)
                val intent = Intent(Intent.ACTION_VIEW)
                val fileUri = FileProvider.getUriForFile(this@WebViewActivity, applicationContext.packageName + ".provider", File(filePath))
                intent.setDataAndType(fileUri, mimetype)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                downloadAndShowLauncher.launch(intent)
            } catch (e: ActivityNotFoundException){
                if(filePath.isNullOrBlank()) {
                    return@launch
                }

                Toast.makeText(this@WebViewActivity, "No PDF app found. Downloading file...", Toast.LENGTH_SHORT).show()
                filePathToMove = filePath
                saveFileLauncher.launch("trustpay.pdf")
            }
        }
    }

    private suspend fun saveFileToUri(uri: Uri) {
        try {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    if (filePathToMove.isNullOrEmpty()) {
                        return@withContext
                    }
                    val cachedFile = filePathToMove?.let { File(it) }
                    cachedFile?.inputStream().use { inputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }
            }
        } catch(e: Exception)  {
            Log.e("SaveFileToUri", e.message.orEmpty())
            Toast.makeText(this, "Unable to save file.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setProgressDialog(context: Context, message: String) {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        progressDialog = dialog
    }

    private fun removeDownloads() {
        val downloadDir = File(cacheDir, "download")
        if(downloadDir.exists()) {
            downloadDir.deleteRecursively()
        }
    }

}

