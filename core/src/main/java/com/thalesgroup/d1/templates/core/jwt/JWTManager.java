package com.thalesgroup.d1.templates.core.jwt;

import android.util.Base64;

import com.thalesgroup.d1.templates.core.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JWTManager.
 */
public class JWTManager {

    static ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Gets Issuer Token String from network.
     *
     * @param jwtCallback {@code JwtCallback}.
     */
    public static void getJwtAuthToken(final JwtCallback jwtCallback) {
        executor.execute(() -> {
            BufferedReader input = null;
            HttpURLConnection httpURLConnection = null;

            try {
                final URL url = new URL(Configuration.jwtUrl + Configuration.consumerId);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                final String userpass = Configuration.jwtUsername + ":" + Configuration.jwtPassword;
                httpURLConnection.setRequestProperty(
                        "Authorization",
                        "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.DEFAULT))
                );

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    input = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    final StringBuilder response = new StringBuilder();
                    String inputLine = input.readLine();
                    while (inputLine != null) {
                        response.append(inputLine);
                        inputLine = input.readLine();
                    }
                    input.close();

                    jwtCallback.onJwtSuccess(response.toString());
                } else {
                    jwtCallback.onJwtError(httpURLConnection.getResponseMessage());
                }
            } catch (final IOException e) {
                jwtCallback.onJwtError(e.toString());
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        // nothing to do
                    }
                }
            }
        });
    }
}
