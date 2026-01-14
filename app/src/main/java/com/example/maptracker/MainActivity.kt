package com.example.maptracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity // Added this
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() { // Added class definition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurare User Agent pentru OSM
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)
    }
}