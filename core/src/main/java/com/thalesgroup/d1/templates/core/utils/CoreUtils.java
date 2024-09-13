package com.thalesgroup.d1.templates.core.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1CoreListener;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * CoreUtils container holding generic helper functions.
 */
final public class CoreUtils {

    private static final String TAG = CoreUtils.class.getSimpleName();

    private static final CoreUtils INSTANCE;

    private CoreUtils() {
        // private constructor
    }

    static {
        INSTANCE = new CoreUtils();
    }

    /**
     * Return singleton of public API for application use.
     *
     * @return {@code CoreUtils}.
     */
    public static CoreUtils getInstance() {
        return INSTANCE;
    }

    /**
     * @param tag   String.
     * @param code  Long.
     * @param issue String.
     */
    public void reportGenericIssue(final String tag, final long code, final String issue) {
        // TODO: Add logging tool class.
        Log.i(tag, code + ": " + issue);

        // Notify application in the main thread if there is any valid listener.
        final WeakReference<D1CoreListener> listener = D1Core.getInstance().getListener();
        if (listener != null) {
            runInMainThread(() -> listener.get().onGenericError(code, issue));
        }
    }

    /**
     * @param runnable Runnable.
     */
    public void runInMainThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * @param context  Context.
     * @param fileName String.
     * @param data     {@code ByteArray}.
     */
    public void writeToFile(final Context context, final String fileName, final byte[] data) {

        try (FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            outputStream.write(data);
        } catch (final IOException exception) {
            if (exception.getMessage() != null) {
                Log.e(TAG, exception.getMessage());
            } else {
                Log.d(TAG, "CoreUtils - writeToFile Error");
            }
        }
    }

    /**
     * @param context  Context.
     * @param fileName String.
     *
     * @return {@code ByteArray}.
     */
    public byte[] readFromFile(final Context context, final String fileName) {
        final byte[] buffer = new byte[1024];
        try (FileInputStream inputStream = context.openFileInput(fileName)) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bytesRead = inputStream.read(buffer);
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = inputStream.read(buffer);
            }

            return outputStream.toByteArray();
        } catch (final IOException exception) {
            // nothing to do
        }
        // nothing to do

        return new byte[0];
    }
}
