package net.authorize.sampleapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import net.authorize.sampleapplication.services.AnetIntentService;
import net.authorize.sampleapplication.receivers.AnetResultReceiver;
import net.authorize.sampleapplication.models.CreditCardObject;

/**
 * Starts the AnetIntentService to either make a transaction using manually entered information
 * or using swipe information and listens to the results of the transaction
 * (from the result receiver) to send back via a callback to the activity.
 */
public class TransactionRetainedFragment extends android.support.v4.app.Fragment
        implements AnetResultReceiver.ReceiverCallback {

    private static AnetResultReceiver resultReceiver;
    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onReceiveTransactionResult(Bundle resultData);
        void onReceiveErrorResult(int resultCode);
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

        Bundle cardInformationBundle = getArguments();
        if (cardInformationBundle != null) {
            if (cardInformationBundle.getString(AnetIntentService.ACTION_TRANSACTION_TYPE_TAG).
                    equals(AnetIntentService.ACTION_MAKE_TRANSACTION)) {
                    CreditCardObject creditCardObject = cardInformationBundle.getParcelable
                            (TransactionFragment.CREDIT_CARD_TAG);
                    String zipcode = cardInformationBundle.getString(TransactionFragment.ZIPCODE_TAG);
                    String totalAmount = cardInformationBundle.getString(TransactionFragment.AMOUNT_TAG);
                    startServiceTransaction(creditCardObject, zipcode, totalAmount);
            } else if (cardInformationBundle.getString(AnetIntentService.ACTION_TRANSACTION_TYPE_TAG).
                    equals(AnetIntentService.ACTION_PERFORM_SWIPE)) {
                String totalAmount = cardInformationBundle.getString(TransactionFragment.AMOUNT_TAG);
                startServicePerformSwipe(totalAmount);
            }
        }
    }

    @Override
    public void onResume() {
        resultReceiver.setReceiverCallback(this);
        super.onResume();
    }


    /**
     * Starts the service to make a transaction with manually entered information
     * Pre: creditCardObject, zipcode and totalAmount are not empty
     * @param creditCardObject parcelable object that contains validated manually entered card information
     *                         from EditTexts
     * @param zipcode billing zipcode for the transaction
     * @param totalAmount total amount for the transaction
     * @return whether the service was successfully started
     */
    public boolean startServiceTransaction(CreditCardObject creditCardObject, String zipcode,
                                           String totalAmount) {
        try {
            Intent intent = new Intent(getActivity(), AnetIntentService.class);
            intent.setAction(AnetIntentService.ACTION_MAKE_TRANSACTION);
            intent.putExtra(TransactionFragment.CREDIT_CARD_TAG, creditCardObject);
            intent.putExtra(TransactionFragment.ZIPCODE_TAG, zipcode);
            intent.putExtra(TransactionFragment.AMOUNT_TAG, totalAmount);
            intent.putExtra(AnetResultReceiver.RESULT_RECEIVER_TAG, resultReceiver);
            getActivity().startService(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Starts the service to make a transaction using swipe information
     * Pre: totalAmount is not empty
     * @param totalAmount total amount for the transaction
     * @return whether the service was successfully started
     */
    public boolean startServicePerformSwipe(String totalAmount) {
        try {
            Intent intent = new Intent(getActivity(), AnetIntentService.class);
            intent.setAction(AnetIntentService.ACTION_PERFORM_SWIPE);
            intent.putExtra(TransactionFragment.AMOUNT_TAG, totalAmount);
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
     * @param resultData the result of the transaction
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == AnetIntentService.TRANSACTION_RESULT_CODE)
            mListener.onReceiveTransactionResult(resultData);
        if (resultCode == AnetIntentService.SESSION_EXPIRED_CODE)
            mListener.onReceiveErrorResult(resultCode);
    }
}
