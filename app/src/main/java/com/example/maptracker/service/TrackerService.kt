package com.example.maptracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.maptracker.R
import org.osmdroid.util.GeoPoint

class TrackerService : Service(), LocationListener {

    companion object {
        // Trimite locația pentru desenat linia
        val locationData = MutableLiveData<GeoPoint>()
        // Trimite textul cu km (Ex: "1.5 km")
        val infoData = MutableLiveData<String>()

        var isServiceRunning = false
    }

    private lateinit var locationManager: LocationManager

    // Variabile pentru calcul distanță
    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0.0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, createNotification())

        // Resetăm datele la zero când pornește serviciul
        totalDistanceMeters = 0.0f
        lastLocation = null
        infoData.postValue("Distanță: 0.00 km")

        startLocationUpdates()
        isServiceRunning = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        isServiceRunning = false
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,
            2f, // Actualizare la fiecare 2 metri
            this
        )
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        val newPoint = GeoPoint(location.latitude, location.longitude)
        locationData.postValue(newPoint)

        // --- CALCUL DISTANȚĂ ---
        if (lastLocation != null) {
            // Calculăm câți metri sunt de la ultimul punct
            val distance = location.distanceTo(lastLocation!!)
            totalDistanceMeters += distance
        }
        lastLocation = location

        // Transformăm metri în Kilometri și formatăm textul
        val km = totalDistanceMeters / 1000
        val infoText = String.format("Distanță: %.2f km", km)

        // Trimitem textul către ecran
        infoData.postValue(infoText)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tracker_channel", "Urmărire", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "tracker_channel")
            .setContentTitle("MapTracker Activ")
            .setContentText("Înregistrare distanță...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}