/*
 * Copyright © 2025 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.utils;

import android.content.Context;

public final class ApplicationContextResolver {

    private static final ApplicationContextResolver INSTANCE = new ApplicationContextResolver();

    private Context mApplicationContext;

    private ApplicationContextResolver() {
        // Private constructor
    }

    public static Context getApplicationContext() {
        return INSTANCE.mApplicationContext;
    }


    public static void setApplicationContext(final Context applicationContext) {
        INSTANCE.mApplicationContext = applicationContext;
    }
}
