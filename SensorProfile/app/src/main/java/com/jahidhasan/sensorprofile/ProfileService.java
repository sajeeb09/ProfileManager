package com.jahidhasan.sensorprofile;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 09 on 12/29/2016.
 */

public class ProfileService extends Service {

    SensorData mSensorData;

    @Override
    public void onCreate() {
        super.onCreate();
        showToast("Service Created");
    }

    public class ProfileBinder extends Binder {
        ProfileService getService() {
            return ProfileService.this;
        }
    }

    public void showToast(String toast){
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private final IBinder mBinder = new ProfileBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showToast("Service Started");
        mSensorData = new SensorData();
        mSensorData.initiate();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorData.unregister();
        showToast("Service Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void finishService(){
        stopSelf();
    }










    //Sensor Event Manager Class.
    public class SensorData implements SensorEventListener{
        private AudioManager mAudioManager;
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        private Sensor mLight;
        private Sensor mProximity;
        private boolean isFaceUp, isShaking, isFaceDown, isObstacle, isHome, isPocket, isSilent;
        private float x, y, z, lastPos;


        public void initiate(){
            isFaceUp = false; isShaking = false; isFaceDown = false; isObstacle = false; isHome = false; isPocket = false; isSilent = false;

            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            lastPos = 0;

            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onSensorChanged(SensorEvent event) throws NullPointerException {
            Sensor sensor = event.sensor;
            int th;
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                isShaking = false;
                x = event.values[0]; y = event.values[1]; z = event.values[2];

                writeLog("X", x+"");
                writeLog("Z", z+"");

                if(isFaceDown) th = 15;
                else th = 2;

                float curPos = Math.abs(x + y + z);

                if(lastPos != 0 && Math.abs(curPos - lastPos) > th){
                    isShaking = true;
                }
                else {
                    isShaking = false;
                }

                lastPos = curPos;

                if(!isShaking) {
                    if (z < 0) {
                        //Face Up.
                        isFaceDown = true;
                        isFaceUp = false;
                    } else {
                        //Face Down.
                        isFaceUp = true;
                        isFaceDown = false;
                    }
                }
                //showToast("ACCELEROMETER");
                mSensorManager.unregisterListener(this);
                mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
            }
            else if(sensor.getType() == Sensor.TYPE_PROXIMITY){
                if(event.values[0] == 0){
                    isObstacle = true;
                }
                else {
                    isObstacle = false;
                }
                //showToast("PROXIMITY");
                setProfile();
                mSensorManager.unregisterListener(this);
                //mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        /*else if (sensor.getType() == Sensor.TYPE_LIGHT){
            if(event.values[0] == 0){
                isFaceDown = false;
            }
            else {
                isFaceDown = true;
            }
            setProfile();
            //showToast("LIGHT");
            mSensorManager.unregisterListener(this);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }*/
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void homeProfile(){
            //Ringer LOUD, NO VIBRATION.
            if(!isHome) {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mAudioManager.setStreamVolume(mAudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_RING), 0);
                writeLog("Ringer mode", "NORMAL");
                isHome = true;
                isPocket = false;
                isSilent = false;
            }
        }

        public void pocketProfile(){
            //VIBRATION, ringer MEDIUM.
            if(!isPocket) {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                mAudioManager.setStreamVolume(mAudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_RING) / 2, 0);
                writeLog("Ringer mode", "POCKET");
                isPocket = true;
                isHome = false;
                isSilent = false;
            }
        }

        public void silentProfile(){
            //ONLY Vibration.
            if(!isSilent) {
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                writeLog("Ringer mode", "VIBRATION");
                isSilent = true;
                isHome = false;
                isPocket = false;
            }
        }

        public void showToast(String toast){
            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
        }

        public void setProfile(){
            if(isFaceUp && !isObstacle){
                homeProfile();
            }
            else if(isFaceDown && !isShaking && isObstacle){
                silentProfile();
            }
            else if(isShaking && isObstacle){
                pocketProfile();
            }
        }

        public void unregister(){
            mSensorManager.unregisterListener(this);
        }

        public void writeLog(String TAG, String log){
            Log.d(TAG, log);
        }
    }

}
