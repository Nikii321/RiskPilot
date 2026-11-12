package com.evedenta.riskpilot.model.response.decision



data class DecisionComparison(val left: SavedDecision,val right: SavedDecision,val totalCostDelta: Double,
    val netBenefitDelta: Double,val budgetDelta: Int,val addedMeasures: List<String>,val removedMeasures: List<String>)
