package com.evedenta.riskpilot.service.decision

import com.evedenta.riskpilot.model.request.decision.SaveDecisionRequest
import com.evedenta.riskpilot.model.response.decision.*
import com.evedenta.riskpilot.repository.decision.DecisionRepository
import com.evedenta.riskpilot.service.analysis.AnalysisService
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class DecisionService(private val repository: DecisionRepository,private val analysis: AnalysisService) {
    fun all() = repository.all()
    fun get(id: String) = repository.get(id)
    fun delete(id: String) = repository.delete(id)
    fun save(input: SaveDecisionRequest): SavedDecision {
        require(input.title.isNotBlank() && input.title.length<=160) { "Название решения: 1–160 символов" }
        return repository.save(SavedDecision(UUID.randomUUID().toString(),input.title,Instant.now().toString(),analysis.analyze(input.request)))
    }
    fun compare(left: String,right: String): DecisionComparison {
        val a=get(left); val b=get(right)
        fun labels(d: SavedDecision) = d.analysis.plan.measures.map { m ->
            val risk=d.analysis.risks.find { it.risk.id==m.riskId }!!.risk.input.title
            "$risk → ${m.title} (${m.cost} тыс. ₽, ${m.days} дней, ${m.reduction*100}%)"
        }
        val first=labels(a); val second=labels(b)
        return DecisionComparison(a,b,b.analysis.plan.totalCost-a.analysis.plan.totalCost,
            b.analysis.plan.netBenefit-a.analysis.plan.netBenefit,b.analysis.request.budget-a.analysis.request.budget,
            second-first.toSet(),first-second.toSet())
    }
}
