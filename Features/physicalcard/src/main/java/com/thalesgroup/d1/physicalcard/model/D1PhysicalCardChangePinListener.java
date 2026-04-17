/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.model;

import com.thalesgroup.gemalto.d1.D1Exception;

import androidx.annotation.NonNull;

public interface D1PhysicalCardChangePinListener {
    void onFirstEntryFinished();

    void onPinMatch(@NonNull final D1SubmitPinChange submitPinChange);

    void onPinMismatch();

    /**
     * Physical card PIN change success.
     */
    void onChangePinSuccess();

    /**
     * Error during physical card operation.
     *
     * @param exception Error detail.
     */
    void onChangePinErrorError(final D1Exception exception);

    interface D1SubmitPinChange {
        void submitPinChange();
    }
}
