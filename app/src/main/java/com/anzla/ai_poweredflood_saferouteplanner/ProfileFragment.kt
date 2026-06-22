package com.anzla.ai_poweredflood_saferouteplanner

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        // Dynamic custom views hooks
        val layoutAuthPrompt = view.findViewById<View>(R.id.layout_auth_prompt)
        val layoutProfileData = view.findViewById<View>(R.id.layout_profile_data)
        val btnProfileLogin = view.findViewById<MaterialButton>(R.id.btnProfileLogin)

        // ✅ FIXED: Ab yeh IDs XML mein majood hain, red line khatam ho jayegi!
        val tvCityLabel = view.findViewById<TextView>(R.id.tvCityLabel)
        val tvPhoneLabel = view.findViewById<TextView>(R.id.tvPhoneLabel)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        if (isGuest) {
            layoutProfileData.visibility = View.GONE
            layoutAuthPrompt.visibility = View.VISIBLE
            btnLogout.text = "Exit Guest Mode"

            tvName.text = "Anonymous Guest"
            tvEmail.text = "Unauthorized Account"
            tvCityLabel.text = "City: Not Available"
            tvPhoneLabel.text = "Phone: Not Available"
        } else {
            layoutProfileData.visibility = View.VISIBLE
            layoutAuthPrompt.visibility = View.GONE
            btnLogout.text = "Logout"

            // ✅ DYNAMIC CLOUD DATA INJECTION
            val cloudName = prefs.getString("user_name", "Dynamic Cloud User")
            val cloudEmail = prefs.getString("user_email", "realtime@cloud.com")
            val cloudCity = prefs.getString("user_city", "Sialkot")
            val cloudPhone = prefs.getString("user_phone", "+92 300 1234567")

            tvName.text = cloudName
            tvEmail.text = cloudEmail
            tvCityLabel.text = "City: $cloudCity"
            tvPhoneLabel.text = "Phone: $cloudPhone"
        }

        btnProfileLogin.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(requireContext(), "Clearing Session...", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.splashFragment)
        }
    }
}