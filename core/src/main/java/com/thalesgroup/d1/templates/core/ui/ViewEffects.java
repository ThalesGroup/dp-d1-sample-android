package com.thalesgroup.d1.templates.core.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.thalesgroup.d1.core.R;

/** Static fade operations with view-local state, released with the registered view lifecycle. */
public final class ViewEffects {
    private ViewEffects() {
    }

    /** Register once per view lifecycle; repeating registration does not reset an active fade. */
    @MainThread
    public static void register(@NonNull LifecycleOwner owner, float initialAlpha, @NonNull View... views) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        for (View view : views) {
            Object existing = view.getTag(R.id.view_effects_state);
            if (existing instanceof State && ((State) existing).lifecycle == lifecycle) {
                continue;
            }
            unregister(view);
            State state = new State(view, lifecycle, initialAlpha);
            view.setTag(R.id.view_effects_state, state);
            view.addOnAttachStateChangeListener(state);
            lifecycle.addObserver(state);
        }
    }

    /** Input eligibility changes immediately, independently of the visual fade. */
    @MainThread
    public static void setEnabled(@NonNull View view, boolean enabled) {
        State state = get(view);
        view.setEnabled(enabled);
        state.fadeTo(enabled ? 1f : .35f);
    }

    @MainThread
    public static void fadeTo(@NonNull View view, float alpha) {
        get(view).fadeTo(alpha);
    }

    /** Optional early cleanup; lifecycle destruction already unregisters all registered views. */
    @MainThread
    public static void unregister(@NonNull View... views) {
        for (View view : views) {
            Object state = view.getTag(R.id.view_effects_state);
            if (state instanceof State) {
                ((State) state).release();
            }
        }
    }

    private static State get(View view) {
        Object state = view.getTag(R.id.view_effects_state);
        if (!(state instanceof State)) {
            throw new IllegalStateException("Register the view with ViewEffects before changing its effects.");
        }
        return (State) state;
    }

    private static final class State implements DefaultLifecycleObserver, View.OnAttachStateChangeListener {
        private final View view;
        private final Lifecycle lifecycle;
        private ValueAnimator animator;
        private float target;

        State(View view, Lifecycle lifecycle, float initialAlpha) {
            this.view = view;
            this.lifecycle = lifecycle;
            target = initialAlpha;
            view.setAlpha(initialAlpha);
        }

        void fadeTo(float alpha) {
            if (target == alpha && (animator != null || view.getAlpha() == alpha)) {
                return;
            }
            cancel();
            target = alpha;
            if (!ValueAnimator.areAnimatorsEnabled() || view.getAlpha() == alpha) {
                view.setAlpha(alpha);
                return;
            }
            ValueAnimator next = ValueAnimator.ofFloat(view.getAlpha(), alpha);
            animator = next;
            next.setDuration(240);
            next.setInterpolator(new FastOutSlowInInterpolator());
            next.addUpdateListener(value -> view.setAlpha((float) value.getAnimatedValue()));
            next.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    if (animator == animation) {
                        view.setAlpha(target);
                        animator = null;
                    }
                }
            });
            next.start();
        }

        private void cancel() {
            if (animator == null) {
                return;
            }
            animator.removeAllListeners();
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }

        void release() {
            cancel();
            view.setAlpha(target);
            view.removeOnAttachStateChangeListener(this);
            lifecycle.removeObserver(this);
            if (view.getTag(R.id.view_effects_state) == this) {
                view.setTag(R.id.view_effects_state, null);
            }
        }

        @Override public void onDestroy(@NonNull LifecycleOwner owner) {
            release();
        }

        @Override public void onViewAttachedToWindow(View view) {
        }

        @Override public void onViewDetachedFromWindow(View view) {
            cancel();
            view.setAlpha(target);
        }
    }
}
