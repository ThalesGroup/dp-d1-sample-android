/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.utils;

import androidx.annotation.NonNull;

/**
 * HexUtils.
 */
public class HexUtils {
    /**
     * @param input String.
     *
     * @return {@code ByteArray}.
     */
    public static byte[] hexStringToByteArray(@NonNull final String input) {
        final int len = input.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4) + Character.digit(input.charAt(i + 1), 16));
        }

        return data;
    }

}
