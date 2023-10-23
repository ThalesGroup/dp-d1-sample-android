/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.model;

import com.thalesgroup.d1.templates.push.ui.digitalpushcarddetail.DigitalPushCardDetailFragment;

import java.util.List;

/**
 * D1PushUiListener.
 */
public interface D1PushUiListener {

    /**
     * Gets list of Digital Pay Card Detail Fragment.
     *
     * @param list List of {@code DigitalPayCardDetailFragment}.
     */
    void onDigitalPushCardDetailFragmentList(final List<DigitalPushCardDetailFragment> list);
}
