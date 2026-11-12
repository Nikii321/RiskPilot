package com.evedenta.riskpilot.model.response.risk

import com.evedenta.riskpilot.domain.knowledge.RuleHit
import com.evedenta.riskpilot.domain.risk.Risk

data class ScoredRisk(val risk: Risk, val probability: Double, val priority: Double, val expectedLoss: Double,
                      val contributions: Map<String, Double>, val rules: List<RuleHit>)
