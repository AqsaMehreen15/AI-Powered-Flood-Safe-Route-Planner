package com.anzla.ai_poweredflood_saferouteplanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2 second ke baad ab ye Sign In screen par jaye ga
        Handler(Looper.getMainLooper()).postDelayed({
            // NavGraph mein jo naya action banaya hai uska ID yahan use karein
            findNavController().navigate(R.id.action_splash_to_signIn)
        }, 2000)
    }
}