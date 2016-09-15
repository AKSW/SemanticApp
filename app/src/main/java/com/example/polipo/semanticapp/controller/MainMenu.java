package com.example.polipo.semanticapp.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.polipo.semanticapp.R;
import com.example.polipo.semanticapp.model.Resource;
import com.example.polipo.semanticapp.model.SemanticWebModul;
import com.example.polipo.semanticapp.model.GPSTracker;


import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK;

/**
 * The Main Menu of the Application.
 */
public class MainMenu extends AppCompatActivity {


    /**
     * The Configurations Class for the semantic Web Classes.
     */
    private SemanticWebModul semanticWebModul;


    /**
     * Longitude.
     */
    private double lon = 12.3731;

    /**
     * Latitude.
     */
    private double lat = 51.3397;
    /**
     * Radius.
     */
    private int radius = 1;

    /**
     * Are you using your GPS?
     */
    private boolean useGps = true;

    /**
     * Is your GPS enabled?
     */
    private boolean gpsEnabled = false;

    /**
     * Name of the Config.
     */
    private String configName = "default.json";

    /**
     * Some Information.
     */
    private String information = "";

    /**
     * The Icon for your Position.
     */
    private OverlayItem startItem;


    /**
     * Overlay for Position Icon.
     */
    private MyOwnItemizedOverlay startOverlay;

    /**
     * All Icons on the Map.
     */
    private ArrayList<OverlayItem> overlayItems;

    /**
     * A OSM MapView.
     */
    private org.osmdroid.views.MapView mapView;

    /**
     * A Controller for the Map.
     */
    private IMapController mapController;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // receive the arguments from the previous SettingActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("configName")) {
                Toast.makeText(getApplicationContext(), "Found Settings", Toast.LENGTH_SHORT).show();

                // assign the values to string-arguments
                this.radius = extras.getInt("radius", 1);
                this.configName = extras.getString("configName", "default");
                this.useGps = extras.getBoolean("useGps", false);
                if (!this.useGps) {
                    this.lat = extras.getDouble("lat", 51.3397);
                    this.lon = extras.getDouble("lon", 12.3731);
                }
            }
            extras.clear();


        }

        mapView = (org.osmdroid.views.MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setTileSource(MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(16);

        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

        gpsEnabled = gpsTracker.isGPSEnabled();

        if (useGps && gpsEnabled) {
            lon = gpsTracker.getLongitude();
            lat = gpsTracker.getLatitude();
        }


        setStartOverlayItem();

        semanticWebModul = new SemanticWebModul(
                getApplicationContext(), lon, lat, radius, configName);


        if (!gpsEnabled) {
            Toast.makeText(getApplicationContext(), "LoadConfig:" + configName + ", Please enable GPS Modul if you want to use it", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "LoadConfig:" + configName + ", Using your GPS", Toast.LENGTH_LONG).show();
        }


        if (!semanticWebModul.getDatabase().databaseIsEmty()) {

            overlayItems = resourcesToOverlayItems(semanticWebModul.getDatabase().getResources());

            redrawMap();
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                mapView.getOverlays().remove(startOverlay);
                setStartOverlayItem();
            }
        });
    }


    /**
     * This Method concverts a HashMap of Resources to an ArrayList of Overlay Items.
     * @param resources HashMap of Resources
     * @return ArrayList of Overlay Items
     */

    public final ArrayList<OverlayItem> resourcesToOverlayItems(final HashMap<String, Resource> resources) {
        ArrayList<OverlayItem> overlayItems = new ArrayList<>();

        Set<String> keySet = resources.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            Resource resource = resources.get(it.next());
            GeoPoint resourceGeo = new GeoPoint(resource.getCoordinates().getLat(), resource.getCoordinates().getLon());
            OverlayItem overlayItem = new OverlayItem(resource.getSubject(), resource.thinToString(), resourceGeo);

            if (resource.getSubject().contains("linkedgeodata.org")) {
                Drawable newMarker = this.getResources().getDrawable(R.drawable.marker_default);
                overlayItem.setMarker(newMarker);
            } else
            if (resource.getSubject().contains("openmobilenetwork.org")) {
                Drawable newMarker = this.getResources().getDrawable(R.drawable.wifi1small);
                overlayItem.setMarker(newMarker);
            }


            overlayItems.add(overlayItem);
        }

        return overlayItems;
    }

    @Override
    public final void onResume() {
        super.onResume();
        // put your code here...
        Toast.makeText(getApplicationContext(), "Resume", Toast.LENGTH_LONG).show();
    }

    /**
     * Method to redraw the Map.
     */
    public final void redrawMap() {
        MyOwnItemizedOverlay overlay = new MyOwnItemizedOverlay(this, getApplicationContext(), overlayItems, semanticWebModul, this);
        mapView.getOverlays().add(overlay);
        mapView.invalidate();
    }

    /**
     * Method to set the Start Icon on the GPS Coordinates on the Map.
     */
    public final void setStartOverlayItem() {

        GeoPoint myLocation = new GeoPoint(lat, lon);
        mapController.setCenter(myLocation);

        overlayItems = new ArrayList<>();
        startItem = new OverlayItem("Here you are", "...or you set the GPS Coordinates here :)", myLocation);
        Drawable newMarker = this.getResources().getDrawable(R.drawable.marker_blue);
        startItem.setMarker(newMarker);
        overlayItems.add(startItem);

        startOverlay = new MyOwnItemizedOverlay(this, getApplicationContext(), overlayItems, semanticWebModul, this);

        mapView.getOverlays().add(startOverlay);

        mapView.invalidate();
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_run) {
            try {
                new SemanticFunktions().execute("");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        }
        if (id == R.id.action_clear) {
            semanticWebModul.clearDatabase();

            mapView.getOverlays().clear();

            overlayItems.clear();

            redrawMap();

            setStartOverlayItem();

            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();

            return true;
        }

        if (id == R.id.action_set_values) {
            Intent intend = new Intent(getApplicationContext(), SettingsActivity.class);
            intend.putExtra("configName", configName);
            intend.putExtra("radius", radius);
            intend.putExtra("useGps", useGps);
            intend.putExtra("lat", lat);
            intend.putExtra("lon", lon);
            MainMenu.this.startActivity(intend);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * AsyncTask to execute a Databaserequest.
     */
    private class SemanticFunktions extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(final String... params) {
            // get the string from params, which is an array
            String myString = params[0];

            try {
                semanticWebModul.databaseRequest();
                overlayItems = resourcesToOverlayItems(semanticWebModul.getDatabase().getResources());

                //if something went wrong
                if (semanticWebModul.getDatabase().databaseIsEmty()) {
                    myString = "Something went wrong. Is the SPARQL Endpoint down, or do you have a typo in the Query?";
                }
                if (!semanticWebModul.getDatabase().databaseIsEmty()) {
                    myString = "It work's!";
                }
            } catch (Exception e) {
                myString = e.toString();
                Log.e("Exception" , e.toString());
            }

            int i = 100;
            publishProgress(i);

            //myString = semanticWebModul.getService();


            return myString;
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            redrawMap();
            Toast.makeText(getApplicationContext(), "Done!" + '\n' + result , Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Inner Class for map behavior.
     */
    public class MyOwnItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {

        /**
         * Context from Activity.
         */
        private Context context;

        /**
         * Activity.
         */
        private Activity activity;

        /**
         * SemanticWebModul from Activity.
         */
        private SemanticWebModul semanticWebModul;

        /**
         * MainMenu.
         */
        private MainMenu mainMenu;

        /**
         * Constructor.
         * @param activity Activity
         * @param context Context
         * @param aList OverlayItem List
         * @param semanticWebModul semanticWebModul
         * @param mainMenu mainMenu
         */
        public MyOwnItemizedOverlay(final Activity activity, final Context context, final List<OverlayItem> aList, final SemanticWebModul semanticWebModul, final MainMenu mainMenu) {
            super(context, aList, new OnItemGestureListener<OverlayItem>() {
                @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
                }
                @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }
            });
            // TODO Auto-generated constructor stub
            this.context = context;
            this.activity = activity;
            this.semanticWebModul = semanticWebModul;
            this.mainMenu = mainMenu;


        }

        @Override
        //public boolean onSibgleTab(MotionEvent e, MapView mapView)
        public final boolean onDoubleTap(final MotionEvent e, final MapView mapView) {

            Projection proj = mapView.getProjection();
            GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
            final String longitude = Double
                    .toString(((double) loc.getLongitudeE6()) / 1000000);
            final String latitude = Double
                    .toString(((double) loc.getLatitudeE6()) / 1000000);


            ///Toast.makeText(context, "lat:" + latitude + "   lon:" + longitude, Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setMessage("Longitude: " + longitude + " Latitude: " + latitude);
            alertDialog.setTitle("Set GPS Coordinates here?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int which) {
                            mainMenu.lat = Double.valueOf(latitude);
                            mainMenu.lon = Double.valueOf(longitude);

                            //mainMenu.setSemanticWebModul(mainMenu.getSemanticWebModul().queryFormat(Double.valueOf(latitude), Double.valueOf(longitude), mainMenu.radius);
                            mainMenu.semanticWebModul.queryFormat(Double.valueOf(latitude), Double.valueOf(longitude), mainMenu.radius);

                            mainMenu.mapView.getOverlays().remove(mainMenu.startOverlay);
                            mainMenu.setStartOverlayItem();
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int which) {


                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return true;
        }


        @Override
        protected final boolean onSingleTapUpHelper(final int index, final OverlayItem item, final MapView mapView) {
            Toast.makeText(context, "Item " + index + " has been tapped!", Toast.LENGTH_SHORT).show();

            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle(item.getTitle());
            alertDialog.setMessage(item.getSnippet());
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    });

            if (!item.getTitle().contains("Here you are")) {
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "More Information",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int which) {



                                Intent myIntent = new Intent(context, Databrowser.class);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //give the values back to mainmenu
                                myIntent.putExtra("resource", semanticWebModul.getDatabase().getResources().get(item.getTitle().toString()));
                                context.startActivity(myIntent);
                                dialog.dismiss();
                            }
                        });
            }



            alertDialog.show();


            return true;
        }
    }


}
