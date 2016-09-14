package com.example.polipo.semanticapp.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.polipo.semanticapp.R;
import com.example.polipo.semanticapp.model.Resource;

/**
 * This Class is used to show the Attributes from a Resource.
 */
public class Databrowser extends AppCompatActivity {
    /**
     * The Resource.
     */
    private Resource resource = null;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databrowser);

        // receive the arguments from the previous SettingActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // assign the values to string-arguments
            resource = (Resource) extras.get("resource");
        }

        TextView textAttributes = (TextView) findViewById(R.id.text_attributes);
        textAttributes.setText(resource.toString());






    }
}
