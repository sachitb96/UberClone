package com.example.appmodel.ridematcher;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.calling.Call;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public final static String HOME_TO_REQUEST = "com.example.appmodel.ridematcher_HOME_TO_REQUEST";
    public final static String HOME_TO_REQUEST_FARE = "com.example.appmodel.ridematcher_HOME_TO_REQUEST_FARE";
    public final static String HOME_TO_REQUEST_BLACK = "com.example.appmodel.ridematcher_HOME_TO_REQUEST_BLACK";
    public final static String HOME_TO_ACCEPTED = "com.example.appmodel.ridematcher_HOME_TO_ACCEPTED";
    public final static String HOME_TO_SERVICE = "com.example.appmodel.ridematcher_HOME_TO_SERVICE";
    public final static String CURRENT_PAYMENT_PREF = "currentPaymentMethodPref";

    public final static String FROM_PREF = "fromPref";
    public final static String ADDRESS_PREF = "addressPref";
    public final static String FARE_PREF = "farePref";
    GoogleMap mMap;
    private Call call;
    DriverMovement dm;
    public static double radius = 5000;
    Fragment currentFragment, requestFragment;
    ArrayList<Marker> markers = new ArrayList<>();
    Marker yourMarker;
    SinchService.SinchServiceInterface mSinchServiceInterface;
    ArrayList<Polyline> mapPolyline = new ArrayList<>();
    ArrayList<DatabaseReference> refs = new ArrayList<>();
    boolean requestOpen = false, requestStandard = false, requestBlack = false, locationOff = false;
    double temp1, temp2;
    String from = "", address = "", distance, fare = "$10.00", blackfare = "$20.00";
    ArrayList<String> driverPhones = new ArrayList<>();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> params = intent.getStringArrayListExtra(DriverService.SERVICE_TO_HOME_PARAMS);
            setListFragment(params);
            unregisterReceiver(broadcastReceiver);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 5000);
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 5000);
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 5000);
        }

    }

    public void requestStandard(View view){
        requestStandard = true;
        requestBlack = false;
        (findViewById(R.id.standardCarImage)).setBackgroundColor(Color.GREEN);
        (findViewById(R.id.blackCarImage)).setBackgroundColor(0);
    }

    public void requestBlack(View view){
        requestStandard = false;
        requestBlack = true;
        (findViewById(R.id.standardCarImage)).setBackgroundColor(0);
        (findViewById(R.id.blackCarImage)).setBackgroundColor(Color.GREEN);
    }

    public void requestRideHelper(){
        String carType = "", tempfare = "";

        if (requestStandard){
            carType = "Standard";
            tempfare = fare;
        }
        else {
            carType = "Black";
            tempfare = blackfare;
        }
        final String carTypeFinal = carType;
        final String tempfareFinal = tempfare;
        refs.get(0).child("Car Type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals(carTypeFinal)){
                    Map<String, Object> map = new HashMap<>();
                    map.put(getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(SignUpPage.PHONENUMBER_PREF, ""), "");
                    refs.get(0).updateChildren(map);
                    Map<String, Object> map2 = new HashMap<>();
                    SharedPreferences.Editor e = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
                    map2.put("From", from);
                    map2.put("To", address);
                    map2.put("Price", tempfareFinal);

                    e.putString(FROM_PREF, from);
                    e.putString(ADDRESS_PREF, address);
                    e.putString(FARE_PREF, tempfareFinal);
                    e.apply();
                    map2.put("Name", getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(InformationPage.FirstPref, ""));
                    refs.get(0).child(getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).getString(SignUpPage.PHONENUMBER_PREF, ""))
                            .updateChildren(map2);
                    getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.RequestFrame)).commit();
                    (findViewById(R.id.RequestFrame)).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        registerBroadcast();
        startService(new Intent(getBaseContext(), DriverService.class).putExtra(HOME_TO_SERVICE, driverPhones));
    }

    public void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DriverService.SERVICE_TO_HOME);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void setListFragment(ArrayList<String> items){
        (findViewById(R.id.placesFrame)).setVisibility(View.GONE);
        (findViewById(R.id.RequestFrame)).setVisibility(View.VISIBLE);
        Fragment driverFragment = new DriverAcceptedFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(HOME_TO_ACCEPTED, items);
        driverFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.RequestFrame, driverFragment);
        ft.commit();
    }



    private void activateSearchBar(){
            PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.placesFrame);
            placeAutocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(temp1 - 0.1, temp2 - 0.1), new LatLng(temp1 + 0.1, temp2 + 0.1)));
            placeAutocompleteFragment.setFilter(new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT).build());
            placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    while (mapPolyline.size() > 0) {
                        mapPolyline.get(0).remove();
                        mapPolyline.remove(0);
                    }
                    address = place.getAddress().toString();
                    RetrieveDirections rd = new RetrieveDirections();
                    rd.execute(from, address);

                }

                @Override
                public void onError(Status status) {

                }
            });
        }

    public void requestRide(View view){
        if (!requestBlack && !requestStandard){
            Toast.makeText(getApplicationContext(), "Select Car Type", Toast.LENGTH_LONG).show();
            return;
        }

        PaymentAdapter paymentAdapter = new PaymentAdapter(getApplicationContext());
        DBHandler db = new DBHandler(getApplicationContext());
        ArrayList<Pair<String, String>> credititems = db.getAllPayments(DBHandler.CREDIT_CARD_TABLE, DBHandler.CREDIT_CARD_NUMBER, "credit");
        ArrayList<Pair<String, String>> paypalitems = db.getAllPayments(DBHandler.PAYPAL_TABLE, DBHandler.PAYPAL_USERNAME, "paypal");
        credititems.addAll(paypalitems);

        paymentAdapter.setItems(credititems);

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this).setTitle("Select Payment Method")
                .setAdapter(paymentAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor e = getSharedPreferences(SignUpPage.USER_DATA_PREF, MODE_PRIVATE).edit();
                        e.putInt(CURRENT_PAYMENT_PREF, which);
                        e.apply();
                        requestRideHelper();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isWithinRadius(double lat, double lng){
        float[] arr = {0,0,0};
        Location.distanceBetween(temp1, temp2, lat, lng, arr);
        return arr[0] < radius;
    }
    public void getDriversFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("Driver");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                refs.clear();
                markers.clear();
                for (DataSnapshot ds : snapshots) {
                    DatabaseReference ref = ds.getRef();
                    refs.add(ref);
                    driverPhones.add(ref.getKey());
                }
                for (int i = 0; i < refs.size(); i++) {
                    final int counter = i;
                    refs.get(i).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.getKey().equals("Location") && dataSnapshot.getValue() != null) {
                                String location = dataSnapshot.getValue().toString();
                                String[] coords = location.split(",");
                                double lat = Double.parseDouble(coords[0]);
                                double longitude = Double.parseDouble(coords[1]);
                                if (isWithinRadius(lat, longitude)) {
                                    LatLng latLng = new LatLng(lat, longitude);
                                    Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.car_map_icon);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, 60, 35, false);
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(
                                            BitmapDescriptorFactory.fromBitmap(bitmap)
                                    ));
                                    markers.add(marker);
                                }
                            }
                        }
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.getKey().equals("Location") && dataSnapshot.getValue() != null) {
                                String location = dataSnapshot.getValue().toString();
                                String[] coords = location.split(",");
                                double lat = Double.parseDouble(coords[0]);
                                double longitude = Double.parseDouble(coords[1]);
                                LatLng latLng = new LatLng(lat, longitude);
                                if (isWithinRadius(lat, longitude)){
                                    if (markers.size() - 1 >= counter)
                                        markers.get(counter).remove();
                                    Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.car_map_icon);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, 60, 35, false);
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                    markers.set(counter, marker);
                                }
                            }
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
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void driverMovement(){
        for (int i = 0; i < refs.size(); i++){
            final int counter = i;
            refs.get(i).child("Location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                     String locationStr = dataSnapshot.getValue().toString();
                    String latString = locationStr.substring(0, locationStr.indexOf(","));
                    String longString = locationStr.substring(locationStr.indexOf(",") + 1);
                    LatLng location = new LatLng(Double.parseDouble(latString), Double.parseDouble(longString));
                    markers.get(counter).remove();
                    Marker temp = mMap.addMarker(new MarkerOptions().position(location).title("Car icon"));
                    markers.set(counter, temp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onServiceConnected(){

    }

    public void startCall(View view){
        DBHandler dbHandler = new DBHandler(this);
        String phoneNumber = dbHandler.retrieveList(DBHandler.CURRENT_RIDES_TABLE).get(0).get(1);
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(phoneNumber);
        }
        Call call = getSinchServiceInterface().callUser(phoneNumber);
        if (call != null){
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(SinchService.CALL_ID, call.getCallId());
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (requestOpen){
            requestOpen = false;
            address = "";
            (findViewById(R.id.RequestFrame)).setVisibility(View.GONE);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(requestFragment);
            ft.commit();
            fm.popBackStack();
            while (mapPolyline.size() > 0) {
                mapPolyline.get(0).remove();
                mapPolyline.remove(0);
            }

        }
        else if (locationOff){
            (findViewById(R.id.HomeFrame)).setVisibility(View.GONE);
            locationOff = false;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(currentFragment);
            ft.commit();
            fm.popBackStack();
            super.onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_payment) {
            startActivity(new Intent(getApplicationContext(), PaymentActivity.class));
        } else if (id == R.id.nav_rides) {
            startActivity(new Intent(getApplicationContext(), YourRidesActivity.class));
        } else if (id == R.id.nav_trips) {
            startActivity(new Intent(getApplicationContext(), TripsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        } else if (id == R.id.nav_legal) {
            startActivity(new Intent(getApplicationContext(), LegalActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (isLocationEnabled(getApplicationContext())) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 9000);
                return;
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 30, locationListener);
                DBHandler dbHandler = new DBHandler(getApplicationContext());
                if (dbHandler.retrieveList(DBHandler.CURRENT_RIDES_TABLE).size() > 0){

                    setListFragment(dbHandler.retrieveList(DBHandler.CURRENT_RIDES_TABLE).get(0));
                }

            }
        }
        else {
            (findViewById(R.id.HomeFrame)).setVisibility(View.VISIBLE);
            (findViewById(R.id.placesFrame)).setVisibility(View.GONE);
            currentFragment = new LocationError();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.HomeFrame, currentFragment).commit();
            locationOff = true;
        }

    }



    public void openRequestFrame(){
        (findViewById(R.id.RequestFrame)).setVisibility(View.VISIBLE);
        RequestFragment requestFragment = new RequestFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HOME_TO_REQUEST_FARE, fare);
        bundle.putString(HOME_TO_REQUEST_BLACK, blackfare);
        bundle.putString(HOME_TO_REQUEST, distance);
        requestFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.RequestFrame, requestFragment);
        ft.commit();
    }


    public boolean isLocationEnabled(Context context){
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            }catch(Settings.SettingNotFoundException e){
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            temp1 = 29.93362574032774;
            temp2 = -95.20403156888471;
            activateSearchBar();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(temp1, temp2, 1);
                from = addresses.get(0).getAddressLine(0);
            }catch (Exception e){
                e.printStackTrace();
            }
            LatLng current = new LatLng(temp1, temp2);
            Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.current_loc_blue);
            bitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, false);
            if (yourMarker != null){
                yourMarker.remove();
            }
            yourMarker = mMap.addMarker(new MarkerOptions().position(current).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            getDriversFirebase();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    class RetrieveDirections extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            String from = params[0].replace(" ", "+");
            String to = params[1].replace(" ", "+");
            String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + from + "&destination=" + to
                    + "&key=" + getString(R.string.google_directions);
            try {
                URL url = new URL(urlString);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                huc.setDoOutput(true);
                huc.setRequestMethod("POST");
                huc.connect();
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                String json;
                while ((json = br.readLine()) != null){
                    stringBuilder.append(json + "\n");
                }
                return stringBuilder.toString();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Parsertask parsertask = new Parsertask();
            parsertask.execute(s);
        }
    }


    private class Parsertask extends AsyncTask<String, Integer, List<LatLng>>{

        @Override
        protected List<LatLng> doInBackground(String... params) {
            JSONObject jsonObject;
            List<LatLng> routes = null;
            try {
                jsonObject = new JSONObject(params[0]);
                DataParser parser = new DataParser();
                routes = parser.parse(jsonObject);
                distance = parser.distance;
                return routes;
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatLng> list) {
            super.onPostExecute(list);
            for (int i = 0; i < list.size(); i++){
                PolylineOptions polylineOptions= new PolylineOptions().addAll(list)
                        .width(10).color(Color.BLUE);
                mapPolyline.add(mMap.addPolyline(polylineOptions));
            }
            openRequestFrame();
        }
    }

   public class DriverMovement extends AsyncTask<Void, Void, Void> {
        double lat = 0, longitude = 0;

       public DriverMovement() {
       }

       @Override
       protected Void doInBackground(Void... params) {
           refs.get(0).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot ds) {
                   Iterable<DataSnapshot> dataSnapshots = ds.getChildren();
                   for (DataSnapshot dataSnapshot : dataSnapshots) {
                       if (dataSnapshot.getKey().equals("Location") && dataSnapshot.getValue() != null) {
                           String location = dataSnapshot.getValue().toString();
                           String[] coords = location.split(",");
                           lat = Double.parseDouble(coords[0]);
                           longitude = Double.parseDouble(coords[1]);

                       }
                   }

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
           while (longitude == 0 || longitude < -95.210) {
               try {
                   Thread.sleep(5000);
                   if (lat != 0) {
                       Map<String, Object> map = new HashMap<>();
                       map.put("Location", String.valueOf(lat) + "," + String.valueOf(longitude + 0.001));
                       longitude += 0.001;

                       refs.get(0).updateChildren(map);
                   }
               } catch (Exception e) {
               }
           }
           return null;
       }
   }
}
