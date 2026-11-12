package com.evedenta.riskpilot.model.response.decision

import com.evedenta.riskpilot.model.response.analysis.AnalysisResponse

data class SavedDecision(val id: String,val title: String,val createdAt: String,val analysis: AnalysisResponse) {
    fun summary() = DecisionSummary(id,title,createdAt,analysis.risks.size,analysis.request.budget,
        analysis.plan.totalCost,analysis.plan.netBenefit,analysis.modelVersion.id)
}
