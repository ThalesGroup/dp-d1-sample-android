package com.thalesgroup.d1.templates.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.provider.Settings;

import com.thalesgroup.d1.templates.core.enums.AirplaneModeState;
import com.thalesgroup.d1.templates.core.enums.InternetState;
import com.thalesgroup.d1.templates.core.enums.NfcState;

/**
 * NetworkUtils.
 */
final public class NetworkUtils {

    private static final NetworkUtils INSTANCE;

    private NetworkUtils() {
        // private constructor
    }

    static {
        INSTANCE = new NetworkUtils();
    }

    /**
     * Return singleton of public API for application use.
     *
     * @return {@code CoreUtils}.
     */
    public static NetworkUtils getInstance() {
        return INSTANCE;
    }


    /**
     * @param context Context.
     *
     * @return {@code Boolean}.
     */
    @SuppressLint("MissingPermission")
    public Boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        final NetworkCapabilities activeNetwork = connectivityManager.getNetworkCapabilities(network);

        return activeNetwork != null && (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    /**
     * @param context Context.
     *
     * @return {@code InternetState}.
     */
    @SuppressLint("MissingPermission")
    public static InternetState getInternetState(final Context context) {
        InternetState internetState;

        if (NetworkUtils.getInstance().isNetworkAvailable(context)) {
            internetState = InternetState.CONNECTED;
        } else {
            internetState = InternetState.DISCONNECTED;
        }

        return internetState;
    }

    /**
     * @param context Context
     *
     * @return {@code AirplaneModeState}.
     */
    public static AirplaneModeState getAirplaneModeState(final Context context) {
        if (Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
            return AirplaneModeState.ON;
        } else {
            return AirplaneModeState.OFF;
        }
    }

    /**
     * @param context Context.
     *
     * @return {@code NfcState}.
     */
    public static NfcState getNfcState(final Context context) {
        final NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter adapter = manager.getDefaultAdapter();

        if (adapter != null && adapter.isEnabled()) {
            return NfcState.ON;
        } else if (adapter != null && !adapter.isEnabled()) {
            return NfcState.OFF;
        } else {
            return NfcState.NONE;
        }
    }
}
