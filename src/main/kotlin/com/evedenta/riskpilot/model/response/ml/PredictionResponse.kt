package com.evedenta.riskpilot.model.response.ml



data class PredictionResponse(val modelId: String,val probability: Double,val threshold: Double,
    val predictedClass: Int,val contributions: Map<String,Double>)
