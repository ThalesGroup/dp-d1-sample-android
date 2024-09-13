/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.model;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.gemalto.d1.D1Params;

/**
 * Main interface for every supported module of the template app.
 */
public interface D1ModuleConnector {
    /**
     * Used by the core to get configuration for the given module.
     *
     * @return Configuration for the given module. It can be null.
     */
    D1Params getConfiguration();

    /**
     * Return module identification.
     *
     * @return {@code Constants.Module}.
     */
    Constants.Module getModuleId();
}
