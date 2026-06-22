package com.anzla.ai_poweredflood_saferouteplanner.logic

// Ye class batati hai ke raste ka aik point kahan hai aur us se aglay points konse hain
data class RoadNode(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val neighbors: MutableMap<String, Double> = mutableMapOf() // Neighbor ID aur raste ka weight (Risk)
)