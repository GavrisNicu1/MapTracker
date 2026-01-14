package com.example.maptracker.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.maptracker.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var btnStartStop: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // --- FIX PENTRU EROAREA CU PREFERENCEMANAGER ---
        // Folosim SharedPreferences standard din Android, care nu necesită importuri speciale
        val context = requireContext()
        val sharedPrefs = context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, sharedPrefs)
        // -----------------------------------------------

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Conectăm elementele din design (layout)
        map = view.findViewById(R.id.map)
        btnStartStop = view.findViewById(R.id.btn_start_stop)

        setupMap()
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK) // Stilul hărții (OpenStreetMap standard)
        map.setMultiTouchControls(true) // Permite zoom cu două degete

        // Setăm un punct de start (Ex: Baia Mare)
        val startPoint = GeoPoint(47.6567, 23.5850)
        map.controller.setZoom(15.0)
        map.controller.setCenter(startPoint)
    }

    override fun onResume() {
        super.onResume()
        // Harta trebuie să știe când aplicația revine pe ecran
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Harta trebuie să știe când aplicația intră în pauză pentru a nu consuma baterie
        map.onPause()
    }
}