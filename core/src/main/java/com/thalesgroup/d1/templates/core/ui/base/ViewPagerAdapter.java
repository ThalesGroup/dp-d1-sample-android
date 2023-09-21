/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.ui.base;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPagerAdapter.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();

    /**
     * @param fragmentManager {@code FragmentManager}.
     * @param lifecycle {@code Lifecycle}.
     */
    public ViewPagerAdapter(@NonNull final FragmentManager fragmentManager,
                            @NonNull final Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        return mFragmentList.get(position);
    }

    /**
     * @param fragment Fragment.
     */
    public void addFragment(final Fragment fragment) {
        mFragmentList.add(fragment);
    }

    /**
     * Clear list of fragments.
     */
    public void clear() {
        mFragmentList.clear();
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }
}
