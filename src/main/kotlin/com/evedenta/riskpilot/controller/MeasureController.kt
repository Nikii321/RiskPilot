package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.model.request.mitigation.MeasureRequest
import com.evedenta.riskpilot.service.mitigation.MeasureService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/risks/{id}/measures")
class MeasureController(private val measures: MeasureService) {
    @GetMapping fun list(@PathVariable id: String) = measures.forRisk(id)
    @PutMapping fun replace(@PathVariable id: String,@RequestBody input: List<MeasureRequest>) = measures.replace(id,input)
    @DeleteMapping fun reset(@PathVariable id: String) = measures.reset(id)
}
