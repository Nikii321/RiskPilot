package com.evedenta.riskpilot.ml.data

import com.evedenta.riskpilot.domain.risk.RiskFeatures
import kotlin.math.*

data class HistoricalObservation(val projectId: Int, val features: RiskFeatures, val occurred: Int)
