package com.evedenta.riskpilot.model.response.recommendation

import com.evedenta.riskpilot.domain.knowledge.RuleHit

data class RiskRecommendation(val riskId: String, val rank: Int, val title: String, val action: String,
    val explanation: String, val selected: Boolean, val ruleHits: List<RuleHit>)
