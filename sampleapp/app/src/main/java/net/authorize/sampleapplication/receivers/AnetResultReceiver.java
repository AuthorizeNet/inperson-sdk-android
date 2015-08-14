package net.authorize.sampleapplication.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Receives all results from the intent service
 */
public class AnetResultReceiver extends ResultReceiver {

    public static final String RESULT_RECEIVER_TAG = "RESULT_RECEIVER_TAG";

    private ReceiverCallback receiverCallback;

    public interface ReceiverCallback {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public AnetResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiverCallback != null) {
            receiverCallback.onReceiveResult(resultCode, resultData);
        }
    }

    public void setReceiverCallback(ReceiverCallback receiver) {
        receiverCallback = receiver;
    }

    public void clearReceiverCallback() {
        receiverCallback = null;
    }
}
