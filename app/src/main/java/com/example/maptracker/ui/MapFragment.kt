package com.example.maptracker.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.maptracker.R
import com.example.maptracker.service.TrackerService // Importul pentru Serviciu
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline // Importul pentru linia roșie
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var btnStartStop: Button
    private var myLocationOverlay: MyLocationNewOverlay? = null

    // Variabila pentru linia roșie care se desenează pe hartă
    private lateinit var pathOverlay: Polyline

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                enableMyLocation()
            } else {
                Toast.makeText(context, "Permisiunea de locație este necesară!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        val sharedPrefs = context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, sharedPrefs)

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    // --- AICI ESTE SCHIMBAREA MAJORĂ ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.map)
        btnStartStop = view.findViewById(R.id.btn_start_stop)

        // Găsim textul pentru informații
        val txtInfo = view.findViewById<android.widget.TextView>(R.id.txt_info)

        setupMap()
        checkPermissionsAndEnableLocation()

        pathOverlay = Polyline()
        pathOverlay.outlinePaint.color = android.graphics.Color.RED
        pathOverlay.outlinePaint.strokeWidth = 15f
        map.overlays.add(pathOverlay)

        // 1. Ascultăm COORDONATELE (pentru desenat)
        TrackerService.locationData.observe(viewLifecycleOwner) { geoPoint ->
            pathOverlay.addPoint(geoPoint)
            map.controller.animateTo(geoPoint)
            map.invalidate()
        }

        // 2. Ascultăm TEXTUL CU DISTANȚA (Nou!)
        TrackerService.infoData.observe(viewLifecycleOwner) { infoText ->
            txtInfo.text = infoText
        }

        // 3. Logica Butonului
        btnStartStop.setOnClickListener {
            val serviceIntent = Intent(requireContext(), TrackerService::class.java)

            if (btnStartStop.text == "Start Traseu") {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent)
                } else {
                    requireContext().startService(serviceIntent)
                }
                btnStartStop.text = "Stop Traseu"
                btnStartStop.setBackgroundColor(android.graphics.Color.RED)

                // Curățăm harta veche când începem un traseu nou
                pathOverlay.setPoints(emptyList())
                map.invalidate()

            } else {
                requireContext().stopService(serviceIntent)
                btnStartStop.text = "Start Traseu"
                btnStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            }
        }
    }


    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0) // Zoom mai mare ca să vezi mișcarea mai bine

        val startPoint = GeoPoint(47.6567, 23.5850)
        map.controller.setCenter(startPoint)
    }

    private fun checkPermissionsAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableMyLocation() {
        if (myLocationOverlay == null) {
            val provider = GpsMyLocationProvider(requireContext())
            myLocationOverlay = MyLocationNewOverlay(provider, map)
            myLocationOverlay?.enableMyLocation()
            // myLocationOverlay?.enableFollowLocation() // Am comentat asta ca să nu se bată cu animatia noastră
            map.overlays.add(myLocationOverlay)
        }
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        myLocationOverlay?.disableMyLocation()
    }
}