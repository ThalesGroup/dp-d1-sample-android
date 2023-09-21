package com.thalesgroup.d1.templates.core.jwt;

import androidx.annotation.NonNull;

/**
 * Callback for JWT fetching.
 */
public interface JwtCallback {
    /**
     * JWT fetched success.
     *
     * @param jwt JWT token.
     */
    void onJwtSuccess(@NonNull final String jwt);

    /**
     * Error fetching JWT.
     *
     * @param error Error description.
     */
    void onJwtError(@NonNull final String error);
}
