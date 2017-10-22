package com.example.appmodel.ridematcher;

import android.*;
        import android.Manifest;
        import android.app.ProgressDialog;
        import android.content.ComponentName;
        import android.content.Intent;

        import android.content.ServiceConnection;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.os.IBinder;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.telephony.SmsManager;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.sinch.android.rtc.SinchError;

        import java.util.Random;


public class SignUpPage extends BaseActivity implements SinchService.StartFailedListener {

    boolean locationEnabled = true, smsEnabled = true, readsms = true;

    public final static String USER_DATA_PREF = "userDataPref";
    public final static String VERIFY_CODE_PREF = "verifyCodePref";
    public final static String PHONENUMBER_PREF = "phoneNumberPref";
    ProgressDialog mSpinner;
    static final int SMS_REQUEST = 500, LOCATION_REQUEST = 400, READ_SMS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        if (ContextCompat.checkSelfPermission(SignUpPage.this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED){
            smsEnabled = false;
            ActivityCompat.requestPermissions(SignUpPage.this, new String[]{Manifest.permission.READ_SMS}, SMS_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(SignUpPage.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            locationEnabled = false;
            ActivityCompat.requestPermissions(SignUpPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(SignUpPage.this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED){
            readsms = false;
            ActivityCompat.requestPermissions(SignUpPage.this, new String[]{Manifest.permission.SEND_SMS},
                    READ_SMS);
        }

    }

    public void finishSetup(View view){
        if (!locationEnabled || !smsEnabled || !readsms)
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
        else {
            String phone = ((EditText)findViewById(R.id.SignUpPhoneField)).getText().toString();
            if (phone.equals("")){
                Toast.makeText(this, "No phone number entered", Toast.LENGTH_SHORT).show();
            }
            else {
                String code = "";
                for (int i = 0; i < 4; i++){
                    Random rand = new Random();
                    int random = rand.nextInt(10);
                    code += String.valueOf(random);
                }

                SharedPreferences sharedPreferences = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putString(VERIFY_CODE_PREF, code);
                e.putString(PHONENUMBER_PREF, phone);
                e.apply();
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, code, null, null);
                startActivity(new Intent(this, VerifyPage.class));
            }
        }
    }
    public void activateSocial(View view){
        if (!locationEnabled || !smsEnabled || !readsms)
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
        else
            startActivity(new Intent(getApplicationContext(), SocialSignUpPage.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case SMS_REQUEST :{
                smsEnabled = true;
            }
            case LOCATION_REQUEST :{
                locationEnabled = true;
            }
            case READ_SMS: {
                readsms = true;
            }
            default:
                break;
        }
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        if (mSpinner != null){
            mSpinner.dismiss();
        }
    }

    @Override
    protected void onPause(){
        if (mSpinner != null){
            mSpinner.dismiss();
        }
        super.onPause();
    }
    @Override
    public void onStarted() {
        mSpinner.dismiss();
        startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
        | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onServiceConnected(){
        getSinchServiceInterface().setStartListener(this);
        startClient();
    }
    private void startClient(){
        if (getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE).getBoolean(VerifyPage.REGISTER_PREF, false)) {
            if (!getSinchServiceInterface().isStarted()) {
                getSinchServiceInterface().startClient(getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE).getString(
                        PHONENUMBER_PREF, ""
                ));
                showSpinner();
            } else {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }

    private void showSpinner(){
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Loading...");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }
}
