package com.thalesgroup.d1.templates.core.jwt;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thalesgroup.d1.templates.core.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
                final Map<String, String> bodyMap = new HashMap<>();
                bodyMap.put("consumer_id", Configuration.consumerId);
                bodyMap.put("kid", Configuration.kid);
                bodyMap.put("iss", Configuration.iss);
                bodyMap.put("aud", Configuration.aud);
                bodyMap.put("scope", Configuration.scope);

                final URL url = new URL(Configuration.jwtUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setInstanceFollowRedirects(false);

                final String userpass = Configuration.jwtUsername + ":" + Configuration.jwtPassword;
                httpURLConnection.setRequestProperty(
                        "Authorization",
                        "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.DEFAULT))
                );

                final Gson gson = new Gson();
                final String jsonBody = gson.toJson(bodyMap);
                final byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                httpURLConnection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

                try (OutputStream os = httpURLConnection.getOutputStream()) {
                    os.write(bytes);
                    os.flush();
                }

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    input = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    final StringBuilder response = new StringBuilder();
                    String inputLine = input.readLine();
                    while (inputLine != null) {
                        response.append(inputLine);
                        inputLine = input.readLine();
                    }
                    input.close();

                    final Map<String, String> respMap = gson.fromJson(response.toString(), new TypeToken<Map<String, String>>() {
                    }.getType());

                    final String token = respMap.get("jwt");

                    if (token == null) {
                        jwtCallback.onJwtError(String.format(Locale.ENGLISH,
                                "Invalid response during JWT authentication: %s",
                                response.toString()));
                        return;
                    }

                    jwtCallback.onJwtSuccess(token);
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
