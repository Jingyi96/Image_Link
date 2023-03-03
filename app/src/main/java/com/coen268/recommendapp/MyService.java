package com.coen268.recommendapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class MyService extends Service {
    class MyLocalBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }
    private Binder myBinder = new MyLocalBinder();

    private static final String TAG = "MyService";
    private boolean randomRunFlag;
    private Thread t;
    private int rNum;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "On Bind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "On Unbind");
        return super.onUnbind(intent);
    }

    public void generateRandomNumber() throws InterruptedException {
        while (randomRunFlag) {
            rNum = new Random().nextInt(1000);
            Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
            Log.i(TAG, "Random number: " + rNum);
            Thread.sleep(1000);
        }
    }

    public int getRandomNumber() {
        return rNum;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "On Start Service");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId() + " Start ID: " + startId);
        Runnable runnable = () -> {
            //ToDo: set the boolean variable to True
            randomRunFlag = true;
            try {
                generateRandomNumber();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        t= new Thread(runnable);
        t.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "On Destroy");
        randomRunFlag = false;
        t = null;
        super.onDestroy();
    }
}