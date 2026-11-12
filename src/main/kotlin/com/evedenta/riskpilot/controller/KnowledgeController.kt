package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.service.knowledge.KnowledgeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class KnowledgeController(private val service: KnowledgeService) {
    @GetMapping("/api/knowledge") fun rules() = service.rules()
}
