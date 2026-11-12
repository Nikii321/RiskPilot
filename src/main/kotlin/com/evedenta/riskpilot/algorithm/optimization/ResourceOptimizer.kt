package com.evedenta.riskpilot.algorithm.optimization

import com.evedenta.riskpilot.domain.mitigation.DefaultMeasureFactory
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.domain.scenario.Scenario
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.optimization.OptimizationPlan
import com.evedenta.riskpilot.model.response.risk.ScoredRisk

object ResourceOptimizer {
    fun loss(risk: ScoredRisk, scenario: Scenario, dayCost: Double) =
        (risk.probability*scenario.probabilityFactor).coerceAtMost(1.0) *
            (risk.risk.input.loss+risk.risk.input.delayDays*dayCost)*scenario.lossFactor
    fun expected(risk: ScoredRisk, request: AnalysisRequest) = request.scenarios.sumOf { it.probability*loss(risk,it,request.dayCost) }
    fun describe(risks: List<ScoredRisk>, request: AnalysisRequest, selected: List<MitigationMeasure>, nodes: Long=0, method: String="Оценка стратегии"): OptimizationPlan {
        val base=risks.sumOf { expected(it,request) }
        val index=risks.associateBy { it.risk.id }
        val avoided=selected.sumOf { expected(index.getValue(it.riskId),request)*it.reduction }
        val cost=selected.sumOf { it.cost }
        return OptimizationPlan(selected,cost,selected.sumOf { it.days },avoided,avoided-cost,base-avoided,base,base-avoided+cost,nodes,method)
    }
    // Multiple-choice 0–1 integer optimization. One measure or no measure per risk.
    // A suffix upper bound ignores resource limits and therefore cannot prune an optimal solution.
    fun solve(risks: List<ScoredRisk>, request: AnalysisRequest, options: Map<String,List<MitigationMeasure>> = risks.associate { it.risk.id to DefaultMeasureFactory.forRisk(it.risk) }): OptimizationPlan {
        require(risks.size<=14) { "Точный оптимизатор MVP поддерживает до 14 рисков" }
        val groups=risks.map { r -> options.getValue(r.risk.id).map { MeasureOption(it,expected(r,request)*it.reduction-it.cost) }
            .filter { it.gain>0 }.sortedByDescending { it.gain } }.sortedByDescending { it.firstOrNull()?.gain ?: 0.0 }
        val upper=DoubleArray(groups.size+1)
        for(i in groups.indices.reversed()) upper[i]=upper[i+1]+(groups[i].firstOrNull()?.gain ?: 0.0)
        var best=0.0; var bestMeasures=emptyList<MitigationMeasure>(); var nodes=0L
        val chosen=mutableListOf<MitigationMeasure>()
        fun dfs(i: Int, cost: Int, days: Int, gain: Double) {
            nodes++
            if(gain+upper[i] <= best+1e-9) return
            if(i==groups.size) { best=gain; bestMeasures=chosen.toList(); return }
            for(o in groups[i]) if(cost+o.measure.cost<=request.budget && days+o.measure.days<=request.capacityDays) {
                chosen.add(o.measure); dfs(i+1,cost+o.measure.cost,days+o.measure.days,gain+o.gain); chosen.removeAt(chosen.lastIndex)
            }
            dfs(i+1,cost,days,gain)
        }
        dfs(0,0,0,0.0)
        return describe(risks,request,bestMeasures,nodes,"Точный метод ветвей и границ; множественный выбор 0–1")
    }
    fun greedy(risks: List<ScoredRisk>, request: AnalysisRequest, options: Map<String,List<MitigationMeasure>> = risks.associate { it.risk.id to DefaultMeasureFactory.forRisk(it.risk) }): OptimizationPlan {
        val selected=mutableListOf<MitigationMeasure>(); var cost=0; var days=0
        for(r in risks.sortedByDescending { it.priority }) {
            val m=options.getValue(r.risk.id).minBy { it.cost }
            if(cost+m.cost<=request.budget && days+m.days<=request.capacityDays && expected(r,request)*m.reduction>m.cost) {
                selected.add(m); cost+=m.cost; days+=m.days
            }
        }
        return describe(risks,request,selected)
    }
}
