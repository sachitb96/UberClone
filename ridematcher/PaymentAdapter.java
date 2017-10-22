package com.example.appmodel.ridematcher;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PaymentAdapter extends BaseAdapter {

    ArrayList<Pair<String, String>> items;
    LayoutInflater layoutInflater;

    public PaymentAdapter(Context context) {
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(ArrayList<Pair<String, String>> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            if (items.get(position).first.equals("credit")){
                convertView = layoutInflater.inflate(R.layout.credit_layout, parent, false);
            }
            else {
                convertView = layoutInflater.inflate(R.layout.paypal_layout, parent, false);
            }
        }
        if (items.get(position).first.equals("credit")){
            TextView credittext = (TextView)convertView.findViewById(R.id.creditLayoutNumber);
            credittext.setText(items.get(position).second);
        }
        else {
            TextView paypaltext = (TextView)convertView.findViewById(R.id.paypalLayoutName);
            paypaltext.setText(items.get(position).second);
        }

        return convertView;
    }
}
