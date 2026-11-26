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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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

            try {
                final Map<String, String> bodyMap = new HashMap<>();
                bodyMap.put("consumer_id", Configuration.consumerId);
                bodyMap.put("kid", Configuration.kid);
                bodyMap.put("iss", Configuration.iss);
                bodyMap.put("aud", Configuration.aud);
                bodyMap.put("scope", Configuration.scope);
                bodyMap.put("cbp", Configuration.cbp);

                final URL url = new URL(Configuration.jwtUrl);
                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Authorization",
                        "Basic " + Base64.encodeToString(String.format("%s:%s",
                                                Configuration.jwtUsername,
                                                Configuration.jwtPassword)
                                        .getBytes(StandardCharsets.UTF_8),
                                Base64.NO_WRAP));
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setInstanceFollowRedirects(false);

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
                    jwtCallback.onJwtError(String.format(Locale.ENGLISH,
                            "Invalid response code: %d",
                            httpURLConnection.getResponseCode()));
                }
            } catch (final MalformedURLException e) {
                throw new IllegalStateException(e);
            } catch (final IOException exception) {
                jwtCallback.onJwtError(Objects.requireNonNull(exception.getLocalizedMessage()));
            } finally {
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
