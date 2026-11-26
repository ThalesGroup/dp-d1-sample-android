package com.thalesgroup.d1.templates.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.pay.ui.PayAbstractBaseFragment;
import com.thalesgroup.d1.templates.ui.home.HomeFragment;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.messaging.MessagingService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Login Fragment.
 */
public class LoginFragment extends PayAbstractBaseFragment<LoginViewModel> {

    public static final String CONSUMERS_JSON = "test_consumers.json";
    private static final String TAG = "LoginFragment";

    private AutoCompleteTextView mConsumerIdInput;
    private List<String> mConsumerIdList = new ArrayList<>();

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

        Log.i(TAG, "SDK versions: " + new Gson().toJson(D1Task.getSDKVersions()));

        mConsumerIdInput = view.findViewById(R.id.consumer_id_input);
        loadConsumersFromAssets();

        view.findViewById(R.id.login_button).setOnClickListener(v -> {
            Configuration.consumerId = mConsumerIdInput.getText().toString();

            showProgressDialog(getString(R.string.login_in_progress));

            mPayViewModel.configure(Configuration.consumerId, requireActivity(), requireActivity().getApplicationContext());

            mPayViewModel.login();
        });

        mPayViewModel.getIsLoginSuccess().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean isLoginSuccess) {
                if (isLoginSuccess) {
                    hideProgressDialog();
                    showFragment(HomeFragment.newInstance(), false);

                    // Note: MessagingService registration has been moved to D1Core#login implementation
                }
            }
        });

        return view;
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


    private void loadConsumersFromAssets() {
        try (InputStream is = requireContext().getAssets().open(CONSUMERS_JSON)) {
            mConsumerIdList = new Gson().fromJson(new InputStreamReader(is), new TypeToken<List<String>>(){}.getType());

            Log.d(TAG, "Consumer list loaded: " + (mConsumerIdList == null ? "null" : ""+mConsumerIdList.size()));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, mConsumerIdList);
            mConsumerIdInput.setAdapter(adapter);

            mConsumerIdInput.setText(mConsumerIdList.get(0), false);

        } catch (Exception e) {
            Log.e(TAG, "Failed to load consumers from assets", e);
        }
    }
}
