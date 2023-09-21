package com.thalesgroup.d1.templates.pay.model;

import com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail.DigitalPayCardDetailFragment;

import java.util.List;

/**
 * D1PayUiListener.
 */
public interface D1PayUiListener {

    /**
     * Gets list of Digital Pay Card Detail Fragment.
     *
     * @param list List of {@code DigitalPayCardDetailFragment}.
     */
    void onDigitalPayCardDetailFragmentList(final List<DigitalPayCardDetailFragment> list);
}
