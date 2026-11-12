package com.evedenta.riskpilot.service.risk

import com.evedenta.riskpilot.model.request.risk.RiskRequest
import com.evedenta.riskpilot.repository.risk.RiskRepository
import org.springframework.stereotype.Service

@Service
class RiskService(private val repository: RiskRepository) {
    fun all() = repository.all()
    fun create(input: RiskRequest) = repository.create(input.toAssessment().also { it.validate() })
    fun update(id: String,input: RiskRequest) = repository.update(id,input.toAssessment().also { it.validate() })
    fun delete(id: String) = repository.delete(id)
}
