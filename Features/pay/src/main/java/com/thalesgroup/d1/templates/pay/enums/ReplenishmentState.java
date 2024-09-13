package com.thalesgroup.d1.templates.pay.enums;

/**
 * Replenishment states.
 */
public enum ReplenishmentState {
    /**
     * Authentication on device has been successful.
     */
    DEVICE_AUTHENTICATION_SUCCESS,

    /**
     * Authentication on device has failed.
     */
    DEVICE_AUTHENTICATION_FAILED,

    /**
     * Authentication on device has encountered an error.
     */
    DEVICE_AUTHENTICATION_ERROR,

    /**
     * Authentication on device help.
     */
    DEVICE_AUTHENTICATION_HELP,

    /**
     * Replenishment success.
     */
    SUCCESS,

    /**
     * Replenishment error.
     */
    ERROR,

    /**
     * TODO
     */
    ODA_SUCCESS,

    /**
     * TODO
     */
    ODA_ERROR
}
