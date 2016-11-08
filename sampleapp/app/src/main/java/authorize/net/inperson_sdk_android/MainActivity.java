package authorize.net.inperson_sdk_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.authorize.ResponseReasonCode;
import net.authorize.aim.emv.EMVActivity;
import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.EMVTransactionType;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.mobile.Result;
import net.authorize.mobile.TransactionType;
import net.authorize.util.StringUtils;
import net.authorize.xml.MessageType;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    List<String> list;
    Button b;
    EditText amount;
    String spinnerAmountStr = "1";
    RelativeLayout cvrLayout;
    private Dialog dialog;

    Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            pr.dismiss();
            finish();
            Intent i =new Intent(context,LoginActivity.class);
            startActivity(i);
        }

    };


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        FragmentManager fmanager = this.getFragmentManager();

        if(fmanager.findFragmentByTag("background")==null){


        }
        setContentView(R.layout.activity_main);
        b = (Button) findViewById(R.id.button);

        b.setOnClickListener(mListner);
        amount = (EditText) findViewById(R.id.editTextAmount);
        context = this;

        checkRuntimePermissions();
    }
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }
    boolean haveAllPermissions = false;
    private void checkRuntimePermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("'Record Audio' to make the card-swipers work correctly");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("and 'Read/Write External Storage' to store/retrieve information.");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("");

        if (!permissionsList.isEmpty()) {
            haveAllPermissions = false;
//            if (!permissionsNeeded.isEmpty()) {
//                // Need Rationale
//                String message = "Please allow access to " + permissionsNeeded.get(0);
//                for (int i = 1; i < permissionsNeeded.size(); i++)
//                    if(!permissionsNeeded.get(i).isEmpty())
//                        message = message + ", " + permissionsNeeded.get(i);
//                showRationale("Permissions Request", permissionsList, message, ALLOW, CANCEL);
//                return;
//            }
            ActivityCompat.requestPermissions(this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    1);
            return;
        }
        else {
//            haveAllPermissions = true;
            //rf.initializeDevice(); // already have permissions so initialize
        }
    }



    EMVTransactionManager.EMVTransactionListener iemvTransaction = new EMVTransactionManager.EMVTransactionListener() {
        @Override
        public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {
            if (result != null) {


                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.receipt);
                TextView status = (TextView) dialog.findViewById(R.id.textViewStatus);
                status.setText("Transaction Successful");
                TextView amount = (TextView) dialog.findViewById(R.id.textViewResAmount);

                amount.setText("$" + spinnerAmountStr + " USD");
                TextView tid = (TextView) dialog.findViewById(R.id.textViewResTransId);
                tid.setText(result.getTransId());
                TextView aid = (TextView) dialog.findViewById(R.id.textViewResApplicationId);
                HashMap<String, String> map = result.getEmvTlvMap();
                //4f is EMV code for application ID
                if(map.get("4f")!=null){
                    aid.setText( map.get("4f"));
                }
                else
                    aid.setVisibility(View.INVISIBLE);



                TextView mode = (TextView) dialog.findViewById(R.id.textViewResMode);
                if (result.getIsIssuerResponse())
                    mode.setText("ISSUER");
                else
                    mode.setText("CARD");



                TextView tvr = (TextView) dialog.findViewById(R.id.textViewResTVR);
                if (map.containsKey("95")) ;
                tvr.setText(map.get("95"));

                TextView iad = (TextView) dialog.findViewById(R.id.textViewResIAD);
                if (map.containsKey("9f10")) ;
                iad.setText(map.get("9f10"));

                TextView tsi = (TextView) dialog.findViewById(R.id.textViewResTSI);
                if (map.containsKey("9b")) ;
                tsi.setText(map.get("9b"));

                TextView arc = (TextView) dialog.findViewById(R.id.textViewResARC);
                if (map.containsKey("8a")) ;
                arc.setText(map.get("8b"));


//                cvrLayout = (RelativeLayout) dialog.findViewById(R.id.cvrLayout);

//                if(result.isShowSignature())
//                    cvrLayout.setVisibility(View.VISIBLE);
//                else
//                    cvrLayout.setVisibility(View.INVISIBLE);

                dialog.show();
            } else;
//                Toast.makeText(getApplicationContext(), "Fallback swipe transactions will be supported in next release", Toast.LENGTH_LONG).show();

//            Button button = (Button)dialog.findViewById(R.id.button);
//
//            button.setOnClickListener(new  View.OnClickListener(){
//
//                @Override
//                public void onClick(View view){
//
//                }
//
//            });

        }

        View.OnClickListener listener =new  View.OnClickListener(){

            @Override
            public void onClick(View view){

            }

        };

        @Override
        public void onEMVReadError(EMVErrorCode emvError) {
            if (emvError == EMVErrorCode.VOLUME_WARNING_NOT_ACCEPTED) {
                AudioManager audioManager = (AudioManager) getSystemService(context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
            } else {
                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setTitle("EMV Status");
                adb.setNeutralButton("OK", null);
                adb.setCancelable(true);
                if (emvError != null) {
                    adb.setMessage(emvError.getErrorString());

                } else
                    adb.setMessage("EMV Error");
                adb.create().show();
            }
        }

        @Override
        public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {


            // do a time out error first

            if (result != null) {

                ArrayList<MessageType> message1 = null;

                message1 = result.getMessages();
                if (message1 != null
                        && message1.size() > 0
                        && message1.get(0) != null
                        && message1.get(0).compareTo(MessageType.E00007) == 0) {
                    Toast.makeText(context.getApplicationContext(), "Session time out: Please log in again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    return;

                }
            }


            if (result != null && result.getEmvTlvMap() != null) {

                HashMap<String, String> emvMap = result.getEmvTlvMap();
                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.receipt);
                TextView amount = (TextView) dialog.findViewById(R.id.textViewResAmount);
                amount.setText("$"+spinnerAmountStr + " USD");
                TextView tid = (TextView) dialog.findViewById(R.id.textViewResTransId);
                if (result.getTransId() != null)
                    tid.setText(result.getTransId());

                TextView mode = (TextView) dialog.findViewById(R.id.textViewResMode);
                if (result.getIsIssuerResponse())
                    mode.setText("ISSUER");
                else
                    mode.setText("CARD");

                TextView aid = (TextView) dialog.findViewById(R.id.textViewResApplicationId);
//
                if(emvMap.get("4f")!=null){
                    aid.setText("ApplicationId: " + emvMap.get("4f"));
                }
                else
                    aid.setVisibility(View.INVISIBLE);

                TextView tvr = (TextView) dialog.findViewById(R.id.textViewResTVR);
                if (emvMap.containsKey("95")) ;
                tvr.setText(emvMap.get("95"));

                TextView iad = (TextView) dialog.findViewById(R.id.textViewResIAD);
                if (emvMap.containsKey("9f10")) ;
                iad.setText(emvMap.get("9f10"));

                TextView tsi = (TextView) dialog.findViewById(R.id.textViewResTSI);
                if (emvMap.containsKey("9b")) ;
                tsi.setText(emvMap.get("9b"));

                TextView arc = (TextView) dialog.findViewById(R.id.textViewResARC);
                if (emvMap.containsKey("8a")) ;
                arc.setText(emvMap.get("8b"));

                TextView errorMsg = (TextView) dialog.findViewById(R.id.textViewStatus);


                //
                ArrayList<ResponseReasonCode> responseError = result
                        .getTransactionResponseErrors();
                if (responseError != null
                        && responseError.size() > 0
                        && !StringUtils.isEmpty(responseError
                        .get(0).getReasonText()))
                    errorMsg.setText(responseError.get(0)
                            .getReasonText());

                else
                    errorMsg.setText("Transaction Unsuccessful");

                //

                TextView merchantText = (TextView) dialog.findViewById(R.id.textViewResMerchant);
                merchantText.setVisibility(View.GONE);
                dialog.show();


            } else {

                ///else

                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                ArrayList<MessageType> message = null;
                if (result != null) {
                    message = result.getMessages();
                }

                if (result == null) {
                    if (emvError == EMVErrorCode.TRANSACTION_DECLINED)
                        adb.setMessage(String.valueOf(emvError.getError()) + "Transaction Decline");
                } else if (message != null
                        && message.size() > 0
                        && message.get(0) != null
                        && message.get(0).compareTo(MessageType.E00007) == 0) {
                    Toast.makeText(context.getApplicationContext(), "Session time out: Please log in again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();


                } else {
                    ArrayList<ResponseReasonCode> responseError = result
                            .getTransactionResponseErrors();
                    if (responseError != null
                            && responseError.size() > 0
                            && !StringUtils.isEmpty(responseError
                            .get(0).getReasonText()))
                        adb.setMessage(responseError.get(0)
                                .getReasonText());

                    else
                        adb.setMessage("Transaction Unsuccessful");
                }

                adb.setTitle("EMV Status");
                adb.setNeutralButton("OK", null);
                adb.setCancelable(true);
                adb.create().show();

            }


        }



    };

    View.OnClickListener mListner = new View.OnClickListener() {

        public void onClick(View view) {

            if(AppManager.merchant==null){
                Intent i = new Intent(context, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }

            String value = amount.getText().toString();
            if (StringUtils.isEmpty(value))
                value = "0.0";
            spinnerAmountStr = value;
            Order order = Order.createOrder();
            OrderItem oi = OrderItem.createOrderItem();
            oi.setItemId("1");
            oi.setItemName("name");

            oi.setItemQuantity("1");
            oi.setItemTaxable(false);
            oi.setItemDescription("desc");
            oi.setItemDescription("Goods");

            order.addOrderItem(oi);
            BigDecimal transAmount = new BigDecimal(value);
            oi.setItemPrice(transAmount);
            order.setTotalAmount(transAmount);
            EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(AppManager.merchant, transAmount);
            emvTransaction.setEmvTransactionType(EMVTransactionType.GOODS);
            emvTransaction.setOrder(order);
            emvTransaction.setSolutionID("SOLUTION ID");
            EMVTransactionManager.startEMVTransaction(emvTransaction, iemvTransaction, context);

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    ProgressDialog pr;

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.logout:
//                pr = new ProgressDialog(context);
//                pr.setIndeterminate(true);
//                pr.setMessage("Logging out...");
//                pr.show();
//                Thread t = new Thread(){
//                    @Override
//                    public void run(){
//
//                        net.authorize.mobile.Transaction logoutTrans =  AppManager.merchant.createMobileTransaction(TransactionType.LOGOUT);
//
//                        Result r = (Result)AppManager.merchant.postTransaction(logoutTrans);
//                        myHandler.sendEmptyMessage(1);
//                    }
//                };
//                t.start();
//                return true;
//        }
//
//        return false;
//    }

}