package com.example.polipo.semanticapp.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.polipo.semanticapp.R;

import java.util.ArrayList;

/**
 * Activity for Settings.
 */
public class SettingsActivity extends AppCompatActivity  {

    /**
     * Edit Text.
     */
    private EditText editRadius, editLat, editLon;

    /**
     * Radius.
     */
    private int radius;
    /**
     * Latitude & Longitude.
     */
    private double lat, lon;
    /**
     * Spinner.
     */
    private Spinner spinner;
    /**
     * The Config Name.
     */
    private String configName = "";
    /**
     * Using GPS?
     */
    private boolean useGps = false;

    /**
     * Application Context.
     */
    private Context context;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.configName = extras.getString("configName", "default.json");
            this.radius = extras.getInt("radius", 1);
            this.useGps = extras.getBoolean("useGps", false);
            this.lat = extras.getDouble("lat", 51.3397);
            this.lon = extras.getDouble("lon", 12.3731);
        }
        extras.clear();



        context = getApplicationContext();

        TextView textViewInfo = (TextView) findViewById(R.id.text_info);

        spinner = (Spinner) findViewById(R.id.spinner);
        loadSpinnerData();

        if (!spinner.getAdapter().isEmpty()) {
            for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                if (spinner.getAdapter().getItem(i).toString().equals(configName)) {
                    spinner.setSelection(i);
                }
            }
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> arg0, final View arg1,
                                       final int arg2, final long arg3) {
                configName = spinner.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(final AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        TextView textViewRadius = (TextView) findViewById(R.id.text_radius);

        editRadius = (EditText) findViewById(R.id.editRadius);
        editRadius.setText(radius + "");

        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_latlon);
        if (useGps) {
            checkBox.isChecked();
        }

        TextView textViewGPS = (TextView) findViewById(R.id.text_gps);

        editLat = (EditText) findViewById(R.id.editLat);
        editLat.setText(lat + "");
        editLon = (EditText) findViewById(R.id.editLon);
        editLon.setText(lon + "");

        final Button buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                // Perform action on click

                Intent myIntent = new Intent(getApplicationContext(), MainMenu.class);

                //give the values back to mainmenu
                myIntent.putExtra("configName", configName);
                myIntent.putExtra("radius", Integer.parseInt(editRadius.getText().toString()));
                if (!checkBox.isChecked()) {
                    myIntent.putExtra("useGps", false);

                    myIntent.putExtra("lat", Double.parseDouble(editLat.getText().toString()));
                    myIntent.putExtra("lon", Double.parseDouble(editLon.getText().toString()));

                } else {
                    myIntent.putExtra("useGps", true);
                }
                startActivity(myIntent);

            }
        });

    }

    /**
     * Load the Spinner Data.
     */
    private void loadSpinnerData() {
        String[] listAll;
        ArrayList<String> listConfig = new ArrayList<>();
        try {
            listAll = this.getAssets().list("");
            for (int i = 0; i < listAll.length; i++) {
                if (listAll[i].contains(".json")) {
                    listConfig.add(listAll[i]);
                }
            }


        } catch (Exception e) {
            e.getMessage();
        }


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listConfig);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }


}
