package com.example.appmodel.ridematcher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class RequestFragment extends Fragment {

    public RequestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, null, false);
        TextView blackCarFare = (TextView)view.findViewById(R.id.blackfareField);
        TextView standardCarFare = (TextView)view.findViewById(R.id.standardfareField);
        TextView distance = (TextView)view.findViewById(R.id.distanceField);
        Bundle bundle = getArguments();
        blackCarFare.setText(bundle.getString(HomeActivity.HOME_TO_REQUEST_BLACK));
        standardCarFare.setText(bundle.getString(HomeActivity.HOME_TO_REQUEST_FARE));
        distance.setText(bundle.getString(HomeActivity.HOME_TO_REQUEST));
        return view;
    }
}
