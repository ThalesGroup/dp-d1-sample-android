/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core;

import com.thalesgroup.gemalto.d1.BuildConfig;

/**
 * Constants for Core Module.
 */
public class Constants {

    /**
     * Error codes for SDK initalisation.
     */
    public static class ErrorCodeCore {
        public static final long SDK_ALREADY_INITIALIZED = 10001;
        public static final long SDK_INIT_ONGOING = 10002;
        public static final long SDK_INIT_FAILED = 10003;
        public static final long SDK_NOT_CONFIGURED = 10004;
    }

    /**
     * Modules which are active.
     */
    public enum Module {
        D1PAY(1),
        D1PUSH(1 << 1),
        VIRTUAL_CARD(1 << 2),
        CORE(1 << 3),
        PHYSICAL_CARD(1 << 4);

        private final int mValue;

        Module(final int value) {
            mValue = value;
        }

        boolean isPresentIn(final int value) {
            return (value & mValue) == mValue;
        }

        int rawValue() {
            return mValue;
        }
    }

    public static final boolean SHOW_LOG = BuildConfig.DEBUG;
    public static final String UPDATE_UI_ACTION = "UPDATE_UI_ACTION";
    public static final String UPDATE_UI_REQUEST = "UPDATE_UI_REQUEST";
    public static final String UPDATE_UI_DIGITAL_PAY_CARD_LIST = "UPDATE_UI_DIGITAL_PAY_CARD_LIST";
    public static final String DIGITAL_PAY_CARD_REPLENISHMENT = "DIGITAL_PAY_CARD_REPLENISHMENT";
    public static final String DIGITAL_PAY_CARD_REPLENISHMENT_STATE = "DIGITAL_PAY_CARD_REPLENISHMENT_STATE";
    public static final String DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE = "DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE";
    public static final String DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED = "DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED";
    public static final String DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_DATA = "DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_DATA";
    public static final String DIGITAL_PAY_CARD_NOTIFICATION = "DIGITAL_PAY_CARD_NOTIFICATION";
    public static final String DIGITAL_PAY_CARD_NOTIFICATION_STATE = "DIGITAL_PAY_CARD_NOTIFICATION_STATE";
    public static final String DIGITAL_PAY_CARD_NOTIFICATION_MESSAGE = "DIGITAL_PAY_CARD_NOTIFICATION_MESSAGE";
}
