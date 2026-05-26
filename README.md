# D1 SDK Sample Android application

This project provides a code demonstrating an integration of Thales D1 SDK into an Android application. This serves not only as a guide but also as a ready made solution if code needs to be transferred 1:1 to a client application.


# Getting started


The following files need to be added to the project and updated based on the client [onboarding](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/configuration/2.-onboarding):

## TLDR files to add:
```bash
.
├── D1
│   ├── d1.properties
│   └── D1 SDK Binaries
│
└── NFC Payment Extras
    ├── Android Keystore
    ├── keystore.properties
    ├── gemcbp.properties
    ├── mobilegateway.properties
    ├── rages.properties
    └── google-services.json
```

>[!NOTE]
> Thales SDK support team could provide a configuration package to connect the app to the [Mobile SDK Sandbox](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/environments/mobile-sdk-sandbox) environment. If you are interested in this option please reach out to your Thales project manager or sales representative.

## D1 Backend Configuration
   
The `d1.properties` file which holds the D1 backend configuration needs to be added to the project.

**`core/src/main/assets/d1.properties`**
```bash
D1_SERVICE_URL = 
ISSUER_ID = 
D1_SERVICE_RSA_EXPONENT = 
D1_SERVICE_RSA_MODULUS = 
DIGITAL_CARD_URL = 
```
For using [Mobile SDK Sandbox](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/environments/mobile-sdk-sandbox) the file must also include:

```bash
CONSUMER_ID = 
CARD_ID = 
JWT_URL = 
JWT_USERNAME = 
JWT_PASSWORD = 
KID = 
ISS = 
AUD = 
SCOPE = 
```


The `d1.properties` file is **not** kept under version control to prevent it from being overwritten during repository update.


## D1 SDK Binaries

This sample application was tested with **D1 SDK version 4.2.0**.
Please refer to the sample application `build.gradle` files for the correct location of D1 SDK.

**`app/build.gradle`**
```groovy
debugImplementation project(":libs:d1-debug")
releaseImplementation project(":libs:d1-release")
```

```bash
libs/
├── d1-debug
│   ├── build.gradle
│   └── d1-debug-d1pay-4.2.0.aar
└── d1-release
    ├── build.gradle
    └── d1-release-d1pay-4.2.0.aar
```

**`settings.gradle`**
```groovy
include':app',
    ':libs:d1-debug',
    ':libs:d1-release'
```

For more details, please refer to the [Binary Integration](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/configuration/1.-binary-integration/android) section of the D1 Developer Portal.

## Additional NFC Payment Configuration:
To use [NFC Payment](https://docs.payments.thalescloud.io/nfc-payment/get-started/overview) service, the following configurations need to be added:

1. Android Keystore
2. Additional Backend Configuration
3. Google Services

### 1. Android Keystore
To use [NFC Payment](https://docs.payments.thalescloud.io/nfc-payment/get-started/overview) service, the sample application needs to be signed with a specific keystore - see: [Issuer Application Signing Key section](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/configuration/2.-onboarding/initial-setup#issuer-application-signing-key). The sample application needs to be updated with the appropriate keystore and signing configuration.

```bash
app/keystore/keystore
```

**`app/build.gradle`**
```groovy

android {
    signingConfigs {
        def keystorePropertiesFile = rootProject.file("app/keystore/keystore.properties")
        def keystoreProperties = new Properties()

        dev {
            keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        }
    }
}
```

**`app/keystore/keystore.properties`**
```bash
storePassword= 
keyPassword= 
keyAlias= 
storeFile=keystore/keystore
```

### 2. Additional Backend Configuration
The following files with a working NFC Payment backend configuration need to be added:

**`1. features/pay/src/main/assets/gemcbp.properties`**
```bash

# The URL of the CPS server.
# To be provided by Thales integrator.
CPS_URL=https://dummy.endpoint.test/mobile/cps

# Server connection timeout in milliseconds.
CPS_CONNECTION_TIMEOUT=90000

# Read timeout for the HTTP input stream in milliseconds.
CPS_READ_TIMEOUT=90000

# Number of retries CPS Connection can perform.
# This is an Integer value and is provided by application developer.
# Default value is set to 5. This value is optional.
CPS_CONNECTION_RETRY_COUNT=5

# Retry connection timeout in milliseconds.
# Default value is set to 10000 and is provided by the application developer.
# This value is optional.
CPS_CONNECTION_RETRY_INTERVAL=10000
```

**`2. features/pay/src/main/assets/mobilegateway.properties`**
```bash

# The URL of the MG server.
# To be provided by Thales integrator.
MG_CONNECTION_URL=https://dummy.endpoint.test/mobile/mg

# Wallet provider id. To be provided by Thales integrator
WALLET_PROVIDER_ID=WalletProviderId

# optional field.
# An ID which identifies the mobile application uniquely.
# This applies in cases where there are different mobile applications for the same provider.
# To be provided by Thales integrator.
WALLET_APPLICATION_ID=myApplicationId

# Server connection timeout in milliseconds.
MG_CONNECTION_TIMEOUT=30000

# Read timeout for the HTTP input stream in milliseconds.
MG_CONNECTION_READ_TIMEOUT=30000

# Number of retries MG Connection can perform.
# This is an Integer value and is provided by application developer.
MG_CONNECTION_RETRY_COUNT=3

# Retry connection timeout in milliseconds.
MG_CONNECTION_RETRY_INTERVAL=10000

# The URL of the MG server.
# To be provided by Thales integrator. (not used for now)
MG_TRANSACTION_HISTORY_CONNECTION_URL=https://dummy.endpoint.test/mobile/mg
```

**`3. features/pay/src/main/assets/rages.properties`**
```bash

# A fixed parameter
REALM=CBP

OAUTH_CONSUMER_KEY=DUMMY_OAUTH_KEY_LABEL

RAGES_GATEWAY_URL=https://dummy.endpoint.test/rest/1.0

# Connection timeout in milliseconds.
RAGES_CONNECTION_TIMEOUT=30000

# Used for Certificate Signing Request in component that secures the HTTPS calls.
# Any value can be set.
# Refer to your Thales integrator for the value to be set for your solution.
CSR_DOMAIN=MyCompany

# Used for Certificate Signing Request in component that secures the HTTPS calls.
# Any value can be set.
# Refer to your Thales integrator for the value to be set for your solution.
CSR_EMAIL=contact@mycompany.com

# Number of retries Rages Connection can perform.
# This is an Integer value and is provided by application developer.
# Default value is set to 5.
# This value is optional.
CPS_CONNECTION_RETRY_COUNT=5

# Retry connection timeout in milliseconds.
# Default value is set to 10000 and is provided by the application developer.
# This value is optional.
CPS_CONNECTION_RETRY_INTERVAL=10000
```

These files are not kept under version control to prevent them from being overwritten during repository update.


### 3. Google Services

[NFC Payment](https://docs.payments.thalescloud.io/nfc-payment/get-started/overview) service requires [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging) integration. For this reason the corresponding `google-services.json` file needs to be added to the sample application.

```bash
app/google-services.json
```

# Authentication
To receive access to all D1 services, the user needs to authenticate with D1. This authentication is done using a [JSON Web Token (JWT)](https://auth0.com/docs/secure/tokens/json-web-tokens). For the [Mobile SDK Sandbox](https://docs.payments.thalescloud.io/nfc-payment/integrate-d1-sdk/getting-started/environments/mobile-sdk-sandbox) a web service is used to fetch the JWT. The JWT configuration is part of the `d1.properties` file.

For more details, please refer to the [D1 SDK Login](https://docs.payments.thalescloud.io/push-provisioning/integrate-the-d1-sdk/getting-started/configuration/5.-authentication/sdk-login) section of the D1 Developer Portal.


# Build and run project
After all of the configurations have been added, the application can be build either using Android Studio, or from the command line.

```bash
>> ./gradlew assemble
>> adb install app/build/outputs/apk/debug/app-debug.apk
```

# Project structure
The sample application is divided in to multiple modules.

```bash
.
├── app
├── core
│
├── features
│   ├── clicktopay
│   ├── pay
│   ├── physicalcard
│   ├── push
│   └── virtualcard
│
└── libs
    ├── D1-debug
    └── D1-release
```


## Use Cases
* app - Main application.
* core - Common classes and components for all modules.
* features/clicktopay - [Click to Pay](https://docs.payments.thalescloud.io/click-to-pay)
* features/pay - [NFC Payment](https://docs.payments.thalescloud.io/nfc-payment)
* features/physicalcard - [Physical Issuance](https://docs.payments.thalescloud.io/pin-management)
* features/push - [Push Provisioning](https://docs.payments.thalescloud.io/push-provisioning)
* features/virtualCard - [Secure Card Display](https://docs.payments.thalescloud.io/secure-card-display)
* libs - D1 SDK binaries.


# Documentation
[Thales D1 Platform Developer Portal](https://docs.payments.thalescloud.io/)


# Contributing
If you are interested in contributing to the D1 SDK Sample Android application, start by reading the [Contributing guide](/CONTRIBUTING.md).

# License
[LICENSE](/LICENSE)
