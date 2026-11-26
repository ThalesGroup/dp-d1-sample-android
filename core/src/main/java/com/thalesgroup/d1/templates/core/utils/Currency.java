/*
 * Copyright © 2025 THALES. All rights reserved.
 */


package com.thalesgroup.d1.templates.core.utils;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

/**
 * Class representing a currency.
 *
 * @author PB
 */
public class Currency {
    /**
     * Position of currency before amount.
     */
    public static final int BEFORE = 0;
    /**
     * Position of currency symbol after amount.
     */
    public static final int AFTER = 1;

    /**
     * The numeric currency code according to ISO 4217.
     * 840 for USD
     */
    private final int mCurrencyNumber;

    /**
     * The numeric currency code according to ISO 4217.
     * ex: 'USD'
     */
    private final String mCurrencyCode;

    /**
     * The currency symbol to be displayed on the UI.
     */
    private final String mCurrencySymbol;
    /**
     * The conversion rate to cents of euros.
     */
    private final int mConversionRate;
    /**
     * Currency symbol display position (before:0 or after:1).
     */
    private final int mPosition;
    /**
     * Decimal separator position from left (0=no decimal places, 1=one decimal place, ...).
     * Also used for parsing from the string where the decimal separator position is implicit.
     */
    private int mDecimalSeparatorPosition;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Building object
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor for Currency.
     *
     * @param currCodeNum    Currency code as Integer.
     * @param currencyCode   Currency doe as String.
     * @param currencySymbol Currency symbol as String.
     * @param conversionRate Conversion rate to Cents of Euros.
     * @param position       Integer.
     */
    public Currency(final int currCodeNum,
                    final String currencyCode,
                    final String currencySymbol,
                    final int conversionRate,
                    final int position) {
        mCurrencyNumber = currCodeNum;
        mCurrencyCode = currencyCode;
        mCurrencySymbol = currencySymbol;
        mConversionRate = conversionRate;
        mPosition = position;
        mDecimalSeparatorPosition = 2;
    }

    /**
     * Formats the amount to the currency specific formatting and adds symbol to it.
     *
     * @param trxAmount Must be two exponent formatted value! E.g. 12.23
     *
     * @return Formatted string for given currency with symbol and correct exponent formatting.
     */
    public String getFormatAmountDisplay(final double trxAmount) {
        String output;
        DecimalFormat decimalFormat;

        switch (mDecimalSeparatorPosition) {
            case 0:
                decimalFormat = new DecimalFormat("0");
                break;
            case 1:
                decimalFormat = new DecimalFormat("0.0");
                break;
            case 2:
                decimalFormat = new DecimalFormat("0.00");
                break;
            case 3:
                decimalFormat = new DecimalFormat("0.000");
                break;
            default:
                decimalFormat = new DecimalFormat();
                break;
        }

        if (mPosition == Currency.BEFORE) {
            output = mCurrencySymbol + " " + decimalFormat.format(trxAmount);
        } else {
            output = decimalFormat.format(trxAmount) + " " + mCurrencySymbol;
        }
        return output;
    }

    /**
     * @param trxAmount Transaction amount as Double.
     *
     * @return String.
     */
    public String getFormatAmountCodeDisplay(final double trxAmount) {
        String output;
        DecimalFormat decimalFormat;

        switch (mDecimalSeparatorPosition) {
            case 0:
                decimalFormat = new DecimalFormat("0");
                break;
            case 1:
                decimalFormat = new DecimalFormat("0.0");
                break;
            case 2:
                decimalFormat = new DecimalFormat("0.00");
                break;
            case 3:
                decimalFormat = new DecimalFormat("0.000");
                break;
            default:
                decimalFormat = new DecimalFormat();
                break;
        }

        if (mPosition == Currency.BEFORE) {
            output = mCurrencyCode + " " + decimalFormat.format(trxAmount);
        } else {
            output = decimalFormat.format(trxAmount) + " " + mCurrencyCode;
        }
        return output;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Getting properties
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the decimal separator position (defaults to {@code 2}).
     *
     * @param decimalSeparatorPosition The position of the decimal separator, left aligned (0=no decimal places, 1=one
     *                                 decimal place, ...).
     *
     * @return Self for call chaining.
     */
    public Currency setDecimalSeparatorPosition(final int decimalSeparatorPosition) {
        mDecimalSeparatorPosition = decimalSeparatorPosition;
        return this;
    }

    /**
     * Retrieves the numeric currency code according to the ISO 4217.
     *
     * @return The currency code number.
     */
    public int getCode() {
        return mCurrencyNumber;
    }

    /**
     * Retrieves the Alphabetical currency code according to the ISO 4217.
     *
     * @return The currency code.
     */
    public String getCurrencyCode() {
        return mCurrencyCode;
    }

    @NonNull
    @Override
    public String toString() {
        return "Currency{" +
                "mCurrencyNumber=" + mCurrencyNumber +
                ", mCurrencyCode='" + mCurrencyCode + '\'' +
                ", mCurrencySymbol='" + mCurrencySymbol + '\'' +
                ", mConversionRate=" + mConversionRate +
                ", mPosition=" + mPosition +
                ", mDecimalSeparatorPosition=" + mDecimalSeparatorPosition +
                '}';
    }
}
