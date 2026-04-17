/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.utils;

import androidx.annotation.NonNull;

/**
 * HexUtils.
 */
public class HexUtils {
    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

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

    /**
     * Creates hex string from bytes.
     *
     * @param bytes Bytes from which to create the hexa string.
     *
     * @return Hex string.
     */
    public static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int value = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[value >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }
        return new String(hexChars);
    }
}
