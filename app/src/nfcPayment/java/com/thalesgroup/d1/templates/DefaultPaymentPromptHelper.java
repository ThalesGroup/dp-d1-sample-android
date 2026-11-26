package com.thalesgroup.d1.templates;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public final class DefaultPaymentPromptHelper {

    private DefaultPaymentPromptHelper() {
    }

    /**
     * Shows a confirmation dialog first, then launches the system role/payment flow.
     *
     * @param activity           Host activity (used to show the AlertDialog)
     * @param walletLauncher     ActivityResultLauncher registered in your Activity/Fragment
     * @param targetService      Your app's HostApduService / OffHostApduService component
     * @param targetServiceLabel Human-readable app/service name shown in the dialog
     */
    public static void promptToChangeDefaultPayment(
            @NonNull Activity activity,
            @NonNull ActivityResultLauncher<Intent> walletLauncher,
            @NonNull ComponentName targetService,
            @NonNull String targetServiceLabel
    ) {
        if (isDefaultPaymentService(activity, targetService)) {
            return;
        }

        String title = activity.getString(R.string.set_default_payment_title, targetServiceLabel);
        String message = activity.getString(R.string.set_default_payment_message, targetServiceLabel);

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) ->
                        launchSystemPaymentFlow(activity, walletLauncher, targetService)
                )
                .setNegativeButton("Not now", null)
                .show();

    }

    public static boolean isDefaultPaymentService(
            @NonNull Context context,
            @NonNull ComponentName targetService
    ) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            return false;
        }
        CardEmulation cardEmulation = CardEmulation.getInstance(nfcAdapter);
        if (cardEmulation == null) {
            return false;
        }
        return cardEmulation.isDefaultServiceForCategory(
                targetService,
                CardEmulation.CATEGORY_PAYMENT
        );
    }

    private static void launchSystemPaymentFlow(
            @NonNull Activity activity,
            @NonNull ActivityResultLauncher<Intent> walletLauncher,
            @NonNull ComponentName targetService
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // API 35+
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            if (roleManager != null
                    && roleManager.isRoleAvailable(RoleManager.ROLE_WALLET)
                    && !roleManager.isRoleHeld(RoleManager.ROLE_WALLET)) {
                try {
                    Intent roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_WALLET);
                    walletLauncher.launch(roleIntent);
                    return;
                } catch (ActivityNotFoundException | SecurityException ignored) {
                    // Fall through to NFC payment settings
                }
            }
            openNfcPaymentSettings(activity);
            return;
        }

        // API 27 through API 34
        Intent intent = new Intent(CardEmulation.ACTION_CHANGE_DEFAULT);
        intent.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT);
        intent.putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, targetService);

        try {
            walletLauncher.launch(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            openNfcPaymentSettings(activity);
        }
    }

    public static void openNfcPaymentSettings(@NonNull Context context) {
        Intent settingsIntent = new Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS);
        if (!(context instanceof Activity)) {
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(settingsIntent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            Intent fallback = new Intent(Settings.ACTION_NFC_SETTINGS);
            if (!(context instanceof Activity)) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(fallback);
        }
    }

    /**
     * Call this after your ActivityResultLauncher finishes to verify whether
     * targetService actually became the default payment service.
     */
    public static boolean didDefaultPaymentChange(
            @NonNull Context context,
            @NonNull ComponentName targetService,
            @Nullable Object unusedResultPlaceholder
    ) {
        return isDefaultPaymentService(context, targetService);
    }
}
