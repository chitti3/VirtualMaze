package com.example.whetherapp;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.whetherapp.Fragment.MainFragment;
import com.example.whetherapp.Model.WhetherModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static com.example.whetherapp.App.CHANNEL_ID;

public class ForeGroundService extends Service {



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String place = intent.getStringExtra("place");
        String max = intent.getStringExtra("max");
        long timmer = intent.getLongExtra("timer",1);

        Intent notificationIntent = new Intent(this, MainFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(max)
                .setContentText(place)
                .setSmallIcon(R.drawable.whether)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();

        new CountDownTimer(timmer, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                getLocation();


            }

        }.start();


        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void getLocation() {
        LocationTrack locationTrack = new LocationTrack(MainFragment.activity);
        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();


            ApiCalling(longitude,latitude);

        } else {

            locationTrack.showSettingsAlert();
        }
    }

    private void ApiCalling(double longitude, double latitude) {

        RequestQueue requestQueue = Volley.newRequestQueue(MainFragment.activity);


        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=7c8f8e54c8a9cc834cacad3f9f270295";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Gson gson = new GsonBuilder().serializeNulls().create();
                WhetherModel whetherModel = gson.fromJson(response.toString(), WhetherModel.class);


                UpdateNotification(whetherModel);



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });
        requestQueue.add(stringRequest);
    }

    private void UpdateNotification(WhetherModel whetherModel) {

        Calendar now = Calendar.getInstance();
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

        long difference = nextHour.getTimeInMillis() - now.getTimeInMillis();
        Intent serviceIntent = new Intent(MainFragment.activity, ForeGroundService.class);
        serviceIntent.putExtra("place", whetherModel.name);
        serviceIntent.putExtra("max", whetherModel.main.temp);
        serviceIntent.putExtra("timer", difference);

        ContextCompat.startForegroundService(MainFragment.activity, serviceIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
