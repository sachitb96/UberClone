package com.example.appmodel.ridematcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InformationPage extends AppCompatActivity {

    public final static String FirstPref = "FirstPref", LastPref = "LastPref", EmailPref = "EmailPref";
    public final static String INFO_TO_VERIFY = "com.example.appmodel.ridematcher_INFO_TO_VERIFY";
    EditText phoneField, firstField, emailField, lastField;
    String phoneNumber;
    boolean fromSocial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_page);
        Intent intent = getIntent();
        String email = intent.getStringExtra(SocialSignUpPage.SOCIAL_TO_INFORMATION_EMAIL);
        phoneField = (EditText)findViewById(R.id.InformationPhone);
        firstField = (EditText)findViewById(R.id.InformationFirstName);
        emailField = (EditText)findViewById(R.id.InformationEmailField);
        lastField = (EditText)findViewById(R.id.InformationLasttName);
        if (email == null){
            TextView phoneText = (TextView)(findViewById(R.id.InformationPhoneText));
            phoneText.setVisibility(View.VISIBLE);
            phoneNumber = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(SignUpPage.PHONENUMBER_PREF, "");
            phoneText.setText(phoneNumber);
            phoneField.setVisibility(View.GONE);
        }
        else {
           fromSocial = true;
            firstField.setText(intent.getStringExtra(SocialSignUpPage.SOCIAL_TO_INFORMATION_FIRST));
            emailField.setText(email);
            lastField.setText(intent.getStringExtra(SocialSignUpPage.SOCIAL_TO_INFORMATION_LAST));
        }
    }

    public void confirmAccount(View view){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("User");
        String phonenumber;
        if (phoneNumber == null){
            phonenumber = phoneField.getText().toString();
        }
        else {
            phonenumber = phoneNumber;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(phonenumber, "");
        databaseReference.updateChildren(map);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("First", firstField.getText().toString());
        map2.put("Last", lastField.getText().toString());
        map2.put("Email", emailField.getText().toString());
        databaseReference.child(phonenumber).updateChildren(map2);
        SharedPreferences.Editor e = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
        e.putString(FirstPref, firstField.getText().toString());
        e.putString(LastPref, lastField.getText().toString());
        e.putString(EmailPref, emailField.getText().toString());
        e.apply();
        if (fromSocial){
            String code = "";
            for (int i = 0; i < 4; i++){
                Random rand = new Random();
                int random = rand.nextInt(10);
                code += String.valueOf(random);
            }
            e.putString(SignUpPage.VERIFY_CODE_PREF, code);
            e.putString(SignUpPage.PHONENUMBER_PREF, phonenumber);
            e.apply();
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phonenumber, null, code, null, null);
            startActivity(new Intent(this, VerifyPage.class).putExtra(INFO_TO_VERIFY, true));
        }
        else {
            SharedPreferences.Editor e2 = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
            e2.putBoolean(VerifyPage.REGISTER_PREF, true);
            e2.apply();

            startActivity(new Intent(this, CreditCardInfoPage.class));
        }
    }
}
