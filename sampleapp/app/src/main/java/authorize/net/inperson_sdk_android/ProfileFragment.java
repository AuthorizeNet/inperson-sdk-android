package authorize.net.inperson_sdk_android;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import net.authorize.data.Address;
import net.authorize.data.cim.CustomerProfile;
import net.authorize.data.cim.PaymentProfile;


public class ProfileFragment extends DialogFragment {
    String TAG = ProfileFragment.class.getSimpleName();
    LinearLayout layoutCustomerProfile, layoutPaymentProfile;
    EditText editCustEmail, editMerchantCustId, editDescription;
    EditText editTextFirstName, editTextLastName, editTextCompany, editTextAddress,
            editTextCity, editTextState, editTextZip, editTextCountryName,
            editTextPhone, editTextFax;
    TextInputLayout inputLayoutEmail;
    Button buttonCancel, buttonCreate;

    public static String CREATE_PAYMENT_PROFILE = "create_payment_profile";
    public static String CREATE_CUSTOMER_PROFILE = "create_customer_and_payment_profile";
    String task = CREATE_CUSTOMER_PROFILE;

    ProfileListener dialogProfileListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public ProfileFragment(ProfileListener dialogProfileListener, String task) {
        Log.i(TAG,"ProfileFragment()");
        this.dialogProfileListener = dialogProfileListener;
        this.task = task;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView()");
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        getDialog().setTitle("Profile");
        mapViewElements(view);
        mapViewElements(view);
        if (task.equals(CREATE_CUSTOMER_PROFILE)) {
            layoutCustomerProfile.setVisibility(View.VISIBLE);
        } else if (task.equals(CREATE_PAYMENT_PROFILE)) {
            layoutCustomerProfile.setVisibility(View.GONE);
        }
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                dialogProfileListener.onProfileCancelled();
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (task.equals(CREATE_PAYMENT_PROFILE)) {
                    dismiss();
                    dialogProfileListener.onPaymentProfileInputSubmitted(getPaymentProfile());
                } else {
                    final String email = editCustEmail.getText().toString();
                    final String custId = editMerchantCustId.getText().toString();
                    final String description = editDescription.getText().toString();
                    if (isValidFields(email,custId,description,inputLayoutEmail)){
                        dismiss();
                        dialogProfileListener.onCustomerProfileInputsSubmitted(getCustomerProfile(email,custId,description)
                                ,getPaymentProfile());
                    }
                }
            }
        });
        return view;
    }

    boolean isValidFields(String email, String custId, String description, TextInputLayout inputLayoutEmail) {
        Log.i(TAG, "isValidFields()");
        boolean result = true;
        if (email.isEmpty() && custId.isEmpty() && description.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "At least one of the fields" +
                            " Customer ID, Description, or Email are required to save a Customer Profile",
                    Toast.LENGTH_LONG).show();
            result = false;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError("Enter valid email");
            result = false;
        }

        return result;
    }

    CustomerProfile getCustomerProfile(String email, String merchantCustomerId, String description) {
        Log.i(TAG, "getCustomerProfile()");
        CustomerProfile customerProfile = CustomerProfile.createCustomerProfile();
        customerProfile.setEmail(email);
        customerProfile.setMerchantCustomerId(merchantCustomerId);
        customerProfile.setDescription(description);
        return customerProfile;
    }


    PaymentProfile getPaymentProfile() {
        Log.i(TAG, "getPaymentProfile()");
        PaymentProfile paymentProfile = PaymentProfile.createPaymentProfile();
        paymentProfile.setBillTo(getAddress());
        return paymentProfile;
    }

    Address getAddress() {
        Log.i(TAG, "getAddress()");
        Address address = Address.createAddress();
        address.setFirstName(editTextFirstName.getText().toString());
        address.setLastName(editTextLastName.getText().toString());
        address.setCity(editTextCity.getText().toString());
        address.setState(editTextState.getText().toString());
        address.setCountry(editTextCountryName.getText().toString());
        address.setZipPostalCode(editTextZip.getText().toString());
        address.setCompany(editTextCompany.getText().toString());
        address.setPhoneNumber(editTextPhone.getText().toString());
        address.setAddress(editTextAddress.getText().toString());
        address.setFaxNumber(editTextFax.getText().toString());
        return address;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public interface ProfileListener {
        void onCustomerProfileInputsSubmitted(CustomerProfile customerProfile, PaymentProfile paymentProfile);
        void onPaymentProfileInputSubmitted(PaymentProfile paymentProfile);
        void onProfileCancelled();
    }

    void mapViewElements(View dialog) {
        Log.i(TAG, "mapElements()");
        layoutCustomerProfile = (LinearLayout) dialog.findViewById(R.id.layout_customer_profile);
        editCustEmail = (EditText) dialog.findViewById(R.id.edit_cust_email_id);
        editMerchantCustId = (EditText) dialog.findViewById(R.id.edit_merchant_customer_id);
        editDescription = (EditText) dialog.findViewById(R.id.edit_descriptionn);
        inputLayoutEmail = (TextInputLayout) dialog.findViewById(R.id.inputLayoutEmailId);

        //Views for payment profile
        layoutPaymentProfile = (LinearLayout) dialog.findViewById(R.id.layout_payment_profile);
        editTextFirstName = (EditText) dialog.findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) dialog.findViewById(R.id.editTextLastName);
        editTextCompany = (EditText) dialog.findViewById(R.id.editTextCompany);
        editTextAddress = (EditText) dialog.findViewById(R.id.editTextAddress);
        editTextCity = (EditText) dialog.findViewById(R.id.editTextCity);
        editTextState = (EditText) dialog.findViewById(R.id.editTextState);
        editTextZip = (EditText) dialog.findViewById(R.id.editTextZip);
        editTextCountryName = (EditText) dialog.findViewById(R.id.editTextCountryName);
        editTextPhone = (EditText) dialog.findViewById(R.id.editTextPhone);
        editTextFax = (EditText) dialog.findViewById(R.id.editTextFax);
        buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
        buttonCreate = (Button) dialog.findViewById(R.id.button_create_profile);
    }
}
