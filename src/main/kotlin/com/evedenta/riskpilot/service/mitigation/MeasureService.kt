package com.evedenta.riskpilot.service.mitigation

import com.evedenta.riskpilot.domain.mitigation.DefaultMeasureFactory
import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.domain.risk.Risk
import com.evedenta.riskpilot.exception.risk.RiskNotFoundException
import com.evedenta.riskpilot.model.request.mitigation.MeasureRequest
import com.evedenta.riskpilot.repository.mitigation.MeasureRepository
import com.evedenta.riskpilot.repository.risk.RiskRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MeasureService(private val measures: MeasureRepository,private val risks: RiskRepository) {
    fun options(register: List<Risk>): Map<String,List<MitigationMeasure>> {
        val stored=measures.all().groupBy { it.riskId }
        return register.associate { it.id to (stored[it.id] ?: DefaultMeasureFactory.forRisk(it)) }
    }
    fun forRisk(id: String): List<MitigationMeasure> {
        val risk=risks.all().find { it.id==id } ?: throw RiskNotFoundException()
        return options(listOf(risk)).getValue(id)
    }
    @Transactional
    fun replace(id: String,input: List<MeasureRequest>): List<MitigationMeasure> {
        require(input.size in 1..2) { "Задайте одну или две альтернативные меры на риск" }
        input.forEach { it.validate() }
        require(input.map { it.title }.distinct().size==input.size) { "Названия альтернатив должны различаться" }
        measures.replace(id,input.map { MitigationMeasure(UUID.randomUUID().toString(),id,it.title,it.cost,it.days,it.reduction) })
        return forRisk(id)
    }
    @Transactional
    fun reset(id: String): List<MitigationMeasure> { measures.reset(id); return forRisk(id) }
}
