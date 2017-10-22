package com.example.appmodel.ridematcher;

import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class IncomingCallScreenActivity extends BaseActivity {

    private String mCallId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming);
        Button answer = (Button)findViewById(R.id.answerButton);
        Button decline = (Button)findViewById(R.id.declineButton);

        answer.setOnClickListener(mClickListener);
        decline.setOnClickListener(mClickListener);

        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
    }

    @Override
    protected void onServiceConnected(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            call.addCallListener(new SinchCallListener());
            TextView remoteUser = (TextView)findViewById(R.id.remoteUser);
            remoteUser.setText(call.getRemoteUserId());
        }
        else {
            finish();
        }
    }

    private void answerClicked(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            try {
                call.answer();
                Intent intent = new Intent(this, CallActivity.class);
                intent.putExtra(SinchService.CALL_ID, mCallId);
                startActivity(intent);
            }catch(MissingPermissionException e){
                ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }

        }
        else {
            finish();
        }

    }

    private void declineClicked(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            call.hangup();
        }
        finish();
    }
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.answerButton :
                    answerClicked();
                    break;
                case R.id.declineButton :
                    declineClicked();
                    break;
            }
        }
    };

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {

        }

        @Override
        public void onCallEstablished(Call call) {

        }

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Toast.makeText(IncomingCallScreenActivity.this, cause.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }
}
