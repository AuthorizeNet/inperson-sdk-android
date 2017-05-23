package authorize.net.inperson_sdk_android;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.authorize.Merchant;
import net.authorize.reporting.Result;
import net.authorize.reporting.Transaction;
import net.authorize.reporting.TransactionType;
import net.authorize.util.StringUtils;

import java.math.BigDecimal;

public class LastTransactionActivity extends AppCompatActivity {

    Button voidButton;
    Button captureButton;
    TextView transactionDetailTextView;
    ProgressDialog progressDialog;

    EditText tipAmountEditText;
    EditText tableNumberEditText;
    EditText employeeIdEditText;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_transaction);
        findViews();
        setOnClickListeners();

        getLastTransactionDetail();
    }

    void setOnClickListeners() {
        MyClickListener listener = new MyClickListener();
        voidButton.setOnClickListener(listener);
        captureButton.setOnClickListener(listener);
    }

    private class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == voidButton) {
                voidLastTransaction();
            } else if (v == captureButton) {
                captureLastTransaction();
            }
        }
    }

    void startLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    void finishLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    void findViews() {
        voidButton = (Button) findViewById(R.id.void_last_transaction_button);
        captureButton = (Button) findViewById(R.id.capture_last_transaction_button);
        transactionDetailTextView = (TextView) findViewById(R.id.last_transaction_detail_text_view);

        tipAmountEditText = (EditText) findViewById(R.id.capture_tip_amount_edit_text);
        tableNumberEditText = (EditText) findViewById(R.id.capture_table_number_edit_text);
        employeeIdEditText = (EditText) findViewById(R.id.capture_employee_id_edit_text);
    }

    void getLastTransactionDetail() {
        if (AppManager.lastTransactionResult != null) {
            transactionDetailTextView.setText(AppManager.lastTransactionResult.toString());

            if (AppManager.lastTransactionResult.getRequestTransactionType() == net.authorize.TransactionType.AUTH_ONLY) {
                captureButton.setEnabled(true);
            } else {
                captureButton.setEnabled(false);
            }


        } else {
            transactionDetailTextView.setText("Please finish at least one transaction to get report.");
            voidButton.setEnabled(false);
            captureButton.setEnabled(false);
        }
    }
    void voidLastTransaction() {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                net.authorize.aim.Transaction voidTransaction = AppManager.merchant.createAIMTransaction(net.authorize.TransactionType.VOID, new BigDecimal(AppManager.lastTransactionResult.getAuthorizedAmount()));
                voidTransaction.setRefTransId(AppManager.lastTransactionResult.getTransId());
                final net.authorize.aim.Result voidResult = (net.authorize.aim.Result)AppManager.merchant.postTransaction(voidTransaction);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (voidResult.isApproved()) {
                            transactionDetailTextView.setText(voidResult.toString());
                            voidButton.setEnabled(false);
                        } else {
                            transactionDetailTextView.setText("Void failed!\n" + voidResult.toString());
                        }
                        finishLoading();
                    }
                });

            }
        }).start();
    }
    void captureLastTransaction() {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tipAmount = tipAmountEditText.getText().toString();
                String employeeId = employeeIdEditText.getText().toString();
                String tableNumber = tableNumberEditText.getText().toString();

                net.authorize.aim.Transaction priorAuthCaptureTransaction = AppManager.merchant.createAIMTransaction(net.authorize.TransactionType.PRIOR_AUTH_CAPTURE, new BigDecimal(AppManager.lastTransactionResult.getAuthorizedAmount()));
                priorAuthCaptureTransaction.setRefTransId(AppManager.lastTransactionResult.getTransId());

                //tip amount
                if (StringUtils.isNotEmpty(tipAmount)) {
                    priorAuthCaptureTransaction.setTipAmount(tipAmount);
                    BigDecimal tipAmountDecimal = new BigDecimal(tipAmount);
                    priorAuthCaptureTransaction.setTotalAmount(priorAuthCaptureTransaction.getTotalAmount().add(tipAmountDecimal));
                }
                //employee id
                if (StringUtils.isNotEmpty(employeeId)) {
                    priorAuthCaptureTransaction.setEmployeeId(employeeId);
                }
                //table number
                if (StringUtils.isNotEmpty(tableNumber)) {
                    priorAuthCaptureTransaction.setTableNumber(tableNumber);
                }


                final net.authorize.aim.Result captureResult = (net.authorize.aim.Result) AppManager.merchant.postTransaction(priorAuthCaptureTransaction);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (captureResult.isApproved()) {
                            transactionDetailTextView.setText(captureResult.toString());
                            captureButton.setEnabled(false);
                        } else {
                            transactionDetailTextView.setText("Capture failed!\n" + captureResult.toString());
                        }
                        finishLoading();
                    }
                });

            }
        }).start();
    }

}
