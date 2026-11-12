package com.evedenta.riskpilot.service.analysis

import com.evedenta.riskpilot.algorithm.ahp.AhpCalculator
import com.evedenta.riskpilot.algorithm.knowledge.KnowledgeRuleEngine
import com.evedenta.riskpilot.algorithm.optimization.ResourceOptimizer
import com.evedenta.riskpilot.algorithm.scenario.ScenarioAnalyzer
import com.evedenta.riskpilot.ml.inference.LogisticRegressionModel
import com.evedenta.riskpilot.model.mapper.ModelVersionMapper
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.model.response.analysis.AnalysisResponse
import com.evedenta.riskpilot.model.response.recommendation.RiskRecommendation
import com.evedenta.riskpilot.model.response.risk.ScoredRisk
import com.evedenta.riskpilot.repository.risk.RiskRepository
import com.evedenta.riskpilot.service.mitigation.MeasureService
import com.evedenta.riskpilot.service.ml.ModelService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnalysisService(private val repository: RiskRepository,private val model: ModelService,private val measureService: MeasureService) {
    @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    fun analyze(request: AnalysisRequest): AnalysisResponse {
        request.validate()
        val ahp=AhpCalculator.calculate(request.matrix)
        require(ahp.acceptable) { "Матрица AHP несогласованна: CR=${"%.4f".format(java.util.Locale.ROOT,ahp.consistencyRatio)} > 0.10. Пересмотрите сравнения" }
        val modelVersion=model.snapshot()
        val predictor=LogisticRegressionModel(modelVersion.report)
        val risks=repository.all()
        val probabilities=risks.map { predictor.predict(it.input.features) }
        val priorities=AhpCalculator.rank(risks.indices.map { i -> with(risks[i].input) { listOf(probabilities[i],loss,delayDays.toDouble(),urgency) } },ahp.weights)
        val scored=risks.indices.map { i ->
            val r=risks[i]; val p=probabilities[i]
            val provisional=ScoredRisk(r,p,priorities[i],0.0,predictor.explain(r.input.features),KnowledgeRuleEngine.infer(r,p,request.dayCost))
            provisional.copy(expectedLoss=ResourceOptimizer.expected(provisional,request))
        }.sortedByDescending { it.priority }
        val options=measureService.options(risks)
        val plan=ResourceOptimizer.solve(scored,request,options)
        val recommendations=scored.mapIndexed { index,r ->
            val selected=plan.measures.find { it.riskId==r.risk.id }
            val action=selected?.title ?: "Наблюдение и организационные меры"
            val explain=if(selected!=null) "Входит в оптимальный портфель: затраты ${selected.cost} тыс. ₽, ${selected.days} чел.-дней; снижение последствий ${(selected.reduction*100).toInt()}%. Ожидаемая чистая выгода ${"%.1f".format(java.util.Locale.ROOT,r.expectedLoss*selected.reduction-selected.cost)} тыс. ₽."
                else if(options.getValue(r.risk.id).none { r.expectedLoss*it.reduction>it.cost }) "Платное мероприятие не окупается при принятых оценках."
                else "Не включено при текущем бюджете и ресурсе: другие сочетания дают большую суммарную чистую выгоду."
            RiskRecommendation(r.risk.id,index+1,r.risk.input.title,action,explain,selected!=null,r.rules)
        }
        return AnalysisResponse("Внедрение корпоративной информационной системы", "Деньги — тыс. ₽; ресурс — чел.-дни; горизонт — до завершения проекта",request,ahp,scored,plan,
            ScenarioAnalyzer.evaluate(scored,request,plan,options),recommendations,modelVersion.report,KnowledgeRuleEngine.rules,
            listOf(modelVersion.report.provenance,
                "Вероятности сценариев, стоимость мероприятий и эффективность — экспертные допущения.",
                "Ущерб складывается из прямых потерь и стоимости задержки; прямые потери должны исключать стоимость простоя.",
                "Риски условно независимы внутри сценария. Эффекты мер не взаимодействуют; горизонт один проект."),
            AhpCalculator.criteria.indices.associate { j ->
                val values=scored.map { r -> with(r.risk.input) { listOf(r.probability,loss,delayDays.toDouble(),urgency)[j].coerceAtLeast(1e-6) } }
                AhpCalculator.criteria[j] to values.map { x -> values.map { y -> x/y } }
            }, ModelVersionMapper.from(modelVersion),options)
    }
}
