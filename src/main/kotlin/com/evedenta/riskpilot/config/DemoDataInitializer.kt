package com.evedenta.riskpilot.config

import com.evedenta.riskpilot.repository.risk.RiskRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DemoDataInitializer(private val repository: RiskRepository): CommandLineRunner { override fun run(vararg args: String) = repository.initializeDemoIfAbsent(DemoRiskData.risks()) }
