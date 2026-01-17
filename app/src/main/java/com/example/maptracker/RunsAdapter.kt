package com.example.maptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunsAdapter(private var runs: List<RunEntity>) : RecyclerView.Adapter<RunsAdapter.RunViewHolder>() {

    class RunViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDate: TextView = view.findViewById(R.id.txt_date)
        val txtDistance: TextView = view.findViewById(R.id.txt_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_run, parent, false)
        return RunViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runs[position]

        // 1. Formatăm data frumos
        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
        val dateString = sdf.format(Date(run.timestamp))
        holder.txtDate.text = "📅 $dateString"

        // 2. Formatăm distanța
        holder.txtDistance.text = "${run.distanceKm} km"
    }

    override fun getItemCount() = runs.size

    // Funcție ca să actualizăm lista când apar date noi
    fun updateData(newRuns: List<RunEntity>) {
        runs = newRuns
        notifyDataSetChanged()
    }
}