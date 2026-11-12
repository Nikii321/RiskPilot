package com.evedenta.riskpilot.model.response.scenario



data class StrategyResult(val strategy: String, val outcomes: List<Double>, val expectedCost: Double,
    val worstCost: Double, val maxRegret: Double, val hurwiczCost: Double)
