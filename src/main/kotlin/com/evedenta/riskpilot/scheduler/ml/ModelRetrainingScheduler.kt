package com.evedenta.riskpilot.scheduler.ml

import com.evedenta.riskpilot.config.ml.ModelRetrainingProperties
import com.evedenta.riskpilot.service.ml.ModelRetrainingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ModelRetrainingScheduler(private val service: ModelRetrainingService,private val properties: ModelRetrainingProperties) {
    @Scheduled(cron="\${riskpilot.ml.retraining.cron}",zone="\${riskpilot.ml.retraining.zone}")
    fun retrain() { if(properties.enabled) service.run("SCHEDULED") }
}
