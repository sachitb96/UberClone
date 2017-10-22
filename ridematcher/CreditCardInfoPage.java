package com.example.appmodel.ridematcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CreditCardInfoPage extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_info_page);
    }

    public void skipSetup(View view){
        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
        Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void saveCard(View view){
        String number = ((EditText)findViewById(R.id.InfoCardNumberField)).getText().toString();
        String month = ((EditText)findViewById(R.id.InfoCardMonthField)).getText().toString();
        String year = ((EditText)findViewById(R.id.InfoCardYearField)).getText().toString();
        String cvc = ((EditText)findViewById(R.id.InfoCVCField)).getText().toString();
       // DBHandler db = new DBHandler(getApplicationContext());
        String[] str = {number, month, year, cvc};
        //db.insertIntoTablePayment(new ArrayList<>(Arrays.asList(str)), DBHandler.CREDIT_TABLE);


        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
