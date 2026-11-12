package com.evedenta.riskpilot.ml

import com.evedenta.riskpilot.algorithm.optimization.ResourceOptimizer
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.domain.risk.Risk
import com.evedenta.riskpilot.domain.risk.RiskAssessment
import com.evedenta.riskpilot.domain.risk.RiskCategory
import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.ml.data.HistoryCsvCodec
import com.evedenta.riskpilot.ml.data.SyntheticHistoryGenerator
import com.evedenta.riskpilot.ml.inference.LogisticRegressionModel
import com.evedenta.riskpilot.ml.training.LogisticRegressionTrainer
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.risk.ScoredRisk
import com.evedenta.riskpilot.service.ml.ModelService
import kotlin.test.*
import org.junit.jupiter.api.Test

class ModelLifecycleTest {
    @Test fun `training CSV round trip and exported artifact preserve predictions`() {
        val model=ModelService();val initial=model.snapshot();val input=RiskFeatures(.8,.3,.9,6,.7,.5)
        val expected=model.predict(input)
        val version=model.train(model.csv(),"history.csv")
        assertNotEquals(initial.id,version.id)
        assertEquals(expected,LogisticRegressionModel(version.report).predict(input),1e-12)
        assertEquals(1200,version.rows)
    }
    @Test fun `failed upload cannot replace active model`() {
        val model=ModelService();val id=model.snapshot().id
        assertFailsWith<IllegalArgumentException> { model.train("bad,header\n1,2","invalid.csv") }
        assertEquals(id,model.snapshot().id)
        val rows=SyntheticHistoryGenerator.generate().map { it.copy(occurred=0) }
        assertFailsWith<IllegalArgumentException> { model.train(HistoryCsvCodec.write(rows),"no-events.csv") }
        assertEquals(id,model.snapshot().id)
    }
    @Test fun `CSV rejects nonfinite values and too few project groups`() {
        val csv=HistoryCsvCodec.write(SyntheticHistoryGenerator.generate())
        assertFailsWith<IllegalArgumentException> { HistoryCsvCodec.parse(csv.replaceFirst(",0.",",NaN")) }
        assertFailsWith<IllegalArgumentException> { LogisticRegressionTrainer().train(SyntheticHistoryGenerator.generate().map { it.copy(projectId=1) },"few projects") }
    }
    @Test fun `user measures affect optimal economic decision`() {
        val risk=com.evedenta.riskpilot.domain.risk.Risk("r",com.evedenta.riskpilot.domain.risk.RiskAssessment("Поставка",com.evedenta.riskpilot.domain.risk.RiskCategory.SUPPLIER,"РП",1000.0,10,5.0,RiskFeatures()))
        val scored=com.evedenta.riskpilot.model.response.risk.ScoredRisk(risk,.7,1.0,0.0,emptyMap(),emptyList())
        val free=com.evedenta.riskpilot.domain.mitigation.MitigationMeasure("custom","r","Бесплатный контроль",0,0,.9)
        val plan=ResourceOptimizer.solve(listOf(scored),com.evedenta.riskpilot.model.request.analysis.AnalysisRequest(budget=0,capacityDays=0),mapOf("r" to listOf(free)))
        assertEquals("custom",plan.measures.single().id)
        assertTrue(plan.netBenefit>0)
    }
}
