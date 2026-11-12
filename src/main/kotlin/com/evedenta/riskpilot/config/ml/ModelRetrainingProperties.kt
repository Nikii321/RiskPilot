package com.evedenta.riskpilot.config.ml

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("riskpilot.ml.retraining")
data class ModelRetrainingProperties(val enabled: Boolean=true,val cron: String="0 0 3 * * *",
    val zone: String="Europe/Moscow",val sourcePath: String="./training-data/history.csv",
    val minValidationAuc: Double=.55,val maxBrierRegression: Double=.005) {
    init {
        require(minValidationAuc.isFinite() && minValidationAuc in .5..1.0)
        require(maxBrierRegression.isFinite() && maxBrierRegression in 0.0..1.0)
    }
}
