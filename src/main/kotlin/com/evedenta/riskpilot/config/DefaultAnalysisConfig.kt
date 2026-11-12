package com.evedenta.riskpilot.config

import com.evedenta.riskpilot.domain.scenario.Scenario

object DefaultAnalysisConfig {
    fun defaultMatrix() = listOf(listOf(1.0,0.5,2.0,1.0),listOf(2.0,1.0,3.0,2.0),listOf(0.5,1.0/3,1.0,0.5),listOf(1.0,0.5,2.0,1.0))
    fun defaultScenarios() = listOf(Scenario("Благоприятный",0.2,0.7,0.8),Scenario("Базовый",0.5,1.0,1.0),Scenario("Стрессовый",0.3,1.35,1.3))
    }
