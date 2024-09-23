package sk.trustpay.api.sdk.shared

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sk.trustpay.api.sdk.BuildConfig
import sk.trustpay.api.sdk.dto.BaseResponse
import java.io.IOException
import java.lang.annotation.ElementType.*
import java.lang.annotation.RetentionPolicy.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


internal object JsonHelper {
    private val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
        .disableHtmlEscaping()
        .create()

    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }
}

internal object HttpUtil {
    var client: OkHttpClient = if (BuildConfig.DEBUG) {
        val trustManager = CustomTrustManager()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        val sslSocketFactory = sslContext.socketFactory
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    suspend inline fun <reified T> performRequest(
        url: String,
        headers: Map<String, String>,
        data: String? = null,
        mediaType: String? = null
    ): Result<T> where T : BaseResponse {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .headers(headers.toHeaders())

            if (!data.isNullOrBlank()) {
                request.post(data.toRequestBody(mediaType?.toMediaType()))
            }

            try {
                var msg: String
                if (BuildConfig.DEBUG) {
                    msg = "Request $data"
                    Log.d("HttpUtil", msg)
                }
                val response = client.newCall(request.build()).execute()
                val responseData = response.body?.string()
                response.close()
                if (BuildConfig.DEBUG) {
                    msg = "Response $responseData"
                    Log.d("HttpUtil", msg)
                }

                if (response.isSuccessful) {
                    return@withContext Result.success(JsonHelper.fromJson(responseData ?: "", T::class.java))
                } else {
                    return@withContext Result.failure(IOException("HTTP Request failed with code ${response.code}"))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext Result.failure(e)
            }
        }
    }
}

@SuppressLint("CustomX509TrustManager")
class CustomTrustManager : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}