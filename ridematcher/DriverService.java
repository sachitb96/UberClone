package com.example.appmodel.ridematcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class DriverService extends Service {

    public final static String paymentPref = "paymentPref";
    public final static String SERVICE_TO_HOME = "com.example.appmodel.ridematcher_SERVICE_TO_HOME";
    public final static String SERVICE_TO_HOME_PARAMS = "com.example.appmodel.ridematcher_SERVICE_TO_HOME_PARAMS";

    public final static String PAYMENT_NUMBER = "paymentNumberPref";


    ArrayList<String> driverPhones = new ArrayList<>();
    int counter = 0;
    DBHandler dbHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHandler = new DBHandler(this);
       driverPhones = intent.getStringArrayListExtra(HomeActivity.HOME_TO_SERVICE);
        final String phonenumber = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(SignUpPage.PHONENUMBER_PREF, "");

        DatabaseReference dr = FirebaseDatabase.getInstance().getReference().getRoot().child("User").child(phonenumber);
        dr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("Request") && dataSnapshot.getValue() != null){
                    String temp = dataSnapshot.getValue().toString();
                    final ArrayList<String> value = new ArrayList<String>(Arrays.asList(temp.split(",")));
                    if (value.get(0).equals("Declined")){

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().getRoot().child("Drivers").child(driverPhones.get(counter));
                        ref.child(phonenumber).removeValue();
                        counter += 1;
                        if (counter >= driverPhones.size()){
                            stopService(new Intent(getApplicationContext(), DriverService.class));
                            return;
                        }
                        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().getRoot().child("Drivers").child(driverPhones.get(counter));
                        String from = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(HomeActivity.FROM_PREF, "");
                        String to = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(HomeActivity.ADDRESS_PREF, "");
                        String fare = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(HomeActivity.FARE_PREF, "");
                        Map<String, Object> map = new HashMap<>();
                        map.put(phonenumber, "");
                        ref2.updateChildren(map);
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("From", from);
                        map2.put("To", to);
                        map2.put("Price", fare);
                        ref2.child(phonenumber).updateChildren(map2);
                    }
                    else {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("DriverPhoto").child(value.get(2) + ".png");
                        storageReference.getBytes(1048576).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                                value.set(value.size() - 1, encoded);
                               sendNotification(value);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                               Toast.makeText(DriverService.this, "Failed Loading Image...", Toast.LENGTH_SHORT).show();
                                sendNotification(value);
                            }
                        });

                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return START_STICKY;
    }

    private void sendBroadcastParams(ArrayList<String> params){
        Intent intent = new Intent();
        intent.setAction(SERVICE_TO_HOME);
        intent.putExtra(SERVICE_TO_HOME_PARAMS, params);
        sendBroadcast(intent);
    }

    private void sendNotification(ArrayList<String> value){
        String status = value.get(0);
        Intent intent = new Intent(this, HomeActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentTitle("Driver: " + status).setContentText("Driver: " + value.get(1));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        builder.setContentIntent(pendingIntent);
        NotificationManager m = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        m.notify(0, builder.build());

        if (status.equals("Accepted")){
            value.remove(0);
            dbHandler.insertIntoRides(DBHandler.CURRENT_RIDES_TABLE, value );
            sendBroadcastParams(value);
        }
        if (status.equals("Delivered")){
            int num = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getInt(PAYMENT_NUMBER, 0);
            ArrayList<Pair<String, String>> credititems = dbHandler.getAllPayments(DBHandler.CREDIT_CARD_TABLE, "credit", DBHandler.CREDIT_CARD_NUMBER);
            ArrayList<Pair<String, String>> paypalitems = dbHandler.getAllPayments(DBHandler.PAYPAL_TABLE, "paypal", DBHandler.PAYPAL_USERNAME);
            credititems.addAll(paypalitems);
            if (credititems.get(num).first.equals("credit"))
                createStripeCharge(num);
            else
                createPayPalCharge();
        }


    }

    private void createPayPalCharge(){
        String meta = PayPalConfiguration.getClientMetadataId(this);
        String refresh = "";
        new PayPalCharge().execute("10", meta, refresh );
    }

    private void createStripeCharge(int num){
        ArrayList<String> item = dbHandler.getCardDetails(num + 1);

        Card card = new Card(item.get(0), Integer.parseInt(item.get(1)), Integer.parseInt(item.get(2)), item.get(3));
        Stripe stripe = new Stripe(this, getString(R.string.stripe_api));
        stripe.createToken(card, new TokenCallback() {
            @Override
            public void onError(Exception error) {

            }

            @Override
            public void onSuccess(Token token) {
                String tokenId = token.getId();
                String amount = "10", driverId = "5", fee = "5";
                new StripeCharge().execute(amount, tokenId, driverId, fee);
            }
        });

    }

    class PayPalCharge extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            String amount = params[0].replace(" ", "+");
            String meta = params[1].replace(" ", "+");
            String refresh = params[2].replace(" ", "+");

            String urlString = "https://IP_ADDRESS/paypal_charge.php?amount=" + amount + "&meta=" + meta + "&refresh=" + refresh;
            try {
                URL url = new URL(urlString);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                huc.setRequestMethod("POST");
                huc.setDoOutput(true);
                huc.connect();

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    class StripeCharge extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            String amount = params[0].replace(" ", "+");
            String tokenId = params[1].replace(" ", "+");
            String driverId = params[2].replace(" ", "+");
            String fee = params[3].replace(" ", "+");
            String urlString = "https://IP_ADDRESS/sample_php.php?amount=" + amount + "&tok=" + tokenId + "&fee=" +
                    fee + "&id=" + driverId;
            try {
                URL url = new URL(urlString);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                huc.setRequestMethod("POST");
                huc.setDoOutput(true);
                huc.connect();

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
