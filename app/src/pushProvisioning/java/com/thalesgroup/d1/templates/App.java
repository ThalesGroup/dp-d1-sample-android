/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates;

import android.app.Application;

import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.utils.ApplicationContextResolver;

/**
 * App.
 */
public class App extends Application {

    /**
     * App onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Set the context resolver for all helpers like accessing text resources etc...
        ApplicationContextResolver.setApplicationContext(this);

        Configuration.loadConfigurationFromAssets(this);
    }
}
