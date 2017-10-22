package com.example.appmodel.ridematcher;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class DriverAcceptedFragment extends android.support.v4.app.Fragment {

    public DriverAcceptedFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        ArrayList<String> items = bundle.getStringArrayList(HomeActivity.HOME_TO_ACCEPTED);
        View root = inflater.inflate(R.layout.driver_accepted_layout, container, false);
        TextView drivername = (TextView)root.findViewById(R.id.driverAcceptName);
        if (items != null) {
            drivername.setText(items.get(0));
            TextView fareText = (TextView) root.findViewById(R.id.driverAcceptFare);
            fareText.setText(items.get(2));
            ImageView image = (ImageView)root.findViewById(R.id.driverAcceptPic);
            image.setImageBitmap(stringToBitmap(items.get(4)));
        }
        return root;
    }

    public static Bitmap stringToBitmap(String encoded){
        byte[] bytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
