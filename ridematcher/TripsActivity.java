package com.example.appmodel.ridematcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class TripsActivity extends AppCompatActivity {

    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        dbHandler = new DBHandler(getApplicationContext());
        ListView listView = (ListView)findViewById(R.id.CurrentTrips);
        ArrayList<ArrayList<String>> allItems = dbHandler.retrieveList(DBHandler.CURRENT_RIDES_TABLE);
        ItemAdapter itemAdapter = new ItemAdapter(getApplicationContext(), allItems);
        listView.setAdapter(itemAdapter);
    }
}
