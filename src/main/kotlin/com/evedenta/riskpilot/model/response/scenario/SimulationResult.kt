package com.evedenta.riskpilot.model.response.scenario



data class SimulationResult(val trials: Int, val seed: Long, val meanBefore: Double, val meanAfter: Double,
    val var95Before: Double, val var95After: Double, val cvar95After: Double, val histogram: List<HistogramBin>)
