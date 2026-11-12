package com.evedenta.riskpilot.algorithm

import com.evedenta.riskpilot.algorithm.ahp.AhpCalculator
import com.evedenta.riskpilot.algorithm.knowledge.KnowledgeRuleEngine
import com.evedenta.riskpilot.algorithm.optimization.ResourceOptimizer
import com.evedenta.riskpilot.algorithm.scenario.ScenarioAnalyzer
import com.evedenta.riskpilot.config.DefaultAnalysisConfig
import com.evedenta.riskpilot.domain.mitigation.DefaultMeasureFactory
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.domain.risk.Risk
import com.evedenta.riskpilot.domain.risk.RiskAssessment
import com.evedenta.riskpilot.domain.risk.RiskCategory
import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.risk.ScoredRisk
import com.evedenta.riskpilot.service.ml.ModelService
import kotlin.test.*
import org.junit.jupiter.api.Test

class DecisionModelsTest {
    private fun risk(id: String, loss: Double=500.0) = ScoredRisk(Risk(id,RiskAssessment("Риск $id",RiskCategory.TECHNICAL,"Владелец",loss,10,4.0,RiskFeatures())),.65,.5,0.0,emptyMap(),emptyList())
    @Test fun `AHP recovers consistent weights`() {
        val w=listOf(.4,.3,.2,.1)
        val a=AhpCalculator.calculate(w.map { x -> w.map { y -> x/y } })
        w.indices.forEach { assertEquals(w[it],a.weights[it],1e-8) }
        assertEquals(0.0,a.consistencyRatio,1e-8)
    }
    @Test fun `default matrix consistent`() { assertTrue(AhpCalculator.calculate(DefaultAnalysisConfig.defaultMatrix()).acceptable) }
    @Test fun `non reciprocal and nonfinite matrices rejected`() {
        assertFailsWith<IllegalArgumentException> { AhpCalculator.calculate(List(4) { List(4) { 2.0 } }) }
        assertFailsWith<IllegalArgumentException> { AhpCalculator.calculate(List(4) { List(4) { Double.NaN } }) }
    }
    @Test fun `inconsistent reciprocal matrix detected`() {
        val a=listOf(listOf(1.0,9.0,1.0/9,1.0),listOf(1.0/9,1.0,9.0,1.0),listOf(9.0,1.0/9,1.0,1.0),listOf(1.0,1.0,1.0,1.0))
        assertFalse(AhpCalculator.calculate(a).acceptable)
    }
    @Test fun `zero budget and empty register handled`() {
        val p=ResourceOptimizer.solve(listOf(risk("a")),AnalysisRequest(budget=0))
        assertTrue(p.measures.isEmpty()); assertEquals(0.0,p.netBenefit)
        assertEquals(0.0,ResourceOptimizer.solve(emptyList(),AnalysisRequest()).totalCost)
    }
    @Test fun `optimizer matches exhaustive independent enumeration`() {
        val risks=listOf(risk("a",900.0),risk("b",400.0),risk("c",1100.0),risk("d",250.0))
        for(budget in listOf(0,40,90,160,300)) for(days in listOf(0,3,6,12)) {
            val request=AnalysisRequest(budget=budget,capacityDays=days)
            var best=0.0
            fun enumerate(i: Int,selected: List<MitigationMeasure>) {
                if(i==risks.size) {
                    val cost=selected.sumOf { it.cost }; val usedDays=selected.sumOf { it.days }
                    if(cost<=budget && usedDays<=days) best=maxOf(best,ResourceOptimizer.describe(risks,request,selected).netBenefit)
                    return
                }
                enumerate(i+1,selected)
                DefaultMeasureFactory.forRisk(risks[i].risk).forEach { enumerate(i+1,selected+it) }
            }
            enumerate(0,emptyList())
            val actual=ResourceOptimizer.solve(risks,request)
            assertEquals(best,actual.netBenefit,1e-7)
            assertTrue(actual.cost<=budget && actual.days<=days)
            assertEquals(actual.measures.size,actual.measures.map { it.riskId }.distinct().size)
        }
    }
    @Test fun `model learns useful ranking and probability bounds`() {
        val model=ModelService()
        val high=RiskFeatures(.9,.1,.95,10,.9,.1); val low=RiskFeatures(.1,.9,.2,0,.1,.9)
        assertTrue(model.predict(high)>model.predict(low))
        assertTrue(model.predict(high) in 0.0..1.0)
        assertTrue(model.report.metrics.rocAuc>.60)
        assertEquals(840,model.report.trainCount); assertEquals(180,model.report.testCount)
        assertTrue(model.report.metrics.brier<model.report.metrics.baselineBrier)
    }
    @Test fun `inference returns evidence for security rule`() {
        val r=risk("x",1500.0).risk
        val hit=KnowledgeRuleEngine.infer(r.copy(input=r.input.copy(category=RiskCategory.SECURITY)),.7,25.0)
        assertTrue(hit.any { it.rule=="K5" && it.facts.isNotEmpty() })
    }
    @Test fun `simulation mean agrees with analytic expectation`() {
        val risks=listOf(risk("a"),risk("b")); val request=AnalysisRequest()
        val plan=ResourceOptimizer.solve(risks,request)
        val u=ScenarioAnalyzer.evaluate(risks,request,plan)
        assertEquals(plan.totalCost,u.simulation.meanAfter,plan.totalCost*.04)
        assertTrue(u.simulation.cvar95After>=u.simulation.var95After)
        assertTrue(u.strategies[2].expectedCost<=u.strategies[0].expectedCost+1e-8)
    }
    @Test fun `scenario probabilities and feature ranges validated`() {
        assertFailsWith<IllegalArgumentException> { AnalysisRequest(scenarios=DefaultAnalysisConfig.defaultScenarios().map { it.copy(probability=.8) }).validate() }
        assertFailsWith<IllegalArgumentException> { RiskFeatures(dependencies=-1).validate() }
        assertFailsWith<IllegalArgumentException> { RiskFeatures(workload=Double.NaN).validate() }
    }
}
