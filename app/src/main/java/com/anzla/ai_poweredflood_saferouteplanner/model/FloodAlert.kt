package com.anzla.ai_poweredflood_saferouteplanner.model

data class FloodAlert(
    val id: Int,
    val title: String,
    val location: String,
    val riskLevel: RiskLevel,
    val description: String,
    val timestamp: String
)