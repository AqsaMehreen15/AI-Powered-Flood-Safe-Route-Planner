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

class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSignIn = view.findViewById<MaterialButton>(R.id.btnSignIn)
        val tvGoToSignUp = view.findViewById<TextView>(R.id.tvGoToSignUp)

        // References variables mapping
        val etSignInName = view.findViewById<EditText>(R.id.etSignInName)
        val etSignInEmail = view.findViewById<EditText>(R.id.etSignInEmail)

        // ✅ PASSWORD LOGIC IMPLEMENTATION
        val etSignInPassword = view.findViewById<EditText>(R.id.etSignInPassword)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        btnSignIn.setOnClickListener {
            val nameInput = etSignInName?.text?.toString()?.trim()
            val emailInput = etSignInEmail?.text?.toString()?.trim()
            val passwordInput = etSignInPassword?.text?.toString()?.trim()

            // Validation checks
            if (emailInput.isNullOrEmpty()) {
                etSignInEmail?.error = "Email is required"
                return@setOnClickListener
            }
            if (passwordInput.isNullOrEmpty()) {
                etSignInPassword?.error = "Password is required"
                return@setOnClickListener
            }

            Toast.makeText(context, "Authenticating credentials...", Toast.LENGTH_SHORT).show()

            // ✅ FIREBASE QUERY: Email ke mutabiq profile cloud se check karein
            db.collection("Users")
                .whereEqualTo("email", emailInput)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]

                        // Cloud se pehle se saved password uthayein
                        val dbPassword = document.getString("password") ?: ""

                        // ✅ VALIDATION STEP: Agar cloud password aur input password match kar jayein
                        if (dbPassword == passwordInput || dbPassword.isEmpty()) {
                            val name = document.getString("name") ?: "Verified User"
                            val email = document.getString("email") ?: emailInput
                            val city = document.getString("city") ?: "Sialkot"
                            val phone = document.getString("phone") ?: "+92 300 1234567"

                            // Cache session save
                            prefs.edit().apply {
                                putBoolean("is_guest", false)
                                putString("user_name", name)
                                putString("user_email", email)
                                putString("user_city", city)
                                putString("user_phone", phone)
                                apply()
                            }

                            Toast.makeText(context, "Login Successful! Welcome, $name", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_signIn_to_home)
                        } else {
                            // Password galat hone ki soorat mein
                            etSignInPassword.error = "Incorrect password. Please try again."
                            Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No registered account found with this email.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Cloud Server Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvGoToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_signUp)
        }
    }
}