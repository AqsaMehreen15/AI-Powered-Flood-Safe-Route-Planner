package com.anzla.ai_poweredflood_saferouteplanner

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSignUp = view.findViewById<MaterialButton>(R.id.btnSignUp)
        val tvGoToSignIn = view.findViewById<TextView>(R.id.tvGoToSignIn)
        val btnSkip = view.findViewById<MaterialButton>(R.id.btnSkip)

        // ✅ TYPE MATCHING FIX: TextInputEditText ki jagah EditText use kiya jo aapki XML se perfectly match karta hai
        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        btnSignUp.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val phone = etPhone?.text?.toString()?.trim() ?: "+92 300 0000000"

            // Fixed Logic for Sialkot base
            val city = "Sialkot"

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(context, "Please enter Name and Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cloud mapping structured creation
            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "city" to city
            )

            Toast.makeText(context, "Creating cloud profile...", Toast.LENGTH_SHORT).show()

            db.collection("Users").add(userMap)
                .addOnSuccessListener {
                    prefs.edit().apply {
                        putBoolean("is_guest", false)
                        putString("user_name", name)
                        putString("user_email", email)
                        putString("user_city", city)
                        putString("user_phone", phone)
                        apply()
                    }
                    Toast.makeText(context, "Account Synchronized Successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signUp_to_home)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvGoToSignIn.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSkip.setOnClickListener {
            Toast.makeText(context, "Logged in as Guest", Toast.LENGTH_SHORT).show()
            prefs.edit().apply {
                putBoolean("is_guest", true)
                putString("user_name", "Guest User")
                putString("user_email", "No Email (Guest)")
                apply()
            }
            findNavController().navigate(R.id.action_signUp_to_home)
        }
    }
}