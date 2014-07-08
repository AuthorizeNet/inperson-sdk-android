package net.authorize.sampleapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/**Fragment that opens an AlertDialog with message M and title T. */
public class DevInfoFragment extends DialogFragment {
    public static DevInfoFragment newInstance(String m, String t) {
        String message = m;
        String title = t;
        DevInfoFragment frag = new DevInfoFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView titleView = new TextView(getActivity());
        titleView.setText(title);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(15, 15, 15, 15);
        titleView.setTextSize(20);
        builder.setCustomTitle(titleView);

        TextView messageView = new TextView(getActivity());
        messageView.setText(message);
        messageView.setTextSize(15);
        messageView.setPadding(15, 15, 15, 15);
        messageView.setGravity(Gravity.CENTER);
        builder.setView(messageView);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog info = builder.create();
        setCancelable(false);
        return info;
    }
}

