package com.example.appmodel.ridematcher;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends BaseActivity {

    private String mCallId;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;

    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        mCallDuration = (TextView)findViewById(R.id.callDuration);
        mCallState = (TextView)findViewById(R.id.callState);
        mCallerName = (TextView)findViewById(R.id.remoteUser);
        Button endCallButton = (Button)findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
    }

    @Override
    public void onResume(){
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    @Override
    public void onPause(){
        super.onPause();
        mTimer.cancel();
        mDurationTask.cancel();
    }
    @Override
    public void onServiceConnected(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            call.addCallListener(new SinchCallListener());
            mCallerName.setText("");
            mCallState.setText(call.getState().toString());
            DBHandler db = new DBHandler(getApplicationContext());
            String photo = db.retrieveList(DBHandler.CURRENT_RIDES_TABLE).get(0).get(4);
            Bitmap bitmap = DriverAcceptedFragment.stringToBitmap(photo);
            ImageView userImage = (ImageView)findViewById(R.id.callerImage);
            bitmap = Bitmap.createScaledBitmap(bitmap, userImage.getWidth(), userImage.getHeight(), false);
            userImage.setImageBitmap(bitmap);
        }
        else {
            finish();
        }
    }


    private void endCall(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds){
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration(){
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null){
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

   private class SinchCallListener implements CallListener {

       @Override
       public void onCallProgressing(Call call) {

       }

       @Override
       public void onCallEstablished(Call call) {
            mCallState.setText(call.getState().toString());
           setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
       }

       @Override
       public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
           String endMsg = "Call ended: " + call.getDetails().toString();
           Toast.makeText(CallActivity.this, endMsg, Toast.LENGTH_SHORT).show();
           endCall();
       }

       @Override
       public void onShouldSendPushNotification(Call call, List<PushPair> list) {

       }
   }
}
