package sk.trustpay.api.sdk.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResultInfo(
    @Expose(deserialize = false)
    @SerializedName("ResultCode")
    val resultCode: Int?,
    @Expose(deserialize = false)
    @SerializedName("CorrelationId")
    val correlationId: String?,
    @Expose(deserialize = false)
    @SerializedName("AdditionalInfo")
    val additionalInfo: String?
)

abstract class BaseResponse {
    val resultInfo: ResultInfo? = null
}

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    @SerializedName("expires_in")
    val expires: String?
) : BaseResponse()