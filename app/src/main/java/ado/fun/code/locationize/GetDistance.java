package ado.fun.code.locationize;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by mahe on 24-02-2018.
 */

public class GetDistance extends Service implements LocationListener {

    SharedPreferences sp;
    SharedPreferences.Editor edit;
    String lat_main, long_main;
    double lat, lon;
    double dist;
    int previous_notification_interrupt_setting;
    int curBrightnessValue;
    LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Intent local = new Intent();
        local.setAction("ado.fun.code.locationize.on");
        this.sendBroadcast(local);
    }

    @Override
    public void onStart(Intent in, int startid) {
        sp = getSharedPreferences("coords", MODE_PRIVATE);
        edit = sp.edit();
        if (sp.contains("Lat") && sp.contains("Long")) {
            lat_main = sp.getString("Lat", "");
            long_main = sp.getString("Long", "");
            lat = Double.parseDouble(lat_main);
            lon = Double.parseDouble(long_main);
        }

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        Intent local = new Intent();
        local.setAction("ado.fun.code.locationize.off");
        this.sendBroadcast(local);
        local.setAction("ado.fun.code.locationize.stop");
        this.sendBroadcast(local);

    }

    @Override
    public void onLocationChanged(final Location location) {
        Handler handler = new Handler(Looper.getMainLooper());
        dist = distance(lat, location.getLatitude(), lon, location.getLongitude());

        Intent local = new Intent();
        local.setAction("ado.fun.code.locationize.off");
        this.sendBroadcast(local);

        if (dist < 100) {
            Log.d("Verma", "Verma");
            startService(new Intent(getBaseContext(), MuteService.class));
            locationManager.removeUpdates(this);
            onDestroy();

        }

        //Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Distance is: " + dist,
                        Toast.LENGTH_SHORT).show();
            }
        });
        //Toast.makeText(GetDistance.this, "Distance is: "+ dist, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "Please connect GPS", Toast.LENGTH_SHORT).show();
        onDestroy();
    }



    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status,
                                Bundle extras) {
        // TODO Auto-generated method stub
    }

}
