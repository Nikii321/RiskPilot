package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.domain.risk.RiskCategory
import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetadataController {
    @GetMapping("/api/meta") fun metadata() = mapOf("name" to "RiskPilot","version" to "1.0.0",
        "categories" to RiskCategory.entries.map { mapOf("id" to it.name,"label" to it.label) },"maxRisks" to 14,"defaults" to AnalysisRequest())
}
