package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.model.request.decision.SaveDecisionRequest
import com.evedenta.riskpilot.service.decision.DecisionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/decisions")
class DecisionController(private val service: DecisionService) {
    @GetMapping fun list() = service.all()
    @GetMapping("/{id}") fun get(@PathVariable id: String) = service.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun save(@RequestBody request: SaveDecisionRequest) = service.save(request)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = service.delete(id)
    @GetMapping("/compare") fun compare(@RequestParam left: String,@RequestParam right: String) = service.compare(left,right)
}
