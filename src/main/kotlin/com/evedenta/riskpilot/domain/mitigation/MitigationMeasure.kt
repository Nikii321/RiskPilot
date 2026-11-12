package com.evedenta.riskpilot.domain.mitigation



data class MitigationMeasure(val id: String, val riskId: String, val title: String, val cost: Int, val days: Int, val reduction: Double)
