package com.evedenta.riskpilot.model.request.decision

import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest

data class SaveDecisionRequest(val title: String,val request: AnalysisRequest)
