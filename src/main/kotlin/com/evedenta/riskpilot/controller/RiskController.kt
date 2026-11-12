package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.model.request.risk.RiskRequest
import com.evedenta.riskpilot.service.risk.RiskService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/risks")
class RiskController(private val service: RiskService) {
    @GetMapping fun all() = service.all()
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody input: RiskRequest) = service.create(input)
    @PutMapping("/{id}") fun update(@PathVariable id: String,@RequestBody input: RiskRequest) = service.update(id,input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = service.delete(id)
}
