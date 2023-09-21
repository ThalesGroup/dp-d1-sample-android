/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.model;

import android.app.Activity;
import android.content.Context;

import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

/**
 * D1PushApi.
 */
public interface D1PushApi {

    /**
     * Creates the D1Push related {@code D1ModuleConnector}.
     *
     * @param activity Activity.
     * @param oemPayType OEM Type.
     * @param samsungServiceID Samsung service ID.
     *
     * @return D1Push related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector(final Activity activity, final OEMPayType oemPayType, final String samsungServiceID);
}
