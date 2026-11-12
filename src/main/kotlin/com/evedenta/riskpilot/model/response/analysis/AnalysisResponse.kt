package com.evedenta.riskpilot.model.response.analysis

import com.evedenta.riskpilot.domain.knowledge.KnowledgeRule
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.ml.model.ModelReport
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.ahp.AhpResult
import com.evedenta.riskpilot.model.response.ml.ModelVersion
import com.evedenta.riskpilot.model.response.optimization.OptimizationPlan
import com.evedenta.riskpilot.model.response.recommendation.RiskRecommendation
import com.evedenta.riskpilot.model.response.risk.ScoredRisk
import com.evedenta.riskpilot.model.response.scenario.UncertaintyResult

data class AnalysisResponse(val project: String, val units: String, val request: AnalysisRequest, val ahp: AhpResult,
    val risks: List<ScoredRisk>,val plan: OptimizationPlan,val uncertainty: UncertaintyResult,val recommendations: List<RiskRecommendation>,
    val model: ModelReport,val knowledgeRules: List<KnowledgeRule>,val warnings: List<String>,
    val alternativeComparisons: Map<String,List<List<Double>>>, val modelVersion: ModelVersion,val availableMeasures: Map<String,List<MitigationMeasure>>)
