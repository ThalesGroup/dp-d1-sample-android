/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.thalesgroup.d1.templates.core.ui.HeaderNavigationTransitions;
import com.thalesgroup.d1.templates.core.ui.LoadingStatusPanel;
import com.thalesgroup.d1.templates.core.ui.base.UiDelegate;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.ui.login.LoginFragment;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.d1pay.D1HCEService;

/**
 * MainActivity.
 */
public class MainActivity extends AppCompatActivity implements UiDelegate {

    private LoadingStatusPanel mLoadingStatusPanel;
    private HeaderNavigationTransitions mHeaderTransitions;

    public AlertDialog mAlertDialog;

    private FrameLayout mOverlayFrameLayout;
    private Boolean mTapAndPaySettingsChecked;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHeaderTransitions = new HeaderNavigationTransitions(this, R.id.main);

        final TextView sdkVersion = findViewById(R.id.tv_sdk_version);
        sdkVersion.setText(getString(R.string.sdk_versions_format, new Gson().toJson(D1Task.getSDKVersions())));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mOverlayFrameLayout = findViewById(R.id.overlay);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                popFromBackstack();
            }
        });
        // Register after normal navigation so loading has priority while visible.
        mLoadingStatusPanel = new LoadingStatusPanel(this);

        mTapAndPaySettingsChecked = false;

        if (savedInstanceState == null) {
            showFragment(LoginFragment.newInstance(), false);
            requestNotificationPermission();
        }
    }

    @Override
    public void showFragment(final Fragment fragment, final boolean addToBackstack) {
        CoreUtils.getInstance().runInMainThread(() -> {
            mHeaderTransitions.prepare();
            final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment);
            if (addToBackstack) {
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commit();
        });
    }

    @Override
    public void runAfterNavigation(@NonNull Fragment owner, @NonNull Runnable action) {
        mHeaderTransitions.runAfterTransition(owner, action);
    }

    @Override
    public void popFromBackstack() {
        CoreUtils.getInstance().runInMainThread(() -> {
            if (mOverlayFrameLayout.getVisibility() == View.VISIBLE) {
                mOverlayFrameLayout.setVisibility(View.GONE);
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                mHeaderTransitions.prepare();
                getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void showProgressDialog(@NonNull final String message) {
        CoreUtils.getInstance().runInMainThread(() -> {
            mLoadingStatusPanel.show(message);
        });
    }

    @Override
    public void hideProgressDialog() {
        CoreUtils.getInstance().runInMainThread(() -> {
            mLoadingStatusPanel.hide();
        });
    }

    @Override
    public void showProgressDialog(@NonNull final String message, @NonNull final Object owner) {
        CoreUtils.getInstance().runInMainThread(() -> mLoadingStatusPanel.show(message, owner));
    }

    @Override
    public void hideProgressDialog(@NonNull final Object owner) {
        CoreUtils.getInstance().runInMainThread(() -> mLoadingStatusPanel.hide(owner));
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

    private final ActivityResultLauncher<Intent> mWalletRoleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        ComponentName service = new ComponentName(
                                this,
                                D1HCEService.class
                        );
                        boolean isNowDefault = DefaultPaymentPromptHelper.isDefaultPaymentService(
                                this,
                                service
                        );
                        // TODO: [Optional] Check isNowDefault status, update UI if needed
                    }
            );

    public String getApplicationName() {
        PackageManager pm = getPackageManager();
        ApplicationInfo appInfo = getApplicationInfo();
        CharSequence label = pm.getApplicationLabel(appInfo);
        return label.toString();
    }

    @Override
    public void checkTapAndPaySettings() {
        CoreUtils.getInstance().runInMainThread(() -> {

            DefaultPaymentPromptHelper.promptToChangeDefaultPayment(
                    this,
                    mWalletRoleLauncher,
                    new ComponentName(this, D1HCEService.class),
                    getApplicationName()
            );

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
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        D1Push.getInstance().handleCardResult(requestCode, resultCode, data);
    }

    /** Requests optional notifications only on Android versions that support the permission. */
    private void requestNotificationPermission() {
        // Denying notifications must not prevent login or navigation.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }
}
