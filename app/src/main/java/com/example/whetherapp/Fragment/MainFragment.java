package com.example.whetherapp.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.whetherapp.ForeGroundService;
import com.example.whetherapp.LocationTrack;
import com.example.whetherapp.Model.WhetherModel;
import com.example.whetherapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment{

    View  view;
    LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    TextView city,mx_temp,max_temp,base;
    LocationTrack locationTrack;
    WhetherModel whetherModel;
    public static Activity activity;
    long difference;


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        activity = getActivity();

        city = view.findViewById(R.id.city);
        mx_temp = view.findViewById(R.id.mx_temp);
        max_temp = view.findViewById(R.id.max_temp);
        base = view.findViewById(R.id.base);

       initial();



        return view;
    }

    private void initial() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {


            locationTrack = new LocationTrack(getActivity());

            if (locationTrack.canGetLocation()) {


                double longitude = locationTrack.getLongitude();
                double latitude = locationTrack.getLatitude();


                ApiCalling(longitude,latitude);

                Toast.makeText(getActivity(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
            } else {

                locationTrack.showSettingsAlert();
            }


        }
    }

    private void ApiCalling(double longitude, double latitude) {

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Loading...");
        pDialog.show();
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=7c8f8e54c8a9cc834cacad3f9f270295";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.hide();
                Gson gson = new GsonBuilder().serializeNulls().create();
                whetherModel = gson.fromJson(response.toString(), WhetherModel.class);


                SetupValue(whetherModel);



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.hide();

            }
        });
        requestQueue.add(stringRequest);
    }

    private void SetupValue(WhetherModel whetherModel) {
        city.setText(whetherModel.name);
        mx_temp.setText(whetherModel.main.temp);
        max_temp.setText(whetherModel.main.tempMax+" / "+whetherModel.main.tempMin);

        if(whetherModel.weather!=null && whetherModel.weather.size()>0)
            base.setText(whetherModel.weather.get(0).main);

        setTimmer();

        startService();

    }

    private void setTimmer() {
        Calendar now = Calendar.getInstance();
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

         difference = nextHour.getTimeInMillis() - now.getTimeInMillis();
    }

    public void startService() {
        Intent serviceIntent = new Intent(getActivity(), ForeGroundService.class);
        serviceIntent.putExtra("place", whetherModel.name);
        serviceIntent.putExtra("max", whetherModel.main.temp);
        serviceIntent.putExtra("timer", difference);

        ContextCompat.startForegroundService(getActivity(), serviceIntent);
    }


}