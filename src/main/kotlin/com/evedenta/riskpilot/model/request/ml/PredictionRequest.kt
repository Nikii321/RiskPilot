package com.evedenta.riskpilot.model.request.ml

import com.evedenta.riskpilot.domain.risk.RiskFeatures

data class PredictionRequest(val complexity: Double=.5,val experience: Double=.5,val workload: Double=.5,
    val dependencies: Int=3,val volatility: Double=.5,val reliability: Double=.5) {
    fun toFeatures() = RiskFeatures(complexity,experience,workload,dependencies,volatility,reliability)
}
