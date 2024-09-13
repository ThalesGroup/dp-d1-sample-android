package com.thalesgroup.d1.templates.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.ui.home.HomeFragment;

/**
 * Login Fragment.
 */
public class LoginFragment extends AbstractBaseFragment<LoginViewModel> {

    /**
     * Creates a new instance of {@code LoginFragment}.
     *
     * @return Instance of {@code LoginFragment}.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    /**
     * Create View.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     *
     * @return view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        view.findViewById(R.id.login_button).setOnClickListener(v -> {

            showProgressDialog(getString(R.string.login_in_progress));

            mViewModel.login();
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean isOperationSuccess) {
                if (isOperationSuccess) {
                    hideProgressDialog();

                    if (D1Core.getInstance().isD1PayEnabled()) {
                        checkTapAndPaySettings();
                    }
                }
            }
        });

        mViewModel.getIsLoginSuccess().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean isLoginSuccess) {
                if (isLoginSuccess) {
                    hideProgressDialog();

                    showFragment(HomeFragment.newInstance(), false);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        showProgressDialog(getString(R.string.initializing_d1_sdk));

        // Configure D1 SDK
        mViewModel.configure(Configuration.consumerId, requireActivity(), requireActivity().getApplicationContext());
    }

    /**
     * Create ViewModel
     *
     * @return {@code LoginViewModel}.
     */
    @NonNull
    @Override
    protected LoginViewModel createViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }
}
