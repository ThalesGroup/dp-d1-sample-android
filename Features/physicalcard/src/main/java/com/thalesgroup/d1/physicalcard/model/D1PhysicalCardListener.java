/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;

/**
 * D1PhysicalCardListener
 */
public interface D1PhysicalCardListener {

    /**
     * Physical card activation success.
     */
    void onActivatePhysicalCardSuccess();

    /**
     * Retrieval of physical card PIN success.
     */
    void onGetPhysicalCardPinSuccess();

    /**
     * Retrieval of physical card {@code CardActivationMethod} method success.
     *
     * @param cardActivationMethod CardActivationMethod.
     */
    void onGetActivationMethodSuccess(@NonNull final CardActivationMethod cardActivationMethod);

    /**
     * Error during physical card operation.
     *
     * @param exception Error detail.
     */
    void onPhysicalCardOperationError(final D1Exception exception);
}
