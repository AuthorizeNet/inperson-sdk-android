package authorize.net.inperson_sdk_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.authorize.ResponseReasonCode;
import net.authorize.aim.emv.EMVDeviceConnectionType;
import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.EMVTransactionType;
import net.authorize.aim.emv.EmvSdkUISettings;
import net.authorize.aim.emv.OTAUpdateManager;
import net.authorize.aim.emv.QuickChipTransactionSession;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.mobile.Result;
import net.authorize.mobile.TransactionType;
import net.authorize.util.StringUtils;
import net.authorize.xml.MessageType;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    List<String> list;
    Button emvButton;
    Button quickChipButton;
    Button prepareDataButton;
    Button clearDataButton;
    Button quickChipTipOptionButton;
    Button quickChipTipAmountButton;
    Button quickChipAuthOnlyButton;
    Button lastTransactionButton;
    TextView statusTextView;
    EditText amount;
    EditText tipAmount;
    EditText tableNumberEditText;
    EditText employeeIdEditText;
    ToggleButton swipeOnlyToggleButton;
    ToggleButton bluetoothToggleButton;
    String spinnerAmountStr = "1";
    RelativeLayout cvrLayout;
    Button clearSavedBTDeviceButton;
    Button startBTScanButton;
    Button connectBTButton;
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
        emvButton = (Button) findViewById(R.id.emvbutton);
        quickChipButton = (Button) findViewById(R.id.quickchipbutton);
        prepareDataButton = (Button) findViewById(R.id.preparequichichipdatabutton);
        clearDataButton = (Button) findViewById(R.id.clearsaveddatabutton);
        statusTextView = (TextView) findViewById(R.id.statustextView);
        quickChipTipOptionButton = (Button) findViewById(R.id.process_payment_with_quickchip_and_tip_options);
        quickChipTipAmountButton = (Button) findViewById(R.id.process_payment_with_quickchip_and_tip_amount);
        quickChipAuthOnlyButton = (Button) findViewById(R.id.quickchipauthonly);
        lastTransactionButton = (Button) findViewById(R.id.last_transaction_button);
        employeeIdEditText = (EditText) findViewById(R.id.employee_id_edit_text);
        tableNumberEditText = (EditText) findViewById(R.id.table_number_edit_text);
        tipAmount = (EditText) findViewById(R.id.tip_amount);
        swipeOnlyToggleButton = (ToggleButton) findViewById(R.id.swipe_only_mode_toggle_button);
        bluetoothToggleButton = (ToggleButton) findViewById(R.id.bluetooth_toggle_button);
        clearSavedBTDeviceButton = (Button) findViewById(R.id.clearsavedbtdevice);
        startBTScanButton = (Button) findViewById(R.id.start_bt_scan_button);
        connectBTButton = (Button) findViewById(R.id.connect_bluetooth_button);

        try {
            emvButton.setOnClickListener(mListner);
            quickChipButton.setOnClickListener(mListner);
            prepareDataButton.setOnClickListener(mListner);
            clearDataButton.setOnClickListener(mListner);
            quickChipTipOptionButton.setOnClickListener(mListner);
            quickChipTipAmountButton.setOnClickListener(mListner);
            quickChipAuthOnlyButton.setOnClickListener(mListner);
            lastTransactionButton.setOnClickListener(mListner);
            clearSavedBTDeviceButton.setOnClickListener(mListner);
            startBTScanButton.setOnClickListener(mListner);
            connectBTButton.setOnClickListener(mListner);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        amount = (EditText) findViewById(R.id.editTextAmount);
        context = this;

        checkRuntimePermissions();
        setUIPreferences();
    }

    void setUIPreferences() {
//        EmvSdkUISettings.setBackgroundDrawableId(R.drawable.emvbackground);
        EmvSdkUISettings.setToastColor(Color.GREEN);

    }

    @Override
    protected void onPause() {
        super.onPause();
        QuickChipTransactionSession.clearSavedTransactionData(this.iemvTransaction);
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
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH))
            permissionsNeeded.add("need Bluetooth permissions to communicate with reader");
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_ADMIN))
            permissionsNeeded.add("");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Need coarse location to access bluetooth device");

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


    EMVTransactionManager.QuickChipTransactionSessionListener iemvTransaction = new EMVTransactionManager.QuickChipTransactionSessionListener() {
        @Override
        public void onReturnBluetoothDevices(final List<BluetoothDevice> bluetoothDeviceList) {
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth Devices");

            String[] titles = new String[bluetoothDeviceList.size()];
            for (int i=0;i<bluetoothDeviceList.size();i++) {
                titles[i] = bluetoothDeviceList.get(i).getName();
            }
            builder.setItems(titles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EMVTransactionManager.connectBTDevice(MainActivity.this, bluetoothDeviceList.get(0), iemvTransaction);
                }
            });

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();


        }

        @Override
        public void onBluetoothDeviceConnected(BluetoothDevice bluetoothDevice) {
            Log.d("Bluetooth device", "bluetooth device connected : " + bluetoothDevice.getName());
        }

        @Override
        public void onTransactionStatusUpdate(String transactionStatus) {
            statusTextView.setTextColor(Color.GREEN);
            statusTextView.setText(transactionStatus);
        }

        @Override
        public void onPrepareQuickChipDataSuccessful() {
            statusTextView.setTextColor(Color.BLACK);
            statusTextView.setText("Chip data saved Successfully");
        }

        @Override
        public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
            statusTextView.setTextColor(Color.RED);
            statusTextView.setText(cause);
        }

        @Override
        public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {
            if (result != null) {

                //save previous successful transaction
                AppManager.lastTransactionResult = result;

                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.receipt);

                TextView status = (TextView) dialog.findViewById(R.id.textViewStatus);
                status.setText("Transaction Successful");
                TextView amount = (TextView) dialog.findViewById(R.id.textViewResAmount);
                TextView tipAmount = (TextView) dialog.findViewById(R.id.textViewResTipAmount);

                amount.setText("$" + result.getAuthorizedAmount() + " USD");

                if (result.getTipAmount() != null) {
                    tipAmount.setText("$" + result.getTipAmount() + " USD");
                } else {
                    tipAmount.setText("N/A");
                }
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
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setTitle("EMV Status");
            adb.setNeutralButton("OK", null);
            adb.setCancelable(true);
            if (emvError != null) {
                adb.setMessage(emvError.getErrorString());

            } else
                adb.setMessage("EMV Error");

            if (emvError == EMVErrorCode.VOLUME_WARNING_NOT_ACCEPTED) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume (AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
            } else {
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
                TextView tipAmount = (TextView) dialog.findViewById(R.id.textViewResTipAmount);

                amount.setText("$" + result.getAuthorizedAmount() + " USD");

                if (result.getTipAmount() != null) {
                    tipAmount.setText("$" + result.getTipAmount() + " USD");
                } else {
                    tipAmount.setText("N/A");
                }
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

    OTAUpdateManager.HeadlessOTAUpdateListener headlessOTAUpdateListener = new OTAUpdateManager.HeadlessOTAUpdateListener() {
        @Override
        public void onReturnOTAUpdateHeadlessProgress(OTAUpdateManager.HeadlessOTAUpdateStatus headlessOTAUpdateStatus, double v) {

        }

        @Override
        public void onReturnCheckForUpdateResult(OTAUpdateManager.HeadlessOTACheckResult headlessOTACheckResult) {

        }

        @Override
        public void onReturnOTAUpdateHeadlessResult(OTAUpdateManager.HeadlessOTAUpdateType headlessOTAUpdateType, OTAUpdateManager.HeadlessOTAUpdateResult headlessOTAUpdateResult, String s) {

        }

        @Override
        public void onReturnDeviceInfo(Hashtable<String, String> deviceInfo) {
            StringBuilder sb = new StringBuilder();
            for (String key : deviceInfo.keySet()) {
                sb.append(key).append(" : ").append(deviceInfo.get(key)).append("\n");
            }
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setTitle("Device Info");
            adb.setMessage(sb.toString());
            adb.setNeutralButton("OK", null);
            adb.setCancelable(true);

            adb.show();
        }

        @Override
        public void onReturnOTAUpdateError(OTAUpdateManager.HeadlessOTAUpdateError headlessOTAUpdateError, String s) {

        }

        @Override
        public void onBluetoothScanTimeout() {

        }

        @Override
        public void onReturnBluetoothDevices(List<BluetoothDevice> list) {

        }

        @Override
        public void onBluetoothDeviceConnected(BluetoothDevice bluetoothDevice) {

        }
    };


    View.OnClickListener mListner = new View.OnClickListener() {

        public void onClick(View view) {
            if (AppManager.merchant == null) {
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

            //optional fields for tip
            emvTransaction.setTableNumber(tableNumberEditText.getText().toString());
            emvTransaction.setEmployeeId(employeeIdEditText.getText().toString());

            EMVTransactionManager.TerminalMode terminalMode = swipeOnlyToggleButton.isChecked() ? EMVTransactionManager.TerminalMode.SWIPE_ONLY
                                                                    : EMVTransactionManager.TerminalMode.SWIPE_OR_INSERT;
            EMVTransactionManager.setTerminalMode(terminalMode);

            EMVDeviceConnectionType deviceConnectionType = bluetoothToggleButton.isChecked()?
                    EMVDeviceConnectionType.BLUETOOTH : EMVDeviceConnectionType.AUDIO;
            EMVTransactionManager.setDeviceConnectionType(deviceConnectionType);

            if (view == emvButton) {
                EMVTransactionManager.startEMVTransaction(emvTransaction, iemvTransaction, context);
            } else if (view == quickChipButton) {
                EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context);
            } else if (view == prepareDataButton) {
                EMVTransactionManager.prepareDataForQuickChipTransaction(context, iemvTransaction);
            } else if (view == clearDataButton) {
                EMVTransactionManager.clearStoredQuickChipData(iemvTransaction);
            } else if (view == clearSavedBTDeviceButton) {
                EMVTransactionManager.clearSavedBTDevice(context);
            } else if (view == quickChipTipOptionButton) {
                EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, new EMVTransactionManager.TipOptions(15, 18, 20));
            } else if (view == quickChipTipAmountButton) {
                double tipAmountValue = 0;
                double amountValue = 0;
                try {
                    tipAmountValue = Double.parseDouble(tipAmount.getText().toString());
                    amountValue = Double.parseDouble(amount.getText().toString());
                } catch (NumberFormatException e) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
                    adb.setTitle("Invalid amount");
                    adb.setNeutralButton("OK", null);
                    adb.setCancelable(false);
                    adb.create().show();
                    return;
                }
                spinnerAmountStr = new DecimalFormat("0.00").format(tipAmountValue + amountValue);
                EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, tipAmountValue);
            } else if (view == quickChipAuthOnlyButton) {
                EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, true, true);
            } else if (view == lastTransactionButton) {
                Intent i = new Intent(MainActivity.this, LastTransactionActivity.class);
                MainActivity.this.startActivity(i);
            }else if (view == startBTScanButton) {
                EMVTransactionManager.setDeviceConnectionType(EMVDeviceConnectionType.BLUETOOTH);
                bluetoothToggleButton.setChecked(true);
                EMVTransactionManager.startBTScan(context, iemvTransaction);
            } else if (view == connectBTButton) {
                //will try to connect last used bluetooth device
                EMVTransactionManager.connectBTDevice(MainActivity.this, null, iemvTransaction);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    ProgressDialog pr;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (bluetoothToggleButton.isChecked()) {
            EMVTransactionManager.setDeviceConnectionType(EMVDeviceConnectionType.BLUETOOTH);
        } else {
            EMVTransactionManager.setDeviceConnectionType(EMVDeviceConnectionType.AUDIO);
        }
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                pr = new ProgressDialog(context);
                pr.setIndeterminate(true);
                pr.setMessage("Logging out...");
                pr.show();
                Thread t = new Thread(){
                    @Override
                    public void run(){

                        net.authorize.mobile.Transaction logoutTrans =  AppManager.merchant.createMobileTransaction(TransactionType.LOGOUT);

                        Result r = (Result)AppManager.merchant.postTransaction(logoutTrans);
                        myHandler.sendEmptyMessage(1);
                    }
                };
                t.start();
                return true;
            case R.id.ota_update:
                OTAUpdateManager.startOTAUpdate(this, false);
                return true;
            case R.id.ota_update_demo:
                OTAUpdateManager.startOTAUpdate(this, true);
                return true;
            case R.id.reset_reader:
                EMVTransactionManager.resetReader(this, iemvTransaction);
                return true;
            case R.id.device_info:
                OTAUpdateManager.getDeviceInfo(this,true, headlessOTAUpdateListener);
                return true;
        }

        return false;
    }

}