/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.model;

/**
 * D1TaskCallbackLambda.
 *
 * @param <T> T.
 */
public interface D1TaskCallbackLambda<T> {
    /**
     * @param var T.
     */
    void onSuccess(T var);
}
