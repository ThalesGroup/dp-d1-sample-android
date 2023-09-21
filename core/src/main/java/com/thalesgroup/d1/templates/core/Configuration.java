/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * D1 SDK Configuration.
 */
public class Configuration {
    private static final String FILENAME = "d1.properties";

    public static String d1ServiceUrl;
    public static String issuerId;
    public static String d1ServiceRsaExponent;
    public static String d1ServiceRsaModulus;

    public static String digitalCardUrl;

    public static String consumerId;
    public static String cardId;

    public static String jwtUrl;
    public static String jwtUsername;
    public static String jwtPassword;

    /**
     * Loads the D1 configuration from assets file.
     *
     * @param context Context.
     */
    public static void loadConfigurationFromAssets(@NonNull final Context context) {
        d1ServiceUrl = getProperty("D1_SERVICE_URL", context, FILENAME);
        issuerId = getProperty("ISSUER_ID", context, FILENAME);
        d1ServiceRsaExponent = getProperty("D1_SERVICE_RSA_EXPONENT", context, FILENAME);
        d1ServiceRsaModulus = getProperty("D1_SERVICE_RSA_MODULUS", context, FILENAME);
        digitalCardUrl = getProperty("DIGITAL_CARD_URL", context, FILENAME);
        consumerId = getProperty("CONSUMER_ID", context, FILENAME);
        cardId = getProperty("CARD_ID", context, FILENAME);
        jwtUrl = getProperty("JWT_URL", context, FILENAME);
        jwtUsername = getProperty("JWT_USERNAME", context, FILENAME);
        jwtPassword = getProperty("JWT_PASSWORD", context, FILENAME);
    }

    /**
     * Reads the property from a property file.
     *
     * @param key      Name of property.
     * @param context  Context.
     * @param fileName Property file.
     *
     * @return Value, if value is {@code null} then {@code IllegalStateException} is thrown.
     */
    private static String getProperty(@NonNull final String key, @NonNull final Context context, @NonNull final String fileName) {
        InputStream inputStream = null;
        try {
            final Properties properties = new Properties();
            final AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(fileName);
            properties.load(inputStream);

            final String property = properties.getProperty(key);
            if (property == null) {
                throw new IllegalStateException(String.format("Missing mandatory property: %s", key));
            }

            return property;
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
    }
}
