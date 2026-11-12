package com.evedenta.riskpilot.model.response.scenario

import com.evedenta.riskpilot.domain.scenario.Scenario

data class ScenarioOutcome(val scenario: Scenario, val expectedLoss: Double, val residualLoss: Double, val totalCost: Double)
