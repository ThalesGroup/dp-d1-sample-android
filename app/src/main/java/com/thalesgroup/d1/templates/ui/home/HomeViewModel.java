/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.home;

import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1CoreListener;
import com.thalesgroup.d1.templates.ui.login.LoginViewModel;

/**
 * HomeViewModel.
 */
public class HomeViewModel extends LoginViewModel implements D1CoreListener {
    final MutableLiveData<Boolean> mIsLogoutSuccess = new MutableLiveData<>(Boolean.FALSE);

    /**
     * Triggers logout.
     */
    public void logout() {
        D1Core.getInstance().logout(this);
    }

    @Override
    public void onLogoutSuccess(final Void unused) {
        mIsLogoutSuccess.postValue(Boolean.TRUE);
    }

    /**
     * Gets {@code MutableLiveData} instance if logout was successful.
     *
     * @return {@code MutableLiveData} instance if logout was successful.
     */
    public MutableLiveData<Boolean> getIsLogoutSuccess() {
        return mIsLogoutSuccess;
    }
}
