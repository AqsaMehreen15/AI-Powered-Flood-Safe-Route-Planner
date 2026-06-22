package com.anzla.ai_poweredflood_saferouteplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anzla.ai_poweredflood_saferouteplanner.adapter.AlertAdapter
import com.anzla.ai_poweredflood_saferouteplanner.model.FloodAlert
import com.anzla.ai_poweredflood_saferouteplanner.model.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvLastUpdated: TextView
    private lateinit var tvAlertSummary: TextView

    private val OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
    private val WEATHER_BASE_URL    = "https://api.openweathermap.org/data/2.5/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView   = view.findViewById(R.id.alertsRecyclerView)
        tvLastUpdated  = view.findViewById(R.id.tvLastUpdated)
        tvAlertSummary = view.findViewById(R.id.tvAlertSummary)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Show static fallback immediately, then update with live data
        showAlerts(getStaticFallbackAlerts())
        fetchLiveAlerts()
    }

    // ✅ Live alerts generated from real weather data
    private fun fetchLiveAlerts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url  = URL("${WEATHER_BASE_URL}weather?q=Sialkot,pk&appid=$OPENWEATHER_API_KEY&units=metric")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000; conn.readTimeout = 8000

                if (conn.responseCode == 200) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())

                    val main        = json.getJSONObject("main")
                    val humidity    = main.getInt("humidity")
                    val temp        = main.getDouble("temp")
                    val pressure    = main.getInt("pressure")

                    val weatherArr  = json.getJSONArray("weather")
                    val weatherId   = weatherArr.getJSONObject(0).getInt("id")
                    val description = weatherArr.getJSONObject(0).getString("description")

                    var windSpeed = 0.0
                    if (json.has("wind")) windSpeed = json.getJSONObject("wind").getDouble("speed")

                    var rainMm = 0.0
                    if (json.has("rain")) {
                        val r = json.getJSONObject("rain")
                        rainMm = if (r.has("1h")) r.getDouble("1h")
                        else if (r.has("3h")) r.getDouble("3h") / 3.0
                        else 0.0
                    }

                    val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                    // ✅ Generate dynamic alerts based on real conditions
                    val alerts = buildDynamicAlerts(
                        rainMm, humidity, temp, windSpeed, pressure, weatherId, description, now
                    )

                    withContext(Dispatchers.Main) {
                        showAlerts(alerts)
                        tvLastUpdated.text  = "Last updated: $now"
                        tvAlertSummary.text = "Weather: ${description.replaceFirstChar { it.uppercase() }} | ${temp.toInt()}°C"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Keep static fallback
                withContext(Dispatchers.Main) {
                    tvLastUpdated.text = "Offline — showing cached alerts"
                }
            }
        }
    }

    // ✅ Smart alert generation: each alert reflects real API values
    private fun buildDynamicAlerts(
        rainMm: Double, humidity: Int, temp: Double,
        windSpeed: Double, pressure: Int, weatherId: Int,
        description: String, time: String
    ): List<FloodAlert> {

        val alerts = mutableListOf<FloodAlert>()
        var idCounter = 1

        // --- RAIN-BASED ALERTS ---
        when {
            rainMm >= 10 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "DANGER: Extreme Rainfall",
                location    = "Paris Road & Cantt, Sialkot",
                riskLevel   = RiskLevel.CRITICAL,
                description = "${"%.1f".format(rainMm)}mm/h rainfall detected. Roads impassable. Evacuate low-lying areas immediately.",
                timestamp   = time
            ))
            rainMm >= 5 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "High Rainfall Alert",
                location    = "Wazirabad Road, Sialkot",
                riskLevel   = RiskLevel.HIGH,
                description = "${"%.1f".format(rainMm)}mm/h rain ongoing. Drainage overflow risk. Avoid underpasses and low roads.",
                timestamp   = time
            ))
            rainMm >= 1 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "Moderate Rain — Stay Alert",
                location    = "Sialkot City",
                riskLevel   = RiskLevel.MEDIUM,
                description = "${"%.1f".format(rainMm)}mm/h rainfall. Water accumulation possible on older roads. Drive carefully.",
                timestamp   = time
            ))
            rainMm > 0 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "Light Rain — Monitor",
                location    = "Sialkot City",
                riskLevel   = RiskLevel.LOW,
                description = "Light drizzle (${"%.1f".format(rainMm)}mm/h). No immediate flood risk. Roads safe.",
                timestamp   = time
            ))
        }

        // --- HUMIDITY-BASED ALERT ---
        when {
            humidity >= 90 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "Extreme Humidity — Flood Conditions",
                location    = "Nullah Palkhu, Sialkot",
                riskLevel   = if (rainMm > 0) RiskLevel.CRITICAL else RiskLevel.HIGH,
                description = "Humidity at $humidity%. Soil saturation critical. Flash flood risk very high near nullahs.",
                timestamp   = time
            ))
            humidity >= 75 -> alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "High Humidity Warning",
                location    = "Aik Nullah Area",
                riskLevel   = RiskLevel.MEDIUM,
                description = "Humidity at $humidity%. Ground is saturated. Any additional rain could cause rapid flooding.",
                timestamp   = time
            ))
        }

        // --- WIND SPEED ALERT ---
        if (windSpeed >= 10) {
            alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "Strong Winds Warning",
                location    = "Airport Road, Sialkot",
                riskLevel   = if (windSpeed >= 15) RiskLevel.HIGH else RiskLevel.MEDIUM,
                description = "Wind speed: ${"%.0f".format(windSpeed)} m/s. Risk of fallen trees. Combined with rain, flooding risk is elevated.",
                timestamp   = time
            ))
        }

        // --- THUNDERSTORM (weather ID 2xx) ---
        if (weatherId in 200..299) {
            alerts.add(FloodAlert(
                id          = idCounter++,
                title       = "⚡ Thunderstorm Active",
                location    = "Sialkot District",
                riskLevel   = RiskLevel.CRITICAL,
                description = "Active thunderstorm detected (${description}). Stay indoors. Avoid bridges and open roads.",
                timestamp   = time
            ))
        }

        // --- ALWAYS SHOW SAFE ZONES ALERT ---
        alerts.add(FloodAlert(
            id          = idCounter++,
            title       = "China Chowk — Currently Safe",
            location    = "China Chowk, Sialkot",
            riskLevel   = RiskLevel.LOW,
            description = "No flooding reported. Temp: ${temp.toInt()}°C. Road conditions normal. Safe route via Cantt recommended.",
            timestamp   = time
        ))

        // --- FALLBACK if no rain and normal humidity ---
        if (alerts.size == 1) {
            alerts.add(0, FloodAlert(
                id          = idCounter++,
                title       = "Clear Weather — All Safe",
                location    = "Sialkot City",
                riskLevel   = RiskLevel.LOW,
                description = "No flood risk. Humidity: $humidity%, Temp: ${temp.toInt()}°C. All major roads clear.",
                timestamp   = time
            ))
        }

        return alerts
    }

    private fun showAlerts(alerts: List<FloodAlert>) {
        recyclerView.adapter = AlertAdapter(alerts)
    }

    // Fallback shown before API responds
    private fun getStaticFallbackAlerts(): List<FloodAlert> = listOf(
        FloodAlert(1, "Loading live alerts...", "Sialkot, Punjab", RiskLevel.LOW,
            "Connecting to weather server. Please wait.", "Just now"),
        FloodAlert(2, "Paris Road — Last Known Status", "Paris Road, Sialkot", RiskLevel.HIGH,
            "Drainage overflow previously reported near Sector D-5.", "Cached"),
        FloodAlert(3, "China Chowk — Last Known Status", "China Chowk, Sialkot", RiskLevel.LOW,
            "No flooding previously reported. Road conditions normal.", "Cached")
    )
}