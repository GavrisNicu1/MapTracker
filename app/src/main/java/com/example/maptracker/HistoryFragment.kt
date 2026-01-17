package com.example.maptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Pregătim Lista
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = RunsAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. Citim din Baza de Date
        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            db.runDao().getAllRuns().collect { listOfRuns ->
                // Când primim datele, le trimitem la listă
                adapter.updateData(listOfRuns)
            }
        }
    }
}