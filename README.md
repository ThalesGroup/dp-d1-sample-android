# D1 SDK Sample Android application v2

Sample application to show the integration of D1 SDK in to an Android application. This serves not only as a guide but also 
as a ready made solution if code needs to be transferred 1:1 to client applications.

# Getting started

*Note: Thales SDK support team will supply all config files directly via email.*

The following files need to be added to the project:

## TLDR files to add:
```bash
.
├── D1
│   ├── d1.properties
│   └── D1 SDK Binaries
│
└── D1-Pay-Extra
    ├── Android Keystore
    ├── keystore.properties
    ├── gemcbp.properties
    ├── mobilegateway.properties
    ├── rages.properties
    └── google-services.json
```

## D1 Backend Configuration
   
The `d1.properties` file which holds the D1 backend configuration needs to be added to the project.

**`core/src/main/assets/d1.properties`**
```bash
D1_SERVICE_URL = 
ISSUER_ID = 
D1_SERVICE_RSA_EXPONENT = 
D1_SERVICE_RSA_MODULUS = 
DIGITAL_CARD_URL = 
CONSUMER_ID = 
CARD_ID = 
JWT_URL = 
JWT_USERNAME = 
JWT_PASSWORD = 
```

The `d1.properties` file is **not** kept under version control to prevent it from being overwritten during repository update.

For more details, please refer to the [D1 SDK Setup](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/4f003bf306c04-initial-setup) section of the D1 Developer Portal.

## D1 SDK Binaries

This sample application was tested with **D1 SDK version 4.0.0**.
Please refer to the sample application `build.gradle` files for the correct location of D1 SDK.

**`app/build.gradle`**
```groovy
debugImplementation project(":libs:d1-debug")
releaseImplementation project(":libs:d1-release")
```

```bash
Libs/
├── D1-debug
│   ├── build.gradle
│   └── d1-debug-d1pay-4.0.0.aar
└── D1-release
    ├── build.gradle
    └── d1-release-d1pay-4.0.0.aar
```

**`settings.gradle`**
```groovy
include':app',
    ':libs:d1-debug',
    ':libs:d1-release'
```

For more details, please refer to the [D1 SDK Integration](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/f2c8e49a37919-integrate-sdk-binary-into-your-android-application) section of the D1 Developer Portal.

## Additional D1-Pay Configuration:
To use [D1-Pay](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/d6a8ba3f3c186-d1-pay-introduction) services, the following configurations need to be added:

* Android Keystore
* D1-Pay Backend Configuration
* Google Services

### Android Keystore
To use [D1Pay](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/d6a8ba3f3c186-d1-pay-introduction) services, the sample application needs to be signed with a specific keystore - [Mobile banking application signing key](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/627614736c261-what-is-app-pk-value-and-how-to-obtain-it). The sample application needs to be updated with the appropriate keystore and signing configuration.

```bash
app/keystore/keystore
```

**`app/build.gradle`**
```groovy

android {
    signingConfigs {
        def keystorePropertiesFile = rootProject.file("app/src/main/assets/keystore.properties")
        def keystoreProperties = new Properties()

        release {
            keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        }

        debug {
            keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        }
    }
}
```

**`app/src/main/assets/keystore.properties`**
```bash
storePassword= 
keyPassword= 
keyAlias= 
storeFile=keystore/keystore
```

### 2. D1-Pay Backend Configuration
The following files with a working D1Pay backend configuration need to be added:

**`1. Features/pay/src/main/assets/gemcbp.properties`**
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

**`2. Features/pay/src/main/assets/mobilegateway.properties`**
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

**`3. Features/pay/src/main/assets/rages.properties`**
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

For more details, please refer to the [D1Pay Service Application Setup](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/09e7447f9cb9b-d1-pay-application-setup) section of the D1 Developer Portal.

### 3. Google Services

[D1Pay](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/d6a8ba3f3c186-d1-pay-introduction) services use [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging). For this reason the corresponding `google-services.json` file needs to be added to the sample application.

```bash
app/google-services.json
```

# Authentication
To receive access to all D1 services, the user needs to authenticate with D1. This authentication is done using a [JSON Web Token (JWT)](https://auth0.com/docs/secure/tokens/json-web-tokens). For the [sandbox environment](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/fa46cefb0ef05-mobile-sdk-sandbox) a web service is used to fetch the JWT. The JWT configuration is part of the `d1.properties` file.

For more details, please refer to the [D1 SDK Login](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/97566495c786d-sdk-login) section of the D1 Developer Portal.

# Build and run project
After all of the configurations have been added, the application can be build. Application can be build either using Android Studio, or from the command line.

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
├── Features
│   ├── pay
│   ├── push
│   └── virtualcard
│
└── Libs
    ├── D1-debug
    └── D1-release
```
## Use Cases
* app - Main application.
* core - Common classes and components for all modules.
* Features/pay - [D1-Pay](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/d6a8ba3f3c186-d1-pay-introduction) use cases.
* Features/push - [D1-Push](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/294f33eaf2378-introduction) use cases.
* Features/virtualCard - [Virtual Card](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/3c8a7e6f0a81a-card-display-introduction) use cases.
* Libs - D1 SDK binaries.

# Documentation
[D1 Developer portal](https://thales-dis-dbp.stoplight.io/docs/d1-developer-portal/branches/main/de9abde9af194-thales-d1-a-card-api-to-modernise-card-issuance)


# Contributing
If you are interested in contributing to the D1 SDK Sample Android application, start by reading the [Contributing guide](/CONTRIBUTING.md).

# License
[LICENSE](/LICENSE)
