package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.model.request.analysis.AnalysisRequest
import com.evedenta.riskpilot.service.analysis.AnalysisService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/analysis")
class AnalysisController(private val service: AnalysisService) {
    @GetMapping fun defaultAnalysis() = service.analyze(AnalysisRequest())
    @PostMapping fun analyze(@RequestBody request: AnalysisRequest) = service.analyze(request)
}
