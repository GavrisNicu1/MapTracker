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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.maptracker.AppDatabase
import com.example.maptracker.HistoryFragment
import com.example.maptracker.RunEntity
import com.example.maptracker.R
import com.example.maptracker.service.TrackerService
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var btnStartStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnHistory: Button // Butonul nou
    private lateinit var txtInfo: TextView

    private var myLocationOverlay: MyLocationNewOverlay? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.map)
        btnStartStop = view.findViewById(R.id.btn_start_stop)
        btnReset = view.findViewById(R.id.btn_reset)
        btnHistory = view.findViewById(R.id.btn_history) // Conectăm butonul de ceas
        txtInfo = view.findViewById(R.id.txt_info)

        setupMap()
        checkPermissionsAndEnableLocation()

        pathOverlay = Polyline()
        pathOverlay.outlinePaint.color = android.graphics.Color.RED
        pathOverlay.outlinePaint.strokeWidth = 15f
        map.overlays.add(pathOverlay)

        TrackerService.locationData.observe(viewLifecycleOwner) { geoPoint ->
            pathOverlay.addPoint(geoPoint)
            map.controller.animateTo(geoPoint)
            map.invalidate()
        }

        TrackerService.infoData.observe(viewLifecycleOwner) { infoText ->
            txtInfo.text = infoText
        }

        // --- BUTONUL START / STOP ---
        btnStartStop.setOnClickListener {
            val serviceIntent = Intent(requireContext(), TrackerService::class.java)

            if (btnStartStop.text == "Start Traseu") {
                btnReset.visibility = View.GONE
                // Ascundem butonul de istoric când alergăm, ca să nu ne încurce
                btnHistory.visibility = View.GONE

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent)
                } else {
                    requireContext().startService(serviceIntent)
                }
                btnStartStop.text = "Stop Traseu"
                btnStartStop.setBackgroundColor(android.graphics.Color.RED)
            } else {
                requireContext().stopService(serviceIntent)
                btnReset.visibility = View.VISIBLE
                btnHistory.visibility = View.VISIBLE // Îl arătăm la loc
                btnStartStop.text = "Start Traseu"
                btnStartStop.setBackgroundColor(android.graphics.Color.parseColor("#6200EE"))
            }
        }

        // --- BUTONUL ISTORIC (CEASUL) ---
        btnHistory.setOnClickListener {
            // Navigăm către ecranul de istoric
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        // --- BUTONUL SALVEAZĂ ȘI ȘTERGE ---
        btnReset.setOnClickListener {
            val text = txtInfo.text.toString()
            val cleanText = text.replace("Distanță: ", "").replace(" km", "")
            // Dacă e 0.0, punem măcar 0.01 ca să avem ce salva la test
            var distanceFloat = cleanText.toFloatOrNull() ?: 0.0f

            // TRUC PENTRU TESTARE: Dacă e zero, salvăm totuși 0 ca să verifici că merge
            lifecycleScope.launch {
                val run = RunEntity(
                    timestamp = System.currentTimeMillis(),
                    distanceKm = distanceFloat
                )
                AppDatabase.getDatabase(requireContext()).runDao().insertRun(run)

                // Mesaj clar că s-a salvat
                Toast.makeText(requireContext(), "✅ Salvat în Istoric: $distanceFloat km", Toast.LENGTH_LONG).show()
            }

            pathOverlay.setPoints(emptyList())
            map.invalidate()
            txtInfo.text = "Distanță: 0.00 km"
            btnReset.visibility = View.GONE
        }
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)
        val startPoint = GeoPoint(47.6567, 23.5850)
        map.controller.setCenter(startPoint)
    }

    private fun checkPermissionsAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun enableMyLocation() {
        if (myLocationOverlay == null) {
            val provider = GpsMyLocationProvider(requireContext())
            myLocationOverlay = MyLocationNewOverlay(provider, map)
            myLocationOverlay?.enableMyLocation()
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