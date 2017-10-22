package com.example.appmodel.ridematcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class ItemAdapter extends ArrayAdapter<ArrayList<String>> {

    ArrayList<ArrayList<String>> items;

    public ItemAdapter(@NonNull Context context, ArrayList<ArrayList<String>> items) {
        super(context, R.layout.trip_layout, items);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_layout, parent, false);
        }
        ArrayList<String> item = getItem(position);
        if (item != null) {
            ((TextView) convertView.findViewById(R.id.DriverName)).setText(item.get(0));
            ((TextView) convertView.findViewById(R.id.DriverPhone)).setText(item.get(1));
            ((TextView) convertView.findViewById(R.id.DriverFare)).setText(item.get(2));
            byte[] arr = Base64.decode(item.get(3), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            ((ImageView)convertView.findViewById(R.id.DriverImage)).setImageBitmap(bitmap);
        }
        return convertView;
    }
}
