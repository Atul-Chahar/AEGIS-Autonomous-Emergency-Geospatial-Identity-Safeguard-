package com.example.aegis.safety

data class IncidentAnalysis(
  val eventType: IncidentType,
  val confidence: Float,
  val contributingFactors: List<String>,
  val recommendedAction: RecommendedAction,
  val timestamp: Long = System.currentTimeMillis(),
)
