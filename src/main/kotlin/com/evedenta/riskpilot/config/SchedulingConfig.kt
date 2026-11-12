package com.evedenta.riskpilot.config

import com.evedenta.riskpilot.config.ml.ModelRetrainingProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ModelRetrainingProperties::class)
class SchedulingConfig
