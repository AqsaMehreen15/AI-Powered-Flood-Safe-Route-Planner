package com.anzla.ai_poweredflood_saferouteplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private lateinit var floodTrendChart: LineChart
    private lateinit var tvAnalyticsHumidity: TextView
    private lateinit var tvAnalyticsWaterMark: TextView
    private lateinit var tvAnalyticsRainfall: TextView
    private lateinit var tvAnalyticsRiskScore: TextView

    private val OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
    private val FORECAST_URL        = "https://api.openweathermap.org/data/2.5/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floodTrendChart      = view.findViewById(R.id.floodTrendChart)
        tvAnalyticsHumidity  = view.findViewById(R.id.tvAnalyticsHumidity)
        tvAnalyticsWaterMark = view.findViewById(R.id.tvAnalyticsWaterMark)
        tvAnalyticsRainfall  = view.findViewById(R.id.tvAnalyticsRainfall)
        tvAnalyticsRiskScore = view.findViewById(R.id.tvAnalyticsRiskScore)

        // Show placeholder chart while loading
        setupPlaceholderChart()

        // Fetch live 5-day forecast data for dynamic chart
        fetchForecastData()
    }

    // ✅ Fetch 5-day / 3-hour forecast — gives ~40 data points
    private fun fetchForecastData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Current weather for summary cards
                val currentUrl = URL("${FORECAST_URL}weather?q=Sialkot,pk&appid=$OPENWEATHER_API_KEY&units=metric")
                val currentConn = currentUrl.openConnection() as HttpURLConnection
                currentConn.connectTimeout = 8000; currentConn.readTimeout = 8000

                var humidity   = 0
                var rainNow    = 0.0
                var temp       = 0.0
                var riskScore  = 0.0

                if (currentConn.responseCode == 200) {
                    val json = JSONObject(currentConn.inputStream.bufferedReader().readText())
                    val main = json.getJSONObject("main")
                    humidity = main.getInt("humidity")
                    temp     = main.getDouble("temp")

                    if (json.has("rain")) {
                        val r = json.getJSONObject("rain")
                        rainNow = if (r.has("1h")) r.getDouble("1h")
                        else if (r.has("3h")) r.getDouble("3h") / 3.0
                        else 0.0
                    }
                }

                // 2. Forecast for chart (3h intervals, next 5 days)
                val forecastUrl = URL("${FORECAST_URL}forecast?q=Sialkot,pk&appid=$OPENWEATHER_API_KEY&units=metric&cnt=16")
                val forecastConn = forecastUrl.openConnection() as HttpURLConnection
                forecastConn.connectTimeout = 8000; forecastConn.readTimeout = 8000

                val chartEntries   = ArrayList<Entry>()
                val chartLabels    = ArrayList<String>()
                var maxWaterLevel  = 0.0
                var totalRisk      = 0.0
                val sdf            = SimpleDateFormat("EEE HH:mm", Locale.getDefault())

                if (forecastConn.responseCode == 200) {
                    val forecastJson = JSONObject(forecastConn.inputStream.bufferedReader().readText())
                    val forecastList = forecastJson.getJSONArray("list")

                    for (i in 0 until forecastList.length()) {
                        val item = forecastList.getJSONObject(i)
                        val mainData = item.getJSONObject("main")
                        val h = mainData.getInt("humidity")

                        var rain3h = 0.0
                        if (item.has("rain")) {
                            val rObj = item.getJSONObject("rain")
                            rain3h = if (rObj.has("3h")) rObj.getDouble("3h") else 0.0
                        }

                        // Water level estimate: base soil saturation + rain accumulation
                        // More realistic formula: (humidity factor) + (rain volume * 0.3)
                        val humidityFactor = (h - 50).coerceAtLeast(0) / 100.0  // 0..0.5
                        val waterLevel     = (humidityFactor * 2.5) + (rain3h * 0.3)
                        if (waterLevel > maxWaterLevel) maxWaterLevel = waterLevel

                        // Risk per point
                        val localRisk = (rain3h / 10.0 + h / 200.0).coerceIn(0.0, 1.0)
                        totalRisk += localRisk

                        chartEntries.add(Entry(i.toFloat(), waterLevel.toFloat()))

                        // Time label from dt
                        val dt   = item.getLong("dt") * 1000L
                        chartLabels.add(sdf.format(Date(dt)))
                    }

                    // Average risk score (0-100)
                    riskScore = if (forecastList.length() > 0)
                        (totalRisk / forecastList.length()) * 100
                    else 0.0
                }

                // ✅ Water mark from real forecast data
                // If no rain data, estimate from current humidity
                val waterMark = if (maxWaterLevel > 0)
                    maxWaterLevel
                else
                    (humidity - 50).coerceAtLeast(0) / 40.0  // rough baseline

                withContext(Dispatchers.Main) {
                    // Update summary cards
                    tvAnalyticsHumidity.text  = "$humidity%"
                    tvAnalyticsWaterMark.text = "${"%.1f".format(waterMark)}m"
                    tvAnalyticsRainfall.text  = "${"%.1f".format(rainNow)}mm/h"
                    tvAnalyticsRiskScore.text = "${riskScore.toInt()}%"

                    // Update chart with live data
                    if (chartEntries.isNotEmpty()) {
                        updateChart(chartEntries, chartLabels)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvAnalyticsHumidity.text  = "--"
                    tvAnalyticsWaterMark.text = "--"
                    // Keep placeholder chart visible
                }
            }
        }
    }

    // ✅ Dynamic chart from live forecast entries
    private fun updateChart(entries: ArrayList<Entry>, labels: ArrayList<String>) {
        val dataSet = LineDataSet(entries, "Water Level Forecast (m)").apply {
            color            = Color.parseColor("#E24B4A")
            valueTextColor   = Color.parseColor("#102A43")
            lineWidth        = 2.5f
            circleRadius     = 4f
            setCircleColor(Color.parseColor("#E24B4A"))
            setDrawFilled(true)
            fillColor        = Color.parseColor("#F8D7DA")
            fillAlpha        = 80
            setDrawValues(false)
            mode             = LineDataSet.Mode.CUBIC_BEZIER
        }

        floodTrendChart.data = LineData(dataSet)

        floodTrendChart.description.isEnabled = false
        floodTrendChart.setTouchEnabled(true)
        floodTrendChart.setPinchZoom(true)
        floodTrendChart.legend.isEnabled = true

        // X Axis: show time labels
        val xAxis = floodTrendChart.xAxis
        xAxis.position         = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity      = 1f
        xAxis.labelRotationAngle = -30f
        xAxis.textSize         = 9f
        xAxis.valueFormatter   = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx >= 0 && idx < labels.size) labels[idx] else ""
            }
        }

        val yLeft = floodTrendChart.axisLeft
        yLeft.axisMinimum = 0f
        yLeft.axisMaximum = 4f
        yLeft.setDrawGridLines(true)
        yLeft.gridColor = Color.parseColor("#EEEEEE")

        floodTrendChart.axisRight.isEnabled = false
        floodTrendChart.animateXY(800, 800)
        floodTrendChart.invalidate()
    }

    // Placeholder shown while loading
    private fun setupPlaceholderChart() {
        val now = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val entries = ArrayList<Entry>()
        val labels  = ArrayList<String>()

        // Generate placeholder slots (grayed out, near-zero)
        for (i in 0 until 8) {
            entries.add(Entry(i.toFloat(), 0.1f))
            now.add(Calendar.HOUR_OF_DAY, 3)
            labels.add(sdf.format(now.time))
        }

        val dataSet = LineDataSet(entries, "Loading forecast...").apply {
            color          = Color.parseColor("#BDBDBD")
            lineWidth      = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(false)
        }

        floodTrendChart.data = LineData(dataSet)
        floodTrendChart.description.isEnabled = false
        floodTrendChart.setTouchEnabled(false)
        floodTrendChart.axisLeft.axisMinimum  = 0f
        floodTrendChart.axisLeft.axisMaximum  = 4f
        floodTrendChart.axisRight.isEnabled   = false
        floodTrendChart.xAxis.position        = XAxis.XAxisPosition.BOTTOM
        floodTrendChart.xAxis.setDrawGridLines(false)
        floodTrendChart.invalidate()
    }
}