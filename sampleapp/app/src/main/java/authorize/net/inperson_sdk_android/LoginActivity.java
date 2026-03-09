package authorize.net.inperson_sdk_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.auth.TransactionKeyAuthentication;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.creditcard.CreditCardPresenceType;
import net.authorize.data.mobile.MobileDevice;

import java.math.BigDecimal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoginActivity extends FragmentActivity {
    Button b;
    EditText login;
    EditText transactionKey;
    Context context;
    Environment environment = Environment.SANDBOX;
    RadioGroup radioGroup;
    RadioButton testButton;
    RadioButton prodButton;
    RadioButton hotspotButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        b= (Button)findViewById(R.id.buttonLoginLoginIn);
        b.setOnClickListener(mListner);

        login = (EditText)findViewById(R.id.editTextLoginLoginID);
        transactionKey = (EditText)findViewById(R.id.editTextLoginPassword);

        radioGroup = (RadioGroup) findViewById(R.id.login_type_radio_group);
        testButton = (RadioButton) findViewById(R.id.test_radio_button);
        prodButton = (RadioButton) findViewById(R.id.prod_radio_button);
        hotspotButton = (RadioButton) findViewById(R.id.hotspot_radio_button);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener (){
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton checkedButton = (RadioButton) findViewById(checkedId);
                if (checkedButton == testButton) {
                    environment = Environment.SANDBOX;
                } else if (checkedButton == prodButton) {
                    environment = Environment.PRODUCTION;
                }
            }
        });



        context = this;
    }


    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message inputMessage) {

            if(inputMessage.what==0){
                Intent i = new Intent(context,MainActivity.class);
                startActivity(i);
                finish();
                pd.dismiss();
            }
            else{
                pd.dismiss();
                Toast.makeText(context.getApplicationContext(),"Login Error",Toast.LENGTH_SHORT).show();
            }

        }

    };

    ProgressDialog pd;
    View.OnClickListener mListner = new View.OnClickListener(){

        public void onClick(View view){
            Thread t = new Thread(){

                @Override
                public void run(){

                    try {
                        // Use TransactionKeyAuthentication (no session token needed)
                        TransactionKeyAuthentication merchantAuth = TransactionKeyAuthentication
                                .createMerchantAuthentication(login.getText().toString(), transactionKey.getText().toString());

                        AppManager.merchant = Merchant.createMerchant(environment, merchantAuth);

                        Log.d("LoginInfo", "Using Transaction Key Authentication");

                        // Transaction key authentication doesn't require a login transaction
                        // Authentication is validated on the first actual transaction
                        handler.sendEmptyMessage(0);

                    } catch (Exception ex) {
                        Log.e("loginException", "Exception: " + ex.getMessage());
                        handler.sendEmptyMessage(1);
                    }

                }
            };
            t.start();
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.setTitle("Logging in..");
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();

        }
    };


}
