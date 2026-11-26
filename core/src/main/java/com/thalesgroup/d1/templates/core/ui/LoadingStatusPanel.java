package com.thalesgroup.d1.templates.core.ui;

import android.content.Context;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.thalesgroup.d1.core.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent bottom loading feedback. Call on the main thread through UiDelegate.
 * The overlay blocks input without changing business-controlled enabled states.
 */
public final class LoadingStatusPanel implements DefaultLifecycleObserver {
    private final AppCompatActivity activity;
    private final FrameLayout root;
    private final List<UnderlayState> underlays = new ArrayList<>();
    private FrameLayout overlay;
    private View status;
    private TextView messageView;
    private View previousFocus;
    private boolean stopped;
    private String pendingMessage;
    private Object pendingOwner;
    private final ViewTreeObserver.OnGlobalLayoutListener layoutListener = this::positionStatus;
    private final OnBackPressedCallback backBlocker = new OnBackPressedCallback(false) {
        @Override public void handleOnBackPressed() {
            // Match the old non-cancelable progress dialog, including predictive back.
        }
    };

    @MainThread
    public LoadingStatusPanel(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        root = activity.findViewById(android.R.id.content);
        activity.getOnBackPressedDispatcher().addCallback(activity, backBlocker);
        activity.getLifecycle().addObserver(this);
    }

    @MainThread
    public void show(@NonNull String message) {
        show(message, this);
    }

    @MainThread
    public void show(@NonNull String message, @NonNull Object owner) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        pendingMessage = message;
        pendingOwner = owner;
        if (stopped) {
            return;
        }
        if (overlay == null) {
            previousFocus = root.findFocus();
            dismissDropdowns(root);
            // Existing touch targets and queued clicks predate the new overlay.
            long time = SystemClock.uptimeMillis();
            MotionEvent cancel = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            root.dispatchTouchEvent(cancel);
            cancel.recycle();
            root.cancelPendingInputEvents();
            for (int i = 0; i < root.getChildCount(); i++) {
                UnderlayState state = new UnderlayState(root.getChildAt(i));
                underlays.add(state);
                state.block();
            }
            InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) {
                keyboard.hideSoftInputFromWindow(root.getWindowToken(), 0);
            }
            overlay = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.loading_status_panel, root, false);
            status = overlay.findViewById(R.id.loading_status_panel);
            messageView = overlay.findViewById(R.id.loading_status_message);
            overlay.setOnGenericMotionListener((v, event) -> true);
            overlay.setOnKeyListener((v, code, event) -> code != KeyEvent.KEYCODE_BACK
                    && code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN
                    && code != KeyEvent.KEYCODE_VOLUME_MUTE);
            root.addView(overlay);
            overlay.requestFocus();
            root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
            backBlocker.setEnabled(true);
        }
        messageView.setText(message.isEmpty() ? activity.getString(R.string.loading_status_default) : message);
        positionStatus();
    }

    @MainThread
    public void hide() {
        pendingMessage = null;
        pendingOwner = null;
        detachOverlay();
    }

    @MainThread
    public void hide(@NonNull Object owner) {
        if (pendingOwner == owner) {
            hide();
        }
    }

    private void detachOverlay() {
        backBlocker.setEnabled(false);
        if (overlay == null) {
            return;
        }
        if (root.getViewTreeObserver().isAlive()) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
        root.removeView(overlay);
        overlay = null;
        status = null;
        messageView = null;
        for (UnderlayState state : underlays) {
            state.restore();
        }
        underlays.clear();
        if (previousFocus != null && previousFocus.isAttachedToWindow()
                && previousFocus.isShown() && previousFocus.isEnabled()) {
            previousFocus.requestFocus();
        }
        previousFocus = null;
    }

    private void positionStatus() {
        if (overlay == null) {
            return;
        }
        int margin = dp(24);
        View footer = root.findViewById(R.id.banking_action_footer);
        if (footer != null && footer.isShown() && footer.getHeight() > 0 && overlay.getHeight() > 0) {
            int[] overlayPosition = new int[2];
            int[] footerPosition = new int[2];
            overlay.getLocationOnScreen(overlayPosition);
            footer.getLocationOnScreen(footerPosition);
            margin = Math.max(margin, overlayPosition[1] + overlay.getHeight() - footerPosition[1] + dp(16));
        }
        // Keep the message reachable even with a small viewport, IME or enlarged text.
        if (overlay.getHeight() > 0 && status.getHeight() > 0) {
            margin = Math.min(margin, Math.max(dp(24), overlay.getHeight() - status.getHeight() - dp(16)));
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) status.getLayoutParams();
        if (params.bottomMargin != margin) {
            params.bottomMargin = margin;
            status.setLayoutParams(params);
        }
    }

    private static void dismissDropdowns(View view) {
        if (view instanceof AutoCompleteTextView) {
            ((AutoCompleteTextView) view).dismissDropDown();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                dismissDropdowns(group.getChildAt(i));
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    @Override public void onStart(@NonNull LifecycleOwner owner) {
        stopped = false;
        if (pendingMessage != null && pendingOwner != null) {
            show(pendingMessage, pendingOwner);
        }
    }

    @Override public void onStop(@NonNull LifecycleOwner owner) {
        stopped = true;
        // Remove window-bound views, but keep the operation pending until hide().
        detachOverlay();
    }

    @Override public void onDestroy(@NonNull LifecycleOwner owner) {
        hide();
        backBlocker.remove();
        owner.getLifecycle().removeObserver(this);
    }

    private static final class UnderlayState {
        private final View view;
        private final float alpha;
        private final int accessibility;
        private final int descendantFocusability;

        UnderlayState(View view) {
            this.view = view;
            alpha = view.getAlpha();
            accessibility = view.getImportantForAccessibility();
            descendantFocusability = view instanceof ViewGroup ? ((ViewGroup) view).getDescendantFocusability() : 0;
        }

        void block() {
            view.setAlpha(alpha * 0.65f);
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            if (view instanceof ViewGroup) {
                ((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }
            view.clearFocus();
        }

        void restore() {
            view.setAlpha(alpha);
            view.setImportantForAccessibility(accessibility);
            if (view instanceof ViewGroup) {
                ((ViewGroup) view).setDescendantFocusability(descendantFocusability);
            }
        }
    }
}
