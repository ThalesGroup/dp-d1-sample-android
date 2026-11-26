package com.thalesgroup.d1.templates.clicktopay.ui.enroll;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.thalesgroup.d1.clicktopay.R;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayOperationStatusUpdate;
import com.thalesgroup.d1.templates.core.model.LoadingState;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.gemalto.d1.clicktopay.BillingAddress;
import com.thalesgroup.gemalto.d1.clicktopay.Card;
import com.thalesgroup.gemalto.d1.clicktopay.ConsumerInfo;
import com.thalesgroup.gemalto.d1.clicktopay.Status;

/**
 * Fragment to edit ConsumerInfo and BillingAddress and enroll or manage card in Click To Pay.
 */
public class ClickToPayFragment extends AbstractBaseFragment<ClickToPayViewModel> {

    private String mCardId;

    private TextView mTvSubtitle;
    private Button mBtnEnroll;
    private Button mBtnUpdate;
    private Button mBtnOptOut;

    private TextInputEditText mEtCardholderName;
    private TextInputEditText mEtFirstName;
    private TextInputEditText mEtMiddleName;
    private TextInputEditText mEtLastName;
    private TextInputEditText mEtLanguage;
    private TextInputEditText mEtPhoneCountryCode;
    private TextInputEditText mEtPhoneNumber;
    private TextInputEditText mEtEmail;

    private TextInputEditText mEtCountryCode;
    private TextInputEditText mEtLine1;
    private TextInputEditText mEtLine2;
    private TextInputEditText mEtLine3;
    private TextInputEditText mEtCity;
    private TextInputEditText mEtState;
    private TextInputEditText mEtZipCode;

    /**
     * Creates a new instance of {@code ClickToPayFragment} with enrollment state and initial Card and ConsumerInfo.
     *
     * @param cardId       Card ID.
     * @return Instance of {@code ClickToPayFragment}.
     */
    public static ClickToPayFragment newInstance(@NonNull final String cardId) {
        final ClickToPayFragment fragment = new ClickToPayFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCardId = getArguments().getString(ARG_CARD_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_click_to_pay, container, false);

        mTvSubtitle = view.findViewById(R.id.tv_c2p_subtitle);
        mEtCardholderName = view.findViewById(R.id.et_cardholder_name);
        mEtFirstName = view.findViewById(R.id.et_first_name);
        mEtMiddleName = view.findViewById(R.id.et_middle_name);
        mEtLastName = view.findViewById(R.id.et_last_name);
        mEtLanguage = view.findViewById(R.id.et_language);
        mEtPhoneCountryCode = view.findViewById(R.id.et_phone_country_code);
        mEtPhoneNumber = view.findViewById(R.id.et_phone_number);
        mEtEmail = view.findViewById(R.id.et_email);

        mEtCountryCode = view.findViewById(R.id.et_country_code);
        mEtLine1 = view.findViewById(R.id.et_line1);
        mEtLine2 = view.findViewById(R.id.et_line2);
        mEtLine3 = view.findViewById(R.id.et_line3);
        mEtCity = view.findViewById(R.id.et_city);
        mEtState = view.findViewById(R.id.et_state);
        mEtZipCode = view.findViewById(R.id.et_zip_code);

        mBtnEnroll = view.findViewById(R.id.btn_c2p_enroll);
        mBtnUpdate = view.findViewById(R.id.btn_c2p_update);
        mBtnOptOut = view.findViewById(R.id.btn_c2p_optout);

        mBtnEnroll.setOnClickListener(v -> performEnrollment());
        mBtnUpdate.setOnClickListener(v -> performUpdate());
        mBtnOptOut.setOnClickListener(v -> performOptOut());

        populateDefaultData();


        // Observe ViewModel LiveData for enrollment state, ConsumerInfo and Card data
        mViewModel.getLoadingState().observe(getViewLifecycleOwner(), this::updateLoadingState);
        mViewModel.getIsEnrolled().observe(getViewLifecycleOwner(), this::updateUiForEnrollmentState);
        mViewModel.getConsumerInfo().observe(getViewLifecycleOwner(), this::populateConsumerInfo);
        mViewModel.getCard().observe(getViewLifecycleOwner(), this::populateCardInfo);
        mViewModel.getOperationStatus().observe(getViewLifecycleOwner(), this::updateOperationStatus);

        if (mCardId != null) {
            mViewModel.getCardAndConsumerProfile(mCardId);
        }

        return view;
    }

    private void updateLoadingState(final LoadingState loadingState){
        if(loadingState.isLoading()){
            showProgressDialog(getString(loadingState.getMessageId()));
        } else{
            hideProgressDialog();
            if(loadingState.hasMessage()) {
                showToast(getString(loadingState.getMessageId()));
            }
        }
    }

    private void updateOperationStatus(final ClickToPayOperationStatusUpdate operationStatusUpdate){

        final String opName = operationStatusUpdate.getOperation().name();
        final Status status = operationStatusUpdate.getStatus();

        if (status == Status.PENDING) {
            showToast(String.format(getString(R.string.c2p_op_pending), opName));
        }
    }

    private void updateUiForEnrollmentState(final boolean isEnrolled) {

        if (isEnrolled) {
            mTvSubtitle.setText(R.string.c2p_enrolled_subtitle);
            mBtnEnroll.setVisibility(View.GONE);
            mBtnUpdate.setVisibility(View.VISIBLE);
            mBtnOptOut.setVisibility(View.VISIBLE);
        } else {
            mTvSubtitle.setText(R.string.c2p_not_enrolled_subtitle);
            mBtnEnroll.setVisibility(View.VISIBLE);
            mBtnUpdate.setVisibility(View.GONE);
            mBtnOptOut.setVisibility(View.GONE);
        }
    }

    private void populateDefaultData() {
        mEtCardholderName.setText("Carlos E. Mendoza");
        mEtFirstName.setText("Carlos");
        mEtMiddleName.setText("Eduardo");
        mEtLastName.setText("Mendoza");
        mEtLanguage.setText("es-MX");
        mEtPhoneCountryCode.setText("+52");
        mEtPhoneNumber.setText("4425550198");
        mEtEmail.setText("carlos.mendoza.test@example.com");
        mEtCountryCode.setText("MX");
        mEtLine1.setText("Av. Constituyentes 123");
        mEtLine2.setText("Int. 402, Col. Carretas");
        mEtCity.setText("Santiago de Querétaro");
        mEtState.setText("QRO");
        mEtZipCode.setText("76050");
    }

    private void populateConsumerInfo(@Nullable final ConsumerInfo consumerInfo) {
        if (consumerInfo == null) {
            return;
        }
        mEtFirstName.setText(consumerInfo.getFirstName());
        mEtMiddleName.setText(consumerInfo.getMiddleName());
        mEtLastName.setText(consumerInfo.getLastName());
        mEtLanguage.setText(consumerInfo.getLanguage());
        mEtPhoneCountryCode.setText(consumerInfo.getPhoneNumberCountryCode());
        mEtPhoneNumber.setText(consumerInfo.getPhoneNumber());
        mEtEmail.setText(consumerInfo.getEmail());
    }

    private void populateCardInfo(@Nullable final Card card) {
        if (card == null) {
            return;
        }
        mEtCardholderName.setText(card.getCardHolderName());

        final BillingAddress billingAddress = card.getBillingAddress();

        if (billingAddress == null) {
            return;
        }

        mEtCountryCode.setText(billingAddress.getCountryCode());
        mEtLine1.setText(billingAddress.getLine1());
        mEtLine2.setText(billingAddress.getLine2());
        mEtLine3.setText(billingAddress.getLine3());
        mEtCity.setText(billingAddress.getCity());
        mEtState.setText(billingAddress.getState());
        mEtZipCode.setText(billingAddress.getZipCode());

    }

    private void performEnrollment() {
        final String cardHolderName = getText(mEtCardholderName);

        final ConsumerInfo consumerInfo = getConsumerInfo();

        final BillingAddress billingAddress = getBillingAddress();

        mViewModel.enrolClickToPay(mCardId, consumerInfo, billingAddress, cardHolderName);
    }

    @NonNull
    private BillingAddress getBillingAddress() {
        final String countryCode = getText(mEtCountryCode);
        final String line1 = getText(mEtLine1);
        final String line2 = getText(mEtLine2);
        final String line3 = getText(mEtLine3);
        final String city = getText(mEtCity);
        final String state = getText(mEtState);
        final String zipCode = getText(mEtZipCode);

        final BillingAddress billingAddress = new BillingAddress(countryCode);
        if (!TextUtils.isEmpty(line1)) {
            billingAddress.setLine1(line1);
        }

        billingAddress.setLine2(line2);
        billingAddress.setLine3(line3);

        if (!TextUtils.isEmpty(city)) {
            billingAddress.setCity(city);
        }
        if (!TextUtils.isEmpty(state)) {
            billingAddress.setState(state);
        }
        if (!TextUtils.isEmpty(zipCode)) {
            billingAddress.setZipCode(zipCode);
        }
        return billingAddress;
    }

    @NonNull
    private ConsumerInfo getConsumerInfo() {
        final String firstName = getText(mEtFirstName);
        final String middleName = getText(mEtMiddleName);
        final String lastName = getText(mEtLastName);
        final String language = getText(mEtLanguage);
        final String phoneCountryCode = getText(mEtPhoneCountryCode);
        final String phoneNumber = getText(mEtPhoneNumber);
        final String email = getText(mEtEmail);

        final ConsumerInfo consumerInfo = new ConsumerInfo(
                firstName,
                middleName,
                lastName,
                language,
                phoneCountryCode,
                phoneNumber,
                email
        );

        return consumerInfo;
    }

    private void performUpdate() {
        final String cardHolderName = getText(mEtCardholderName);

        final ConsumerInfo consumerInfo = getConsumerInfo();

        final BillingAddress billingAddress = getBillingAddress();

        mViewModel.updateClickToPay(mCardId, consumerInfo, billingAddress, cardHolderName);
    }

    private void performOptOut() {
        mViewModel.optOutClickToPay(mCardId);
    }

    @NonNull
    private String getText(@NonNull final TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @NonNull
    @Override
    protected ClickToPayViewModel createViewModel() {
        return new ViewModelProvider(this).get(ClickToPayViewModel.class);
    }
}
