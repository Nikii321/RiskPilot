package com.evedenta.riskpilot.model.response.decision



data class DecisionSummary(val id: String,val title: String,val createdAt: String,val risks: Int,val budget: Int,
    val totalCost: Double,val netBenefit: Double,val modelVersion: String)
