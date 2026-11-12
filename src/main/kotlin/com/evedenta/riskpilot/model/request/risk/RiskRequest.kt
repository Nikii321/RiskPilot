package com.evedenta.riskpilot.model.request.risk

import com.evedenta.riskpilot.domain.risk.RiskAssessment
import com.evedenta.riskpilot.domain.risk.RiskCategory
import com.evedenta.riskpilot.domain.risk.RiskFeatures

data class RiskRequest(val title: String,val category: RiskCategory,val owner: String,val loss: Double,
    val delayDays: Int,val urgency: Double,val features: RiskFeatures,val version: Int=0) {
    fun toAssessment() = RiskAssessment(title,category,owner,loss,delayDays,urgency,features,version)
}
