package com.evedenta.riskpilot.model.response.optimization

import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure

data class OptimizationPlan(val measures: List<MitigationMeasure>, val cost: Int, val days: Int,
    val avoidedLoss: Double, val netBenefit: Double, val residualLoss: Double,
    val baselineLoss: Double, val totalCost: Double, val exploredNodes: Long, val method: String)
