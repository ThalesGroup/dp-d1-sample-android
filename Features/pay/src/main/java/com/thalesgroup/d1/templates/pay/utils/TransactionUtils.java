/*
 * MIT License
 *
 * Copyright (c) 2021 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.thalesgroup.d1.templates.pay.utils;

import android.content.Context;
import android.util.Log;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.utils.Currency;
import com.thalesgroup.d1.templates.core.utils.UtilsCurrenciesConstants;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * TransactionUtils.
 */
public final class TransactionUtils {


    /**
     * @param amount        Double.
     * @param displayAmount Amount string for display.
     * @param currencyCode  Currency code string.
     * @param context       Context.
     *
     * @return Amount string.
     */
    public static String getAmount(final double amount, final String displayAmount, final String currencyCode, final Context context) {
        String formattedAmount;
        final DecimalFormat decimalFormat = new DecimalFormat("0.00"); // only in case no currency

        final Currency currency = UtilsCurrenciesConstants.getCurrency(currencyCode);

        //Android Currency library
        java.util.Currency currencyAndroid;

        if (Constants.SHOW_LOG) {
            Log.d("getAmount", "getFormattedAmountToDisplay::trxAmount=" + amount);
            Log.d("getAmount", "getFormattedAmountToDisplay::currencyCode=" + currencyCode);
        }

        if (currency == null) {
            if (Constants.SHOW_LOG) {
                Log.w("getAmount", "getFormattedAmountToDisplay: unable to resolve D1 Templates currency based on the provided currency code");
            }
            // Try to get Android currency
            currencyAndroid = java.util.Currency.getInstance(currencyCode);
            if (currencyAndroid != null) {
                formattedAmount = decimalFormat.format(amount) + " " + currencyAndroid.getCurrencyCode();
            } else {
                formattedAmount = decimalFormat.format(amount);
            }
        } else {
            if (Constants.SHOW_LOG) {
                Log.w("getAmount", "getFormattedAmountToDisplay::currency=" + currency);
            }

            final String deviceCurrency = java.util.Currency.getInstance(Locale.getDefault()).getCurrencyCode();

            // Compare transaction currency with current system currency
            if (!currency.getCurrencyCode().equals(deviceCurrency)) {
                formattedAmount = currency.getFormatAmountCodeDisplay(amount);
            } else {
                formattedAmount = currency.getFormatAmountDisplay(amount);
            }
        }

        return formattedAmount;
    }

    /**
     * Formats a dateTime string.
     *
     * @param dateTime String with specific pattern.
     *
     * @return New string with pattern "dd.MM.yyyy HH:mm"
     */
    public static String getFormattedLocalDateTime(final String dateTime) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
        final LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return formatter.format(localDateTime);
    }
}
