package com.example.appmodel.ridematcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private SinchService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (SinchService.class.getName().equals(name.getClassName())){
            mSinchServiceInterface = (SinchService.SinchServiceInterface) service;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (SinchService.class.getName().equals(name.getClassName())){
            mSinchServiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected(){

    }

    protected void onServiceDisconnected(){

    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface(){
        return mSinchServiceInterface;
    }
}
