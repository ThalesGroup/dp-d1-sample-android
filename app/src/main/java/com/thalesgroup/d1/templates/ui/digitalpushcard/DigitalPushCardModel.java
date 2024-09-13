/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.digitalpushcard;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.push.model.D1PushUiListener;
import com.thalesgroup.d1.templates.push.ui.digitalpushcarddetail.DigitalPushCardDetailFragment;

import java.util.List;

/**
 * DigitalPushCardModel.
 */
public class DigitalPushCardModel extends BaseViewModel implements D1PushUiListener {

    private final MutableLiveData<List<DigitalPushCardDetailFragment>> mDigitalPushCardDetailFragmentList = new MutableLiveData<>();

    /**
     * Retrieves the {@code DigitalPushCardDetailFragment} list.
     */
    public void getD1PushDigitalCardList(@NonNull final String cardId) {
        D1Push.getInstance().getDigitalPayCardDetailFragmentList(cardId, this);
    }

    /**
     * Gets {@code MutableLiveData} instance of the {@code DigitalPayCardDetailFragment} list.
     *
     * @return {@code MutableLiveData} instance of the {@code DigitalPayCardDetailFragment} list.
     */
    public MutableLiveData<List<DigitalPushCardDetailFragment>> getDigitalPushCardDetailFragmentList() {
        return mDigitalPushCardDetailFragmentList;
    }

    @Override
    public void onDigitalPushCardDetailFragmentList(final List<DigitalPushCardDetailFragment> list) {
        mDigitalPushCardDetailFragmentList.postValue(list);
    }
}
