/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.model;

import android.content.Context;

import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.PINDisplayTextView;
import com.thalesgroup.gemalto.d1.PINEntryUI;
import com.thalesgroup.gemalto.d1.SecureEditText;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;

/**
 * D1PhysicalCardApi.
 */
public interface D1PhysicalCardApi {
    /**
     * Activates the physical card.
     *
     * @param cardId         Card ID.
     * @param secureTextEdit Secure text edit, where the code is entered.
     * @param listener       Callback.
     */
    void activatePhysicalCard(@NonNull final String cardId,
                              @NonNull final SecureEditText secureTextEdit,
                              @NonNull final D1PhysicalCardListener listener);

    /**
     * Retrieves the PIN of the physical card.
     *
     * @param cardId             Card ID.
     * @param pinDisplayTextView PINDisplayTextView where the PIN is displayed.
     * @param listener           Callback.
     */
    void getPhysicalCardPin(@NonNull final String cardId,
                            @NonNull final PINDisplayTextView pinDisplayTextView,
                            @NonNull final D1PhysicalCardListener listener);

    /**
     * Retrieves physical card activation method.
     *
     * @param cardId   Card ID.
     * @param listener Callback.
     */
    void getActivationMethod(@NonNull final String cardId, @NonNull final D1PhysicalCardListener listener);

    /**
     * Changes the PIN for the physical card.
     *
     * @param cardId               Card ID.
     * @param enterPinSecureTextEdit  Secure text edit, where the new PIN is entered.
     * @param confirmPinSecureTextEdit Secure text edit, where the new PIN is confirmed.
     * @param listener             Callback.
     */
    void changePin(@NonNull final String cardId,
                   @NonNull final SecureEditText enterPinSecureTextEdit,
                   @NonNull final SecureEditText confirmPinSecureTextEdit,
                   @NonNull final D1PhysicalCardChangePinListener listener);

    /**
     * Creates the D1PhysicalCard related {@code D1ModuleConnector}.
     *
     * @return D1Pay related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector();
}
