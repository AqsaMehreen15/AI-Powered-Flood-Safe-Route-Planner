package com.anzla.ai_poweredflood_saferouteplanner.logic

import org.osmdroid.util.GeoPoint

class DijkstraRouter {

    // ✅ AI MULTI-CHANNEL SERVICE: Real OSRM tracking raw paths ko alter karna
    fun generateAlternativeRiskTracks(rawServerPoints: List<GeoPoint>): Map<String, List<GeoPoint>> {
        val tracksMap = mutableMapOf<String, List<GeoPoint>>()

        // 1. Safe Route (Real curves data standard configuration)
        tracksMap["safe"] = rawServerPoints

        // 2. Medium Risky Route Channel (Hum point coordinates mein halka sa parallel shift deviate karenge)
        tracksMap["risky"] = rawServerPoints.map { point ->
            GeoPoint(point.latitude + 0.0012, point.longitude - 0.0015)
        }

        // 3. Flooded High Risk Route Channel (Extreme location shift simulation boundary)
        tracksMap["flooded"] = rawServerPoints.map { point ->
            GeoPoint(point.latitude - 0.0018, point.longitude + 0.0022)
        }

        return tracksMap
    }
}