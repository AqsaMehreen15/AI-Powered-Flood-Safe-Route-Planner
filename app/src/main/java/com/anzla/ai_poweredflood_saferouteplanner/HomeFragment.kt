package com.anzla.ai_poweredflood_saferouteplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private lateinit var tvWeatherStatus: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var btnNavigateMap: MaterialButton
    private lateinit var cardPlanRoute: View

    private val OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
    private val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI Initialization
        tvWeatherStatus = view.findViewById(R.id.tvWeatherStatus)
        tvTemperature   = view.findViewById(R.id.tvTemperature)
        btnNavigateMap  = view.findViewById(R.id.btnNavigateMap)
        cardPlanRoute   = view.findViewById(R.id.cardPlanRoute)

        fetchLiveWeatherData()

        // 1. Navigation to Plan Route Screen (Using ID from your nav_graph)
        cardPlanRoute.setOnClickListener {
            try {
                findNavController().navigate(R.id.planRouteFragment2)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigation Error: Check Nav Graph ID", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Direct Map Navigation
        btnNavigateMap.setOnClickListener {
            findNavController().navigate(R.id.navigation_maps)
        }
    }

    private fun fetchLiveWeatherData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "${WEATHER_BASE_URL}weather?q=Sialkot,pk&appid=$OPENWEATHER_API_KEY&units=metric"
                val connection = URL(urlString).openConnection() as HttpURLConnection
                if (connection.responseCode == 200) {
                    val json = JSONObject(connection.inputStream.bufferedReader().readText())
                    val weatherDesc = json.getJSONArray("weather").getJSONObject(0).getString("description")
                    val mainObj = json.getJSONObject("main")
                    val temp = mainObj.getDouble("temp")
                    val humidity = mainObj.getInt("humidity")

                    withContext(Dispatchers.Main) {
                        tvWeatherStatus.text = weatherDesc.replaceFirstChar { it.uppercase() }
                        tvTemperature.text = "Temp: ${temp.toInt()}°C | Humidity: $humidity%"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvWeatherStatus.text = "Weather data unavailable"
                }
            }
        }
    }
}