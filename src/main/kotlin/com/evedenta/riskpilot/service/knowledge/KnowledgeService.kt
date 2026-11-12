package com.evedenta.riskpilot.service.knowledge

import com.evedenta.riskpilot.algorithm.knowledge.KnowledgeRuleEngine
import org.springframework.stereotype.Service

@Service
class KnowledgeService { fun rules() = KnowledgeRuleEngine.rules }
