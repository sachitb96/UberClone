package com.example.appmodel.ridematcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalService;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {

    public final static String REFRESH_TOKEN = "refreshToken";
    public final static String CLIENT_ID = "ASrNAVxoF9Dbq57rwrXJutnSwAWn1QVBrqsVKdtKhA8tprcuw7OSFhvfzY_Lnvn8ejVXoLkoslJKoxQ0";
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(CLIENT_ID)
            .merchantName("Ridematcher");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ListView paymentList = (ListView)findViewById(R.id.paymentList);
        DBHandler db = new DBHandler(getApplicationContext());
        ArrayList<Pair<String, String>> credititems = db.getAllPayments(DBHandler.CREDIT_CARD_TABLE, DBHandler.CREDIT_CARD_NUMBER, "credit");
        ArrayList<Pair<String, String>> paypalitems = db.getAllPayments(DBHandler.PAYPAL_TABLE, DBHandler.PAYPAL_USERNAME, "paypal");
        credititems.addAll(paypalitems);
        PaymentAdapter paymentAdapter = new PaymentAdapter(getApplicationContext());
        paymentAdapter.setItems(credititems);
        paymentList.setAdapter(paymentAdapter);

    }

    public void addCreditCardInfo(View view){
        startActivity(new Intent(this, CreditCardInfoPage.class));
    }

    public void addPaypalAccount(View view){
        Intent intent = new Intent(PaymentActivity.this, PayPalFuturePaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startActivityForResult(intent, 1);

    }

    public void sendAuthorizationToServer(String auth){
        class SendAuth extends AsyncTask<String, String, String>{
            @Override
            protected String doInBackground(String... params) {
                String auth = params[0];
                String urlString = "https://IPADDRESS/refreshToken.php?auth=" + auth;
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                    huc.setDoOutput(true);
                    huc.connect();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                    String json = "";
                    while ((json = br.readLine()) != null){
                        sb.append(json);
                    }
                    return sb.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                SharedPreferences.Editor sp = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
                sp.putString(REFRESH_TOKEN, s);
                sp.apply();
            }
        }
        SendAuth sendAuth = new SendAuth();
        sendAuth.execute(auth);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            PayPalAuthorization auth = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
            if (auth != null){
                try {
                    String authorization = auth.getAuthorizationCode();
                    sendAuthorizationToServer(authorization);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
