package com.anzla.ai_poweredflood_saferouteplanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PlanRouteFragment : Fragment(R.layout.fragment_plan_route) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFrom = view.findViewById<TextInputEditText>(R.id.etFrom)
        val etTo = view.findViewById<TextInputEditText>(R.id.etTo)
        val btnFindRoute = view.findViewById<MaterialButton>(R.id.btnFindSafeRoute)

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission && etFrom.text.isNullOrEmpty()) {
            etFrom.setText("Paris Road, Sialkot") // Dynamic fallback string
        }

        btnFindRoute.setOnClickListener {
            val startText = etFrom.text.toString().trim()
            val destText = etTo.text.toString().trim()

            if (startText.isEmpty()) {
                etFrom.error = "Starting point is required"
                return@setOnClickListener
            }

            if (destText.isEmpty()) {
                etTo.error = "Please enter a destination"
                return@setOnClickListener
            }

            // ✅ REAL-TIME BUNDLE: Passing actual string names to map engine
            val bundle = Bundle().apply {
                putString("start_point_name", startText)
                putString("target_destination_name", destText)
            }
            // Make sure action ID is correct in your nav_graph
            findNavController().navigate(R.id.action_planRouteFragment_to_navigation_maps, bundle)
        }
    }
}