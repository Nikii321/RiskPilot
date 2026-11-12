package com.evedenta.riskpilot.algorithm.scenario

import com.evedenta.riskpilot.algorithm.optimization.ResourceOptimizer
import com.evedenta.riskpilot.domain.mitigation.DefaultMeasureFactory
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.optimization.OptimizationPlan
import com.evedenta.riskpilot.model.response.risk.ScoredRisk
import com.evedenta.riskpilot.model.response.scenario.HistogramBin
import com.evedenta.riskpilot.model.response.scenario.ScenarioOutcome
import com.evedenta.riskpilot.model.response.scenario.SimulationResult
import com.evedenta.riskpilot.model.response.scenario.StrategyResult
import com.evedenta.riskpilot.model.response.scenario.UncertaintyResult
import java.util.Random
import kotlin.math.ceil

object ScenarioAnalyzer {
    fun evaluate(risks: List<ScoredRisk>,request: AnalysisRequest,optimal: OptimizationPlan, options: Map<String,List<MitigationMeasure>> = risks.associate { it.risk.id to DefaultMeasureFactory.forRisk(it.risk) }): UncertaintyResult {
        val plans=listOf("Принять риски" to ResourceOptimizer.describe(risks,request,emptyList()),
            "По приоритету AHP" to ResourceOptimizer.greedy(risks,request,options),"Оптимальный портфель" to optimal)
        fun costs(plan: OptimizationPlan) = request.scenarios.map { s ->
            plan.cost + risks.sumOf { r -> ResourceOptimizer.loss(r,s,request.dayCost)*(1-(plan.measures.find { it.riskId==r.risk.id }?.reduction ?: 0.0)) }
        }
        val matrix=plans.map { costs(it.second) }
        val minimum=List(3) { j -> matrix.minOf { it[j] } }
        val strategies=plans.indices.map { i ->
            val row=matrix[i]
            StrategyResult(plans[i].first,row,row.indices.sumOf { row[it]*request.scenarios[it].probability },row.max(),
                row.indices.maxOf { row[it]-minimum[it] },.6*row.min()+.4*row.max())
        }
        val outcomes=request.scenarios.map { s ->
            val base=risks.sumOf { ResourceOptimizer.loss(it,s,request.dayCost) }
            val residual=risks.sumOf { r -> ResourceOptimizer.loss(r,s,request.dayCost)*(1-(optimal.measures.find { it.riskId==r.risk.id }?.reduction ?: 0.0)) }
            ScenarioOutcome(s,base,residual,residual+optimal.cost)
        }
        return UncertaintyResult(outcomes,strategies,strategies.minBy { it.expectedCost }.strategy,
            strategies.minBy { it.worstCost }.strategy,strategies.minBy { it.maxRegret }.strategy,
            strategies.minBy { it.hurwiczCost }.strategy,.6,simulate(risks,request,optimal))
    }
    private fun simulate(risks: List<ScoredRisk>,request: AnalysisRequest,plan: OptimizationPlan): SimulationResult {
        val rng=Random(73); val before=DoubleArray(10000); val after=DoubleArray(10000)
        val reductions=plan.measures.associate { it.riskId to it.reduction }
        repeat(10000) { i ->
            val u=rng.nextDouble(); var cumulative=0.0
            val s=request.scenarios.firstOrNull { cumulative+=it.probability; u<cumulative } ?: request.scenarios.last()
            after[i]=plan.cost.toDouble()
            risks.forEach { r ->
                val p=(r.probability*s.probabilityFactor).coerceAtMost(1.0)
                val occurred=rng.nextDouble()<p
                if(occurred) {
                    val damage=(r.risk.input.loss+r.risk.input.delayDays*request.dayCost)*s.lossFactor
                    before[i]+=damage
                    // Measures reduce conditional severity; efficacy is an expert assumption.
                    after[i]+=damage*(1-(reductions[r.risk.id] ?: 0.0))
                }
            }
        }
        val sortedBefore=before.sorted(); val sortedAfter=after.sorted()
        val index=ceil(.95*before.size).toInt()-1
        val max=sortedAfter.last().coerceAtLeast(1.0)
        val histogram=(0..9).map { b -> HistogramBin(max*b/10,max*(b+1)/10,after.count { kotlin.math.min(9,(it/max*10).toInt())==b }) }
        return SimulationResult(10000,73,before.average(),after.average(),sortedBefore[index],sortedAfter[index],sortedAfter.drop(index).average(),histogram)
    }
}
