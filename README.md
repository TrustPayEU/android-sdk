<h1 align="center">TrustPay Android SDK</h1>

<p align="center">
  <a href="#about">About</a> &#xa0; | &#xa0; 
  <a href="#features">Features</a> &#xa0; | &#xa0;
  <a href="#requirements">Requirements</a> &#xa0; | &#xa0;
  <a href="#usage">Usage</a> &#xa0; | &#xa0;
  <a href="#license">License</a> &#xa0; | &#xa0;
</p>


## About ##

The TrustPay Android SDK is a beta version designed to integrate basic payment processing functionalities into Android applications. Developed in Kotlin, this SDK supports crucial payment methods such as card transactions and wire transfers, offering a secure and straightforward solution for Android developers.

## Features ##

- Wire Transfers
- Card(Purchase)

## Requirements ##

- Android API level 26 (Oreo) or higher
- Kotlin 1.3 or later
- Android Studio 3.0 or later

## Installation ##

To integrate the SDK into your Android Studio project, follow these steps:

#### Gradle Dependency ####

Add the git repository to apps. settings.gradle.kts
```
sourceControl {
    gitRepository(URI.create("https://github.com/TrustPayEU/Android-SDK.git")) {
        producesModule("sk.trustpay.api:sdk")
    }
}
```

Add the following dependency to your app's build.gradle file:

```
dependencies {
    implementation("sk.trustpay.api:sdk:0.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.browser:browser:1.7.0")
}
```

## Usage ##

Import the SDK in your Kotlin file:
```
import sk.trustpay.api.sdk.common.*
import sk.trustpay.api.sdk.dto.*
import sk.trustpay.api.sdk.methods.*
```
Setup TokenProvider:
```
val tokenProvider = TokenProvider({YourProjectId}, {YourSecretKey})
```
Setup Wire Request: 
```
val wireRequest = WireRequest(
            MerchantIdentification({ProjectId}), PaymentInformation(
                AmountWithCurrency({amount}, {currency}),
                References({reference})
            ), CallbackUrls(
                "{example.com/result?state=success}",
                "{example.com/result?state=cancel}",
                "{example.com/result?state=error}"
            )
        )
```

Setup CardRequest:
```
val request = CardRequest(
            MerchantIdentification(DemoPaymentData.ProjectId),
            PaymentInformation(
                AmountWithCurrency(amount, currency),
                References(reference)
            ),
            CardTransaction(CardPaymentType.Purchase),
            ), CallbackUrls(
                "{example.com/result?state=success}",
                "{example.com/result?state=cancel}",
                "{example.com/result?state=error}"
            )
```

#### Get response and show gateway ###
```
val result = wireRequest.createPaymentRequest(tokenProvider)
if (result.isFailure) {
    Toast.makeText(this@MainActivity, result.exceptionOrNull()?.message ?: "Something went wrong", Toast.LENGTH_LONG)
        .show()
    return
}
result.getOrThrow().launchPopupWebView(this@MainActivity, {RedirectUrlYouWantToCatch},
{YourActivityWhereYouWantToShowResult}::class.java.name, applicationContext.packageName)
```
Customization

As this is a beta version, customization options are limited. Future updates will include more customization features.

## License ##

This project is under license from MIT. For more details, see the [LICENSE](LICENSE.md) file.

&#xa0;

<a href="#top">Back to top</a>
