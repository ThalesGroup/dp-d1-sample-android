/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.thalesgroup.d1.templates.core.ui.base.UiDelegate;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.ui.login.LoginFragment;
import com.thalesgroup.gemalto.d1.d1pay.D1HCEService;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity.
 */
public class MainActivity extends AppCompatActivity implements UiDelegate {

    public ProgressDialog mProgressDialog;

    public AlertDialog mAlertDialog;

    private FrameLayout mOverlayFrameLayout;
    private Boolean mTapAndPaySettingsChecked;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOverlayFrameLayout = findViewById(R.id.overlay);

        mTapAndPaySettingsChecked = false;

        if (checkMandatoryPermissions()) {
            showFragment(LoginFragment.newInstance(), false);
        }
    }

    @Override
    public void onBackPressed() {
        popFromBackstack();
    }

    @Override
    public void showFragment(final Fragment fragment, final boolean addToBackstack) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, fragment);
        if (addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    @Override
    public void popFromBackstack() {
        if (mOverlayFrameLayout.getVisibility() == View.VISIBLE) {
            mOverlayFrameLayout.setVisibility(View.GONE);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void showProgressDialog(@NonNull final String message) {
        CoreUtils.getInstance().runInMainThread(() -> {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        });
    }

    @Override
    public void hideProgressDialog() {
        CoreUtils.getInstance().runInMainThread(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        });
    }

    @Override
    public void showAlertDialog(final String title, final String description, final String positiveText,
                                final Runnable positiveAction, final String negativeText, final Runnable negativeAction) {
        CoreUtils.getInstance().runInMainThread(() -> {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, com.thalesgroup.d1.core.R.style.AlertDialogTheme);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(description);
            alertDialogBuilder.setCancelable(true);

            alertDialogBuilder.setPositiveButton(
                    positiveText,
                    (dialog, id) -> {

                        if (positiveAction != null) {
                            positiveAction.run();
                        }

                    });

            if (negativeText != null) {
                alertDialogBuilder.setNegativeButton(
                        negativeText,
                        (dialog, id) -> {

                            if (negativeAction != null) {
                                negativeAction.run();
                            }

                        });
            }

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.show();
        });
    }

    @Override
    public void checkTapAndPaySettings() {
        CoreUtils.getInstance().runInMainThread(() -> {
            final CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(getApplicationContext()));
            final ComponentName componentName = new ComponentName(this, D1HCEService.class.getCanonicalName());
            if (!cardEmulation.isDefaultServiceForCategory(componentName, CardEmulation.CATEGORY_PAYMENT) && !mTapAndPaySettingsChecked) {
                mTapAndPaySettingsChecked = true;
                // set application to be the default payment application
                final Intent activate = new Intent();
                activate.setAction(CardEmulation.ACTION_CHANGE_DEFAULT);
                activate.putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, componentName);
                activate.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT);
                startActivity(activate);
            }
        });
    }

    @Override
    public void showLoginFragment() {
        showFragment(LoginFragment.newInstance(), false);
    }

    @Override
    public void showToast(@NonNull final String message) {
        CoreUtils.getInstance().runInMainThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void showOverlayFragment(final Fragment fragment) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.overlay, fragment);

        fragmentTransaction.commit();

        mOverlayFrameLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearFragmentBackStack() {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i ++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (final int result : grantResults) {
            if (result != 0) {
                showToast("Cannot grant permission.");
                return;
            }
        }

        showFragment(LoginFragment.newInstance(), false);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        D1Push.getInstance().handleCardResult(requestCode, resultCode, data);
    }

    /**
     * Checks the required runtime permissions.
     *
     * @return {@code True} if all permissions are present, else {@code false}.
     */
    private boolean checkMandatoryPermissions() {
        try {
            // Get list of all permissions defined in app manifest.
            final PackageInfo info = getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            return checkPermissions(info.requestedPermissions);
        } catch (final PackageManager.NameNotFoundException exception) {
            // App package must be present.
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Checks for runtime permission.
     *
     * @param permissions List of permissions.
     *
     * @return {@code True} if permissions are present, else {@code false}.
     */
    private boolean checkPermissions(final String... permissions) {
        // Update list of permissions based on granted status.
        final List<String> permissionsToCheck = new ArrayList<>();
        for (final String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
                permissionsToCheck.add(permission);
            }
        }

        // Some permissions are not granted. Ask user for them.
        if (!permissionsToCheck.isEmpty()) {
            final String[] notGrantedArray = permissionsToCheck.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, notGrantedArray, 0);
        }

        return permissionsToCheck.isEmpty();
    }
}
