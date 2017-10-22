package com.example.appmodel.ridematcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class VerifyPage extends AppCompatActivity {

    public final static String REGISTER_PREF = "registerPref";
    boolean fromSocial = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_page);
        Intent intent = getIntent();
        fromSocial = intent.getBooleanExtra(InformationPage.INFO_TO_VERIFY, false);
    }

    public void verifyCode(View view){
        String code = ((EditText)findViewById(R.id.VerifyCodeField)).getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE);
        String phoneCode = sharedPreferences.getString(SignUpPage.VERIFY_CODE_PREF, "");
        if (code.equals(phoneCode)){
            if (!fromSocial){
                startActivity(new Intent(this, InformationPage.class));
            }
            else {
                SharedPreferences.Editor e  = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
                e.putBoolean(REGISTER_PREF, true);
                e.apply();
                startActivity(new Intent(this, CreditCardInfoPage.class));
            }
        }
    }

    public void backToSignUp(View view){
        super.onBackPressed();
    }
}
