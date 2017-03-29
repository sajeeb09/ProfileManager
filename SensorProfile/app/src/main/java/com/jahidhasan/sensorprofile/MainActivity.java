package com.jahidhasan.sensorprofile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btnStart,btnStop;
    boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        isBound = false;
    }

    public void startProfileService(View v){
        Intent service = new Intent(this, ProfileService.class);
        startService(service);
        doBindService();
    }

    public void stopProfileService(View v){
        doUnbindService();
        Intent service = new Intent(this, ProfileService.class);
        stopService(service);
    }

    private ProfileService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((ProfileService.ProfileBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,ProfileService.class), mConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService(){
        if(isBound){
            unbindService(mConnection);
            isBound=false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
