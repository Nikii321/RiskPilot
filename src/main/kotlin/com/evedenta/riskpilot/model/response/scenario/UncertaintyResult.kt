package com.evedenta.riskpilot.model.response.scenario



data class UncertaintyResult(val scenarioOutcomes: List<ScenarioOutcome>, val strategies: List<StrategyResult>,
    val bayes: String,val wald: String,val savage: String,val hurwicz: String,val alpha: Double,val simulation: SimulationResult)
