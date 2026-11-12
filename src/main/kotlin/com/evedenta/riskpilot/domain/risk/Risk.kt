package com.evedenta.riskpilot.domain.risk

import java.util.UUID

data class Risk(val id: String, val input: RiskAssessment) {
    companion object { fun create(input: RiskAssessment) = Risk(UUID.randomUUID().toString(), input) }
}
