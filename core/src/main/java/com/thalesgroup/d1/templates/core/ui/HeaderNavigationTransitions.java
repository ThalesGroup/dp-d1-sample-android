package com.thalesgroup.d1.templates.core.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Morphs only public header visuals; never snapshots a page, card, or secure input. */
public final class HeaderNavigationTransitions implements DefaultLifecycleObserver {
    private static final String HEADER_NAME = "banking_page_header";
    private final AppCompatActivity activity;
    private final ViewGroup host;
    private final int containerId;
    private final List<ImageView> ghosts = new ArrayList<>();
    private Snapshot pending;
    private OneShotPreDrawListener preDraw;
    private ValueAnimator animator;
    private Fragment targetFragment;
    private View targetPage;
    private View targetHeader;
    private TextView targetTitle;
    private TextView targetDescription;
    private ViewTreeObserver watchedTree;
    private final List<RecyclerView> watchedLists = new ArrayList<>();
    private final List<ScrollPosition> scrollPositions = new ArrayList<>();
    private final List<DeferredAction> deferredActions = new ArrayList<>();
    private boolean stopped;
    private final TextWatcher textChanges = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
        @Override public void afterTextChanged(Editable text) { finish(); }
    };
    private final View.OnLayoutChangeListener layoutChanges = (view, left, top, right, bottom,
            oldLeft, oldTop, oldRight, oldBottom) -> {
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            finish();
        }
    };
    private final View.OnAttachStateChangeListener attachmentChanges = new View.OnAttachStateChangeListener() {
        @Override public void onViewAttachedToWindow(View view) { }
        @Override public void onViewDetachedFromWindow(View view) { finish(); }
    };
    private final ViewTreeObserver.OnScrollChangedListener scrollChanges = () -> {
        for (ScrollPosition position : scrollPositions) {
            if (position.view.getScrollX() != position.x || position.view.getScrollY() != position.y) {
                finish();
                return;
            }
        }
    };
    private final RecyclerView.OnScrollListener listScrollChanges = new RecyclerView.OnScrollListener() {
        @Override public void onScrolled(@NonNull RecyclerView list, int dx, int dy) {
            if (dx != 0 || dy != 0) {
                finish();
            }
        }
    };

    private final FragmentManager.FragmentLifecycleCallbacks callbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override public void onFragmentViewCreated(@NonNull FragmentManager manager, @NonNull Fragment fragment,
                @NonNull View view, @Nullable Bundle state) {
            if (fragment.getId() != containerId || pending == null) {
                return;
            }
            Snapshot outgoing = pending;
            targetFragment = fragment;
            preDraw = OneShotPreDrawListener.add(view, () -> {
                preDraw = null;
                if (pending != outgoing) {
                    return;
                }
                pending = null;
                animate(outgoing, view);
            });
        }

        @Override public void onFragmentViewDestroyed(@NonNull FragmentManager manager, @NonNull Fragment fragment) {
            deferredActions.removeIf(action -> action.owner == fragment);
            if (fragment == targetFragment) {
                finish();
            }
        }
    };

    public HeaderNavigationTransitions(@NonNull AppCompatActivity activity, int containerId) {
        this.activity = activity;
        this.containerId = containerId;
        host = activity.findViewById(containerId);
        activity.getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, false);
        activity.getLifecycle().addObserver(this);
    }

    /** Call immediately before replacing the main fragment or popping its back stack. */
    public void prepare() {
        // Superseded pages must never inflate their queued card views during the next navigation.
        deferredActions.clear();
        finish();
        if (!ValueAnimator.areAnimatorsEnabled() || activity.isFinishing()) {
            return;
        }
        Fragment current = activity.getSupportFragmentManager().findFragmentById(containerId);
        HeaderViews header = current == null ? null : findHeader(current.getView());
        if (header != null) {
            pending = capture(header);
        }
    }

    /** Keeps main-thread-only secure view creation out of the header animation's frame budget. */
    public void runAfterTransition(@NonNull Fragment owner, @NonNull Runnable action) {
        View view = owner.getView();
        if (view == null || !owner.isAdded()) {
            return;
        }
        if (pending != null || preDraw != null || animator != null || stopped) {
            deferredActions.add(new DeferredAction(owner, view, action));
        } else {
            action.run();
        }
    }

    private void drainDeferredActions() {
        if (stopped) {
            return;
        }
        List<DeferredAction> ready = new ArrayList<>(deferredActions);
        deferredActions.clear();
        Fragment current = activity.getSupportFragmentManager().findFragmentById(containerId);
        for (DeferredAction action : ready) {
            if (action.owner == current && action.owner.isAdded() && action.owner.getView() == action.view) {
                action.work.run();
            }
        }
    }

    private void animate(Snapshot outgoing, View page) {
        HeaderViews incoming = findHeader(page);
        Snapshot destination = incoming == null ? null : capture(incoming);
        if (destination == null || !ValueAnimator.areAnimatorsEnabled()) {
            finish();
            return;
        }
        targetPage = page;
        targetHeader = incoming.container;
        targetTitle = incoming.title;
        targetDescription = incoming.description;
        targetTitle.addTextChangedListener(textChanges);
        targetDescription.addTextChangedListener(textChanges);
        targetHeader.addOnLayoutChangeListener(layoutChanges);
        targetHeader.addOnAttachStateChangeListener(attachmentChanges);
        watchedTree = host.getViewTreeObserver();
        watchedTree.addOnScrollChangedListener(scrollChanges);
        watchLists(page);
        // Keep the logo in the page so it slides with the rest of the incoming content.
        targetTitle.setVisibility(View.INVISIBLE);
        targetDescription.setVisibility(View.INVISIBLE);

        ImageView oldTitle = ghost(outgoing.title, "header-title-out");
        ImageView newTitle = ghost(destination.title, "header-title-in");
        ImageView oldDescription = ghost(outgoing.description, "header-description-out");
        ImageView newDescription = ghost(destination.description, "header-description-in");
        float contentShift = Math.max(-dp(240), Math.min(dp(240), outgoing.bottom - destination.bottom));
        Part logo = destination.logo == null ? outgoing.logo : null;
        ImageView logoGhost = logo == null ? null : ghost(logo, "header-logo");
        Rect logoEnd = logo == null ? null : new Rect(logo.bounds);
        if (logoEnd != null) {
            logoEnd.offset(0, -Math.round(contentShift));
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(320);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(value -> {
            float progress = (float) value.getAnimatedValue();
            blend(oldTitle, newTitle, outgoing.title.bounds, destination.title.bounds, progress);
            blend(oldDescription, newDescription, outgoing.description.bounds, destination.description.bounds, progress);
            if (logoGhost != null) {
                position(logoGhost, logo.bounds, logoEnd, progress);
                logoGhost.setAlpha(1f - progress);
            }
            page.setTranslationY(contentShift * (1f - progress));
            page.setAlpha(progress);
        });
        // Apply the initial frame before the first draw, avoiding a one-frame flash.
        blend(oldTitle, newTitle, outgoing.title.bounds, destination.title.bounds, 0f);
        blend(oldDescription, newDescription, outgoing.description.bounds, destination.description.bounds, 0f);
        page.setTranslationY(contentShift);
        page.setAlpha(0f);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { finish(); }
        });
        animator.start();
    }

    private void blend(ImageView outgoing, ImageView incoming, Rect start, Rect end, float progress) {
        position(outgoing, start, end, progress);
        position(incoming, start, end, progress);
        outgoing.setAlpha(1f - progress);
        incoming.setAlpha(progress);
    }

    private static void position(View view, Rect start, Rect end, float progress) {
        int left = Math.round(start.left + (end.left - start.left) * progress);
        int top = Math.round(start.top + (end.top - start.top) * progress);
        int width = Math.round(start.width() + (end.width() - start.width()) * progress);
        int height = Math.round(start.height() + (end.height() - start.height()) * progress);
        view.layout(left, top, left + width, top + height);
    }

    private ImageView ghost(Part part, String tag) {
        ImageView view = new ImageView(activity);
        view.setImageBitmap(part.bitmap);
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        view.setTag(tag);
        host.getOverlay().add(view);
        position(view, part.bounds, part.bounds, 1f);
        ghosts.add(view);
        return view;
    }

    @Nullable private HeaderViews findHeader(@Nullable View view) {
        if (view == null) {
            return null;
        }
        if (HEADER_NAME.equals(view.getTransitionName()) && view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            ImageView logo = null;
            TextView title = null;
            TextView description = null;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    logo = (ImageView) child;
                }
                else if (child instanceof TextView) {
                    if (title == null) {
                        title = (TextView) child;
                    } else {
                        description = (TextView) child;
                    }
                }
            }
            if (title != null && description != null) {
                return new HeaderViews(group, logo, title, description);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                HeaderViews found = findHeader(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Nullable private Snapshot capture(HeaderViews header) {
        Rect visible = new Rect();
        if (!header.container.isShown() || !header.container.getGlobalVisibleRect(visible)
                || header.title.getWidth() == 0 || header.title.getHeight() == 0
                || header.description.getWidth() == 0 || header.description.getHeight() == 0) {
            return null;
        }
        Part logo = header.logo != null && header.logo.getWidth() > 0 && header.logo.getHeight() > 0
                ? capturePart(header.logo) : null;
        return new Snapshot(capturePart(header.title), capturePart(header.description), logo, bounds(header.container).bottom);
    }

    private Part capturePart(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.setDensity(activity.getResources().getDisplayMetrics().densityDpi);
        view.draw(new Canvas(bitmap));
        return new Part(bitmap, bounds(view));
    }

    private Rect bounds(View view) {
        int[] location = new int[2];
        int[] origin = new int[2];
        view.getLocationOnScreen(location);
        host.getLocationOnScreen(origin);
        return new Rect(location[0] - origin[0], location[1] - origin[1],
                location[0] - origin[0] + view.getWidth(), location[1] - origin[1] + view.getHeight());
    }

    private void watchLists(View view) {
        if (view instanceof ScrollView || view instanceof HorizontalScrollView || view instanceof NestedScrollView) {
            scrollPositions.add(new ScrollPosition(view));
        }
        if (view instanceof RecyclerView) {
            RecyclerView list = (RecyclerView) view;
            list.addOnScrollListener(listScrollChanges);
            watchedLists.add(list);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                watchLists(group.getChildAt(i));
            }
        }
    }

    private void finish() {
        if (targetTitle != null) {
            targetTitle.removeTextChangedListener(textChanges);
        }
        if (targetDescription != null) {
            targetDescription.removeTextChangedListener(textChanges);
        }
        if (targetHeader != null) {
            targetHeader.removeOnLayoutChangeListener(layoutChanges);
            targetHeader.removeOnAttachStateChangeListener(attachmentChanges);
        }
        if (watchedTree != null && watchedTree.isAlive()) {
            watchedTree.removeOnScrollChangedListener(scrollChanges);
        }
        watchedTree = null;
        for (RecyclerView list : watchedLists) {
            list.removeOnScrollListener(listScrollChanges);
        }
        watchedLists.clear();
        scrollPositions.clear();
        if (preDraw != null) {
            preDraw.removeListener();
        }
        preDraw = null;
        pending = null;
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        if (targetTitle != null) {
            targetTitle.setVisibility(View.VISIBLE);
        }
        if (targetDescription != null) {
            targetDescription.setVisibility(View.VISIBLE);
        }
        if (targetPage != null) {
            targetPage.setAlpha(1f);
            targetPage.setTranslationY(0f);
        }
        for (ImageView ghost : ghosts) {
            host.getOverlay().remove(ghost);
            ghost.setImageDrawable(null);
        }
        ghosts.clear();
        targetHeader = null;
        targetTitle = null;
        targetDescription = null;
        targetPage = null;
        targetFragment = null;
        drainDeferredActions();
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    @Override public void onStart(@NonNull LifecycleOwner owner) {
        stopped = false;
        if (pending == null && animator == null && preDraw == null) {
            drainDeferredActions();
        }
    }

    @Override public void onStop(@NonNull LifecycleOwner owner) {
        stopped = true;
        finish();
    }

    @Override public void onDestroy(@NonNull LifecycleOwner owner) {
        deferredActions.clear();
        finish();
        activity.getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
        owner.getLifecycle().removeObserver(this);
    }

    private static final class ScrollPosition {
        final View view;
        final int x;
        final int y;
        ScrollPosition(View view) { this.view = view; x = view.getScrollX(); y = view.getScrollY(); }
    }

    private static final class DeferredAction {
        final Fragment owner;
        final View view;
        final Runnable work;
        DeferredAction(Fragment owner, View view, Runnable work) {
            this.owner = owner;
            this.view = view;
            this.work = work;
        }
    }

    private static final class HeaderViews {
        final ViewGroup container;
        final ImageView logo;
        final TextView title;
        final TextView description;
        HeaderViews(ViewGroup container, ImageView logo, TextView title, TextView description) {
            this.container = container;
            this.logo = logo;
            this.title = title;
            this.description = description;
        }
    }

    private static final class Part {
        final Bitmap bitmap;
        final Rect bounds;
        Part(Bitmap bitmap, Rect bounds) { this.bitmap = bitmap; this.bounds = bounds; }
    }

    private static final class Snapshot {
        final Part title;
        final Part description;
        final Part logo;
        final int bottom;
        Snapshot(Part title, Part description, Part logo, int bottom) {
            this.title = title;
            this.description = description;
            this.logo = logo;
            this.bottom = bottom;
        }
    }
}
