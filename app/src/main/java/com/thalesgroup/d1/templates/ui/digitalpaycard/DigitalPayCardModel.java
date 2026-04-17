/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.digitalpaycard;

import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.pay.model.D1PayUiListener;
import com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail.DigitalPayCardDetailFragment;

import java.util.List;

/**
 * DigitalPayCardModel.
 */
public class DigitalPayCardModel extends BaseViewModel implements D1PayUiListener {

    private final MutableLiveData<List<DigitalPayCardDetailFragment>> mDigitalPayCardDetailFragmentList = new MutableLiveData<>();

    /**
     * Retrieves the {@code DigitalPayCardDetailFragment} list.
     */
    public void getD1PayDigitalCardList() {
        D1Pay.getInstance().getDigitalPayCardDetailFragmentList(this);
    }

    /**
     * Gets {@code MutableLiveData} instance of the {@code DigitalPayCardDetailFragment} list.
     *
     * @return {@code MutableLiveData} instance of the {@code DigitalPayCardDetailFragment} list.
     */
    public MutableLiveData<List<DigitalPayCardDetailFragment>> getDigitalPayCardDetailFragmentList() {
        return mDigitalPayCardDetailFragmentList;
    }

    @Override
    public void onDigitalPayCardDetailFragmentList(final List<DigitalPayCardDetailFragment> fragmentList) {
        mDigitalPayCardDetailFragmentList.postValue(fragmentList);
    }
}
