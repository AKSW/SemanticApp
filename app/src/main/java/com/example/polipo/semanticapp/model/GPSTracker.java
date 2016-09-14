package com.example.polipo.semanticapp.model;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by polipo on 20.04.16.
 */

public class GPSTracker extends Activity implements LocationListener {

    /**
     * Android Context.
     */
    private final Context mContext;

    /**
     * Check if GPS is enabled.
     * @return true if GPS is enabled
     */
    public final boolean isGPSEnabled() {
        return isGPSEnabled;
    }

    /**
     * flag for GPS status.
     */
    private boolean isGPSEnabled = false;

    /**
      * flag for network status.
      */
    private boolean isNetworkEnabled = false;

    /**
     * flag for Location status.
     */
    private boolean canGetLocation = false;

    /**
     * The Location.
     */
    private Location location;

    /**
     * Latitude.
     */
    private double latitude;

    /**
     * Longitude.
     */
    private double longitude;

    /**
     * Minimal Distance Change for Updates.
     */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    /**
     * Minimal Time for Update.
     */
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    /**
     * get Location Manager.
     * @return Location Manager
     */
    public final LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * get Context.
     * @return Context
     */
    public final Context getmContext() {
        return mContext;
    }

    /**
     * is Network enabled.
     * @return true if enabled
     */
    public final boolean isNetworkEnabled() {
        return isNetworkEnabled;
    }

    /**
     * CanGetLocation.
     * @return true or false
     */
    public final boolean isCanGetLocation() {
        return canGetLocation;
    }
    /**
     * A Location Manager.
     */
    private LocationManager locationManager;

    /**
     * Constructor.
     * @param context Android Activity Context
     */
    public GPSTracker(final Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Get the Location.
     * @return a Location
     */
    public final Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);


            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                //do something
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Stop using GPS.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public final void stopUsingGPS() {
        if (locationManager != null) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }


    /**
     * get Latitude.
     * @return latitude
     */
    public final double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * get longitude.
     * @return longitude
     */
    public final double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * can get Location.
     * @return true or false
     */
    public final boolean canGetLocation() {
        return this.canGetLocation;
    }


    /**
     * show settings.
     */
    public final void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS is settings");

        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public final void onLocationChanged(final Location currentLocation) {

        this.location = currentLocation;
        getLatitude();
        getLongitude();

    }

    @Override
    public void onProviderDisabled(final String provider) {

    }

    @Override
    public void onProviderEnabled(final String provider) {


    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

    }
}
