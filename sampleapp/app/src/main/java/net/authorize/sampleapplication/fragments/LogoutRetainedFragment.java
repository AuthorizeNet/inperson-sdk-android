package net.authorize.sampleapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import net.authorize.sampleapplication.services.AnetIntentService;
import net.authorize.sampleapplication.receivers.AnetResultReceiver;

/**
 * Starts the AnetIntentService to log a user out and listens to the results of the
 * logout transaction (from the result receiver) to send back via a callback to the activity.
 */
public class LogoutRetainedFragment extends android.support.v4.app.Fragment implements
        AnetResultReceiver.ReceiverCallback {

    private AnetResultReceiver resultReceiver;
    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onReceiveLogoutResult(Bundle resultData);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        resultReceiver = new AnetResultReceiver(new Handler());
        resultReceiver.setReceiverCallback(this);

        startServiceLogout();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (resultReceiver != null)
            resultReceiver.clearReceiverCallback();
    }


    /**
     * Starts the service to log a user out
     * @return whether the service was successfully started
     */
    public boolean startServiceLogout() {
        try {
            Intent intent = new Intent(getActivity(), AnetIntentService.class);
            intent.setAction(AnetIntentService.ACTION_LOGOUT);
            intent.putExtra(AnetResultReceiver.RESULT_RECEIVER_TAG, resultReceiver);
            getActivity().startService(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Uses a callback to propagate the result of the service request back to the calling activity
     * @param resultCode result code for the transaction defined in and delivered by the service
     * @param resultData the result of the logout transaction
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == AnetIntentService.LOGOUT_RESULT_CODE)
            mListener.onReceiveLogoutResult(resultData);
    }
}
