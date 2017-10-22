package com.example.appmodel.ridematcher;

import android.app.Service;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class SinchService extends Service {

    private static final String APP_KEY = "6d5a8a03-ffef-450e-9f24-c5559cd92098";
    private static final String APP_SECRET = "JbumuQEemUW/f0j/1JmOZg==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    public final static String CALL_ID = "com.example.appmodel.ridematcher_CALL_ID";
    private SinchClient mSinchClient;
    private StartFailedListener mListener;
    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private String mUserId;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void start(String userName){
        if (mSinchClient == null){
            mUserId = userName;
            mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)
                        .applicationKey(APP_KEY).applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();

            mSinchClient.setSupportCalling(true);
            mSinchClient.startListeningOnActiveConnection();
            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.getCallClient().setRespectNativeCalls(false);
            mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListeenr());
            mSinchClient.start();
        }
    }

    private void stop(){
        if (mSinchClient != null){
            mSinchClient.terminate();
            mSinchClient = null;
        }
    }

    private boolean isStarted(){
        return (mSinchClient != null && mSinchClient.isStarted());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder  {

        public Call callPhoneNumber(String phoneNumber){
            return mSinchClient.getCallClient().callPhoneNumber(phoneNumber);
        }

        public Call callUser(String userId){
            if (mSinchClient == null){
                return null;
            }
            return mSinchClient.getCallClient().callUser(userId);
        }

        public String getUserName(){
            return mUserId;
        }

        public boolean isStarted(){
            return SinchService.this.isStarted();
        }

        public void startClient(String userName){
            start(userName);
        }

        public void setStartListener(StartFailedListener listener){
            mListener = listener;
        }

        public void stopClient(){
            stop();
        }

        public Call getCall(String callId){
            return mSinchClient.getCallClient().getCall(callId);
        }
    }

    public interface StartFailedListener {
        void onStartFailed(SinchError error);
        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientStarted(SinchClient sinchClient) {
            if (mListener != null){
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {

        }

        @Override
        public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
            if (mListener != null){
                mListener.onStartFailed(sinchError);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

        }

        @Override
        public void onLogMessage(int i, String s, String s1) {

        }
    }

    private class SinchCallClientListeenr implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Intent intent = new Intent(SinchService.this, HomeActivity.class);
            intent.putExtra(CALL_ID, call.getCallId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SinchService.this.startActivity(intent);
        }
    }
}
