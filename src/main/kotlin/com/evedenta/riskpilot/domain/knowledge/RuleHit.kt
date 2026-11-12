package com.evedenta.riskpilot.domain.knowledge



data class RuleHit(val rule: String, val facts: List<String>, val conclusion: String)
