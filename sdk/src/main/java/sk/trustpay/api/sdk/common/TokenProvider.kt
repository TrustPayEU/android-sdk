package sk.trustpay.api.sdk.common

import sk.trustpay.api.sdk.dto.TokenResponse
import sk.trustpay.api.sdk.shared.AUTH_ENDPOINT
import sk.trustpay.api.sdk.shared.HttpUtil
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

private var tokenCached: TokenResponse? = null
private var tokenExpire: Instant? = null

class TokenProvider(private val projectId: String, private val secret: String) {
    suspend fun getToken(): Result<TokenResponse> {

        if (tokenExpire != null && Instant.now() < tokenExpire) {
            return Result.success(tokenCached!!)
        }

        val now = Instant.now().plusSeconds(-1)
        val response = HttpUtil.performRequest<TokenResponse>(
            AUTH_ENDPOINT,
            mapOf("Authorization" to encodeCredentials(projectId, secret)),
            "grant_type=client_credentials",
            "application/x-www-form-urlencoded"
        )

        if (!response.isSuccess) {
            return Result.failure(
                IOException(
                    response.exceptionOrNull()?.message ?: "Error occurred"
                )
            )
        }

        val tokenObject = response.getOrThrow()
        if (tokenObject.accessToken.isNullOrBlank()) {
            return Result.failure(IOException("Unable to get token: ${tokenObject.resultInfo?.resultCode} ${tokenObject.resultInfo?.additionalInfo}"))
        }

        tokenCached = tokenObject
        tokenExpire = now.plusSeconds(tokenCached!!.expires!!.toLong())
        return Result.success(tokenCached!!)
    }

    private fun encodeCredentials(projectId: String, secret: String): String {
        val credentials = "$projectId:$secret"
        val base64Credentials = Base64.getEncoder()
            .encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))
        return "Basic $base64Credentials"
    }
}