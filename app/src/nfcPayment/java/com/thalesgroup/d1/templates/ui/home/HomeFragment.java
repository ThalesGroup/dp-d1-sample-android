/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.home;

import android.view.View;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.ui.digitalpaycard.DigitalPayCardFragment;

/** NFC home adds Digital Pay to the shared home services. */
public class HomeFragment extends BaseHomeFragment {
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected void configureFlavorActions(@NonNull final View view) {
        view.findViewById(R.id.digital_pay_card_button)
                .setOnClickListener(button -> showFragment(DigitalPayCardFragment.newInstance(), true));
    }
}
